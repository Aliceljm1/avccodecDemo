package yzriver.avc.avccodec;

import android.content.Context;

import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import java.io.IOException;
import java.lang.Thread;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Queue ;
import java.util.LinkedList ;

import android.os.Message;

import yzriver.avc.avccodecDemo.R;

public class VideoCameraView extends SurfaceView  implements SurfaceHolder.Callback,
								android.hardware.Camera.PreviewCallback {
	
	static final int MSG_VIDEOCAMERAVIEW_UPDATE_PREVIEWFRAMERATE = 1001;
	static final String TAG = "VideoCameraView" ;
		
	private android.hardware.Camera mCamera = null ;
	private int rotateAngle = 0 ;
	private String captureYuvFilename = "/sdcard/avccodecDemo/capture.yuv420sp" ;
	private FileOutputStream captureYuv = null ;
	private int preview_w ;
	private int preview_h ;
	private int preview_yuvbytes ; 
	private byte[][] previewBuffer   ;
	private int  numBuffFilled = 0; 
	private int  currentBuffToRead = 0 ;
	
	private String captureJpegFilename = "/sdcard/avccodecDemo/capture" ;
	static private int iJpegNum = 0 ;
	private double mAspectRatio = 3.0 / 3.0;
	private byte[] avcBitStream = null ;
	private int[] avcBitStreamLength = new int[1]; 
	int[] nalType = new int[1] ;
	private YzrAvcEnc  yzrAvcEnc  = null ;
	private boolean     mEncodeOk = false ;
	
	private long timeStart = 0 ; 
	private int  onPreviewCalled ;
	private Handler handlerIn ;


	private boolean mRec = false ;
		
	private SurfaceHolder surfaceHolder = null ;
	public VideoCameraView(Context context ){
		super(context) ;
		Log.v(TAG,"VideoCameraView( "+context+" )" );
	}

	public VideoCameraView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    
		//SurfaceView surfaceView = (SurfaceView) findViewById(R.id.lsurfaceViewLoopEnc);
	    surfaceHolder = this.getHolder();
	    surfaceHolder.addCallback(this);

		
	    Log.v(TAG,"VideoCameraView( "+context+" "+ attrs + ")" );
	}

	public double getAspectRatio() {
		return mAspectRatio ;
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {		
		surfaceHolder =  holder ;
		Log.v(TAG, "surfacedChanged " + "width="+width+"height="+height+"Format="+format) ;
			
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.v(TAG, "surfacedCreated " ) ;
		surfaceHolder =  holder ;
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v(TAG, "surfacedDestroyed " ) ;
	}	
	
    protected void onMeasure(int widthSpec, int heightSpec) {
        int previewWidth = MeasureSpec.getSize(widthSpec);
        int previewHeight = MeasureSpec.getSize(heightSpec);

        if (previewWidth > previewHeight * mAspectRatio) {
            previewWidth = (int) (previewHeight * mAspectRatio + .5);
        } else {
            previewHeight = (int) (previewWidth / mAspectRatio + .5);
        }

        // Ask children to follow the new preview dimension.
        super.onMeasure(MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(previewHeight, MeasureSpec.EXACTLY));
    }
	
	
	private void saveToJpeg(byte[] data) {
		String captureJpegFilename1 = captureJpegFilename+iJpegNum ;
		captureJpegFilename1 += ".jpeg" ;
		iJpegNum ++ ;
		FileOutputStream captureJpeg = null ;
		File f = new File( captureJpegFilename1 ) ;
		try {
			captureJpeg = new FileOutputStream(f) ;
		}catch(FileNotFoundException e) {
			Log.e(TAG,"FileNotFoundException") ;
		}		
		if( captureJpeg != null ) {
			android.graphics.YuvImage yuvImage = new android.graphics.YuvImage(data ,android.graphics.ImageFormat.NV21, preview_w , preview_h , null  ) ;
			yuvImage.compressToJpeg(new android.graphics.Rect(0, 0 , preview_w,preview_h), 95 , captureJpeg) ;
			try {
				captureJpeg.close() ;
			}catch (IOException e){
				
			}
		}
		
	}

	public void onPreviewFrame (byte[] data, android.hardware.Camera camera){
		//Log.v(TAG, "onPreviewFrame in"  ) ;
		onPreviewCalled ++ ;
		
		/*if( onPreviewCalled %20 ==0  && ( handlerIn!=null )  ){
			long timee = System.currentTimeMillis() ;
			timee -= timeStart ;
			int timeee = (int) timee ;
			Message msg = handlerIn.obtainMessage(MSG_VIDEOCAMERAVIEW_UPDATE_PREVIEWFRAMERATE, onPreviewCalled , timeee) ;
			handlerIn.sendMessage( msg ) ;
		}*/

		if( mRec ) {
			synchronized( this ){
				numBuffFilled ++ ;
			}
		}
			
		
		if( captureYuv != null ) {
			try {
				captureYuv.write( data ) ;
			}catch(IOException e) {
				
			}
		}
		
		//saveToJpeg(data) ;	

		//Log.v(TAG, "onPreviewFrame out"  ) ;
	}
	private void prepareCapture() {
		File f = new File( captureYuvFilename ) ;
		try {
			captureYuv = new FileOutputStream(f) ;
		}catch(FileNotFoundException e) {
			Log.e(TAG,"FileNotFoundException") ;
		}
		
	}

	public void openCamera(int w , int h){

		mRec = false ;
		
		if( surfaceHolder == null  )
			return ;
		mCamera = android.hardware.Camera.open() ;
		try {
			mCamera.setPreviewDisplay(surfaceHolder);
		}catch(IOException e ){
			Log.e(TAG,"mCamera.setPreviewDisplay( " + surfaceHolder +") fail"  ) ;
			return ;
		}

		android.hardware.Camera.Parameters p = mCamera.getParameters() ;
		List<android.hardware.Camera.Size>  listPreview = p.getSupportedPreviewSizes() ;
		Log.v(TAG, "preview size is "+listPreview) ;
		int ii = -1 ; 
		int delta = 0x7fffff ;
		for( int i = 0 ; i < listPreview.size() ; i ++) {
			android.hardware.Camera.Size size = listPreview.get(i) ;
			String ws = Integer.toString(size.width);
			String hs = Integer.toString(size.height) ;			
			Log.v(TAG, "elements "+i+":"+ws+"x"+hs) ;
			if( java.lang.Math.abs(size.width - w ) < delta ) {
				delta = java.lang.Math.abs(size.width - w ) ;
				ii = i ;
			}
		}
		preview_w = listPreview.get(ii).width ;
		preview_h = listPreview.get(ii).height ;
		preview_yuvbytes = preview_w*preview_h*3/2 ;


		mAspectRatio = (double)preview_w / preview_h;
		p.setPreviewSize( preview_w , preview_h ) ;

		List<int[]>  fpRange = p.getSupportedPreviewFpsRange() ;
		int max = 100 ;
		int min = 0 ;
		for(int i = 0  ; i < fpRange.size() ; i ++ ) {
			int[] fpr = fpRange.get(i) ;
			Log.v(TAG, "min "+ fpr[0]+ " max " + fpr[1]) ;			
		}
		

		mCamera.setParameters(p);
		mCamera.setPreviewCallbackWithBuffer( this ) ;
		
		android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo() ; 
		mCamera.getCameraInfo( 0 , cameraInfo ) ;
		rotateAngle = cameraInfo.orientation ;
		Log.v(TAG,"Camera.CameraInfo.orientation="+ cameraInfo.orientation );		
		//mCamera.setDisplayOrientation(cameraInfo.orientation) ;
		//prepareCapture();
		requestLayout() ;		
		timeStart = System.currentTimeMillis() ;
		onPreviewCalled = 0 ;
		mCamera.startPreview();
	}
	public void setYzrAvcEncHandle(YzrAvcEnc yzrAvcEnc ) {
		this.yzrAvcEnc = yzrAvcEnc ;
	}
	
	public int getPreviewWidth(){
		return preview_w ;
	}

	public int getPreviewHeight(){
		return preview_h ;
	}	

	public void startRec() {
		mRec = true ;
		previewBuffer = new byte[2][] ;
		previewBuffer[0]  = new byte[preview_yuvbytes] ;
		previewBuffer[1]  = new byte[preview_yuvbytes] ;
		numBuffFilled = 0  ;
		currentBuffToRead = 0 ;
		mCamera.addCallbackBuffer( previewBuffer[0] ) ;
		mCamera.addCallbackBuffer( previewBuffer[1] ) ;
	}
	public void stopRec() {
		mRec = false ;
	}
		
	
	public int  encodeOneFrame(byte[] bitstream , int bitStreamLength){
		int i = 0 ;
		while( (i++ < 10) && ( numBuffFilled < 1) ) {
			try {
				Thread.sleep(10) ;
			}catch( InterruptedException e) {
				
			}			
		}
		if( numBuffFilled < 1 )
			return 0 ;

		byte[] bu = previewBuffer[ currentBuffToRead ] ;
		currentBuffToRead = (currentBuffToRead+1)%2 ;
		avcBitStream = bitstream ;
		avcBitStreamLength[0] = bitStreamLength ;
		//Log.v(TAG, "start YzrAvcEncEncodeOneFrame") ;
		yzrAvcEnc.YzrAvcEncEncodeOneFrame(bu, avcBitStream, avcBitStreamLength, nalType);
		synchronized ( this ) {
			numBuffFilled -- ;
		}
		mCamera.addCallbackBuffer( bu ) ;
		//Log.v(TAG, "after YzrAvcEncEncodeOneFrame") ;

		return avcBitStreamLength[0] ;
	}
	
	
	public void stopCamera(){
		if( mCamera == null ) {
			return ;
		}
		mCamera.setPreviewCallback(null) ;
		mCamera.stopPreview() ;
		mCamera.release() ;
		mCamera = null ;
  
		if(captureYuv != null ) {
			try {
				captureYuv.close() ;
			}catch (IOException e){
				
			}
		}
	}
	
	public void setHandler(Handler h) {
		handlerIn = h ;
	}

}
