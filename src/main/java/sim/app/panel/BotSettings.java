/**
* Copyright 2016 Tim Pearce
**/

package sim.app.panel;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

public class BotSettings extends JPanel {

	public JButton bot_stop;
	public JButton bot_start;
	public JButton bot_reset;
	public JSpinner accel;
	public JSpinner deccel;
	public JSpinner refVoltage;
	public JSpinner refRPM;
	public JSpinner maxVoltage;
	public JTextField maxRPM;
	
	public BotSettings () {
		initialize();
	}
	
	private void initialize() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Box container = new Box(BoxLayout.Y_AXIS);
		this.add(container);
		
		JPanel h = new JPanel();
		h.setBackground(Color.LIGHT_GRAY);
		h.add(new JLabel("Bot Simulation", JLabel.CENTER));
		container.add(h);
		
		JPanel settings = new JPanel();
		
		SpringLayout springLayout = new SpringLayout();
		settings.setLayout(springLayout);
		
		JPanel panel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel, 6, SpringLayout.NORTH, container);
		springLayout.putConstraint(SpringLayout.WEST, panel, 10, SpringLayout.WEST, settings);
		springLayout.putConstraint(SpringLayout.EAST, panel, -10, SpringLayout.EAST, settings);
		settings.add(panel);
		panel.setLayout(new GridLayout(0, 2, 10, 0));
		
		bot_start = new JButton("Start");
		panel.add(bot_start);
		
		bot_stop = new JButton("Stop");
		panel.add(bot_stop);
		
		bot_reset = new JButton("Reset");
		springLayout.putConstraint(SpringLayout.NORTH, bot_reset, 6, SpringLayout.SOUTH, panel);
		springLayout.putConstraint(SpringLayout.WEST, bot_reset, 10, SpringLayout.WEST, settings);
		springLayout.putConstraint(SpringLayout.EAST, bot_reset, -10, SpringLayout.EAST, settings);
		settings.add(bot_reset);
		
		JPanel sep = new JPanel();
		sep.setBackground(Color.GRAY);
		springLayout.putConstraint(SpringLayout.NORTH, sep, 6, SpringLayout.SOUTH, bot_reset);
		springLayout.putConstraint(SpringLayout.WEST, sep, 0, SpringLayout.WEST, panel);
		springLayout.putConstraint(SpringLayout.SOUTH, sep, 8, SpringLayout.SOUTH, bot_reset);
		springLayout.putConstraint(SpringLayout.EAST, sep, 0, SpringLayout.EAST, panel);
		settings.add(sep);
		
		JLabel lblMaxAcceleration = new JLabel("Max Accel %/20ms");
		springLayout.putConstraint(SpringLayout.WEST, lblMaxAcceleration, 6, SpringLayout.WEST, sep);
		settings.add(lblMaxAcceleration);
		
		JLabel lblMaxDecceleration = new JLabel("Max Decel %/20ms");
		springLayout.putConstraint(SpringLayout.WEST, lblMaxDecceleration, 6, SpringLayout.WEST, sep);
		settings.add(lblMaxDecceleration);
		
		JLabel lblReferenceVoltage = new JLabel("Reference Voltage:");
		springLayout.putConstraint(SpringLayout.WEST, lblReferenceVoltage, 6, SpringLayout.WEST, sep);
		settings.add(lblReferenceVoltage);
		
		JLabel lblReferenceRpm = new JLabel("Reference RPM");
		springLayout.putConstraint(SpringLayout.WEST, lblReferenceRpm, 6, SpringLayout.WEST, sep);
		settings.add(lblReferenceRpm);
		
		JLabel lblRunningVoltage = new JLabel("Max Voltage");
		springLayout.putConstraint(SpringLayout.WEST, lblRunningVoltage, 6, SpringLayout.WEST, sep);
		settings.add(lblRunningVoltage);
		
		JLabel lblMaxRPM = new JLabel("Max RPM");
		springLayout.putConstraint(SpringLayout.WEST, lblMaxRPM, 6, SpringLayout.WEST, sep);
		settings.add(lblMaxRPM);	
		
		accel = new JSpinner();
		accel.setModel(new SpinnerNumberModel(new Float(0.01f), new Float(0), new Float(1), new Float(0.01f)));
		((JSpinner.DefaultEditor)accel.getEditor()).getTextField().setColumns(3);
		springLayout.putConstraint(SpringLayout.NORTH, lblMaxAcceleration, 3, SpringLayout.NORTH, accel);
		springLayout.putConstraint(SpringLayout.NORTH, accel, 6, SpringLayout.SOUTH, sep);
		springLayout.putConstraint(SpringLayout.EAST, accel, 0, SpringLayout.EAST, panel);
		settings.add(accel);
		
		deccel = new JSpinner();
		deccel.setModel(new SpinnerNumberModel(new Float(-0.02f), new Float(-1), new Float(0), new Float(0.01f)));
		((JSpinner.DefaultEditor)deccel.getEditor()).getTextField().setColumns(3);
		springLayout.putConstraint(SpringLayout.NORTH, lblMaxDecceleration, 3, SpringLayout.NORTH, deccel);
		springLayout.putConstraint(SpringLayout.NORTH, deccel, 6, SpringLayout.SOUTH, accel);
		springLayout.putConstraint(SpringLayout.EAST, deccel, 0, SpringLayout.EAST, panel);
		settings.add(deccel);
				
		refVoltage = new JSpinner();
		refVoltage.setModel(new SpinnerNumberModel(new Float(6.0f), new Float(0), new Float(12), new Float(0.1f)));
		((JSpinner.DefaultEditor)refVoltage.getEditor()).getTextField().setColumns(3);
		springLayout.putConstraint(SpringLayout.NORTH, lblReferenceVoltage, 3, SpringLayout.NORTH, refVoltage);
		springLayout.putConstraint(SpringLayout.NORTH, refVoltage, 6, SpringLayout.SOUTH, deccel);
		springLayout.putConstraint(SpringLayout.EAST, refVoltage, 0, SpringLayout.EAST, panel);
		settings.add(refVoltage);
				
		refRPM = new JSpinner();
		refRPM.setModel(new SpinnerNumberModel(new Float(180), new Float(0), new Float(500), new Float(10f)));
		((JSpinner.DefaultEditor)refRPM.getEditor()).getTextField().setColumns(3);
		springLayout.putConstraint(SpringLayout.NORTH, lblReferenceRpm, 3, SpringLayout.NORTH, refRPM);
		springLayout.putConstraint(SpringLayout.NORTH, refRPM, 6, SpringLayout.SOUTH, refVoltage);
		springLayout.putConstraint(SpringLayout.EAST, refRPM, 0, SpringLayout.EAST, panel);
		settings.add(refRPM);
				
		maxVoltage = new JSpinner();
		maxVoltage.setModel(new SpinnerNumberModel(new Float(8.7f), new Float(0), new Float(18), new Float(0.1f)));
		((JSpinner.DefaultEditor)maxVoltage.getEditor()).getTextField().setColumns(3);
		springLayout.putConstraint(SpringLayout.NORTH, lblRunningVoltage, 3, SpringLayout.NORTH, maxVoltage);
		springLayout.putConstraint(SpringLayout.NORTH, maxVoltage, 6, SpringLayout.SOUTH, refRPM);
		springLayout.putConstraint(SpringLayout.EAST, maxVoltage, 0, SpringLayout.EAST, panel);
		settings.add(maxVoltage);
		
		maxRPM = new JTextField();
		maxRPM.setEditable(false);
		maxRPM.setColumns(5);
		maxRPM.setHorizontalAlignment(SwingConstants.TRAILING);
		springLayout.putConstraint(SpringLayout.NORTH, lblMaxRPM, 3, SpringLayout.NORTH, maxRPM);
		springLayout.putConstraint(SpringLayout.NORTH, maxRPM, 6, SpringLayout.SOUTH, maxVoltage);
		springLayout.putConstraint(SpringLayout.EAST, maxRPM, 0, SpringLayout.EAST, panel);
		settings.add(maxRPM);

		
		//springLayout.putConstraint(SpringLayout.SOUTH, settings, 6, SpringLayout.SOUTH, maxRPM);
		
		container.add(settings);	
	}
}
