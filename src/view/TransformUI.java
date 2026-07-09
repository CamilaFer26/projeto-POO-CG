package view;

import javax.swing.*;
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
	private String[] shapes = {
			"Escolha uma opção",
			"Quadrado",
			"Triângulo",
			"Circulo",
			"Vetor"
	};
	private String[] transDefault = {
			"Personalizado",
			"Identidade",
			"Escala",
			"Rotação",
			"Reflexão no eixo X",
			"Reflexão no eixo Y",
			"Reflexão na origem",
			"Cisalhamento horizontal",
			"Cisalhamento vertical",
			"Projeção em X",
			"Projeção em Y"
	};
	private JComboBox<String> transforms;
	private JPanel panelAuxiliar;
	private Matriz matriz;
	private JLabel lblDeterminante;
	JTextArea descricao;
	
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
		JLabel lbltransfLineares = new JLabel("Matriz de transformação");
		lbltransfLineares.setForeground(Color.WHITE);
		lbltransfLineares.setFont(new Font("Times New Roman", Font.BOLD, 15));
		lbltransfLineares.setBounds(10, 119, 180, 14);
		panel.add(lbltransfLineares);
		
		JComboBox<String> formas = new JComboBox<>(shapes);
		formas.setBounds(11, 80, 179, 22);
		panel.add(formas);
		formas.addActionListener(e -> {
			int index = formas.getSelectedIndex();
			if (index > 0) {
				motor.shape(index - 1);
			}
		});
		
		// Lista de transformações lineares básicas
		JLabel lblFormas = new JLabel("Forma geométrica");
		lblFormas.setForeground(new Color(255, 255, 255));
		lblFormas.setFont(new Font("Times New Roman", Font.BOLD, 15));
		lblFormas.setBounds(10, 57, 124, 14);
		panel.add(lblFormas);
		
		transforms = new JComboBox<>(transDefault);
		transforms.setBounds(9, 139, 179, 22);
		panel.add(transforms);
		transforms.addActionListener(e -> {
		    configurarControles();
		});
		
		//--------- Tabela (Matriz) ---------------------
		String[] colunas = {"c1", "c2"};
        matriz = new Matriz(1, 0, 0, 1);
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
		lblDeterminante = new JLabel("Determinante: " + matriz.determinante());
		lblDeterminante.setForeground(Color.WHITE);
		lblDeterminante.setFont(new Font("Times New Roman", Font.BOLD, 15));
		lblDeterminante.setBounds(118, 186, 140, 14);
		panel.add(lblDeterminante);
		
		// área de texto para descrições educacionais
		descricao = new JTextArea();
		descricao.setBounds(120, 202, 124, 84);
		panel.add(descricao);
		descricao.setEnabled(false);
		descricao.setEditable(false);
		descricao.setLineWrap(true);       // ativa a quebra automática
		descricao.setWrapStyleWord(true);  // quebra entre palavras, não no meio delas
		descricao.setBackground(new Color(62, 75, 142));
		descricao.setText(matriz.descricaoDet());
		
		// painel auxiliar para slider de rotação
		panelAuxiliar = new JPanel();
		panelAuxiliar.setBackground(new Color(62, 75, 142));
		panelAuxiliar.setBounds(10, 312, 238, 55);
		panel.add(panelAuxiliar);
		panelAuxiliar.setLayout(new GridLayout(2, 0, 0, 0));
		
		// listener para alterações na tabela
		table.getModel().addTableModelListener(e -> {
			int linha = e.getFirstRow();
			int coluna = e.getColumn();
			
			try {
				double valor = Double.parseDouble(table.getValueAt(linha, coluna).toString());
				System.out.printf("[%d, %d] = %f", linha, coluna, valor);
				
				if(matriz.getValor(linha, coluna) != valor) { // só atualiza se houver alteração
					matriz.setValor(valor, linha, coluna);
					lblDeterminante.setText("Determinante: " + matriz.determinante());	// atualiza o determinante
					descricao.setText(matriz.descricaoDet());
					
					float[][] valores = matriz.toFloat();
					motor.update(valores[0][0], valores[0][1], valores[1][0],  valores[1][1]);
				}

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
			formas.setSelectedIndex(0);
			transforms.setSelectedIndex(0);
			
			atualizarTabela();
			panelAuxiliar.removeAll();
		    //motor.cleanup();
		});
		panel.add(reset);
	}
	//--------------- FUNÇÕES AUXILIARES -----------------
	// retorna o canvas para motor gráfico
	public Canvas getCanvas() {
		return glCanvas;
	}
	
	// atualiza a tabela e o motor
	private void atualizarTabela() {
	    Object[][] dados = matriz.getObjeto();
	    for(int i=0;i<2;i++)
	        for(int j=0;j<2;j++)
	            table.setValueAt(dados[i][j], i, j);

	    float[][] m = matriz.toFloat();

	    motor.update(
	        m[0][0],
	        m[0][1],
	        m[1][0],
	        m[1][1]
	    );

	    lblDeterminante.setText(
	        "Determinante: " + matriz.determinante()
	    );

	    descricao.setText(
	        matriz.descricaoDet()
	    );
	}
	
	// controles para transformações lineares básicas
	private void configurarControles() {
	    panelAuxiliar.removeAll();
	    
	    if(transforms.getSelectedIndex() == 0) {
	    	table.setEnabled(true);
	    	panelAuxiliar.removeAll();
	    } else {
	    	table.setEnabled(false);
	    }
	    switch (transforms.getSelectedIndex()) {
	        case 1: // Identidade
	            matriz.setValores(1, 0, 0, 1);
	            atualizarTabela();
	            break;

	        case 2: // Escala
	            criarSliderEscala();
	            break;
	        	
	        case 3: // Rotação
	            criarSliderRotacao();
	            break;

	        case 4: // Reflexão no eixo X
	        	matriz.setValores(1, 0, 0, -1);
	        	atualizarTabela();
	        	break;
	        	
	        case 5: // Reflexão no eixo Y
	        	matriz.setValores(-1, 0, 0, 1);
	        	atualizarTabela();
	        	break;

	        case 6: // Reflexão na origem
	        	matriz.setValores(-1, 0, 0, -1);
	        	atualizarTabela();
	        	break;
	        	
	        case 7: // Cisalhamento horizontal
	            criarSliderCisalhamentoH();
	            break;
	            
	        case 8: // Cisalhamento vertical
	            criarSliderCisalhamentoV();
	            break;          
	        
	        case 9: // Projeção em X
	        	matriz.setValores(1, 0, 0, 0);
	        	atualizarTabela();
	        	break;
	        
	        case 10: // Projeção em Y
	        	matriz.setValores(0, 0, 0, 1);
	        	atualizarTabela();
	        	break;
	    }

	    panelAuxiliar.revalidate();
	    panelAuxiliar.repaint();
	}
	
	// slider para escala interativa
	private void criarSliderEscala() {
	    JSlider sliderx = new JSlider(0, 500, 100);
	    JSlider slidery = new JSlider(0, 500, 100);
	    sliderx.addChangeListener(e -> {
	        double sx = sliderx.getValue()/100.0;
	        double sy = slidery.getValue()/100.0;
	        
	        matriz.setValores(
	            sx,0,
	            0,sy
	        );
	        atualizarTabela();
	    });

	    slidery.addChangeListener(e -> {
	        double sx = sliderx.getValue()/100.0;
	        double sy = slidery.getValue()/100.0;
	        
	        matriz.setValores(
	            sx,0,
	            0,sy
	        );
	        atualizarTabela();
	    });
	    
	    panelAuxiliar.add(sliderx);
	    panelAuxiliar.add(slidery);
	}
	
	// slider para rotação interativa
	private void criarSliderRotacao() {
	    JSlider slider = new JSlider(0, 360, 0);

	    slider.addChangeListener(e -> {
	        double angulo =
	            Math.toRadians(slider.getValue());

	        matriz.setValores(
	            Math.cos(angulo),
	            -Math.sin(angulo),
	            Math.sin(angulo),
	            Math.cos(angulo)
	        );

	        atualizarTabela();
	    });

	    panelAuxiliar.add(slider);
	}
	
	// slider para cisalhamento vertical interativo
	private void criarSliderCisalhamentoV() {
	    JSlider slider = new JSlider(0, 360, 0);

	    slider.addChangeListener(e -> {
	        double s = slider.getValue()/100.0;

	        matriz.setValores(
		            1,0,
		            s,1
		    );

	        atualizarTabela();
	    });

	    panelAuxiliar.add(slider);
	}
	
	// slider para cisalhamento horizontal interativo
	private void criarSliderCisalhamentoH() {
	    JSlider slider = new JSlider(0, 360, 0);

	    slider.addChangeListener(e -> {
	        double s = slider.getValue()/100.0;

	        matriz.setValores(
		            1,s,
		            0,1
		    );

	        atualizarTabela();
	    });

	    panelAuxiliar.add(slider);
	}
}