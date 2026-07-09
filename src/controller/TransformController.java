package controller;

import graphics.MotorGrafico;
import model.Matriz;
import model.Transformacao;

// concentra a lógica de interação com usuário, controle dos estados
// das matrizes, e comunicação com o motor gráfico
public class TransformController {
	private final MotorGrafico motor;
	private Matriz matrizAtual;
	private Matriz acumulada;
	private TransformListener listener;
 
	public TransformController(MotorGrafico motor) {
		this.motor = motor;
		this.matrizAtual = Matriz.identidade();
		this.acumulada = Matriz.identidade();
	}
 
	public void setListener(TransformListener listener) {
		this.listener = listener;
	}
 
	public Matriz getMatrizAtual() {
		return matrizAtual;
	}
 
	public Matriz getAcumulada() {
		return acumulada;
	}
 
	// Aplica transformação pré definida
	public void aplicarTransformacao(Transformacao transformacao, double... parametros) {
		matrizAtual = transformacao.gerar(parametros);
		sincronizarMotor();
		notificarMatriz();
	}
 
	// Matriz personalizável
	public void editarValorMatriz(int linha, int coluna, double valor) {
		if (matrizAtual.getValor(linha, coluna) == valor) {
			return;
		}
		matrizAtual.setValor(valor, linha, coluna);
		sincronizarMotor();
		notificarMatriz();
	}
 
	public void resetarMatrizAtual() {
		matrizAtual = Matriz.identidade();
		sincronizarMotor();
		notificarMatriz();
	}
 
	// Compõe a matriz atual com a acumulada
	public void aplicarNaAcumulada() {
		acumulada = matrizAtual.multiplicar(acumulada);
		float[][] m = acumulada.toFloat();
		motor.update(m[0][0], m[0][1], m[1][0], m[1][1]);
		notificarAcumulada();
	}
 
	public void resetarAcumulada() {
		acumulada = Matriz.identidade();
		notificarAcumulada();
	}
 
	public void selecionarForma(int indice) {
		motor.shape(indice);
	}
 
	private void sincronizarMotor() {
		float[][] m = matrizAtual.toFloat();
		motor.update(m[0][0], m[0][1], m[1][0], m[1][1]);
	}
 
	private void notificarMatriz() {
		if (listener != null) {
			listener.onMatrizAtualizada(matrizAtual);
		}
	}
 
	private void notificarAcumulada() {
		if (listener != null) {
			listener.onAcumuladaAtualizada(acumulada);
		}
	}
}
