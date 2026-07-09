package model;

// Interface utilizada pelas trasnformações lineares clássicas
public interface Transformacao {
	String getNome();
	
	String getDescricao();
	
	int getNumeroSliders();
	
	Matriz gerar(double...parametros);
	
	double getSliderMin();
	
	double getSliderMax();
	
	double getSliderInicio();
	
	default boolean isPersonalizado() {
		return false;
	}
}
