package yzriver.avc.avccodec;

import yzriver.avc.avccodecDemo.R;
import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity; 
import android.content.Context;
import android.content.Intent;

public class AvcViewActivity extends Activity {
	private GraphicsView graphicsView ;
	private AvcThread avcThread ;
	private PowerManager.WakeLock wl ;
	
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState ) ;
		setContentView(R.layout.avcview) ;
		graphicsView = (GraphicsView)findViewById(R.id.GraphicsAvcview) ;
		Intent intent = getIntent() ;
		String url = intent.getDataString() ;
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "avcthread" );
		avcThread = new AvcThread( wl , graphicsView  ) ;
		avcThread.startAvcDec(null, url, null);
		return ;	
	}
}
