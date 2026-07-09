package view;

import javax.swing.SwingUtilities;

import graphics.MotorGrafico;

public class Main {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			TelaInicial tela = new TelaInicial(Main::iniciarVisualizador);
			tela.setVisible(true);
		});
	}

	// abre o visualizador principal (chamado quando o usuário clica em "Iniciar" na tela de entrada)
	private static void iniciarVisualizador() {
		MotorGrafico motor = new MotorGrafico();

		SwingUtilities.invokeLater(() -> {
			TransformUI ui = new TransformUI(motor);
			ui.setVisible(true);

			Thread glThread = new Thread(() -> {
				motor.init(ui.getCanvas());
			});

			glThread.start();
		});
	}
}