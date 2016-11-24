package timo.ngimuReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

//Custom classes
import timo.ngimuReader.utils.Slip;
import timo.ngimuReader.utils.SlipIndices;
import timo.ngimuReader.utils.OSC;
import timo.ngimuReader.utils.OSCObject;
import timo.ngimuReader.utils.OSCBundle;
import timo.ngimuReader.utils.OSCControl;

public class NGIMUMEMORYReader extends NGIMUReader{
	ArrayList<ArrayList<Double>> sensorData = null;
	ArrayList<ArrayList<Double>> quaternionData = null;
	byte[] fileData = null;
	double sensorRate;
	double quaternionRate;
	int maxLength = 1;
	
	public NGIMUMEMORYReader(String fileIn){
		File tempFile = new File(fileIn);
		int fileLength = (int) tempFile.length();
		Boolean goOn = true;
		try{
			BufferedInputStream is = new BufferedInputStream(new FileInputStream(fileIn));
			fileData	 = new byte[fileLength];
			int bytesRead = is.read(fileData,0,fileData.length); //Read all of the data into memory
			is.close();
			maxLength = fileLength/(8+4*9+8+4*4);
			//Reserve ArrayLists for sensor and quaternion data, set the capacity to maxLength to improve efficiency
			sensorData = new ArrayList<ArrayList<Double>>(11);	//TimeStampseconds,fractional, gx,gy,gz,ax,ay,az,mx,my,mz
			for (int i = 0;i<11;++i){
				sensorData.add(new ArrayList<Double>(maxLength));
			}
			quaternionData = new ArrayList<ArrayList<Double>>(6); //TimeStamp seconds,fractional, w,x,y,z
			for (int i = 0;i<6;++i){
				quaternionData.add(new ArrayList<Double>(maxLength));
			}
			
			
			
			//System.out.println("Read "+bytesRead+ " bytes of "+fileData.length+" requested");
		}catch (Exception e){
			goOn = false;
			System.out.println("File read failed "+fileIn);
		}
		if (goOn){
			decodeData();
		}
	}
	
	//Break data into Slip packages (one OSC frame each). Feed frames into frame decoding, assign frames to correct places
	private void decodeData(){
		//Get slip inits and ends
		ArrayList<SlipIndices> slipPackageIndices = Slip.getSlips(fileData);
		//Go through slip packages = OSC frames
		//for (int i = 0; i < 2000;++i){
		for (int i = 0; i < slipPackageIndices.size();++i){
			
			byte[] oscFrame = Slip.getSlipArray(Arrays.copyOfRange(fileData,slipPackageIndices.get(i).init,slipPackageIndices.get(i).end+1));
			//Decode OSC packages here
			OSCObject oObj = OSC.decode(oscFrame);
			//Handle controls
			if (oObj instanceof timo.ngimuReader.utils.OSCControl){
				OSCControl oCon = (OSCControl) oObj;
				handleControl(oCon);
			}
			//Handle bundles
			if (oObj instanceof timo.ngimuReader.utils.OSCBundle){
				OSCBundle oBun = (OSCBundle) oObj;
				for (int b = 0; b<oBun.controls.size();++b){
					handleControl(oBun.controls.get(b),oBun.seconds,oBun.rational);
				}
				
				//System.out.println();
			}
		}
		//System.out.println("Done decoding sRate "+sensorRate+" qRate "+quaternionRate);
		//System.out.println("Sensor data "+sensorData.get(0).size()+" quat "+quaternionData.get(0).size());
	}
	
	//Interested in /rate/sensors /rate/quaternion
	private void handleControl(OSCControl in){
		switch (in.address){
			case "/rate/quaternion":
				quaternionRate = in.doubles.get(0);
				break;
			case "/rate/sensors":
				sensorRate = in.doubles.get(0);
				break;
			default:
				break;
		}
	}
	
	//Only interested in /sensor and /quaternion in bundles...
	private void handleControl(OSCControl in, double seconds, double rational){
		switch (in.address){
			case "/quaternion":
				quaternionData.get(0).add(seconds);
				quaternionData.get(1).add(rational);
				for (int i = 2;i<quaternionData.size();++i){
						quaternionData.get(i).add(in.doubles.get(i-2));
				}
				break;
			case "/sensors":
				sensorData.get(0).add(seconds);
				sensorData.get(1).add(rational);
				for (int i = 2;i<sensorData.size();++i){
						sensorData.get(i).add(in.doubles.get(i-2));
				}
				break;
			default:
				break;
		}
	}
		
	@Override
	public double[][] getSensors(){
		return getArray(sensorData);
	}
	
	@Override
	public double[][] getQuaternions(){
		return getArray(quaternionData);
	}
	
	public void printFile(String fileIn){
		String sensorsOut = fileIn.substring(0,fileIn.length()-4)+"_sensors_"+String.format("%.0f",sensorRate)+"_Hz.bin";
		String quatOut = fileIn.substring(0,fileIn.length()-4)+"_quats_"+String.format("%.0f",quaternionRate)+"_Hz.bin";
		writeFile(sensorsOut,sensorData);
		writeFile(quatOut,quaternionData);
	}

	//To be used from the command line
	public static void main(String[] arg){
		String fIn = arg[0];
		NGIMUMEMORYReader mr = new NGIMUMEMORYReader(fIn);
		mr.printFile(fIn);
	}
	
}