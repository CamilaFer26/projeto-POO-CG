package model;

public class Identidade implements Transformacao{

	@Override
	public String getNome() {
		return "Identidade";
	}

	@Override
	public String getDescricao() {
		return "A matriz identidade não altera nada: cada ponto do plano "
				+ "permanece exatamente onde estava. É a transformação neutra, "
				+ "assim como multiplicar um número por 1.";
	}

	@Override
	public int getNumeroSliders() {
		return 0;
	}

	@Override
	public Matriz gerar(double... parametros) {
		return Matriz.identidade();
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
