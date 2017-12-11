package yzriver.avc.avccodec;


import java.lang.Thread;
import java.lang.Runnable;
import android.os.Handler; 
import android.os.Message;
import android.os.PowerManager ;
import android.util.Log;
import android.content.Context ;
import java.io.FileInputStream ;
import java.io.FileOutputStream ;
import java.io.IOException;
import android.os.Handler;
import java.lang.System;
import android.widget.TextView;


 
public class AvcThread {
	static public  final int MSG_ENCODER_FINISH = 1;
	static public  final int MSG_UPDATE_YUVVIEW = 2;
	static public  final int MSG_DECODER_FINISH = 3;
	static public  final int MSG_UPDATE_FRAMERATE = 4;
	static public  final int MSG_AVCREC_FINISH = 5;	
	
	static private final String TAG = "AvcThread" ;

 
	private   byte[]    yuv ;
	private   byte[]    avcBitStream ;
	private   int[]      avcBitStreamLength  ;
	private   int[]      nalBufferType ;
	private   YzrAvcEnc              yzrAvcEnc ;
	private   double              mFrameRate  = -0.002 ;
	private   TextView           frameRateTextView = null ;
	private   String                frameRatePreText  = null ;  
	private   int                    avcBitStreamBufLength  ;	
	private   PowerManager.WakeLock  wl ;
	private   GraphicsView           graphicsView ;
	private   int[]         argbArray ; 
	private   int           yuv_width ;
	private   int           yuv_height ;	
	private   boolean    encodeContinue ; 
	private   Yuv2Rgb    yuv2Rgb = null ;
	private   Thread      thread = null ;


	private   YzrAvcDec   yzrAvcDec ;
	private   boolean      decodeContinue ;
	
	
	private   boolean      loopContinue ;
	private   VideoCameraView cameraView ;

	private   boolean	recContinue  ;

	
	private final Handler myHandler = new MainHandler() ;

