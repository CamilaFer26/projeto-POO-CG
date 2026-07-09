package controller;

import model.Matriz;

public interface TransformListener {
	// é chamado sempre que a matriz de transformação atual muda.
	void onMatrizAtualizada(Matriz matriz);
 
	// Chamado sempre que a matriz acumulada muda.
	void onAcumuladaAtualizada(Matriz acumulada);
}
