package view;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import model.Matriz;

public class MatrizPanel extends JScrollPane{
	private static final long serialVersionUID = 1L;
	private final JTable table;
	private EditorListener listener;
	private boolean atualizando = false;
 
	public interface EditorListener {
		void onValorEditado(int linha, int coluna, double valor);
	}
 
	// Gera panel com a tabela que representa uma matriz
	public MatrizPanel(Matriz matrizInicial) {
		String[] colunas = { "c1", "c2" };
		table = new JTable(matrizInicial.getObjeto(), colunas);
 
		table.setFont(new Font("Tahoma", Font.PLAIN, 13));
		table.setTableHeader(null);
		table.setFillsViewportHeight(true);
		table.setRowHeight(48);
 
		DefaultTableCellRenderer center = new DefaultTableCellRenderer();
		center.setHorizontalAlignment(JLabel.CENTER);
		table.setDefaultRenderer(Object.class, center);
		setViewportView(table);
 
		table.getModel().addTableModelListener(evento -> {
			if (atualizando) {
				return; // ignora eventos disparados por atualizar() para evitar loop
			}
 
			int linha = evento.getFirstRow();
			int coluna = evento.getColumn();
			if (linha < 0 || coluna < 0) {
				return; // eventos estruturais (não é edição de célula)
			}
 
			try { // verifica se a entrada do usuário é válida
				Object valorObj = table.getValueAt(linha, coluna);
				double valor = Double.parseDouble(valorObj.toString());
				
				if (listener != null) {
					listener.onValorEditado(linha, coluna, valor);
				}
			} catch (NullPointerException e1) {
				JOptionPane.showMessageDialog(this, "Insira um valor!",
						"Alerta", JOptionPane.WARNING_MESSAGE);
			} catch (NumberFormatException e2) {
				JOptionPane.showMessageDialog(this, "Insira um valor numérico!",
						"Alerta", JOptionPane.WARNING_MESSAGE);
			}
		});
	}
 
	public void setEditorListener(EditorListener listener) {
		this.listener = listener;
	}
 
	public void setEditavel(boolean editavel) {
		table.setEnabled(editavel);
	}
 
	// Atualiza a matriz exibida na tabela
	public void atualizar(Matriz matriz) {
		atualizando = true;
		try {
			Object[][] dados = matriz.getObjeto();
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					table.setValueAt(dados[i][j], i, j);
				}
			}
		} finally {
			atualizando = false;
		}
	}
}
