package yzriver.avc.avccodec;



import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;
import android.widget.EditText;
import android.widget.Button;
import java.io.File ;
import java.io.IOException ;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import yzriver.avc.avccodecDemo.R;
import android.os.Handler;

import android.content.res.AssetManager;  


public class AvccodecDemoActivity extends Activity implements View.OnClickListener {
    /** Called when the activity is first created. */
	static final String TAG = "avccodecDemo" ;
	int       YUV_WIDTH = 320 ;
	int       YUV_HEIGHT = 240 ;
	private PowerManager.WakeLock wl ;
	EditText edTextEncyuvfilename ;
	EditText edTextEncedavcfilename ;
	EditText edTextDecyuvfilename ;
	EditText edTextDecAvcfilename ;
	EditText edTextLoopAvcfilename ;
	Button bEnc ;
	Button bEncStop ;
	Button bDec ;
	Button bDecStop ;
	Button bLoop ;
	Button bLoopStop ;	
	
	GraphicsView  graphicsViewEnc ;
	GraphicsView  graphicsViewDec ;
	
	GraphicsView  graphicsViewLoop ;
	VideoCameraView loopVideoCameraView ;
	
	AvcThread avcThread  = null ;
	
	private final Handler handler = new MainHandler() ;
	
	private class MainHandler extends Handler{
		@Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case AvcThread.MSG_ENCODER_FINISH: {
	            	bEnc.setEnabled(true) ;
	            	Log.v(TAG,"AvccodecDemoActivity Finish encoding " ) ;
		             break;
	            }
	            case AvcThread.MSG_DECODER_FINISH:{
		           	bDec.setEnabled(true) ;
		           	Log.v(TAG,"AvccodecDemoActivity Finish decoding " ) ;						
		             break;
	            }
	        }
	    }
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.v(TAG+" VideoCameraView","AvccodecDemoActivity.onCreate" ) ;
        super.onCreate(savedInstanceState);
        Init.PrepareRawData( this ) ; 
        setContentView(R.layout.main);
		TabHost tabs=(TabHost)findViewById(R.id.tabhost);		
		tabs.setup();		
		
		TabHost.TabSpec spec;
		
		spec=tabs.newTabSpec("tag1");
		spec.setContent(R.id.enctab);
		String encIndicator = this.getString(R.string.enc_tab_indicator);
		spec.setIndicator( encIndicator ) ;//   "Encode");		
		tabs.addTab(spec);		
		
		spec=tabs.newTabSpec("tag2");
		spec.setContent(R.id.dectab);
		String decIndicator = this.getString(R.string.dec_tab_indicator);
		spec.setIndicator( decIndicator ) ;//   "Decode");
		tabs.addTab(spec);

		spec=tabs.newTabSpec("tag3");
		spec.setContent(R.id.looptab);
		String loopIndicator = this.getString(R.string.loop_tab_indicator);
		spec.setIndicator( loopIndicator ) ;//   "loop");
		tabs.addTab(spec);
		
		
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "avcthread" );
		graphicsViewEnc = (GraphicsView)findViewById(R.id.GraphicsViewenc) ;
		graphicsViewDec = (GraphicsView)findViewById(R.id.GraphicsViewdec) ;

		graphicsViewLoop = (GraphicsView)findViewById(R.id.GraphicsViewLoopDec) ; 
		loopVideoCameraView =(VideoCameraView)findViewById(R.id.surfaceViewLoopEnc) ;
		
		
		avcThread = new AvcThread( wl ) ;
		edTextEncyuvfilename =(EditText) findViewById(R.id.encyuvfilename) ;
		edTextEncedavcfilename =(EditText) findViewById(R.id.encedavcfilename) ;
		edTextDecyuvfilename =(EditText) findViewById(R.id.decyuvfilename) ;
		edTextDecAvcfilename =(EditText) findViewById(R.id.decavcfilename) ;
		edTextLoopAvcfilename =(EditText) findViewById(R.id.loopavcfilename) ;
		
		bEnc = (Button)findViewById( R.id.button_encode  ) ;		
		bEnc.setOnClickListener(this) ;
		bEncStop = (Button)findViewById( R.id.button_encode_stop  ) ;
		bEncStop.setOnClickListener(this) ;
		bDec = (Button)findViewById( R.id.button_decode  ) ;		
		bDec.setOnClickListener(this) ;
		bDecStop = (Button)findViewById( R.id.button_decode_stop  ) ;
		bDecStop.setOnClickListener(this) ;
		bLoop  = (Button)findViewById( R.id.loop_start_button  ) ;
		bLoop.setOnClickListener(this) ;
		bLoopStop =  (Button)findViewById( R.id.loop_stop_button  ) ;
		bLoopStop.setOnClickListener(this) ;
		
		Log.v(TAG+" VideoCameraView","AvccodecDemoActivity.onCreate finished" ) ;
    }
    
    
    public void onClick(View v)
    {
    	int id = v.getId() ; 
    	switch ( id )
    	{
    		case R.id.button_encode :
				String yuv = edTextEncyuvfilename.getText().toString() ;
				String avc = edTextEncedavcfilename.getText().toString() ;
				bEnc.setEnabled(false) ; 
				avcThread.setGraphicsView(graphicsViewEnc) ;
				avcThread.startAvcEnc( yuv, avc, 320 , 240 , handler ) ;    			
				break;
    		case R.id.button_encode_stop:    			
    			avcThread.stopAvcEnc() ;
    			break;
    		case R.id.button_decode:
    			yuv = edTextDecyuvfilename.getText().toString() ;
    			avc = edTextDecAvcfilename.getText().toString() ; 
    			bDec.setEnabled( false ) ;
    			avcThread.setGraphicsView(graphicsViewDec) ;
    			avcThread.startAvcDec(yuv, avc, handler);
    			break ;
    		case R.id.button_decode_stop:
    			avcThread.stopAvcDec() ;
    			break;	
    		case R.id.loop_start_button:
    			avc = edTextLoopAvcfilename.getText().toString() ;
    			avcThread.setGraphicsView(graphicsViewLoop) ;    			
    			avcThread.startAvcLoop( loopVideoCameraView ,  avc, 352, 288,  handler);    			
    			break;
    		case R.id.loop_stop_button:
    			avcThread.stopAvcLoop() ; 
    			break;
    	}  	
    }    
}
