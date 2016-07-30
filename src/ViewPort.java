import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class ViewPort extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int width, height;
	
	BufferedImage canvas;
	Graphics2D canvas_g2;
	
	public ViewPort()
	{
		canvas = new BufferedImage(3000, 2000, BufferedImage.TYPE_INT_RGB);
		canvas_g2 = (Graphics2D)canvas.getGraphics();
		
		setupTrack();
	}
	
	private void setupTrack()
	{
		canvas_g2.setBackground(Color.white);
		canvas_g2.clearRect(0, 0, 3000, 2000);
				
		canvas_g2.setColor(Color.black);
		canvas_g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		canvas_g2.setStroke(new BasicStroke(10));
		
	    TrackNode[] nodes = MathTest.track;
	    for(int i = 0; i < nodes.length; i++ )
    	{
	    	Point2D p1 = nodes[i].p;
	    	Point2D p2 = nodes[(i == nodes.length -1) ? 0 : i + 1].p;
	    	canvas_g2.drawLine((int)p1.x * 100, (int)p1.y * 100, (int)p2.x * 100, (int)p2.y * 100);
    	}
	    
	    canvas_g2.setColor(Color.gray);
	    for(int i = 0; i < nodes.length; i++ )
    	{
	    	Point2D p1 = nodes[i].a;
	    	Point2D p2 = nodes[i].b;
	    	canvas_g2.drawLine((int)p1.x * 100, (int)p1.y * 100, (int)p2.x * 100, (int)p2.y * 100);
    	}
	    
	    /*
	    canvas_g2.setColor(Color.red);
	    for(int i = 0; i < nodes.length; i++ )
    	{
	    	Point2D p1 = nodes[i].a;
	    	Point2D p2 = nodes[(i == nodes.length -1) ? 0 : i + 1].a;
	    	canvas_g2.drawLine((int)p1.x * 100, (int)p1.y * 100, (int)p2.x * 100, (int)p2.y * 100);
    	}
	    
	    canvas_g2.setColor(Color.green);
	    for(int i = 0; i < nodes.length; i++ )
    	{
	    	Point2D p1 = nodes[i].b;
	    	Point2D p2 = nodes[(i == nodes.length -1) ? 0 : i + 1].b;
	    	canvas_g2.drawLine((int)p1.x * 100, (int)p1.y * 100, (int)p2.x * 100, (int)p2.y * 100);
    	}
    	*/
	}
	
	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    
	    height = this.getHeight();
	    width = this.getWidth();
	    
	    Graphics2D g2 = (Graphics2D) g;	    
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	   
	    g2.drawImage(canvas, 0, 0, width, height, this);

	    
	  }

}
