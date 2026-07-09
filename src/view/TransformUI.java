package view;

import javax.swing.*;
import controller.TransformController;
import controller.TransformListener;
import graphics.MotorGrafico;
import model.*;
import java.awt.*;

// View principal. Responsabilidades: montar os
// componentes Swing e traduzir eventos de UI em chamadas ao
// TransformController.
public class TransformUI extends JFrame implements TransformListener{
	private final long serialVersionUID = 1L;
	private final TransformController controller;
	private Canvas glCanvas;
	private String[] shapes = {
			"Escolha uma opção",
			"Quadrado",
			"Triângulo",
			"Circulo",
			"Vetor"
	};
	
	private final Transformacao[] transformacoes = {
			new Personalizado(),
			new Identidade(),
			new Escala(),
			new Rotacao(),
			new ReflexaoX(),
			new ReflexaoY(),
			new ReflexaoOrigem(),
			new CisalhamentoHorizontal(),
			new CisalhamentoVertical(),
			new ProjecaoX(),
			new ProjecaoY()
	};
	
	private JComboBox<Transformacao> transforms;
	private JPanel panelAuxiliar;
	private JLabel lblDeterminante;
	private JLabel vetoresBase;
	private JTextArea descricao;
	private JLabel lblAuxiliar;
	
	private MatrizPanel matrizPanel;
	private MatrizPanel acumuladaPanel;
	
	private static final Color BG = new Color(166, 188, 201);
	private static final Color PANEL_BG = new Color(61, 21, 52);
	private static final Color TEXT = new Color(246, 224, 182);
	private static final Color MUTED = new Color(255, 244, 235);
	private static final Color ACCENT = new Color(62, 75, 142);
	
