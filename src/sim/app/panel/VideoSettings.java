/**
* Copyright 2016 Tim Pearce
**/

package sim.app.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class VideoSettings extends JPanel {

	/*
	 * Video Settings:
	 * 
	 *  Resolution: XXX x YYY
	 *  Server Port:    PPPPP
	 * 
	 *  Status:       Running
	 * 
	 *  | Start |   | Stop |
	 */
	
	public JTextField resX;
	public JTextField resY;
	public JTextField port;
	
	public JButton server_stop;
	public JButton server_start;
	
	public JLabel runningStatus; 
	public JCheckBox flipVideo;
	public JSlider lightness;
	
	public JRadioButton formatPNG;
	public JRadioButton formatJPG;
	
	public VideoSettings() {	

		initialize();
		
	}
	
	private void initialize() {
		//frame = new JFrame();
		//frame.setBounds(100, 100, 191, 300);
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel h = new JPanel();
		h.setBackground(Color.LIGHT_GRAY);
		h.add(new JLabel("Video Stream Settings", JLabel.CENTER));
		this.add(h);
		
		JPanel settings = new JPanel();
		
		SpringLayout springLayout = new SpringLayout();
		settings.setLayout(springLayout);
		
		JLabel lblResolution = new JLabel("Resolution:");
		settings.add(lblResolution);
		
		resX = new JTextField();
		resX.setText("640");
		springLayout.putConstraint(SpringLayout.NORTH, lblResolution, 3, SpringLayout.NORTH, resX);
		springLayout.putConstraint(SpringLayout.NORTH, resX, 7, SpringLayout.NORTH, settings);
		settings.add(resX);
		resX.setColumns(3);
		
		JLabel lblX = new JLabel("x");
		springLayout.putConstraint(SpringLayout.NORTH, lblX, 10, SpringLayout.NORTH, settings);
		springLayout.putConstraint(SpringLayout.EAST, resX, -6, SpringLayout.WEST, lblX);
		settings.add(lblX);
		
		resY = new JTextField();
		resY.setText("480");
		springLayout.putConstraint(SpringLayout.NORTH, resY, 7, SpringLayout.NORTH, settings);
		springLayout.putConstraint(SpringLayout.EAST, lblX, -6, SpringLayout.WEST, resY);
		springLayout.putConstraint(SpringLayout.EAST, resY, -10, SpringLayout.EAST, settings);
		settings.add(resY);
		resY.setColumns(3);

		JLabel lblPort = new JLabel("Port:");
		springLayout.putConstraint(SpringLayout.WEST, lblResolution, 0, SpringLayout.WEST, lblPort);
		springLayout.putConstraint(SpringLayout.WEST, lblPort, 10, SpringLayout.WEST, settings);
		settings.add(lblPort);
		
		port = new JTextField();
		port.setText("10000");
		springLayout.putConstraint(SpringLayout.NORTH, lblPort, 3, SpringLayout.NORTH, port);
		springLayout.putConstraint(SpringLayout.NORTH, port, 6, SpringLayout.SOUTH, resX);
		springLayout.putConstraint(SpringLayout.EAST, port, -10, SpringLayout.EAST, settings);
		settings.add(port);
		port.setColumns(5);
		
		JLabel lblStatus = new JLabel("Status:");
		springLayout.putConstraint(SpringLayout.WEST, lblStatus, 0, SpringLayout.WEST, lblResolution);
		settings.add(lblStatus);
		
		runningStatus = new JLabel("Stopped");
		springLayout.putConstraint(SpringLayout.NORTH, lblStatus, 0, SpringLayout.NORTH, runningStatus);
		springLayout.putConstraint(SpringLayout.NORTH, runningStatus, 6, SpringLayout.SOUTH, port);
		springLayout.putConstraint(SpringLayout.EAST, runningStatus, 0, SpringLayout.EAST, resY);
		settings.add(runningStatus);
		
		flipVideo = new JCheckBox("Flip Bot");
		springLayout.putConstraint(SpringLayout.NORTH, flipVideo, 6, SpringLayout.SOUTH, lblStatus);
		springLayout.putConstraint(SpringLayout.WEST, flipVideo, 10, SpringLayout.WEST, settings);
		springLayout.putConstraint(SpringLayout.EAST, flipVideo, -10, SpringLayout.EAST, settings);
		settings.add(flipVideo);
		
		JPanel outputPanel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, outputPanel, 6, SpringLayout.SOUTH, flipVideo);
		springLayout.putConstraint(SpringLayout.WEST, outputPanel, 10, SpringLayout.WEST, settings);
		springLayout.putConstraint(SpringLayout.EAST, outputPanel, -10, SpringLayout.EAST, settings);
		settings.add(outputPanel);
		outputPanel.setLayout(new GridLayout(0, 2, 10, 0));
		
		formatPNG = new JRadioButton("PNG");
		formatPNG.setActionCommand("PNG");
		formatPNG.setSelected(true);
		outputPanel.add(formatPNG);
		
		formatJPG = new JRadioButton("JPG");
		formatJPG.setActionCommand("JPG");
		formatJPG.setSelected(false);
		outputPanel.add(formatJPG);
		
		ButtonGroup group = new ButtonGroup();
		group.add(formatPNG);
		group.add(formatJPG);
		
		/**
		lightness = new JSlider(-5, 5, 0);
		springLayout.putConstraint(SpringLayout.NORTH, lightness, 6, SpringLayout.SOUTH, flipVideo);
		springLayout.putConstraint(SpringLayout.WEST, lightness, 10, SpringLayout.WEST, settings);
		springLayout.putConstraint(SpringLayout.EAST, lightness, -10, SpringLayout.EAST, settings);
		settings.add(lightness);
		*/
			
		JPanel panel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel, 6, SpringLayout.SOUTH, outputPanel);
		springLayout.putConstraint(SpringLayout.WEST, panel, 10, SpringLayout.WEST, settings);
		springLayout.putConstraint(SpringLayout.EAST, panel, -10, SpringLayout.EAST, settings);
		settings.add(panel);
		panel.setLayout(new GridLayout(0, 2, 10, 0));
		
		server_start = new JButton("Start");
		panel.add(server_start);
		
		server_stop = new JButton("Stop");
		panel.add(server_stop);
		
		springLayout.putConstraint(SpringLayout.SOUTH, settings, 6, SpringLayout.SOUTH, panel);
		
		this.add(settings);
		
		this.setSize(this.getPreferredSize());
		this.doLayout();
	}
	
}
