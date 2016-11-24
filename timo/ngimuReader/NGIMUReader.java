package timo.ngimuReader;
import java.util.ArrayList;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.nio.ByteBuffer;

public abstract class NGIMUReader{
	abstract double[][] getSensors();
	abstract double[][] getQuaternions();
	
	//Helper method to convert ArrayList<ArrayList<Double>> into double[][]
	protected double[][] getArray(ArrayList<ArrayList<Double>> a){
		double[][] returnArray = new double[a.size()][a.get(0).size()];
		for (int i = 0; i<a.size();++i){
			for (int j= 0; j<a.get(i).size(); ++j){
				returnArray[i][j] = a.get(i).get(j);
			}
		}
		return returnArray;
	}
	
	public void writeFile(String fileIn,ArrayList<ArrayList<Double>> a){
		try{
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(fileIn));
			for (int j= 0; j<a.get(0).size(); ++j){
				for (int i = 0; i<a.size();++i){
					
						os.write(ByteBuffer.allocate(Double.SIZE/Byte.SIZE).putDouble(a.get(i).get(j)).array());
					
				}
			}
			os.flush();
			os.close();
		}catch (Exception err){
			System.out.println("Could not write file "+fileIn);
		}
	}
	
}