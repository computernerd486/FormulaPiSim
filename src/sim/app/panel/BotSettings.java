/**
* Copyright 2016 Tim Pearce
**/

package sim.app.panel;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class BotSettings extends JPanel {

	public JButton bot_stop;
	public JButton bot_start;
	public JButton bot_reset;
	
	public BotSettings () {
		initialize();
	}
	
	private void initialize() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel h = new JPanel();
		h.setBackground(Color.LIGHT_GRAY);
		h.add(new JLabel("Bot Simulation", JLabel.CENTER));
		this.add(h);
		
		JPanel settings = new JPanel();
		
		SpringLayout springLayout = new SpringLayout();
		settings.setLayout(springLayout);
		
		JPanel panel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel, 6, SpringLayout.NORTH, settings);
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
		
		this.add(settings);
	}
}
