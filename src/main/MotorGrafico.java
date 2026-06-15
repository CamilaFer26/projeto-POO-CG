package main;

public class MotorGrafico {
	static {
		System.loadLibrary("motor");
	}
	
	public native void init();
	
	public native void update(float tx, float ty, float angle);
	
	public native void cleanup();
}
