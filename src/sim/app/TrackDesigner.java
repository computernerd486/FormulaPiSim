/**
* Copyright 2016 Tim Pearce
**/

package sim.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import sim.object.*;
import sim.app.panel.*;

public class TrackDesigner extends JFrame {

	private static final long serialVersionUID = 1L;
	
	TrackDesigner designer;
	Track track;
	int width = 1024, height = 600;
	
	ViewPort view;
	JPanel controlPanel;
	

	public TrackDesigner() {
		
		track = new Track();
		
		this.setSize(width, height);
		view = new ViewPort(track);
		view.setPreferredSize(new Dimension(800, height));
		this.getContentPane().add(view, BorderLayout.CENTER);

		controlPanel = new JPanel();
		controlPanel.setPreferredSize(new Dimension((int) (width - view.getPreferredSize().getWidth()), height));
		this.getContentPane().add(controlPanel, BorderLayout.LINE_END);

		setupControlPanel();
		designer = this;
	}
	
	private void setupControlPanel() {
		
		JPanel fileControls = new JPanel();
		JButton btnSave = new JButton("Save");
		JButton btnLoad = new JButton("Load");
		
		new BoxLayout( fileControls, BoxLayout.LINE_AXIS );

		fileControls.add(btnLoad);
		fileControls.add(btnSave);
		
		controlPanel.add(fileControls);
		
		
		btnSave.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				track.save("test.trk");
			}
		});
		
		btnLoad.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				track.load("final.trk");
				view.setupTrack();
				designer.getContentPane().repaint();
			}
		});
	}
	
	
	
	public static void main(String[] args) {

		TrackDesigner w = new TrackDesigner();
		w.setVisible(true);
		w.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
	}

}
