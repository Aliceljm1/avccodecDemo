package yzriver.avc.avccodec;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Gallery;
import android.widget.BaseAdapter;
import android.content.Context;
import android.widget.AdapterView ;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast ;
import android.widget.ImageView;
import android.util.Log;
import android.view.View ;
import android.view.ViewGroup;
import android.content.res.TypedArray;
import android.widget.Button;
import android.content.Intent;
import yzriver.avc.avccodecDemo.R;


public class HelloGalleryActivity extends Activity  implements View.OnClickListener{
    /** Called when the activity is first created. */
	
	Button button1 ;
	Button button2 ;
	Button button3 ;
	Button button4 ;
	@Override	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    Init.PrepareRawData( this ) ;
	    setContentView( R.layout.hellogallery); 

	    button1 = (Button) findViewById(R.id.button_start_loop) ;
	    button1.setOnClickListener(this) ;
	 //   button2 = (Button) findViewById(R.id.button_start_separate) ;
	  //  button2.setOnClickListener(this) ;
	    button3 = (Button) findViewById(R.id.button_list_avc) ;
	    button3.setOnClickListener(this) ;
	    button4 = (Button) findViewById(R.id.button_avc_rec) ;
	    button4.setOnClickListener(this) ;

	    
	    Gallery g = (Gallery) findViewById(R.id.gallery);	    
	    g.setGravity(77) ;
	    g.setAdapter(new ImageAdapter(this));

	}
	
	public void onClick(View v){
		Intent intent = null ;
		int id = v.getId() ;
		switch( id ) {
//			case R.id.button_start_separate:
			//	;//intent =  new  Intent( this , AvccodecDemoActivity.class );
	//			break ;
			case R.id.button_start_loop:
				intent =  new  Intent( this , LoopAvccodecActivity.class );
				break ;			
			case R.id.button_list_avc:
				intent =  new  Intent( this , AvcFileListActivity.class );
				break ;			
			case R.id.button_avc_rec:
				intent =  new  Intent( this , AvcRecActivity.class );
				break ;			
				
		}				
		startActivity( intent ) ;
		Log.v("hjc2345", "start other activity") ;
		//finish() ;
	}
	
	public class ImageAdapter extends BaseAdapter {
	    int mGalleryItemBackground;
	    private Context mContext;

	    private Integer[] mImageIds = {
	            R.drawable.help1 ,
	            R.drawable.help2 ,
	            R.drawable.help3 
	    };

	    public ImageAdapter(Context c) {
	        mContext = c;
	    }

	    public int getCount() {
	    	Log.v("hjc2345", "getCount()") ;
	        return mImageIds.length;
	    }

	    public Object getItem(int position) {
	    	Log.v("hjc2345", "getItem("+position+" )") ;
	        return position;
	    }

	    public long getItemId(int position) {
	    	Log.v("hjc2345", "getItemId( " + position + " ) in ");
	        if( position == 2 ){
	        	button1.setVisibility(View.VISIBLE) ;
	        	;//button2.setVisibility(View.VISIBLE) ;
	        	button3.setVisibility(View.VISIBLE) ;
	        	button4.setVisibility(View.VISIBLE) ;        	
	        }
	        else
	        {
	        	button1.setVisibility(View.INVISIBLE) ;
	//        	button2.setVisibility(View.INVISIBLE) ;
	        	button3.setVisibility(View.INVISIBLE) ;
	        	button4.setVisibility(View.INVISIBLE) ;	        	
	        }
	        Log.v("hjc2345", "getItemId("+position+" ) out") ;        
	        return position;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	        ImageView i = new ImageView(mContext);

	        i.setImageResource(mImageIds[position]);
	        //i.setLayoutParams(new Gallery.LayoutParams(800, 480));
	        i.setScaleType(ImageView.ScaleType.FIT_XY);
	        Log.v("hjc2345", "getView(" + position+")");
 
	        return i;
	    }
	}	
	
    static { 
    	System.loadLibrary("avccodec"); 
        System.loadLibrary("avccodecjni");        
    }	
	
}


