/**
* Copyright 2016 Tim Pearce
**/

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import sim.app.panel.BotSettings;
import sim.app.panel.VideoSettings;

public class PanelTest extends JFrame {
	
	public PanelTest()
	{
	
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(200, 500));
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		//SpringLayout springLayout = new SpringLayout();
		//p.setLayout(springLayout);
		this.getContentPane().add(p, BorderLayout.EAST);
	
		VideoSettings vs = new VideoSettings();
		//springLayout.putConstraint(SpringLayout.WEST, vs, 0, SpringLayout.WEST, p);
		//springLayout.putConstraint(SpringLayout.EAST, vs, 0, SpringLayout.EAST, p);
		p.add(vs);
		
		
		BotSettings bs = new BotSettings();
		//springLayout.putConstraint(SpringLayout.NORTH, bs, 100, SpringLayout.SOUTH, vs);
		//springLayout.putConstraint(SpringLayout.WEST, bs, 0, SpringLayout.WEST, p);
		//springLayout.putConstraint(SpringLayout.EAST, bs, 0, SpringLayout.EAST, p);
		p.add(bs);
		
		this.getContentPane().add(p, BorderLayout.CENTER);
		this.pack();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PanelTest p = new PanelTest();
		p.setVisible(true);
		p.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

}
