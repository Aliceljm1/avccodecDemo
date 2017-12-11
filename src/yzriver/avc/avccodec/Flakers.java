package yzriver.avc.avccodec;

import android.util.Log;


public class Flakers {
	static final String TAG = "avccodecDemo" ;
	public Flakers(){  
		
	}
	static {
		Log.e( TAG , "Flakers class  create") ;
	}
	private static int age = 0 ; 
	static public void increment() { 
		age ++ ; 
	}
}
