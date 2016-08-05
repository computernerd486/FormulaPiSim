import java.awt.BorderLayout;

import javax.swing.JFrame;

import sim.app.panel.VideoSettings;

public class PanelTest extends JFrame {
	
	public PanelTest()
	{
		VideoSettings vs = new VideoSettings();
		
		this.getContentPane().add(vs, BorderLayout.CENTER);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PanelTest p = new PanelTest();
		p.setVisible(true);
		p.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

}
