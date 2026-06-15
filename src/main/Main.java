package main;

import javax.swing.SwingUtilities;

public class Main {

	public static void main(String[] args) {
		MotorGrafico motor = new MotorGrafico();
		
		Thread glThread = new Thread(() ->{
			motor.init();
		});
		glThread.start();
		
		SwingUtilities.invokeLater(() -> {
			TransformUI ui = new TransformUI(motor);
			ui.setVisible(true);
		});
	}
}
