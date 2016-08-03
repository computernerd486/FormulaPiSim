import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class ViewPort extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int width, height;

	BufferedImage background;
	BufferedImage canvas;
	Graphics2D canvas_g2;
	Track track;

	
	public ViewPort() {
		this(new Track());
	}
	
	public ViewPort(Track track) {
		this.track = track;
				
		canvas = new BufferedImage(3000, 2000, BufferedImage.TYPE_INT_RGB);
		canvas_g2 = (Graphics2D) canvas.getGraphics();
		
		setupTrack();
	}
	
	public void setupTrack() {
		canvas_g2.setBackground(Color.white);
		canvas_g2.clearRect(0, 0, 3000, 2000);

		canvas_g2.setColor(Color.black);
		canvas_g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		canvas_g2.setStroke(new BasicStroke(10));

		TrackNode[] nodes = track.nodes;
		int trackSize = nodes.length;
		
		for (int i = 0; i < trackSize; i++) {
			Point2D p1 = nodes[i].p;
			Point2D p2 = nodes[(i == trackSize - 1) ? 0 : i + 1].p;
			canvas_g2.drawLine((int)(p1.x * 10), (int)(p1.y * 10), (int)(p2.x * 10), (int)(p2.y * 10));
		}
		
		canvas_g2.setColor(Color.gray);
		for (int i = 0; i < trackSize; i++) {
			Point2D p1 = nodes[i].a;
			Point2D p2 = nodes[i].p;
			canvas_g2.drawLine((int)(p1.x * 10), (int)(p1.y * 10), (int)(p2.x * 10), (int)(p2.y * 10));
		}

		
		canvas_g2.setColor(Color.lightGray);
		for (int i = 0; i < trackSize; i++) {
			Point2D p1 = nodes[i].b;
			Point2D p2 = nodes[i].p;
			canvas_g2.drawLine((int)(p1.x * 10), (int)(p1.y * 10), (int)(p2.x * 10), (int)(p2.y * 10));
		}
		

		canvas_g2.setColor(Color.red);
		for (int i = 0; i < trackSize; i++) {
			Point2D p1 = nodes[i].a;
			Point2D p2 = nodes[(i == trackSize - 1) ? 0 : i + 1].a;
			canvas_g2.drawLine((int)(p1.x * 10), (int)(p1.y * 10), (int)(p2.x * 10), (int)(p2.y * 10));
			canvas_g2.fillOval((int)(p1.x * 10) - 15, (int)(p1.y * 10) - 15, 30, 30);
		}

		canvas_g2.setColor(Color.green);
		for (int i = 0; i < trackSize; i++) {
			Point2D p1 = nodes[i].b;
			Point2D p2 = nodes[(i == trackSize - 1) ? 0 : i + 1].b;
			canvas_g2.drawLine((int)(p1.x * 10), (int)(p1.y * 10), (int)(p2.x * 10), (int)(p2.y * 10));
			canvas_g2.fillOval((int)(p1.x * 10) - 15, (int)(p1.y * 10) - 15, 30, 30);
		}
		
		canvas_g2.setColor(Color.cyan);
		for (int i = 0; i < trackSize; i++) {
			Point2D p = nodes[i].p;
			canvas_g2.fillOval((int)(p.x * 10) - 20, (int)(p.y * 10) - 20, 40, 40);
		}
		
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		setupTrack();

		height = this.getHeight();
		width = this.getWidth();
		
		AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(1, -1));
        at.concatenate(AffineTransform.getTranslateInstance(0, -height));

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.transform(at);
		g2.drawImage(canvas, 0, 0, width, height, this);

	}

}
