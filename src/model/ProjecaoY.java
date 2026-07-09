package model;

public class ProjecaoY implements Transformacao {

	@Override
	public String getNome() {
		return "Projeção em Y";
	}

	@Override
	public String getDescricao() {
		return "\"Achata\" a figura sobre o eixo Y: todo ponto (x, y) vira "
				+ "(0, y), perdendo a informação horizontal. O determinante é "
				+ "zero — assim como na projeção em X, a transformação não é "
				+ "invertível.";
	}

	@Override
	public int getNumeroSliders() {
		return 0;
	}

	@Override
	public Matriz gerar(double... parametros) {
		return new Matriz(0, 0, 0, 1);
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
