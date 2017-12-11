package yzriver.avc.avccodec;

import android.app.ListActivity; 

import android.os.Bundle;
import java.util.List;
import java.util.ArrayList;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.content.Context;
import android.content.Intent;
import android.net.Uri ;


import yzriver.avc.avccodecDemo.R;

public class AvcFileListActivity extends ListActivity  {
	private Context cthis = this ;
	private String[] avfs ;
	private String  sddir ;
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    AvcFileList afl = new AvcFileList() ;
	    avcFiles = new ArrayList<String>() ;
	    sddir = this.getString(R.string.sddir) ;
	    avfs = afl.getAvcFiles(sddir);
	    for( int i = 0; i < avfs.length ; i ++ )
	    	avcFiles.add(avfs[i]) ;
	    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>( this, R.layout.avcfile_list , avfs ) ;// avcFiles  ) ;	    
	    this.setListAdapter( arrayAdapter );
	    
	    ListView lv = getListView();
	    //lv.setTextFilterEnabled(true); 
	    lv.setOnItemClickListener(new OnItemClickListener() {
  	    public void onItemClick(AdapterView<?> parent, View view,
  	        int position, long id) {
  	    	
  	    	Intent intent = new Intent( cthis , AvcViewActivity.class ) ;
  	    	Uri uri = Uri.parse( sddir+"/"+avfs[position] ) ;
  	    	intent.setData(uri ) ;
  	    	String s = uri.getPath() ;
  	    	String s1 = uri.toString() ;
  	    	startActivity(intent) ;
  	      // When clicked, show a toast with the TextView text
  	      //Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
  	        //  Toast.LENGTH_SHORT).show();
  	    }
  	  });
	    
	    
	}
	private List<String> avcFiles  ;
}
