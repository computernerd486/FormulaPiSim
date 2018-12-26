/**
* Copyright 2016 Tim Pearce
**/

package sim.app.panel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import sim.object.*;
import sim.util.*;

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
				
		canvas = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_RGB);
		canvas_g2 = (Graphics2D) canvas.getGraphics();
		
		try {
			background = ImageIO.read(Paths.get("img/design_track_color.png").toFile());
		} catch ( Exception e) { System.err.println("Unable to load background");}
		
		
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println(e.getX() + "," + (height - e.getY()));
				
			}
		});
		
				
		setupTrack();
	}
	
	public void setupTrack() {
		
		canvas_g2.setBackground(Color.white);
		canvas_g2.clearRect(0, 0, 1200, 800);

		canvas_g2.setColor(Color.black);
		canvas_g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		canvas_g2.setStroke(new BasicStroke(2));
		
		canvas_g2.drawImage(background, 0, 0, this);

		TrackNode[] nodes = track.nodes;
		int trackSize = nodes.length;
		
		for (int i = 0; i < trackSize; i++) {
			Point2D p1 = nodes[i].p;
			Point2D p2 = nodes[(i == trackSize - 1) ? 0 : i + 1].p;
			canvas_g2.drawLine((int)(p1.x), (int)(p1.y), (int)(p2.x), (int)(p2.y));
		}
		
		canvas_g2.setColor(Color.gray);
		for (int i = 0; i < trackSize; i++) {
			Point2D p1 = nodes[i].a;
			Point2D p2 = nodes[i].p;
			canvas_g2.drawLine((int)(p1.x), (int)(p1.y), (int)(p2.x), (int)(p2.y));
		}

		
		canvas_g2.setColor(Color.lightGray);
		for (int i = 0; i < trackSize; i++) {
			Point2D p1 = nodes[i].b;
			Point2D p2 = nodes[i].p;
			canvas_g2.drawLine((int)(p1.x), (int)(p1.y), (int)(p2.x), (int)(p2.y));
		}
		

		canvas_g2.setColor(Color.red);
		for (int i = 0; i < trackSize; i++) {
			Point2D p1 = nodes[i].a;
			Point2D p2 = nodes[(i == trackSize - 1) ? 0 : i + 1].a;
			canvas_g2.drawLine((int)(p1.x), (int)(p1.y), (int)(p2.x), (int)(p2.y));
			canvas_g2.fillOval((int)(p1.x) - 5, (int)(p1.y) - 5, 10, 10);
		}

		canvas_g2.setColor(Color.green);
		for (int i = 0; i < trackSize; i++) {
			Point2D p1 = nodes[i].b;
			Point2D p2 = nodes[(i == trackSize - 1) ? 0 : i + 1].b;
			canvas_g2.drawLine((int)(p1.x), (int)(p1.y), (int)(p2.x), (int)(p2.y));
			canvas_g2.fillOval((int)(p1.x) - 5, (int)(p1.y) - 5, 10, 10);
		}
		
		canvas_g2.setColor(Color.cyan);
		for (int i = 0; i < trackSize; i++) {
			Point2D p = nodes[i].p;
			canvas_g2.fillOval((int)(p.x) - 8, (int)(p.y) - 8, 16, 16);
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
		//g2.drawImage(canvas, 0, 0, width, height, this);
		g2.drawImage(canvas, 0, 0, this);

	}

}
