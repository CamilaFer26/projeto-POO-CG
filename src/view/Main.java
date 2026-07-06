package view;

import javax.swing.SwingUtilities;

import graphics.MotorGrafico;

public class Main {

	public static void main(String[] args) {
		MotorGrafico motor = new MotorGrafico();
		
		SwingUtilities.invokeLater(() -> {
			TransformUI ui = new TransformUI(motor);
			ui.setVisible(true);
			
			Thread glThread = new Thread(() ->{
				motor.init(ui.getCanvas());
			});
			
			glThread.start();			
		});
	}
}