package yzriver.avc.avccodec;

import java.io.File;
import java.io.FilenameFilter;
import java.io.File;
import android.util.Log;

public class AvcFileList {
	private String TAG = "AvcFileList" ;
	private AvcFilenameFilter avcFileFilter;	
	AvcFileList() {
		avcFileFilter = new AvcFilenameFilter();
	}	
	public String[] getAvcFiles(String dir) {
		 File f = new File( dir ) ;
		 String[] afs = f.list(avcFileFilter) ;
		 return afs ;
	}
	
	public void test() {
		AvcFileList afl = new AvcFileList() ;
		String[] afs = afl.getAvcFiles("/sdcard/avccodecDemo") ;
		Log.v(TAG, "list avc files ") ;
		for( int i = 0 ; i < afs.length; i ++ )
			Log.v(TAG,afs[i]) ;
	}

}


class AvcFilenameFilter implements FilenameFilter {
	private String TAG = "AvcFilenameFilter" ;
	private boolean isAvcFile(String fname) {
		if( fname.toLowerCase().endsWith(".avc"))
			return true;
		else 
			return false ;
	}
	public boolean accept(File dir , String fname) {
		Log.v(TAG, dir.getAbsolutePath()+"/"+fname) ;
		return isAvcFile( fname ) ;
	}
}