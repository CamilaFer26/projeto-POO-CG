package main;

import java.awt.Canvas;

public class MotorGrafico {
	static {
		System.loadLibrary("motor");
	}
	public native void init(Canvas canvas);
	
	public native void update(float tx, float ty, float angle);
	
	public native void cleanup();
}