	public TransformUI(MotorGrafico motor) {
		this.controller = new TransformController(motor);
		this.controller.setListener(this);
 
		getContentPane().setBackground(BG);
 
		// ------------ Canvas para o OpenGL ------------------------
		glCanvas = new Canvas();
		glCanvas.setBounds(10, 10, 750, 590);
		getContentPane().add(glCanvas);
		setTitle("Visualizador educacional de transformações lineares");
		this.setSize(1050, 650);
		setResizable(false);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
 
		// ------------ Painel de opções/ferramentas ----------------
		Panel panel = new Panel();
		panel.setBackground(PANEL_BG);
		panel.setBounds(766, 10, 258, 591);
		getContentPane().add(panel);
		panel.setLayout(null);
 
		JLabel lbl1 = new JLabel(" Transformações Lineares");
		lbl1.setForeground(TEXT);
		lbl1.setFont(new Font("Times New Roman", Font.BOLD, 22));
		lbl1.setBounds(0, 0, 258, 46);
		panel.add(lbl1);
 
		JLabel lblFormas = new JLabel("Forma geométrica");
		lblFormas.setForeground(TEXT);
		lblFormas.setFont(new Font("Times New Roman", Font.BOLD, 15));
		lblFormas.setBounds(10, 57, 124, 14);
		panel.add(lblFormas);
 
		JComboBox<String> formas = new JComboBox<>(shapes);
		formas.setBounds(11, 80, 179, 22);
		panel.add(formas);
		formas.addActionListener(e -> {
			int index = formas.getSelectedIndex();
			if (index > 0) {
				controller.selecionarForma(index - 1);
			}
		});
 
		JLabel lbltransfLineares = new JLabel("Matriz de transformação");
		lbltransfLineares.setForeground(TEXT);
		lbltransfLineares.setFont(new Font("Times New Roman", Font.BOLD, 15));
		lbltransfLineares.setBounds(10, 119, 180, 14);
		panel.add(lbltransfLineares);
 
		transforms = new JComboBox<>(transformacoes);
		transforms.setBounds(9, 139, 179, 22);
		// override do renderer para utilizar getNome() nas transformacoes
		transforms.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
 
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				String texto = (value instanceof Transformacao) ? ((Transformacao) value).getNome() : String.valueOf(value);
				return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
			}
		});
		panel.add(transforms);
		transforms.addActionListener(e -> configurarControles());
 
		// --------- Painel: matriz atual ---------------------
		matrizPanel = new MatrizPanel(controller.getMatrizAtual());
		matrizPanel.setBounds(10, 186, 100, 100);
		matrizPanel.setEditorListener(controller::editarValorMatriz);
		panel.add(matrizPanel);
 
		JLabel lblMatriz = new JLabel("Matriz");
		lblMatriz.setForeground(TEXT);
		lblMatriz.setFont(new Font("Times New Roman", Font.BOLD, 15));
		lblMatriz.setBounds(10, 171, 46, 14);
		panel.add(lblMatriz);
 
		lblDeterminante = new JLabel();
		lblDeterminante.setForeground(TEXT);
		lblDeterminante.setFont(new Font("Times New Roman", Font.BOLD, 13));
		lblDeterminante.setBounds(118, 171, 140, 14);
		lblDeterminante.setToolTipText("<html> det = 0 -> transformação comprime o plano em uma reta ou ponto.<br>"
				+ "A matriz não é invertível.<br>"
				+ "det > 0 -> A orientação do sistema é preservada. A matriz é invertível.<br>"
				+ "det < 0 -> Há inversão de orientação (reflexão). A matriz é invertível.<br><html>");
		
		panel.add(lblDeterminante);
 
		// vetores base transformados (i e j)
		vetoresBase = new JLabel();
		vetoresBase.setForeground(TEXT);
		vetoresBase.setFont(new Font("Times New Roman", Font.PLAIN, 11));
		vetoresBase.setBounds(116, 186, 140, 17);
		vetoresBase.setToolTipText(
				"Para onde vão os vetores da base (1,0) e (0,1) após a "
						+ "transformação");
		panel.add(vetoresBase);
 
		descricao = new JTextArea();
		descricao.setBounds(118, 201, 130, 134);
		panel.add(descricao);
		descricao.setEnabled(false);
		descricao.setEditable(false);
		descricao.setLineWrap(true);
		descricao.setWrapStyleWord(true);
		descricao.setFont(new Font("Tahoma", Font.PLAIN, 11));
		descricao.setForeground(TEXT);
		descricao.setBackground(PANEL_BG);
 
		panelAuxiliar = new JPanel();
		panelAuxiliar.setBackground(PANEL_BG);
		panelAuxiliar.setBounds(10, 337, 238, 60);
		panel.add(panelAuxiliar);
		panelAuxiliar.setLayout(new GridLayout(3, 0, 0, 0));
 
		lblAuxiliar = new JLabel("");
		lblAuxiliar.setForeground(TEXT);
		lblAuxiliar.setFont(new Font("Times New Roman", Font.BOLD, 15));
		panelAuxiliar.add(lblAuxiliar);
 
		// ------------ Botão reset (matriz atual) ----------------------------
		JButton reset = new JButton("Resetar");
		reset.setBounds(69, 541, 131, 39);
		reset.addActionListener(e -> {
			formas.setSelectedIndex(0);
			transforms.setSelectedIndex(0);
			controller.resetarMatrizAtual();
			panelAuxiliar.removeAll();
			panelAuxiliar.add(lblAuxiliar);
		});
		reset.setBackground(TEXT);
		panel.add(reset);
 
		// --------- Painel: matriz acumulada ---------------------
		acumuladaPanel = new MatrizPanel(controller.getAcumulada());
		acumuladaPanel.setBounds(43, 420, 100, 100);
		acumuladaPanel.setEditavel(false);
		panel.add(acumuladaPanel);
 
		JLabel lblMatrizAcumulada = new JLabel("Matriz Acumulada");
		lblMatrizAcumulada.setForeground(TEXT);
		lblMatrizAcumulada.setFont(new Font("Times New Roman", Font.BOLD, 15));
		lblMatrizAcumulada.setBounds(31, 401, 135, 14);
		lblMatrizAcumulada.setToolTipText(
				"Guarda o produto de todas as transformações aplicadas com "
						+ "\"Aplicar\", na ordem em que foram aplicadas — assim "
						+ "dá pra compor várias transformações em sequência.");
		panel.add(lblMatrizAcumulada);
 
		JButton btnAplicar = new JButton("Aplicar");
		btnAplicar.setBounds(158, 445, 89, 23);
		btnAplicar.setToolTipText("Multiplica a matriz atual pela acumulada");
		btnAplicar.addActionListener(e -> controller.aplicarNaAcumulada());
		btnAplicar.setBackground(BG);
		panel.add(btnAplicar);
 
		JButton btnResetarAcumulada = new JButton("Resetar");
		btnResetarAcumulada.setBounds(159, 475, 89, 23);
		btnResetarAcumulada.addActionListener(e -> controller.resetarAcumulada());
		btnResetarAcumulada.setBackground(BG);
		panel.add(btnResetarAcumulada);
 
		// estado inicial da UI
		onMatrizAtualizada(controller.getMatrizAtual());
		onAcumuladaAtualizada(controller.getAcumulada());
		configurarControles();
	}
	//--------------- FUNÇÕES AUXILIARES -----------------
	// retorna o canvas para motor gráfico
	public Canvas getCanvas() {
		return glCanvas;
	}

	// configuração do painel de sliders e descrição
	private void configurarControles() {
		panelAuxiliar.removeAll();
		lblAuxiliar.setText("");
		panelAuxiliar.add(lblAuxiliar);
 
		Transformacao selecionada = (Transformacao) transforms.getSelectedItem();
 
		if (selecionada.isPersonalizado()) {
			matrizPanel.setEditavel(true);
		} else {
			matrizPanel.setEditavel(false);
			int n = selecionada.getNumeroSliders();
			if (n == 0) {
				controller.aplicarTransformacao(selecionada);
			} else {
				criarSliders(selecionada, n);
			}
		}
 
		atualizarDescricao(selecionada);
 
		panelAuxiliar.revalidate();
		panelAuxiliar.repaint();
	}
 
	// cria os sliders necessários para a transformação interativa
	private void criarSliders(Transformacao t, int quantidade) {
		JSlider[] sliders = new JSlider[quantidade];
 
		for (int i = 0; i < quantidade; i++) {
			int min = (int) Math.round(t.getSliderMin() * 100);
			int max = (int) Math.round(t.getSliderMax() * 100);
			int inicial = (int) Math.round(t.getSliderInicio() * 100);
 
			JSlider slider = new JSlider(min, max, inicial);
			sliders[i] = slider;
			panelAuxiliar.add(slider);
		}
 
		// listeners 
		Runnable aoMudar = () -> {
			double[] valores = new double[quantidade];
			StringBuilder texto = new StringBuilder();
			for (int i = 0; i < quantidade; i++) {
				valores[i] = sliders[i].getValue() / 100.0;
				texto.append(valores[i] + "   ");
			}
			
			controller.aplicarTransformacao(t, valores);
			lblAuxiliar.setText(texto.toString());
		};
 
		for (JSlider slider : sliders) {
			slider.addChangeListener(e -> aoMudar.run());
		}
 
		aoMudar.run();
	}
 
	// atualiza descrição da matriz
	private void atualizarDescricao(Transformacao t) {
		descricao.setText(t.getDescricao());
	}
 
	// --------------- TransformListener -----------------
 
	@Override
	public void onMatrizAtualizada(Matriz matriz) {
		matrizPanel.atualizar(matriz);
		lblDeterminante.setText("Det: " + arredondar(matriz.determinante()));
 
		double[] i = matriz.getImagemVetorI();
		double[] j = matriz.getImagemVetorJ();
		vetoresBase.setText("i → (" + arredondar(i[0]) + ", " + arredondar(i[1])
				+ ") j → (" + arredondar(j[0]) + ", " + arredondar(j[1]) + ")");
	}
 
	@Override
	public void onAcumuladaAtualizada(Matriz acumulada) {
		acumuladaPanel.atualizar(acumulada);
	}
 
	private double arredondar(double valor) {
		return Math.round(valor * 100.0) / 100.0;
	}
}