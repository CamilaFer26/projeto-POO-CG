package model;

public class ProjecaoX implements Transformacao {

	@Override
	public String getNome() {
		return "Projeção em X";
	}

	@Override
	public String getDescricao() {
		return "\"Achata\" a figura sobre o eixo X: todo ponto (x, y) vira "
				+ "(x, 0), perdendo a informação de altura. O determinante é "
				+ "zero — a transformação não é invertível, pois é impossível "
				+ "voltar a saber qual era o y original.";
	}

	@Override
	public int getNumeroSliders() {
		return 0;
	}

	@Override
	public Matriz gerar(double... parametros) {
		return new Matriz(1, 0, 0, 0);
	}

	@Override
	public double getSliderMin() {
		return 0;
	}

	@Override
	public double getSliderMax() {
		return 0;
	}

	@Override
	public double getSliderInicio() {
		return 0;
	}

}
