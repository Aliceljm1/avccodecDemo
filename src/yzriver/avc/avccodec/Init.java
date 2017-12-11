package yzriver.avc.avccodec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import yzriver.avc.avccodecDemo.R;
import android.content.res.AssetManager;
import android.util.Log;
import android.content.Context;

public class Init {
	static private String TAG="avccodec.Init" ;

	static public  void PrepareRawData( Context  context ) {
	Flakers.increment() ;	
    	String sddir = context.getString(R.string.sddir) ;
    	File file = new File(sddir) ;
    	if( !file.exists() ){
    		try{
    			file.mkdir() ;
    		}
    		catch(SecurityException e){    			
    		}
    	}
    	if( !file.exists() )
    		return ;
    	
    	String yuv =  context.getString(R.string.enc_yuv_file) ;
    	file = new File( yuv ) ;
    	if( file.exists() )
    		return ;
    	FileOutputStream fo = null ;
    	try {
    		fo = new FileOutputStream(file) ;
    	}
    	catch(FileNotFoundException e){
           	Log.v(TAG,"Init yuv data file fails" ) ;    		
    	}
    	AssetManager asm = context.getResources().getAssets() ;
    	InputStream fi = null ;
    	try {
    		fi = asm.open( context.getString(R.string.asset_yuv_file) ) ;
    	}
    	catch (IOException e ) {    		
    	}
    	byte[] buf = new byte[1024] ;
    	int readb ;
    	while ( true  ) {
    		try {
    			readb =  fi.read(buf) ;
    			if( readb <= 0 )
    				break ;
    			fo.write(buf , 0, readb) ;
    		}
    		catch(IOException e){
    			break ;
    		}    		
    	}
    	try {
    		fo.close() ;
    		fi.close() ;
    	}
    	catch(IOException e){
    		
    	}
    } 

}
