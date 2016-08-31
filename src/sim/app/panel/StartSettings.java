package sim.app.panel;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class StartSettings extends JPanel {

	public JButton lightsOff;
	public JButton lightsRed;
	public JButton lightsGreen;
	public JComboBox lane;
	
	public StartSettings() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Box container = new Box(BoxLayout.Y_AXIS);
		this.add(container);
		
		JPanel h = new JPanel();
		h.setBackground(Color.LIGHT_GRAY);
		h.add(new JLabel("Start Settings", JLabel.CENTER));
		container.add(h);
		
		JPanel settings = new JPanel();
		
		SpringLayout springLayout = new SpringLayout();
		settings.setLayout(springLayout);
		
		lightsOff = new JButton("Off");
		lightsOff.setEnabled(false);
		springLayout.putConstraint(SpringLayout.NORTH, lightsOff, 6, SpringLayout.NORTH, settings);
		springLayout.putConstraint(SpringLayout.WEST, lightsOff, 10, SpringLayout.WEST, settings);
		springLayout.putConstraint(SpringLayout.EAST, lightsOff, -10, SpringLayout.EAST, settings);
		settings.add(lightsOff);
		
		lightsGreen = new JButton("Green");
		springLayout.putConstraint(SpringLayout.NORTH, lightsGreen, 6, SpringLayout.SOUTH, lightsOff);
		springLayout.putConstraint(SpringLayout.WEST, lightsGreen, 0, SpringLayout.WEST, lightsOff);
		springLayout.putConstraint(SpringLayout.EAST, lightsGreen, 0, SpringLayout.EAST, lightsOff);
		settings.add(lightsGreen);
		
		lightsRed = new JButton("Red");
		springLayout.putConstraint(SpringLayout.NORTH, lightsRed, 6, SpringLayout.SOUTH, lightsGreen);
		springLayout.putConstraint(SpringLayout.WEST, lightsRed, 0, SpringLayout.WEST, lightsGreen);
		springLayout.putConstraint(SpringLayout.EAST, lightsRed, 0, SpringLayout.EAST, lightsGreen);
		settings.add(lightsRed);
		
		JLabel lblLane = new JLabel("Start Lane:");
		springLayout.putConstraint(SpringLayout.WEST, lblLane, 0, SpringLayout.WEST, lightsOff);
		settings.add(lblLane);
		
		lane = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, lblLane, 3, SpringLayout.NORTH, lane);
		springLayout.putConstraint(SpringLayout.NORTH, lane, 6, SpringLayout.SOUTH, lightsRed);
		springLayout.putConstraint(SpringLayout.EAST, lane, 0, SpringLayout.EAST, lightsOff);
		settings.add(lane);
		
		this.add(settings);
	}
}
