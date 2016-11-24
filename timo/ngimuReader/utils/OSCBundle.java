package timo.ngimuReader.utils;
import java.util.ArrayList;

public class OSCBundle extends OSCObject{
	public double tStamp;
	public double seconds;
	public double rational;
	public ArrayList<OSCControl> controls;
	public OSCBundle(double tStamp,double seconds, double rational){
		this.tStamp = tStamp;
		this.seconds = seconds;
		this.rational = rational;
		controls = new ArrayList<OSCControl>();
	}
	public void addControl(OSCControl a){
		controls.add(a);
	}
}