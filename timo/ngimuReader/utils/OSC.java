package timo.ngimuReader.utils;
import java.util.Arrays;
import java.util.ArrayList;


public class OSC{
	public static final double fractionalSecond = 1d/(Math.pow(2d,32d)-1d);
	public static OSCObject decode(byte[] frame){
		switch (frame[0]){
			case (byte) 47:
				return decodeControl(frame);
			case (byte) 35:
				return decodeBundle(frame);			
			default:
				return null;
		}
	}
	
	private static OSCControl decodeControl(byte[] frame){
		OSCString address = new OSCString(frame);
		OSCString fmt = new OSCString(address.remain);
		OSCControl returnVal = new OSCControl(address.string,fmt.string);
		//System.out.println(address.string+"_"+fmt.string+"_");
		
		byte[] remData = fmt.remain;
		for (int i = 1;i<fmt.string.length();++i){
			switch (fmt.string.charAt(i)){
				case 'F':
					//No data associated with this
					returnVal.bools.add(false);
					break;
				case 'T':
					//No data associated with this
					returnVal.bools.add(true);
					break;
				case 'f':
					
					float fl = Float.intBitsToFloat(
						((int) (remData[0] & 0xff)) << 24	|
						((int) (remData[1] & 0xff)) << 16	| 
						((int) (remData[2] & 0xff)) << 8	| 
						((int) (remData[3] & 0xff))
						);
					returnVal.doubles.add((double) fl);
					remData = Arrays.copyOfRange(remData,4,remData.length);
					
					break;
				case 'i':
					int inte = (int) (
						((int) (remData[0] & 0xff)) << 24	|
						((int) (remData[1] & 0xff)) << 16	| 
						((int) (remData[2] & 0xff)) << 8	| 
						((int) (remData[3] & 0xff))
						);
					
					returnVal.ints.add(inte);
					remData = Arrays.copyOfRange(remData,4,remData.length);
					break;
				case 's':
					OSCString temp = new OSCString(remData);
					returnVal.strings.add(temp.string);
					//System.out.println("decodeControl i "+i+" got s "+temp.string);
					remData = temp.remain;
					break;
				default:
					break;
			}
			
		}
		
		//System.out.println("Control address "+address.string);
		return returnVal;
	}
	
	private static OSCBundle decodeBundle(byte[] frame){
		OSCString bundle = new OSCString(frame);
		byte[] remData = bundle.remain;
		long seconds = (long) (
			((long) (remData[0] & 0xff)) << 24	|
			((long) (remData[1] & 0xff)) << 16	| 
			((long) (remData[2] & 0xff)) << 8	| 
			((long) (remData[3] & 0xff))
			);
		
		remData = Arrays.copyOfRange(remData,4,remData.length);
		long fractional = (long) (
			((long) (remData[0] & 0xff)) << 24	|
			((long) (remData[1] & 0xff)) << 16	| 
			((long) (remData[2] & 0xff)) << 8	| 
			((long) (remData[3] & 0xff))
			);

		remData = Arrays.copyOfRange(remData,4,remData.length);
		double tStamp = ((double) seconds)+((double) fractional)*fractionalSecond;
		//System.out.println("Bundle "+bundle.string+" tStamp "+tStamp+" seconds "+seconds);
		OSCBundle returnVal = new OSCBundle(tStamp,seconds,fractional);
		//Get the control blobs
		while (true){
			int blobLength = (int) (
						((int) (remData[0] & 0xff)) << 24	|
						((int) (remData[1] & 0xff)) << 16	| 
						((int) (remData[2] & 0xff)) << 8	| 
						((int) (remData[3] & 0xff))
						);
			remData = Arrays.copyOfRange(remData,4,remData.length);
			returnVal.addControl(decodeControl(Arrays.copyOfRange(remData,0,blobLength)));
			if (blobLength < remData.length){
				remData = Arrays.copyOfRange(remData,blobLength,remData.length);
			}else{
				break;
			}
		}
		return returnVal;
	}

	private static class OSCString{
		byte[] remain;
		String string;
		public OSCString(byte[] in){
			ArrayList<FindIndices> nullIndices = Slip.find(in,(byte) 0);
			string = new String(Arrays.copyOfRange(in,0,nullIndices.get(0).init));
			int startInd = 0;
			int remainder =(nullIndices.get(0).init+1) % 4;
			if (remainder == 0){
				startInd = 	nullIndices.get(0).init+1;
			}else{
				startInd = 	nullIndices.get(0).init+4-remainder+1;
			}
			remain = Arrays.copyOfRange(in,startInd,in.length);
		}
	}
}