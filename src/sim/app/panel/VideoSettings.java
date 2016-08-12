package sim.app.panel;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
	
	private JTextField resX;
	private JTextField resY;
	private JTextField port;
	
	public VideoSettings() {	
		
		//this.setPreferredSize(new Dimension(200, 300));
		initialize();
		
	}
	
	private void initialize() {
		//frame = new JFrame();
		//frame.setBounds(100, 100, 191, 300);
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		this.setLayout(springLayout);
		
		JLabel lblResolution = new JLabel("Resolution:");
		this.add(lblResolution);
		
		resX = new JTextField();
		resX.setText("640");
		springLayout.putConstraint(SpringLayout.NORTH, lblResolution, 3, SpringLayout.NORTH, resX);
		springLayout.putConstraint(SpringLayout.NORTH, resX, 7, SpringLayout.NORTH, this);
		this.add(resX);
		resX.setColumns(3);
		
		JLabel lblX = new JLabel("x");
		springLayout.putConstraint(SpringLayout.NORTH, lblX, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, resX, -6, SpringLayout.WEST, lblX);
		this.add(lblX);
		
		resY = new JTextField();
		resY.setText("480");
		springLayout.putConstraint(SpringLayout.NORTH, resY, 7, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, lblX, -6, SpringLayout.WEST, resY);
		springLayout.putConstraint(SpringLayout.EAST, resY, -10, SpringLayout.EAST, this);
		this.add(resY);
		resY.setColumns(3);
		
		JLabel lblPort = new JLabel("Port:");
		springLayout.putConstraint(SpringLayout.WEST, lblResolution, 0, SpringLayout.WEST, lblPort);
		springLayout.putConstraint(SpringLayout.WEST, lblPort, 10, SpringLayout.WEST, this);
		this.add(lblPort);
		
		port = new JTextField();
		port.setText("10000");
		springLayout.putConstraint(SpringLayout.NORTH, lblPort, 3, SpringLayout.NORTH, port);
		springLayout.putConstraint(SpringLayout.NORTH, port, 6, SpringLayout.SOUTH, resX);
		springLayout.putConstraint(SpringLayout.EAST, port, -10, SpringLayout.EAST, this);
		this.add(port);
		port.setColumns(5);
		
		JLabel lblStatus = new JLabel("Status:");
		springLayout.putConstraint(SpringLayout.WEST, lblStatus, 0, SpringLayout.WEST, lblResolution);
		this.add(lblStatus);
		
		JLabel lblRunning = new JLabel("Running");
		springLayout.putConstraint(SpringLayout.NORTH, lblStatus, 0, SpringLayout.NORTH, lblRunning);
		springLayout.putConstraint(SpringLayout.NORTH, lblRunning, 6, SpringLayout.SOUTH, port);
		springLayout.putConstraint(SpringLayout.EAST, lblRunning, 0, SpringLayout.EAST, resY);
		this.add(lblRunning);
				
		JPanel panel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel, 6, SpringLayout.SOUTH, lblStatus);
		springLayout.putConstraint(SpringLayout.WEST, panel, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, panel, -10, SpringLayout.EAST, this);
		this.add(panel);
		panel.setLayout(new GridLayout(0, 2, 10, 0));
		
		JButton btnStart = new JButton("Start");
		panel.add(btnStart);
		
		JButton btnStop = new JButton("Stop");
		panel.add(btnStop);
	}
	
}
