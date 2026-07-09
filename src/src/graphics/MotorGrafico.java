package graphics;

import java.awt.Canvas;

public class MotorGrafico {
	static {
		System.loadLibrary("motor");
	}
	
	public native void init(Canvas canvas);
	
	public native void update(float a, float b, float c, float d); // matriz de transformação 2x2
	
	public native void cleanup();
	
	public native void shape(int shape);
	/* 0 -> quadrado
	 * 1 -> triângulo
	 * 2 -> retângulo
	*/
}