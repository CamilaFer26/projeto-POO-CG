package graphics;

import java.awt.Canvas;

public class MotorGrafico {
	static {
		System.loadLibrary("motor");
	}
	public native void init(Canvas canvas);
	
	public native void update(float a, float b, float c, float d);
	
	public native void cleanup();
}