package model;

public class CisalhamentoVertical implements Transformacao {

	@Override
	public String getNome() {
		return "Cisalhamento Vertical";
	}

	@Override
	public String getDescricao() {
		return "Desloca cada ponto na vertical, proporcionalmente à sua "
				+ "posição horizontal (x). Assim como no cisalhamento "
				+ "horizontal, a área é preservada — só os ângulos internos "
				+ "da figura mudam.";
	}

	@Override
	public int getNumeroSliders() {
		return 1;
	}

	@Override
	public Matriz gerar(double... parametros) {
		double s = parametros[0];
		return new Matriz(1, 0, s, 1);
	}

	@Override
	public double getSliderMin() {
		return -5;
	}

	@Override
	public double getSliderMax() {
		return 5;
	}

	@Override
	public double getSliderInicio() {
		return 0;
	}

}
