package main;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;

public class TransformUI extends JFrame{
	private final long serialVersionUID = 1L;
	private MotorGrafico motor;
	private Canvas glCanvas;
	private JSlider sliderx;
	private JSlider slidery;
	private JSlider angle;
	
	public TransformUI(MotorGrafico motor) {
		getContentPane().setBackground(new Color(166, 188, 201));
		this.motor = motor;
		
		setTitle("Transformações lineares");
		this.setSize(1050, 650);
		setResizable(false);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		
		glCanvas = new Canvas();
		glCanvas.setBounds(10, 40, 750, 550);
		getContentPane().add(glCanvas);
		
		Panel panel = new Panel();
		panel.setBackground(new Color(62, 75, 142));
		panel.setBounds(766, 10, 258, 591);
		getContentPane().add(panel);
		panel.setLayout(new GridLayout(4, 0, 0, 0));
		
		JPanel panel_1_1 = new JPanel();
		panel_1_1.setLayout(null);
		panel_1_1.setBackground(new Color(62, 75, 142));
		panel.add(panel_1_1);
		
		sliderx = new JSlider();
		sliderx.setBounds(32, 64, 200, 26);
		panel_1_1.add(sliderx);
		
		JLabel lblNewLabel_1 = new JLabel("X");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblNewLabel_1.setBounds(32, 46, 46, 14);
		panel_1_1.add(lblNewLabel_1);
		
		JPanel panel_1_2 = new JPanel();
		panel_1_2.setLayout(null);
		panel_1_2.setBackground(new Color(62, 75, 142));
		panel.add(panel_1_2);
		
		slidery = new JSlider();
		slidery.setBounds(32, 64, 200, 26);
		panel_1_2.add(slidery);
		
		JLabel lblY = new JLabel("Y");
		lblY.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblY.setBounds(32, 46, 46, 14);
		panel_1_2.add(lblY);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(62, 75, 142));
		panel.add(panel_1);
		panel_1.setLayout(null);
		
		angle = new JSlider();
		angle.setBounds(32, 64, 200, 26);
		panel_1.add(angle);
		
		JLabel lblNewLabel = new JLabel("angle");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblNewLabel.setBounds(32, 11, 154, 49);
		panel_1.add(lblNewLabel);
		
		JButton reset = new JButton("Resetar");
		reset.addActionListener(e -> {
			sliderx.setValue(0);
			slidery.setValue(0);
			angle.setValue(0);
		});
		panel.add(reset);
		
		ChangeListener listener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				System.out.println("Mudou");
				updateMotor();
			}
		};
		
		sliderx.addChangeListener(listener);
		slidery.addChangeListener(listener);
		angle.addChangeListener(listener);
		
	}
	
	public Canvas getCanvas() {
		return glCanvas;
	}
	
	public void updateMotor() {
		float tx = sliderx.getValue()/100.0f;
		float ty = slidery.getValue()/100.0f;
		float rotate = angle.getValue();
		motor.update(tx, ty, rotate);
	}
}
