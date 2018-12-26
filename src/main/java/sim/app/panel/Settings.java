package sim.app.panel;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.GridLayout;

public class Settings {

	private JFrame frame;
	private JTextField resX;
	private JTextField resY;
	private JTextField port;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Settings window = new Settings();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Settings() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 191, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		frame.getContentPane().setLayout(springLayout);

		JLabel lblResolution = new JLabel("Resolution:");
		frame.getContentPane().add(lblResolution);
		
		resX = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, lblResolution, 3, SpringLayout.NORTH, resX);
		resX.setText("640");
		springLayout.putConstraint(SpringLayout.NORTH, resX, 7, SpringLayout.NORTH, frame.getContentPane());
		frame.getContentPane().add(resX);
		resX.setColumns(3);
		
		JLabel lblX = new JLabel("x");
		springLayout.putConstraint(SpringLayout.NORTH, lblX, 10, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, resX, -6, SpringLayout.WEST, lblX);
		frame.getContentPane().add(lblX);
		
		resY = new JTextField();
		resY.setText("480");
		springLayout.putConstraint(SpringLayout.NORTH, resY, 7, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, lblX, -6, SpringLayout.WEST, resY);
		springLayout.putConstraint(SpringLayout.EAST, resY, -10, SpringLayout.EAST, frame.getContentPane());
		frame.getContentPane().add(resY);
		resY.setColumns(3);
		
		JLabel lblPort = new JLabel("Port:");
		springLayout.putConstraint(SpringLayout.WEST, lblPort, 10, SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, lblResolution, 0, SpringLayout.WEST, lblPort);
		frame.getContentPane().add(lblPort);
		
		port = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, lblPort, 3, SpringLayout.NORTH, port);
		port.setText("10000");
		springLayout.putConstraint(SpringLayout.NORTH, port, 6, SpringLayout.SOUTH, resX);
		springLayout.putConstraint(SpringLayout.EAST, port, -10, SpringLayout.EAST, frame.getContentPane());
		frame.getContentPane().add(port);
		port.setColumns(5);
		
		JLabel lblStatus = new JLabel("Status:");
		springLayout.putConstraint(SpringLayout.WEST, lblStatus, 10, SpringLayout.WEST, frame.getContentPane());
		frame.getContentPane().add(lblStatus);
		
		JLabel lblRunning = new JLabel("Running");
		springLayout.putConstraint(SpringLayout.NORTH, lblStatus, 0, SpringLayout.NORTH, lblRunning);
		springLayout.putConstraint(SpringLayout.NORTH, lblRunning, 6, SpringLayout.SOUTH, port);
		springLayout.putConstraint(SpringLayout.EAST, lblRunning, 0, SpringLayout.EAST, resY);
		frame.getContentPane().add(lblRunning);
		
		JPanel panel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel, 6, SpringLayout.SOUTH, lblStatus);
		springLayout.putConstraint(SpringLayout.WEST, panel, 10, SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panel, -10, SpringLayout.EAST, frame.getContentPane());
		frame.getContentPane().add(panel);
		panel.setLayout(new GridLayout(0, 2, 10, 0));
		
		JButton btnStart = new JButton("Start");
		panel.add(btnStart);
		
		JButton btnStop = new JButton("Stop");
		panel.add(btnStop);

	}
}
