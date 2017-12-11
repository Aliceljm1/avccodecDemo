package yzriver.avc.avccodec;


import yzriver.avc.avccodecDemo.R;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.os.Debug ;
import java.util.Date;
import java.util.Calendar;

public class LoopAvccodecActivity  extends Activity implements View.OnClickListener {

	static final String TAG = "LoopAvccodecActivity" ;
	
	private PowerManager.WakeLock wl ;

	AvcThread avcThread  = null ;
	
	Button bLoop ;
	Button bLoopStop ;
	Button bLoopReport ;
	TextView loopReport ;
	TextView previewFrameRateTextView ;
	GraphicsView  graphicsViewLoop ;
	VideoCameraView loopVideoCameraView ;
	EditText edTextLoopAvcfilename ;
	private final Handler handler = new MainHandler() ;
	

	private class MainHandler extends Handler{
		@Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case AvcThread.MSG_ENCODER_FINISH: {
//	            	bEnc.setEnabled(true) ;
	            	Log.v(TAG,"AvccodecDemoActivity Finish encoding " ) ;
		             break;
	            }
	            case AvcThread.MSG_DECODER_FINISH:{
//		           	bDec.setEnabled(true) ;
		           	Log.v(TAG,"AvccodecDemoActivity Finish decoding " ) ;						
		             break;
	            }
		        case VideoCameraView.MSG_VIDEOCAMERAVIEW_UPDATE_PREVIEWFRAMERATE:{
		        	double dd = (double)msg.arg1/msg.arg2*1000 ;
		        	String s = "preview frame rate: " + dd ;
		        	if( previewFrameRateTextView != null )
		        		previewFrameRateTextView.setText( s ) ;		        	
		        	break ;
	            }
	        }
	    }
    }
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.v( TAG ,"LoopAvccodecActivity.onCreate" ) ;
        super.onCreate(savedInstanceState);
	  Debug.startMethodTracing("avccodecDemo" ) ;	
        Init.PrepareRawData( this ) ;
        setContentView(R.layout.loop);
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "avcthread" );

		graphicsViewLoop = (GraphicsView)findViewById(R.id.lGraphicsViewLoopDec) ; 
		loopVideoCameraView =(VideoCameraView)findViewById(R.id.lsurfaceViewLoopEnc) ;
		SurfaceHolder surfaceHolder = loopVideoCameraView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS) ;
		loopVideoCameraView.setHandler( handler ) ;
		
		avcThread = new AvcThread( wl ) ;
		
		bLoop  = (Button)findViewById( R.id.lloop_start_button  ) ;
		bLoop.setOnClickListener(this) ;
		bLoopStop =  (Button)findViewById( R.id.lloop_stop_button  ) ;
		bLoopStop.setOnClickListener(this) ;
		bLoopReport = (Button)findViewById( R.id.lloop_report_button  ) ;
		bLoopReport.setOnClickListener(this) ;
		
		loopReport = (TextView)findViewById( R.id.loopReport  ) ;
		previewFrameRateTextView = (TextView) findViewById( R.id.previewFramerate ) ;
		
		Log.v(TAG ,"LoopAvccodecActivity.onCreate finished" ) ;
    }

    protected void onPause() {
        avcThread.stopAvcLoop() ;	
        super.onPause();
    }


    public void onStop() {
		super.onStop() ;
		Debug.stopMethodTracing() ;
		Log.v(TAG,"onStop" ) ;		
    }
		
    public void onClick(View v)
    {
    	int id = v.getId() ; 
    	switch ( id )
    	{
    		case R.id.lloop_start_button:
    			//loopVideoCameraView.openCamera(320, 240) ;
    			//loopVideoCameraView.setHandler( handler ) ;
    			 
		    	String sddir = this.getString(R.string.sddir) ;
		    	Calendar cal = Calendar.getInstance() ;
		
		    	int mon = cal.get(Calendar.MONTH) ;
		    	String mm ;
		    	if( mon < 9 )
		    		mm = "0"+(mon+1) ;
		    	else
		    		mm = Integer.toString(mon+1) ;
		    	mon = cal.get(Calendar.DAY_OF_MONTH) ;
		    	if( mon < 10 )
		    		mm += "0"+(mon) ;
		    	else
		    		mm += (mon) ;
		    	mon = cal.get(Calendar.HOUR_OF_DAY) ;
		    	if( mon < 10 )
		    		mm += "0"+(mon) ;
		    	else
		    		mm += (mon) ;		    	
		    	mon = cal.get(Calendar.MINUTE) ;
		    	if( mon < 10 )
		    		mm += "0"+(mon) ;
		    	else
		    		mm += (mon) ;
		    	mon = cal.get(Calendar.SECOND) ;
		    	if( mon < 10 )
		    		mm += "0"+(mon) ;
		    	else
		    		mm += (mon) ;		    	
	    	 
		    	String avc = sddir+"/"+cal.get(Calendar.YEAR)+mm+".avc" ;
    			avcThread.setGraphicsView(graphicsViewLoop) ;
    			avcThread.setFrameRateTextView(loopReport) ;
    			avcThread.startAvcLoop( loopVideoCameraView ,  avc, 352, 288,  handler);
    			v.setEnabled(false) ;    		 	 
    			break;
    		case R.id.lloop_stop_button:
    			avcThread.stopAvcLoop() ;
    			bLoop.setEnabled(true) ;
    			break;
    		case R.id.lloop_report_button:
    			loopReport.setText("1234567890\nabcdefghijklmn\n\1qawsdert") ;
    			loopReport.bringToFront();
    			break;    			
    	}  	
    } 
    
}
