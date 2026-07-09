package model;

public class Escala implements Transformacao{

	@Override
	public String getNome() {
		return "Escala";
	}

	@Override
	public String getDescricao() {
		return "Multiplica as coordenadas X e Y por fatores independentes. "
				+ "Fatores maiores que 1 esticam a figura, entre 0 e 1 "
				+ "encolhem, e fatores negativos espelham o eixo "
				+ "correspondente enquanto escalam.";
	}

	@Override
	public int getNumeroSliders() {
		return 2;
	}

	@Override
	public Matriz gerar(double... parametros) {
		double sy = parametros[0];
		double sx = parametros[1];
		
		return new Matriz(sx, 0, 0, sy);
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
		return 1;
	}
}
