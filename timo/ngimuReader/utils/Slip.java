package timo.ngimuReader.utils;
import java.util.ArrayList;

public class Slip{
	public static final byte slipControlValue1 = (byte) 0xC0;
	public static final byte slipControlValue2 = (byte) 0xDB;
	public static final byte slipControlValue3 = (byte) 0xDC;
	public static final byte slipControlValue4 = (byte) 0xDD;
	
	//Get the inits and ends of slip packages
	public static ArrayList<SlipIndices> getSlips(byte[] fileData){
		ArrayList<SlipIndices> slips = new ArrayList<SlipIndices>(1000000);
		ArrayList<FindIndices> temp = find(fileData,slipControlValue1);
		int init = 0;
		int end = 0;
		for (int i =0;i<temp.size();++i){
			end = temp.get(i).init-1;
			slips.add(new SlipIndices(init,end));
			init = end+2;
		}
		System.out.println("Found "+slips.size()+" slip packages");
		return slips;
	}
	
	public static ArrayList<FindIndices> find(byte[] dataIn, byte lookFor){
		ArrayList<FindIndices> matches = new ArrayList<FindIndices>(1000000);
		for (int i =0;i<dataIn.length;++i){
			if (dataIn[i] == lookFor){
				matches.add(new FindIndices(i));
			}
		}
		return matches;
	}
	
	//Decode the slip package
	public static byte[] getSlipArray(byte[] slipIn){
		ArrayList<Byte> temp = new ArrayList<Byte>();
		ArrayList<FindIndices> matches = find(slipIn,slipControlValue2);
		if (matches.size() > 0){
			//Replace slip replacements
			int d = 0;
			while (d<slipIn.length){
				if (slipIn[d] == slipControlValue2){
					switch (slipIn[d+1]){
						case slipControlValue3:
							temp.add(slipControlValue1);
							break;
						case slipControlValue4:
							temp.add(slipControlValue2);
							break;
					}
					++d;
				}else{
					temp.add(slipIn[d]);
				}
				++d;				
			}
			//Return as byte array
			return getArray(temp);			
		}else{
			return slipIn;
		}
		
	}
	
	public static byte[] getArray(ArrayList<Byte> a){
		byte[] returnArray = new byte[a.size()];
		for (int j= 0; j<a.size(); ++j){
			returnArray[j] = a.get(j);
		}
		return returnArray;
	}

}