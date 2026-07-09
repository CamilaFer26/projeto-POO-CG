package model;

public class ReflexaoX implements Transformacao {

	@Override
	public String getNome() {
		return "Reflexão em X";
	}

	@Override
	public String getDescricao() {
		return "Espelha a figura em relação ao eixo X: cada ponto (x, y) "
				+ "passa a ser (x, -y).";
	}

	@Override
	public int getNumeroSliders() {
		return 0;
	}

	@Override
	public Matriz gerar(double... parametros) {
		return new Matriz(1, 0, 0, -1);
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
