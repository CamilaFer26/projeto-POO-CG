package main;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;

public class TransformUI extends JFrame{
	private final long serialVersionUID = 1L;
	private MotorGrafico motor;
	private JSlider sliderx;
	private JSlider slidery;
	private JSlider rotate;
	
	public TransformUI(MotorGrafico motor) {
		this.motor = motor;
		
		setTitle("Transformações lineares");
		setSize(440, 300);
		setLayout(new GridLayout(4, 1));
		setResizable(false);
		
		sliderx = new JSlider(-100, 100, 0);
		slidery = new JSlider(-100, 100, 0);
		rotate = new JSlider(0, 360, 0);
		
		add(createPanel("Translação X", sliderx));
		add(createPanel("Translação y", slidery));
		add(createPanel("Rotação", rotate));
		
		JButton reset = new JButton("Resetar");
		reset.addActionListener(e -> {
			sliderx.setValue(0);
			slidery.setValue(0);
			rotate.setValue(0);
			
		});
		
		add(reset);
		
		ChangeListener listener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				System.out.println("Mudou");
				updateMotor();
			}
		};
		
		sliderx.addChangeListener(listener);
		slidery.addChangeListener(listener);
		rotate.addChangeListener(listener);
		
	}
	
	public JPanel createPanel(String title, JSlider slider) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(title), BorderLayout.NORTH);
		panel.add(slider, BorderLayout.CENTER);
		return panel;
	}
	
	public void updateMotor() {
		float tx = sliderx.getValue()/100.0f;
		float ty = slidery.getValue()/100.0f;
		float angle = rotate.getValue();
		
		motor.update(tx, ty, angle);
	}

}
