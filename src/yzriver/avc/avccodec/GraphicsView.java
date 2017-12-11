package yzriver.avc.avccodec;

import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.util.Log;
import android.view.View;
import android.util.AttributeSet;
import android.graphics.Rect;
import android.graphics.Bitmap;

public class GraphicsView extends View{
	private static final String TAG = "GraphicsDraw";
	private   int[] colorArray = null ;
	private   int   bitmapWidth ;
	private   int   bitmapHeight ;
	private double mAspectRatio = 3.0/3.0 ;

	private int previewWidth ;
	private int previewHeight ;
	private Rect  src = new Rect()  ;
	private Rect dst  = new Rect() ;

	

/*	public    void SetBitmapSize(int w , int h){
	bitmapWidth = w ;
	bitmapHeight = h ;		
	}
*/
	public GraphicsView(Context context ){
		super(context) ;
	}

	public GraphicsView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}

/*
	public int[] GetArgbArray(){        	
		colorArray = new int[bitmapWidth*bitmapHeight] ;        		
		return colorArray ;
	}
*/	

	public void setAspectRatio(double  vv ) {
		mAspectRatio =  vv  ;	
		requestLayout() ;
	}
	public void update( int[]  updateArgb , int width , int height ) {
		if( updateArgb != null ) {
			Log.v(TAG, "update "+ updateArgb[0] );
			colorArray = updateArgb ;
			bitmapWidth = width ;
			bitmapHeight = height ;
			invalidate();	
		}
	}	
	protected void onDraw(Canvas canvas){
		Paint cPaint = new Paint( Paint.ANTI_ALIAS_FLAG ) ;
		if( colorArray != null )
		{	
			Log.v(TAG, "onDraw "+ colorArray[0] );
			if(  (bitmapWidth == previewWidth) && (bitmapHeight == previewHeight) )
				canvas.drawBitmap(colorArray, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight, true, cPaint);
			else if( (bitmapWidth < previewWidth) && (bitmapHeight < previewHeight) ) {
				src.left = src.top = 0 ;
				src.right = bitmapWidth ;
				src.bottom = bitmapHeight ;
				dst.left = (previewWidth-bitmapWidth)/2 ;
				dst.top = (previewHeight-bitmapHeight)/2 ;
				dst.right = dst.left + bitmapWidth ;
				dst.bottom = dst.top + bitmapHeight ;
				Bitmap bmp = Bitmap.createBitmap( colorArray , 0, bitmapWidth,bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888 )  ;
				canvas.drawBitmap( bmp , src , dst , cPaint ) ;				
			}
			else {
				src.left = src.top = 0 ;
				src.right = bitmapWidth ;
				src.bottom = bitmapHeight ;
				dst.left = dst.top = 0 ;
				dst.right = previewWidth ;
				dst.bottom = previewHeight ;
				Bitmap bmp = Bitmap.createBitmap( colorArray , 0, bitmapWidth,bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888 )  ;
				canvas.drawBitmap( bmp , src , dst , cPaint ) ;
			}
			Log.v(TAG, "onDraw finished" );
		}
		return ;
	}

    protected void onMeasure(int widthSpec, int heightSpec) {
        previewWidth = MeasureSpec.getSize(widthSpec);
        previewHeight = MeasureSpec.getSize(heightSpec);
	 Log.v(TAG , "previewWidth="+previewWidth+" previewHeight="+previewHeight ) ;
        if (previewWidth > previewHeight * mAspectRatio) {
            previewWidth = (int) (previewHeight * mAspectRatio + .5);
        } else {
            previewHeight = (int) (previewWidth / mAspectRatio + .5);
        }

	Log.v(TAG , "previewWidth="+previewWidth+" previewHeight="+previewHeight ) ;
        // Ask children to follow the new preview dimension.
        super.onMeasure(MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(previewHeight, MeasureSpec.EXACTLY));
    }


	
}