	private int updateTimes = 0 ;
	private class MainHandler extends Handler{
		@Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case AvcThread.MSG_UPDATE_YUVVIEW: {
	            	updateTimes ++ ;	
	            	Log.v("GraphicsDraw", "AvcThread.MSG_UPDATE_YUVVIEW") ;
	            	graphicsView.update( argbArray ,yuv_width , yuv_height  );
	                break;
	            }
	            case AvcThread.MSG_UPDATE_FRAMERATE: {
			String str ;
			if( frameRatePreText == null ) 
				str = "Enc/Dec frame rate is " ;
			else
				str = frameRatePreText ;
	            	str +=  mFrameRate ;
	            	frameRateTextView.setText( str  ) ;
	            	frameRateTextView.bringToFront();		
	            	break ;
	            }
	        }
	    }
    }
	
	AvcThread( PowerManager.WakeLock wl1){
		Log.v(TAG, "AvcThread created constructor1");
		wl = wl1 ;		
	}
	
	AvcThread( PowerManager.WakeLock wl1 , GraphicsView graphicsView  )	{
		Log.v(TAG, "AvcThread created");
		wl = wl1 ;		
		this.graphicsView = graphicsView ;
	}

	public void setGraphicsView( GraphicsView graphicsView ) {
		this.graphicsView = graphicsView ;
	}
	public void setFrameRateTextView( TextView tv ) {	
		frameRateTextView = tv ;	
	}
	
	void startAvcEnc(final String yuvf, final String avcf , final int width , final int height , final Handler handler )
	{
		yuv_width = width ;
		yuv_height = height ;
		yuv = new byte[ width * height *3/2];
		yzrAvcEnc = new YzrAvcEnc(width,height, 15 , 300000 , 3 , 2 , YzrAvcEnc.YUVFORMAT_YUV420P  ) ;
		avcBitStreamBufLength = width*height*3/2 ;
		argbArray = new int[width*height] ;
		yuv2Rgb = new Yuv2Rgb( width , height , Yuv2Rgb.YUVFORMAT_YUV420P  ) ;
		encodeContinue = true ; 
		
		final Runnable EncRun = new Runnable() {		    	
		    	public void run() {

				boolean	spspps = true ;  	
				int yuvbytes ;	
				int frames = 0 ;
				avcBitStream = new byte[avcBitStreamBufLength];
				avcBitStreamLength  = new int[1] ;
				nalBufferType = new int[1] ;
				FileInputStream fi ;
				FileOutputStream fo ;
				wl.acquire() ;
		    		try {
		    			fi = new FileInputStream( yuvf ) ;
		    			fo = new FileOutputStream ( avcf ) ;
		    		}
		    		catch ( IOException e)
		    		{
		    			return ;
		    		}
		    		while( encodeContinue )
		    		{
		    			//if( frames % 10 == 0 )
		    				Log.v(TAG+"-ENC","In run AvcThread.encodeContinue="+encodeContinue+" frames="+frames ) ;
		    			try
		    			{
		    				if( spspps )
		    				{
			    				yzrAvcEnc.YzrAvcEncGetSps( avcBitStream, avcBitStreamLength ) ;
			    				fo.write( avcBitStream , 0, avcBitStreamLength[0]) ;    				
			    				yzrAvcEnc.YzrAvcEncGetPps( avcBitStream, avcBitStreamLength ) ;
			    				fo.write( avcBitStream , 0, avcBitStreamLength[0]) ;
			    				spspps = false ;
		    				}
		    				yuvbytes = fi.read(yuv) ;
		    				if( yuvbytes < width*height*3/2  )
		    					break ;
		    				
		    				frames ++ ;
		    				 yuv2Rgb.Yuv2RgbOneFrame(yuv, argbArray );
		    				 myHandler.sendEmptyMessage(MSG_UPDATE_YUVVIEW) ;

		    				avcBitStreamLength[0] = avcBitStreamBufLength ;	
		    				yzrAvcEnc.YzrAvcEncEncodeOneFrame(yuv,  avcBitStream , avcBitStreamLength , nalBufferType) ;
		    				fo.write( avcBitStream , 0, avcBitStreamLength[0]) ;
		    				
		    			}
		    			catch (IOException e)
		    			{
		    				
		    			}	    			
		    		}
				yzrAvcEnc.YzrAvcEncClose() ;		
	
		    		Log.v(TAG,"In run AvcThread.encodeContinue="+encodeContinue+" frames="+frames ) ;
		    		
		    		try 
		    		{
		    			fi.close() ;
		    			fo.close() ;
		    		}
		    		catch (IOException e)
		    		{
		    				
		    		}		
		    		wl.release() ;
		    		handler.sendEmptyMessage(MSG_ENCODER_FINISH ) ;
		    		Log.v(TAG,"AvcThread Finish encoding " ) ;
			}
		} ;		
		startLowPriorityNewThread(EncRun);		
	}
	public void stopAvcEnc() {
		encodeContinue = false ;
		Log.v(TAG,"in stopAvcEnc AvcThread.encodeContinue="+encodeContinue ) ;
	}
	
	
	void startAvcDec(final String yuvf, final String avcf , final Handler handler )
	{
		decodeContinue = true ; 
		final Runnable DecRun = new Runnable() {		    	
			public void run() {	 
				int         avbitbytes = 0 ;	 
				int         bitstreamOffset = 0 ;
				boolean  needRead = true ;
				avcBitStreamBufLength = 1024*30 ;
				avcBitStream = new byte[avcBitStreamBufLength];
				int[] offsetOfFrameInAvcBitStream  = new int[1] ;
				int[] lengthOfFrame = new int[1] ;
				FileInputStream fi ;
				int frames = 0 , noframes = 0 ;
				int readbytes = 0 ;

				if( yuvf == null )
					yzrAvcDec = new YzrAvcDec() ;
				else
					yzrAvcDec = new YzrAvcDec(yuvf) ;
				NativeYzrAvcDecYUV420 yzrDecYuv420 = new NativeYzrAvcDecYUV420() ;
		    		wl.acquire() ;
		    		try {
		    			fi = new FileInputStream ( avcf ) ;
		    		}
		    		catch ( IOException e)
		    		{
		    			return ; 
		    		}

		    		while( decodeContinue )
		    		{
		    			long timeStart = System.currentTimeMillis() ;
		    			if( frames%10==0 )
		    				Log.v(TAG+"-DEC","In run AvcThread.decodeContinue="+decodeContinue+" frames="+frames ) ;
		    			try
		    			{
		    				if( needRead ) {
		    					avbitbytes = fi.read(avcBitStream, bitstreamOffset , avcBitStreamBufLength - bitstreamOffset ) ;
							readbytes += avbitbytes ;
							if( avbitbytes <=  0  ) {
								Log.v(TAG,"file end" ) ;	
		    						break ;
		    					}		
							avbitbytes += bitstreamOffset ;
							bitstreamOffset = 0 ;
							offsetOfFrameInAvcBitStream[0]  = 0 ;
							needRead = false ;
		    				}
						lengthOfFrame[0] =  avbitbytes - offsetOfFrameInAvcBitStream[0] ;	
						boolean b = yzrAvcDec.YzrAvcDecAnnexBGetNALUnit(  avcBitStream , offsetOfFrameInAvcBitStream , lengthOfFrame ) ;
						if( ! b  )  {							
							int i ;
							for( i = 0 ; i < avbitbytes - offsetOfFrameInAvcBitStream[0] ; i ++ ){
								avcBitStream[i] = avcBitStream[i + offsetOfFrameInAvcBitStream[0]] ; 
							}
							bitstreamOffset = 	avbitbytes - offsetOfFrameInAvcBitStream[0] ;
							needRead = true ;	
							continue ;
						}
 
						offsetOfFrameInAvcBitStream[0] -= 3 ; 
						lengthOfFrame[0] += 3 ;
						b = yzrAvcDec.decodeOneFrame( yzrDecYuv420, avcBitStream , offsetOfFrameInAvcBitStream[0] , lengthOfFrame[0] ) ;
						if( yzrDecYuv420.yPointer != 0  ) {
							frames ++ ; 
							if( frames == 712)
								Log.v(TAG, "come ");
							if( argbArray == null ) {
								argbArray = new int[ yzrDecYuv420.width * yzrDecYuv420.height ] ;
							}
							if( yuv2Rgb == null )
								yuv2Rgb = new Yuv2Rgb( yzrDecYuv420.width ,  yzrDecYuv420.height , yuv2Rgb.YUVFORMAT_YUV420P ) ;
							
							yuv2Rgb.Yuv2RgbOneFrame( yzrDecYuv420.yPointer, yzrDecYuv420.uPointer, yzrDecYuv420.vPointer, argbArray );	
							yuv_width = yzrDecYuv420.width ;
							yuv_height = yzrDecYuv420.height ;
							//graphicsView.setAspectRatio( (double)yuv_width/yuv_height ) ;
							argbArray[0] =  frames ;
							myHandler.sendEmptyMessage(MSG_UPDATE_YUVVIEW) ;
						}
						else {
							noframes ++ ;							
						}
 
		    				offsetOfFrameInAvcBitStream[0] += lengthOfFrame[0] ;
						if( avcBitStreamBufLength == offsetOfFrameInAvcBitStream[0]  ) {
							bitstreamOffset = 0 ;
							offsetOfFrameInAvcBitStream[0] = 0  ;
							needRead = true ;	
							continue ;		
						}
		    			}
		    			catch (IOException e) {
		    				Log.v(TAG,"error") ;		    				
		    			}
		    			long timeEnd = System.currentTimeMillis() ;
		    			if( timeEnd-timeStart < 40){ 
		    				try {
		    					Thread.sleep( 40-(timeEnd-timeStart))  ;
		    					//Thread.sleep(0) ;
		    				}
		    				catch (InterruptedException e)	{
		    					Log.v(TAG, "thread sleeping error") ;
		    				}
		    			}
		    				
		    		}
		    		yzrAvcDec.YzrAvcDecClose() ;					
		    		Log.v(TAG,"In run AvcThread.decodeContinue="+decodeContinue+" frames="+frames ) ;
		    		Log.v(TAG, "Total read "+ readbytes ) ;
		    		Log.v(TAG, "frames="+frames+" noframes="+noframes+" updateTimes="+updateTimes ) ;
		    		try 
		    		{  
		    			fi.close() ;		    			
		    		}
		    		catch (IOException e) {
					Log.v(TAG , "error 2" ) ;		    				
		    		}		
		    		wl.release() ;
		    		if( handler != null  )
		    			handler.sendEmptyMessage(MSG_DECODER_FINISH ) ;
		    		Log.v(TAG,"AvcThread Finish decoding " ) ;
			}
		} ;		
		startLowPriorityNewThread ( DecRun );		
	}

	public void stopAvcDec() {
		decodeContinue = false ;
		if( thread != null  ) {
			try {
			   thread.join() ;
			}catch( InterruptedException e  ) {
				
			}		
		}
		thread = null;	
		Log.v(TAG,"in stopAvcEnc AvcThread.decodeContinue="+decodeContinue ) ;
	}
