package yzriver.avc.avccodec;
import android.util.Log;

public class YzrAvcEnc { 
	static public final int YUVFORMAT_YUV420P = 1 ;
	static public final int YUVFORMAT_YUV420SP = 2 ;
			
	private int width ;
	private int height ;
	private int framerate ;
	private final int YZRAVCRATECONTROLTYPE1 = 1; //hope to target bitrate
	private final int YZRAVCRATECONTROLTYPE2 = 2; //const quantization parameter
	private int ratecontrolType ;
	private int bitrate ;
	private int cpbsizeFactor ;
	private int quantizationPar ;
	private int iFramesIntervalSec;
	private int yuvFormat ;
	
	private int mNativeYzrAvcEnc ;

	private native boolean NativeOpenEnc() ;
	private native int NativeYzrAvcEncClose(int NativeYzrAvcEnc ) ;
	private native int NativeYzrAvcEncGetMaxBufferSize(int NativeYzrAvcEnc, int[] size ) ;
	private native int NativeYzrAvcEncGetSps(int NativeYzrAvcEnc , byte[] nalBuffer , int[] nalBufferLength ) ;
	private native int NativeYzrAvcEncGetPps(int NativeYzrAvcEnc , byte[] nalBuffer , int[] nalBufferLength  ) ;
	private native int NativeYzrAvcEncIframeRequest(int NativeYzrAvcEnc) ;
	private native int NativeYzrAvcEncEncodeOneFrame(int NativeYzrAvcEnc, byte[]  yuv , byte[] nalBuffer,  int[] nalBufferLength,  int[] nalType  ) ;
	

	private YzrAvcEnc(int width, int height , int framerate,int  iFramesIntervalSec, int yuvFormat ) 
	{
		this.width = width ;
		this.height = height ;
		this.framerate = framerate ;
		this.iFramesIntervalSec = iFramesIntervalSec ;
		this.yuvFormat = yuvFormat ;	
	}
	
	
	public YzrAvcEnc(int width, int height , int framerate ,int bitrate , int cpbsizeFactor, int  iFramesIntervalSec, int yuvFormat )
	{
		this( width, height ,  framerate,  iFramesIntervalSec, yuvFormat ) ;
		this.ratecontrolType = YZRAVCRATECONTROLTYPE1 ;
		this.bitrate = bitrate ;
		this.cpbsizeFactor = cpbsizeFactor ;
		NativeOpenEnc();
	}
	public YzrAvcEnc(int width, int height , int framerate ,int quantizationPar, int  iFramesIntervalSec, int yuvFormat ) 
	{	
		this( width, height ,  framerate,  iFramesIntervalSec, yuvFormat ) ;
		this.ratecontrolType = YZRAVCRATECONTROLTYPE2 ;
		this.quantizationPar = quantizationPar ;
		NativeOpenEnc();
	}
	public void YzrAvcEncClose()
	{
		NativeYzrAvcEncClose(mNativeYzrAvcEnc);
	}

	public int YzrAvcEncGetMaxBufferSize() 
	{	
		int[] size={0} ;
		int r = NativeYzrAvcEncGetMaxBufferSize( mNativeYzrAvcEnc, size );
		/// should throw exception if r<0 
		return size[0]  ;
	}
	public int YzrAvcEncGetSps( byte[] nalBuffer, int[] nalBufferLength  ) 
	{
		int r ;		
		r = NativeYzrAvcEncGetSps( mNativeYzrAvcEnc, nalBuffer ,  nalBufferLength  );
		return r ;
	}
	public int YzrAvcEncGetPps( byte[] nalBuffer , int[] nalBufferLength )
	{	
		int r;
		r = NativeYzrAvcEncGetPps( mNativeYzrAvcEnc,  nalBuffer ,  nalBufferLength ) ;
		return r ;	
	}
	public int YzrAvcEncIframeRequest() 
	{
		int r;
		r = NativeYzrAvcEncIframeRequest(mNativeYzrAvcEnc) ;
		return r ;	
	}
	public int YzrAvcEncEncodeOneFrame( byte[]  yuv , byte[] nalBuffer   , int[] nalBufferLength , int[] nalType ) 
	{
		int r;
		Log.v("YzrCodec", "YzrAvcEncEncodeOneFrame in" ) ;
		r = NativeYzrAvcEncEncodeOneFrame( mNativeYzrAvcEnc,  yuv ,  nalBuffer  ,   nalBufferLength , nalType  ) ;
		Log.v("YzrCodec", "YzrAvcEncEncodeOneFrame out " + r ) ;
		return r ;
	}	

}
