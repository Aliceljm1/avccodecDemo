package yzriver.avc.avccodec;

import java.util.Calendar;

import yzriver.avc.avccodecDemo.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button; 
import android.widget.EditText;
import android.widget.TextView;



public class AvcRecActivity  extends Activity implements View.OnClickListener {

	static final String TAG = "AvcRecActivity" ;
	
	private PowerManager.WakeLock wl ;

	AvcThread avcThread  = null ;
	
	Button bRec ;
	Button bRecStop ;	
	TextView avcRecFrameTextView ;
	
	VideoCameraView avcRecVideoCameraView ;
	
	private final Handler handler = new MainHandler() ;
	

	private class MainHandler extends Handler{
		@Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case AvcThread.MSG_AVCREC_FINISH: {
	            	bRec.setEnabled(true) ;	
		             break;
	            }
	        }
	    }
    }
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.v( TAG ,"AvcRecActivity.onCreate" ) ;
        super.onCreate(savedInstanceState);
	  Debug.startMethodTracing("avccodecDemo" ) ;	
        Init.PrepareRawData( this ) ;
        setContentView(R.layout.avcrec);
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "avcthread" );

		 
		avcRecVideoCameraView =(VideoCameraView)findViewById(R.id.lsurfaceViewAvcRec) ;
		SurfaceHolder surfaceHolder = avcRecVideoCameraView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS) ;
		avcRecVideoCameraView.setHandler( handler ) ;
		
		avcThread = new AvcThread( wl ) ;
		
		bRec  = (Button)findViewById( R.id.AvcRec_start_button  ) ;
		bRec.setOnClickListener(this) ;
		bRecStop =  (Button)findViewById( R.id.AvcRec_stop_button  ) ;
		bRecStop.setOnClickListener(this) ;		

		avcRecFrameTextView = (TextView) findViewById( R.id.AvcRecFramerate ) ;
		
		Log.v(TAG ,"AvcRecActivity.onCreate finished" ) ;
    }

    protected void onPause() {
        avcThread.stopAvcRec();	
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
    		case R.id.AvcRec_start_button:
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
    			
    			avcThread.setFrameRateTextView(avcRecFrameTextView) ;
    			avcThread.startAvcRec(avcRecVideoCameraView , avc ,  352 , 288 , handler);
    			v.setEnabled(false) ;    		 	 
    			break;
    		case R.id.AvcRec_stop_button:
    			avcThread.stopAvcRec() ;
    			break;
    	}  	
    } 
    
}
