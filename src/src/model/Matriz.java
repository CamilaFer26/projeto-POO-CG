package model;

public class Matriz {
	private double[][] valores;
	
	public Matriz(double c11, double c12, double c21, double c22) {
		double[][] matriz = {
				{c11, c12},
				{c21, c22}
		};
		
		this.valores = matriz;
	}
	
	public void setValor(double val, int linha, int coluna) {
		this.valores[linha][coluna] = val;
	}
	
	public void setValores(double[][] valores) {
		this.valores = valores;
	}
	
	public void setValores(double c11, double c12, double c21, double c22) {
		double[][] matriz = {
				{c11, c12},
				{c21, c22}
		};
		
		this.valores = matriz;
	}
	
	public double getValor(int linha, int coluna) {
		return this.valores[linha][coluna];
	}
	
	public double[][] getMatriz(){
		return this.valores;
	}
	
    public Object[][] getObjeto() {
        Object[][] dados = new Object[2][2];

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                dados[i][j] = valores[i][j];
            }
        }

        return dados;
    }
    
    public double determinante() {
        return valores[0][0] * valores[1][1]
             - valores[0][1] * valores[1][0];
    }
    
    public String descricaoDet() {
    	double det = determinante();
    	
        if (det == 0) {
            return """
                A transformação comprime o plano em uma reta ou ponto.
                A matriz não é invertível.
                """;
        }

        if (det > 0) {
            return """
                A orientação do sistema é preservada.
                A matriz é invertível.
                """;
        }

        return """
            Há inversão de orientação (reflexão).
            A matriz é invertível.
            """;
    }
    
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
    
}
