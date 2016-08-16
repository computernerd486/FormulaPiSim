/**
* Copyright 2016 Tim Pearce
**/

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import sim.app.panel.VideoSettings;

public class PanelTest extends JFrame {
	
	public PanelTest()
	{
		VideoSettings vs = new VideoSettings();
		
		this.getContentPane().setPreferredSize(new Dimension(200, 500));
		this.getContentPane().add(vs, BorderLayout.CENTER);
		
		this.pack();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PanelTest p = new PanelTest();
		p.setVisible(true);
		p.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

}
