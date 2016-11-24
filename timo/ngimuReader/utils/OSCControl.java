package timo.ngimuReader.utils;
import java.util.ArrayList;

public class OSCControl extends OSCObject{
	public ArrayList<String> strings = null;
	public ArrayList<Double> doubles = null;
	public ArrayList<Integer> ints = null;
	public ArrayList<Boolean> bools = null;
	public String address;
	public String fmt;
	public OSCControl(String address, String fmt){
		this.address = address;
		this.fmt = fmt;
		if (fmt.indexOf("f") != -1 ){
			doubles = new ArrayList<Double>();
		}
		if (fmt.indexOf("F") != -1 || fmt.indexOf("T") != -1){
			bools = new ArrayList<Boolean>();
		}
		if (fmt.indexOf("i") != -1 ){
			ints = new ArrayList<Integer>();
		}
		if (fmt.indexOf("s") != -1 ){
			strings = new ArrayList<String>();
		}
		
	}
}