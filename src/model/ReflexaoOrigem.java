package model;

public class ReflexaoOrigem implements Transformacao {

	@Override
	public String getNome() {
		return "Reflexão na origem";
	}

	@Override
	public String getDescricao() {
		return "Gira a figura 180° em torno da origem: cada ponto (x, y) "
				+ "passa a ser (-x, -y). Equivale a aplicar as duas reflexões "
				+ "de eixo ao mesmo tempo — por isso o determinante é positivo.";
	}

	@Override
	public int getNumeroSliders() {
		return 0;
	}

	@Override
	public Matriz gerar(double... parametros) {
		return new Matriz(-1, 0, 0, -1);
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
