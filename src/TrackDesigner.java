import javax.swing.JFrame;

public class TrackDesigner extends JFrame {

	private static final long serialVersionUID = 1L;

	public TrackDesigner()
	{
		this.setSize(800, 600);
		this.add(new ViewPort());
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TrackDesigner w = new TrackDesigner();
		w.setVisible(true);
		w.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
	}

}