///loop
	
	void startAvcLoop(VideoCameraView vcv, final String avcf ,  int width ,  int height , final Handler handler )
	{ 
		frameRatePreText = "Enc/Dec frame rate is " ;
		cameraView = vcv ;
		cameraView.openCamera(width, height ) ;
		graphicsView.setAspectRatio( cameraView.getAspectRatio()) ;
		width = cameraView.getPreviewWidth() ;
		height = cameraView.getPreviewHeight() ;
		yuv_width = width ;
		yuv_height = height ;
		yuv = new byte[ width * height *3/2];
		yzrAvcEnc = new YzrAvcEnc(width,height, 15 , 300000 , 3 , 2 , YzrAvcEnc.YUVFORMAT_YUV420SP  ) ;
		avcBitStreamBufLength = width*height*3/2 ;
		argbArray = new int[width*height] ;
		//yuv2Rgb = new Yuv2Rgb( width , height , Yuv2Rgb.YUVFORMAT_YUV420P  ) ;
		loopContinue = true ; 

		cameraView.setYzrAvcEncHandle( yzrAvcEnc ) ;

		yzrAvcDec = new YzrAvcDec() ;
				
		
		final Runnable LoopRun = new Runnable() {		    	
			public void run() {
				boolean	spspps = true ;  	
				int yuvbytes ;	
				int frames = 0 ;
				avcBitStream = new byte[avcBitStreamBufLength];
				avcBitStreamLength  = new int[1] ;
				nalBufferType = new int[1] ;
				FileInputStream fi ;
				FileOutputStream fo ;
				
				NativeYzrAvcDecYUV420 yzrDecYuv420 = new NativeYzrAvcDecYUV420() ;
				int decframes = 0 ;
				int decnoframes = 0 ;
				
				wl.acquire() ;
				try {		    			
					fo = new FileOutputStream ( avcf ) ;
				}catch ( IOException e)	{
					return ;
				}
				long timeStart = System.currentTimeMillis() ;
				
				while( loopContinue )
				{
					if( frames % 10 == 0 )
						Log.v(TAG+"-Loop","In run AvcThread.encodeContinue="+loopContinue+" frames="+decframes ) ;
					try {
						if( spspps ) {
							cameraView.startRec() ;
							yzrAvcEnc.YzrAvcEncGetSps( avcBitStream, avcBitStreamLength ) ;
							fo.write( avcBitStream , 0, avcBitStreamLength[0]) ;
							boolean b = yzrAvcDec.decodeOneFrame( yzrDecYuv420, avcBitStream , 0 , avcBitStreamLength[0] ) ;
							yzrAvcEnc.YzrAvcEncGetPps( avcBitStream, avcBitStreamLength ) ;
							fo.write( avcBitStream , 0, avcBitStreamLength[0]) ;
							b = yzrAvcDec.decodeOneFrame( yzrDecYuv420, avcBitStream , 0 , avcBitStreamLength[0] ) ;
							spspps = false ;
						}

						Log.v("Performance", "before encode") ;
						int r = cameraView.encodeOneFrame(avcBitStream , avcBitStreamBufLength ) ;
						Log.v("Performance", "after encode with return "+r) ;
						if( r > 0 )
							fo.write( avcBitStream , 0, r) ;	

						frames ++ ;
						Log.v("Performance", "before decode ") ;
						boolean b = yzrAvcDec.decodeOneFrame( yzrDecYuv420, avcBitStream , 0 , r ) ;
						Log.v("Performance", "after decode with return  " + b) ;
						if( yzrDecYuv420.yPointer != 0  ) {
							decframes ++ ; 
							if( argbArray == null ) {
								argbArray = new int[ yzrDecYuv420.width * yzrDecYuv420.height ] ;
							}
							if( yuv2Rgb == null )
								yuv2Rgb = new Yuv2Rgb( yzrDecYuv420.width ,  yzrDecYuv420.height , yuv2Rgb.YUVFORMAT_YUV420P ) ;
							Log.v("Performance", "before yuv2rgb ") ;	
							yuv2Rgb.Yuv2RgbOneFrame( yzrDecYuv420.yPointer, yzrDecYuv420.uPointer, yzrDecYuv420.vPointer, argbArray );	
							Log.v("Performance", "after yuv2rgb ") ;
							yuv_width = yzrDecYuv420.width ;
							yuv_height = yzrDecYuv420.height ;
							argbArray[0] =  frames ;
							myHandler.sendEmptyMessage(MSG_UPDATE_YUVVIEW) ;
						} else {
							decnoframes ++ ; 
						}		    				
					}catch (IOException e)	{
					
					}
					if( frames % 20 == 0 ) {
						long timeCurrent = System.currentTimeMillis() ;
						double frameRate = frames*1000 ;
						frameRate /=  timeCurrent-timeStart ;
						mFrameRate = frameRate ;
						myHandler.sendEmptyMessage(MSG_UPDATE_FRAMERATE) ;
					}
				}
				long timeEnd = System.currentTimeMillis() ;
				
				
				yzrAvcEnc.YzrAvcEncClose() ;
				yzrAvcDec.YzrAvcDecClose() ;
				Log.v(TAG,"In run AvcThread.encodeContinue="+encodeContinue+" frames="+frames ) ;
		    		
				try  {
					byte fr[] = new byte[8] ;
					fr[0] = 102 ;
					fr[1] = 114 ;
					fr[2] = 116 ;
					fr[3] = 101 ;
					long lframeRate = frames*1000000 ;
					lframeRate /= timeEnd-timeStart ;
					int frameR = (int)lframeRate ;
					fr[4] = (byte)(frameR & 0xff) ;
					fr[5] = (byte)((frameR>>8) & 0xff) ;
					fr[6] = (byte)((frameR>>16) & 0xff) ;
					fr[7] = (byte)((frameR>>24) & 0xff) ;
					fo.write( fr ) ;
					fo.close() ;
				} catch (IOException e) 	{
						
				}		
				wl.release() ;
				cameraView.stopRec() ;
				handler.sendEmptyMessage(MSG_ENCODER_FINISH ) ;
				cameraView.stopCamera() ;
				Log.v(TAG,"AvcThread Finish encoding " ) ;
			}
		} ;	 
		startLowPriorityNewThread ( LoopRun ) ;
	}

	public void stopAvcLoop() {
		loopContinue = false ;	
		if( thread != null  ) {
			try {
			   thread.join() ;
			}catch( InterruptedException e  ) {
				
			}		
		}

		thread = null;	
		//cameraView.stopCamera() ;
		Log.v(TAG,"in stopAvcLoop AvcThread.loopContinue="+loopContinue ) ;
	}
	


	void startAvcRec(VideoCameraView vcv, final String avcf ,  int width ,  int height , final Handler handler )
	{ 
		frameRatePreText = "Enc frame rate is " ;
		cameraView = vcv ;
		cameraView.openCamera(width, height ) ;

		width = cameraView.getPreviewWidth() ;
		height = cameraView.getPreviewHeight() ;
		yuv_width = width ;
		yuv_height = height ;
		yuv = new byte[ width * height *3/2];
		yzrAvcEnc = new YzrAvcEnc(width,height, 15 , 300000 , 3 , 2 , YzrAvcEnc.YUVFORMAT_YUV420SP  ) ;
		avcBitStreamBufLength = width*height*3/2 ;
		argbArray = new int[width*height] ;
		recContinue = true ; 

		cameraView.setYzrAvcEncHandle( yzrAvcEnc ) ;
				
		
		final Runnable recRun = new Runnable() {
			public void run() {
				boolean	spspps = true ;  	
				int yuvbytes ;	
				int frames = 0 ;
				avcBitStream = new byte[avcBitStreamBufLength];
				avcBitStreamLength  = new int[1] ;
				nalBufferType = new int[1] ;
				FileOutputStream fo ;				
				
				wl.acquire() ;
				try {		    			
					fo = new FileOutputStream ( avcf ) ;
				}catch ( IOException e)	{
					return ;
				}
				long timeStart = System.currentTimeMillis() ;
				
				while( recContinue )
				{
					try {
						if( spspps ) {
							cameraView.startRec() ;
							yzrAvcEnc.YzrAvcEncGetSps( avcBitStream, avcBitStreamLength ) ;
							fo.write( avcBitStream , 0, avcBitStreamLength[0]) ;
							yzrAvcEnc.YzrAvcEncGetPps( avcBitStream, avcBitStreamLength ) ;
							fo.write( avcBitStream , 0, avcBitStreamLength[0]) ;
							spspps = false ;
						}

						Log.v("Performance", "before encode") ;
						int r = cameraView.encodeOneFrame(avcBitStream , avcBitStreamBufLength ) ;						
						Log.v("Performance", "after encode with return "+r) ;
						if( r > 0 ){
							fo.write( avcBitStream , 0, r) ;	
							frames ++ ;
						}

					}catch (IOException e)	{
					
					}
					if( frames % 20 == 0 ) {
						long timeCurrent = System.currentTimeMillis() ;
						double frameRate = frames*1000 ;
						frameRate /=  timeCurrent-timeStart ;
						mFrameRate = frameRate ;
						myHandler.sendEmptyMessage(MSG_UPDATE_FRAMERATE) ;
					}
				}
				long timeEnd = System.currentTimeMillis() ;	
				
				yzrAvcEnc.YzrAvcEncClose() ;		    		
				try  {
					byte fr[] = new byte[8] ;
					fr[0] = 102 ;
					fr[1] = 114 ;
					fr[2] = 116 ;
					fr[3] = 101 ;
					long lframeRate = frames*1000000 ;
					lframeRate /= timeEnd-timeStart ;
					int frameR = (int)lframeRate ;
					fr[4] = (byte)(frameR & 0xff) ;
					fr[5] = (byte)((frameR>>8) & 0xff) ;
					fr[6] = (byte)((frameR>>16) & 0xff) ;
					fr[7] = (byte)((frameR>>24) & 0xff) ;
					fo.write( fr ) ;
					fo.close() ;
				} catch (IOException e) 	{
						
				}		
				wl.release() ;
				cameraView.stopRec() ;
				handler.sendEmptyMessage( MSG_AVCREC_FINISH ) ;
				cameraView.stopCamera() ;
				Log.v(TAG,"AvcThread Finish encoding " ) ;
			}
		} ;	 
		startLowPriorityNewThread ( recRun ) ;
	}
	
	private void startLowPriorityNewThread(Runnable run) {
		thread = new Thread( run ) ;
		int pr = thread.getPriority() ;
		Log.v( TAG, "Original Thread priority is " + pr ) ;
		thread.setPriority( pr - 1 ) ;
		pr = thread.getPriority() ;
		Log.v( TAG, "new Thread priority is " + pr ) ;
		thread.start();		
	}
	
	
	public void stopAvcRec() {		 
		recContinue = false ;		
		if( thread != null  ) {
			try {
			   thread.join() ;
			}catch( InterruptedException e  ) {
				
			}		
		}

		thread = null;
	}
	
	
	
}
