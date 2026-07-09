package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Janela de entrada exibida ao abrir a aplicação, antes do visualizador
 * principal. Mostra o título do projeto, um resumo do que ele faz e um
 * botão para iniciar. Também tem um botão "Sobre" com mais detalhes do
 * contexto acadêmico do projeto.
 *
 * Ao clicar em "Iniciar", esta janela se fecha e o {@code aoIniciar}
 * (passado no construtor) é executado — quem decide o que acontece depois
 * é o Main, esta classe não conhece o MotorGrafico nem o TransformUI.
 */
public class TelaInicial extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final Color BG = new Color(0x11111b);
	private static final Color PANEL_BG = new Color(0x181825);
	private static final Color TEXT = new Color(0xcdd6f4);
	private static final Color MUTED = new Color(0x9399b2);
	private static final Color ACCENT = new Color(0x89b4fa);

	public TelaInicial(Runnable aoIniciar) {
		setTitle("Visualizador de Transformações Lineares");
		setSize(560, 420);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		getContentPane().setBackground(BG);
		getContentPane().setLayout(new BorderLayout());

		getContentPane().add(buildCentro(aoIniciar), BorderLayout.CENTER);
		getContentPane().add(buildRodape(), BorderLayout.SOUTH);
	}

	private JPanel buildCentro(Runnable aoIniciar) {
		JPanel centro = new JPanel();
		centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
		centro.setBackground(new Color(62, 75, 142));
		centro.setBorder(new EmptyBorder(48, 48, 24, 48));

		JLabel titulo = new JLabel("Transformações Lineares");
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
		titulo.setForeground(new Color(255, 255, 255));
		titulo.setAlignmentX(0.5f);

		JLabel subtitulo = new JLabel("Visualizador educacional — POO + Computação Gráfica");
		subtitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
		subtitulo.setForeground(new Color(255, 255, 255));
		subtitulo.setAlignmentX(0.5f);

		JTextArea resumo = new JTextArea(
				"      Explore translação, rotação, escala, reflexão e cisalhamento ");
		resumo.setTabSize(3);
		resumo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		resumo.setForeground(new Color(255, 255, 255));
		resumo.setBackground(new Color(62, 75, 142));
		resumo.setLineWrap(true);
		resumo.setWrapStyleWord(true);
		resumo.setEditable(false);
		resumo.setFocusable(false);
		resumo.setAlignmentX(0.5f);
		resumo.setMaximumSize(new Dimension(440, 80));
		resumo.setBorder(new EmptyBorder(16, 0, 24, 0));

		JButton iniciar = new JButton("Iniciar visualizador");
		iniciar.setFont(new Font("Segoe UI", Font.BOLD, 14));
		iniciar.setForeground(new Color(62, 75, 142));
		iniciar.setBackground(new Color(255, 255, 255));
		iniciar.setFocusPainted(false);
		iniciar.setBorder(new EmptyBorder(12, 24, 12, 24));
		iniciar.setAlignmentX(0.5f);
		iniciar.addActionListener(e -> {
			dispose();
			if (aoIniciar != null) {
				aoIniciar.run();
			}
		});

		JButton sobre = new JButton("Sobre o projeto");
		sobre.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		sobre.setForeground(new Color(255, 255, 255));
		sobre.setBackground(BG);
		sobre.setBorder(BorderFactory.createEmptyBorder(10, 24, 0, 24));
		sobre.setFocusPainted(false);
		sobre.setContentAreaFilled(false);
		sobre.setAlignmentX(0.5f);
		sobre.addActionListener(e -> abrirSobre());

		centro.add(titulo);
		centro.add(Box.createVerticalStrut(6));
		centro.add(subtitulo);
		centro.add(resumo);
		centro.add(iniciar);
		centro.add(sobre);

		return centro;
	}

	private JPanel buildRodape() {
		JPanel rodape = new JPanel(new GridLayout(1, 1));
		rodape.setForeground(new Color(62, 75, 142));
		rodape.setBackground(new Color(255, 255, 255));
		rodape.setBorder(new EmptyBorder(10, 16, 10, 16));

		JLabel creditos = new JLabel(
				"Projeto interdisciplinar — Programação Orientada a Objetos & Computação Gráfica",
				SwingConstants.CENTER);
		creditos.setBackground(new Color(192, 192, 192));
		creditos.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		creditos.setForeground(new Color(62, 75, 142));

		rodape.add(creditos);
		return rodape;
	}

	private void abrirSobre() {
		JDialog dialog = new JDialog(this, "Sobre o projeto", true);
		dialog.setSize(520, 420);
		dialog.setLocationRelativeTo(this);
		dialog.getContentPane().setBackground(new Color(62, 75, 142));
		dialog.getContentPane().setLayout(new BorderLayout());

		JTextArea texto = new JTextArea(
				"Este projeto integra as disciplinas de Programação Orientada a "
				+ "Objetos e Computação Gráfica, através de uma aplicação Java "
				+ "(camada de interface e modelagem) que se comunica, via JNI, "
				+ "com um motor gráfico nativo em C/C++ com OpenGL.\n\n"
				+ "POO (Java): modelagem das transformações lineares, "
				+ "interface do usuário e o contrato de comunicação com o motor.\n\n"
				+ "Computação Gráfica (C/C++): pipeline de renderização e "
				+ "integração via JNI.\n\n"
				+ "Equipe POO: Camila Fernada, Erick Batista, Lara Pereira\n"
				+ "Equipe CG: Hemilly Uchoa, Mikael Gomes\n");
		texto.setFont(new Font("Segoe UI Bold", Font.PLAIN, 13));
		texto.setForeground(new Color(255, 255, 255));
		texto.setBackground(new Color(62, 75, 142));
		texto.setLineWrap(true);
		texto.setWrapStyleWord(true);
		texto.setEditable(false);
		texto.setFocusable(false);
		texto.setBorder(new EmptyBorder(20, 20, 20, 20));

		JButton fechar = new JButton("Fechar");
		fechar.addActionListener(e -> dialog.dispose());
		JPanel rodapeDialog = new JPanel();
		rodapeDialog.setBackground(new Color(255, 255, 255));
		rodapeDialog.add(fechar);

		dialog.getContentPane().add(texto, BorderLayout.CENTER);
		dialog.getContentPane().add(rodapeDialog, BorderLayout.SOUTH);
		dialog.setVisible(true);
	}
}