package model;
// Representa a matriz 2x2 utilizada nas trasnformações lineares
public class Matriz {
	private double[][] valores;
	
	public Matriz(double c11, double c12, double c21, double c22) {
		double[][] matriz = {
				{c11, c12},
				{c21, c22}
		};
		
		this.valores = matriz;
	}
	
	public Matriz(double[][] valores) {
		if (valores == null || valores.length != 2 || valores[0].length != 2 || valores[1].length != 2) {
			throw new IllegalArgumentException("Matriz deve ser 2x2");
		}

		this.valores = new double[][] {
				{valores[0][0], valores[0][1]},
				{valores[1][0], valores[1][1]}
		};
	}
	
	public static Matriz identidade() {
		return new Matriz(1, 0, 0, 1);
	}
	
	// set de um único valor da matriz
	public void setValor(double val, int linha, int coluna) {
		this.valores[linha][coluna] = val;
	}
	
	// set de todos os valores da matriz
	public void setValores(double[][] valores) {
		if (valores == null || valores.length != 2 || valores[0].length != 2 || valores[1].length != 2) {
			throw new IllegalArgumentException("Matriz deve ser 2x2");
		}
		
		this.valores = valores;
	}
	
	public void setValores(double c11, double c12, double c21, double c22) {
		double[][] matriz = {
				{c11, c12},
				{c21, c22}
		};
		
		this.valores = matriz;
	}
	
	// get de um único valor da matriz
	public double getValor(int linha, int coluna) {
		return this.valores[linha][coluna];
	}
	
	// get de todos os valores da matriz
	public double[][] getValores() {
		return new double[][] {
				{valores[0][0], valores[0][1]},
				{valores[1][0], valores[1][1]}
		};
	}
	
	// retorna a matriz como objeto para tabelas
    public Object[][] getObjeto() {
        Object[][] dados = new Object[2][2];

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                dados[i][j] = valores[i][j];
            }
        }

        return dados;
    }
    
    // retorna o determinante da matriz
    public double determinante() {
        return valores[0][0] * valores[1][1]
             - valores[0][1] * valores[1][0];
    }
    
    // retorna o vetor base i da matriz (0, 1)
    public double[] getImagemVetorI() {
		return new double[] {valores[0][0], valores[1][0]};
	}
    
    // retorna o vetor base j da matriz (1, 0)
	public double[] getImagemVetorJ() {
		return new double[] {valores[0][1], valores[1][1]};
	}
	
	// retorna a matriz como float
    public float[][] toFloat() {
    	double[][] matriz = this.valores;
        int linhas = matriz.length;
        int colunas = matriz[0].length;

        float[][] resultado = new float[linhas][colunas];

        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                resultado[i][j] = (float) matriz[i][j];
            }
        }

        return resultado;
    }
    
    // multiplicação de matrizes para composição de trasnformações
    public Matriz multiplicar(Matriz outra) {
        double a11 = getValor(0,0);
        double a12 = getValor(0,1);
        double a21 = getValor(1,0);
        double a22 = getValor(1,1);

        double b11 = outra.getValor(0,0);
        double b12 = outra.getValor(0,1);
        double b21 = outra.getValor(1,0);
        double b22 = outra.getValor(1,1);

        return new Matriz(
            a11*b11 + a12*b21,
            a11*b12 + a12*b22,
            a21*b11 + a22*b21,
            a21*b12 + a22*b22
        );
    }
}
