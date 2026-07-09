package model;

public class CisalhamentoHorizontal implements Transformacao {

	@Override
	public String getNome() {
		return "Cisalhamento Horizontal";
	}

	@Override
	public String getDescricao() {
		return "Desloca cada ponto na horizontal, proporcionalmente à sua "
				+ "altura (y). Um quadrado vira um paralelogramo \"inclinado\". "
				+ "A área da figura não muda, só o formato — o determinante "
				+ "continua 1.";
	}

	@Override
	public int getNumeroSliders() {
		return 1;
	}

	@Override
	public Matriz gerar(double... parametros) {
		double s = parametros[0];
		return new Matriz(1, s, 0, 1);
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
