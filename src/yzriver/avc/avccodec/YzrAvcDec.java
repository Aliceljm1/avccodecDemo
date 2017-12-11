package yzriver.avc.avccodec; 

import android.util.Log;

public class YzrAvcDec {

	private int              yzrAvcDecHandle ;
	private String          dumpYuvFileName ;
	private int              dumpFp = 0 ;
	private int              isDumpYuvFile = 0 ;

		 
	private native int nativeYzrAvcDecAnnexBGetNALUnit(byte[] stream , int[] offsetOfNalArray, int[] streamSizeArray  ) ;
	private native int nativeYzrAvcDecOpen() ;
	private native int nativeYzrAvcDecDecodeOneFrame( int yzrAvcDecHandle , NativeYzrAvcDecYUV420  nativeYzrAvcDecYUV420 ,  byte[] stream, int offset ,int count);
	private native int nativeYzrAvcDecClose(int yzrAvcDecHandle) ;
	private native int nativeDumpYuvToFile ( int fp , int yPointer , int uPointer, int vPointer , int width , int height) ;

	public YzrAvcDec() {
		isDumpYuvFile = 0 ;
		yzrAvcDecHandle = nativeYzrAvcDecOpen() ;   
	}

	public YzrAvcDec(String dumpYuvFileName ) { 
		isDumpYuvFile = 1 ;
		this.dumpYuvFileName =  dumpYuvFileName ;
		yzrAvcDecHandle = nativeYzrAvcDecOpen() ; 
	}

	public boolean YzrAvcDecAnnexBGetNALUnit(byte[] stream , int[] offsetOfNalArray, int[] streamSizeArray  )  {
		int ret ;
		ret = nativeYzrAvcDecAnnexBGetNALUnit(  stream ,  offsetOfNalArray,  streamSizeArray  ) ;
		if( ret == 1 )
			return true ;
		else
			return false ;
	}		
	
	public boolean decodeOneFrame(NativeYzrAvcDecYUV420 nativeYzrAvcDecYUV420 ,byte[] stream, int offset ,int count){
		
		Log.v("YzrCodec", "decodeOneFrame in" ) ;
		nativeYzrAvcDecYUV420.yPointer = 0 ;
		nativeYzrAvcDecYUV420.uPointer = 0 ;
		nativeYzrAvcDecYUV420.vPointer = 0 ;  
		nativeYzrAvcDecYUV420.width = 0 ;
		nativeYzrAvcDecYUV420.height = 0 ;
		if( yzrAvcDecHandle != 0 )			
			nativeYzrAvcDecDecodeOneFrame( yzrAvcDecHandle , nativeYzrAvcDecYUV420 ,  stream, offset , count);
		Log.v("YzrCodec", "decodeOneFrame out " + nativeYzrAvcDecYUV420.yPointer ) ;
		if( nativeYzrAvcDecYUV420.yPointer !=0 )
		{
			if( (isDumpYuvFile == 1) &&  (dumpFp!=0)  )
				nativeDumpYuvToFile ( dumpFp  , nativeYzrAvcDecYUV420.yPointer , 
				nativeYzrAvcDecYUV420.uPointer ,
				nativeYzrAvcDecYUV420.vPointer ,
				nativeYzrAvcDecYUV420.width  ,  
				nativeYzrAvcDecYUV420.height) ;
			return true ;  
		}   
		return false ;
	}

	public void YzrAvcDecClose() {
		Log.v("avccodecDemo" , "Before  nativeYzrAvcDecClose "+ dumpFp ) ;
		nativeYzrAvcDecClose( yzrAvcDecHandle ) ;
		Log.v("avccodecDemo" , "After  nativeYzrAvcDecClose " ) ;
	}

}
