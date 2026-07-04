package view;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;

import graphics.MotorGrafico;
import model.Matriz;

import java.awt.*;

public class TransformUI extends JFrame{
	private final long serialVersionUID = 1L;
	private MotorGrafico motor;
	private Canvas glCanvas;
	private JSlider slider;
	private JTable table;
	
	public TransformUI(MotorGrafico motor) {
		this.motor = motor;
		getContentPane().setBackground(new Color(166, 188, 201));
		
		//------------ Canvas para o OpenGL ------------------------
		glCanvas = new Canvas();
		glCanvas.setBounds(10, 10, 750, 590);
		getContentPane().add(glCanvas);		
		setTitle("Visualizador educacional de transformações lineares");
		this.setSize(1050, 650);
		setResizable(false);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		
		//------------ Painel de opções/ferramentas ----------------
		Panel panel = new Panel();
		panel.setBackground(new Color(62, 75, 142));
		panel.setBounds(766, 10, 258, 591);
		getContentPane().add(panel);
		panel.setLayout(null);
		
		// Título
		JLabel lbl1 = new JLabel(" Transformações Lineares");
		lbl1.setForeground(new Color(255, 255, 255));
		lbl1.setFont(new Font("Times New Roman", Font.BOLD, 22));
		lbl1.setBounds(0, 0, 258, 46);
		panel.add(lbl1);
		
		// Lista de formas geométricas
		JLabel lbltransfLineares = new JLabel("Transformações básicas");
		lbltransfLineares.setForeground(Color.WHITE);
		lbltransfLineares.setFont(new Font("Times New Roman", Font.BOLD, 15));
		lbltransfLineares.setBounds(10, 119, 180, 14);
		panel.add(lbltransfLineares);		
		
		JList listFormas = new JList();
		listFormas.setBounds(10, 77, 180, 21);
		panel.add(listFormas);
		
		// Lista de transformações lineares básicas
		JLabel lblFormas = new JLabel("Forma geométrica");
		lblFormas.setForeground(new Color(255, 255, 255));
		lblFormas.setFont(new Font("Times New Roman", Font.BOLD, 15));
		lblFormas.setBounds(10, 57, 124, 14);
		panel.add(lblFormas);
		
		JList listTransform = new JList();
		listTransform.setBounds(10, 139, 180, 21);
		panel.add(listTransform);
		
		//--------- Tabela (Matriz) ---------------------
		String[] colunas = {"c1", "c2"};
        Matriz matriz = new Matriz(1, 0, 0, 1);
		table = new JTable(matriz.getObjeto(), colunas);
		
		table.setFont(new Font("Tahoma", Font.PLAIN, 20));
		table.setTableHeader(null);
		table.setFillsViewportHeight(true);
		table.setRowHeight(48);
		
		// centraliza o texto na tabela
		DefaultTableCellRenderer center = new DefaultTableCellRenderer();
		center.setHorizontalAlignment(JLabel.CENTER);
		table.setDefaultRenderer(Object.class, center);
		
		// scroll pane para a tabela
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(10, 186, 100, 100);
		panel.add(scrollPane);
		
		JLabel lblMatriz = new JLabel("Matriz");
		lblMatriz.setForeground(new Color(255, 255, 255));
		lblMatriz.setFont(new Font("Times New Roman", Font.BOLD, 15));
		lblMatriz.setBounds(10, 171, 46, 14);
		panel.add(lblMatriz);
		
		// determinante da matriz
		JLabel lblDeterminante = new JLabel("Determinante: " + matriz.determinante());
		lblDeterminante.setForeground(Color.WHITE);
		lblDeterminante.setFont(new Font("Times New Roman", Font.BOLD, 15));
		lblDeterminante.setBounds(118, 186, 140, 14);
		panel.add(lblDeterminante);
		
		// área de texto para descrições educacionais
		JTextArea descricao = new JTextArea();
		descricao.setBounds(120, 202, 124, 84);
		panel.add(descricao);
		descricao.setEnabled(false);
		descricao.setEditable(false);
		descricao.setLineWrap(true);       // ativa a quebra automática
		descricao.setWrapStyleWord(true);  // quebra entre palavras, não no meio delas
		descricao.setBackground(new Color(62, 75, 142));
		descricao.setText(matriz.descricaoDet());
		
		// painel auxiliar para slider de rotação
		JPanel panelAuxiliar = new JPanel();
		panelAuxiliar.setBackground(new Color(62, 75, 142));
		panelAuxiliar.setBounds(10, 312, 238, 26);
		panel.add(panelAuxiliar);
		
		// listener para alterações na tabela
		table.getModel().addTableModelListener(e -> {
			int linha = e.getFirstRow();
			int coluna = e.getColumn();
			
			try {
				double valor = Double.parseDouble(table.getValueAt(linha, coluna).toString());
				System.out.printf("[%d, %d] = %f", linha, coluna, valor);
				
				matriz.setValor(valor, linha, coluna);
				lblDeterminante.setText("Determinante: " + matriz.determinante());	// atualiza o determinante
				descricao.setText(matriz.descricaoDet());
			} catch(NullPointerException e1){
				JOptionPane.showMessageDialog(this, "Insira um valor!", "Alerta", 
						JOptionPane.WARNING_MESSAGE);
			} catch(NumberFormatException e2) {
				JOptionPane.showMessageDialog(this, "Insira um valor numérico!", "Alerta",
						JOptionPane.WARNING_MESSAGE);
			}
		});
		
		// slider para rotação
		slider = new JSlider();
		slider.setBounds(20, 357, 200, 26);
		slider.setValue(0);
		
		//------------ Botão reset ----------------------------
		JButton reset = new JButton("Resetar");
		reset.setBounds(69, 541, 131, 39);
		reset.addActionListener(e -> {
			matriz.setValores(1, 0, 0, 1);
			
			Object[][] dados = matriz.getObjeto();
		    for (int i = 0; i < dados.length; i++) {
		        for (int j = 0; j < dados[i].length; j++) {
		            table.setValueAt(dados[i][j], i, j);
		        }
		    }
		});
		panel.add(reset);
	}
	
	public Canvas getCanvas() {
		return glCanvas;
	}
	
	public void updateMotor() {

	}
}
