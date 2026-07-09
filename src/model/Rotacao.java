package model;

public class Rotacao implements Transformacao{

	@Override
	public String getNome() {
		return "Rotação";
	}

	@Override
	public String getDescricao() {
		return "Gira todos os pontos do plano em torno da origem pelo ângulo "
				+ "escolhido, no sentido anti-horário. Comprimentos e ângulos "
				+ "entre vetores são preservados — a figura muda de direção, "
				+ "nunca de forma ou tamanho.";
	}

	@Override
	public int getNumeroSliders() {
		return 1;
	}

	@Override
	public Matriz gerar(double... parametros) {
		double graus = parametros[0];
		double rad = Math.toRadians(graus);
		return new Matriz(
				Math.cos(rad), -Math.sin(rad),
				Math.sin(rad), Math.cos(rad));
	}

	@Override
	public double getSliderMin() {
		return 0;
	}

	@Override
	public double getSliderMax() {
		return 360;
	}

	@Override
	public double getSliderInicio() {
		return 0;
	}

}
