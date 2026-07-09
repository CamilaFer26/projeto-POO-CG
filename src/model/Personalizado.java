package model;

public class Personalizado implements Transformacao{

	@Override
	public String getNome() {
		return "Personalizado";
	}

	@Override
	public String getDescricao() {
		return "Edite livremente os valores da matriz na tabela ao lado e "
				+ "observe, em tempo real, como cada coeficiente afeta a "
				+ "figura e o determinante.";
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

	@Override
	public boolean isPersonalizado() {
		return true;
	}
}
