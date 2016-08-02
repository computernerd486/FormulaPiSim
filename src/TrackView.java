
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class TrackView extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	GLCanvas glcanvas;
	GLU glu;
	
	int width = 1280, height = 800;
	int view_width_overhead = 800, view_height_overhead = 600;
	int view_width_firstperson = 400, view_height_firstperson = 300;
	
	//Texture Section, this should be in a loader
	Texture tex_trackRoad;
	Texture tex_bot;

	//Bot
	public Bot bot;
	BotUpdater botUpdater;
	
	//This is for output
	public BufferedImage botView;
	
	public TrackView() {
	
		botView = new BufferedImage(view_width_firstperson, view_height_firstperson, BufferedImage.TYPE_INT_RGB);
		
		GLCapabilities capabilities = new GLCapabilities(GLProfile.getDefault());
		glcanvas = new GLCanvas(capabilities);
		glcanvas.addGLEventListener(new GLEventListener() {
			
			@Override
			public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {}
			
			@Override
			public void init(GLAutoDrawable glautodrawable ) {
				loadTextures(glautodrawable);
			}
			
			@Override
			public void dispose(GLAutoDrawable glautodrawable ) {}
			
			@Override
			public void display(GLAutoDrawable glautodrawable ) {
				 draw(glautodrawable);
			}
		});
		
		Animator anim = new Animator(glcanvas);
		anim.start();
		
		glu = new GLU();


		this.setSize(width, height);
		this.getContentPane().add(glcanvas, BorderLayout.CENTER);
		
		JPanel p = new JPanel();
		p.add(new JLabel("Overview:"));
		p.setPreferredSize(new Dimension(view_width_firstperson, 100));
		{
			SpinnerNumberModel direction = new SpinnerNumberModel(180, 0, 360, 5);
			JSpinner directionSpinner = new JSpinner(direction);
					
			directionSpinner.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					// TODO Auto-generated method stub
					bot.setDirection((int)directionSpinner.getValue());
				}
			});
			p.add(directionSpinner);
		}
		
		{
			SpinnerNumberModel direction = new SpinnerNumberModel(.3, .05, 1, .05);
			JSpinner heightSpinner = new JSpinner(direction);
					
			heightSpinner.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					// TODO Auto-generated method stub
					bot.height = (float)(double)heightSpinner.getValue();
				}
			});
			p.add(heightSpinner);
		}
		
		{
			SpinnerNumberModel direction = new SpinnerNumberModel(0, 0, 100, 1);
			JSpinner speedSpinner = new JSpinner(direction);
					
			speedSpinner.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					// TODO Auto-generated method stub
					bot.p_m1 = ((float)(int)speedSpinner.getValue()) / 100;
					bot.p_m1 = ((float)(int)speedSpinner.getValue()) / 100;
				}
			});
			p.add(speedSpinner);
		}

		this.getContentPane().add(p, BorderLayout.EAST);

		TrackNode start = MathTest.track[0];
		
		bot = new Bot(new Point2D(start.p.x, start.p.y), 180f);
		botUpdater = new BotUpdater(bot);
		
		botUpdater.start();
	}
	
	public void loadTextures(GLAutoDrawable glautodrawable)
	{
		GL2 gl2 = glautodrawable.getGL().getGL2();
		gl2.glEnable(GL2.GL_CULL_FACE);
		gl2.glCullFace(GL2.GL_BACK);
		gl2.glFrontFace(GL2.GL_CW);
		
		 try {
			 Texture t;
			 t = TextureIO.newTexture(this.getClass().getResource("track_v2.png"), false, ".png");
			 t.setTexParameterf(gl2, gl2.GL_TEXTURE_MIN_FILTER, gl2.GL_LINEAR);
			 t.setTexParameterf(gl2, gl2.GL_TEXTURE_MAG_FILTER, gl2.GL_LINEAR);
			 t.setTexParameterf(gl2, gl2.GL_TEXTURE_WRAP_S, gl2.GL_REPEAT);
			 t.setTexParameterf(gl2, gl2.GL_TEXTURE_WRAP_T, gl2.GL_REPEAT);
			 tex_trackRoad = t;
			 
			 t = TextureIO.newTexture(this.getClass().getResource("bot.png"), false, ".png");
			 t.setTexParameterf(gl2, gl2.GL_TEXTURE_MIN_FILTER, gl2.GL_LINEAR);
			 t.setTexParameterf(gl2, gl2.GL_TEXTURE_MAG_FILTER, gl2.GL_LINEAR);
			 t.setTexParameterf(gl2, gl2.GL_TEXTURE_WRAP_S, gl2.GL_CLAMP_TO_EDGE);
			 t.setTexParameterf(gl2, gl2.GL_TEXTURE_WRAP_T, gl2.GL_CLAMP_TO_EDGE);
			 tex_bot = t;
			 
			 
		 } catch (Exception e) {
			 e.printStackTrace(System.err);
		 }
	}
	
	
	public void draw(GLAutoDrawable glautodrawable){
		
		GL2 gl2 = glautodrawable.getGL().getGL2();
		gl2.glLineWidth(2);		
		gl2.glEnable(GL2.GL_SCISSOR_TEST);
		
		//Draw the overhead view
		{
			gl2.glViewport(glcanvas.getWidth() - view_width_overhead, 0, view_width_overhead, view_height_overhead);
			gl2.glScissor(glcanvas.getWidth() - view_width_overhead, 0, view_width_overhead, view_height_overhead);
			gl2.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
			
			gl2.glMatrixMode(GL2.GL_PROJECTION);
			gl2.glLoadIdentity();
			gl2.glOrtho(0, 30, 0, 20, -1, 1);
			
			drawTrack(glautodrawable);
			drawBot(glautodrawable);
		}
		
		//Draw the First Person/Robot View
		{
			gl2.glViewport(0, height - view_height_firstperson, view_width_firstperson, view_height_firstperson);
			gl2.glScissor(0, height - view_height_firstperson, view_width_firstperson, view_height_firstperson);
			
			gl2.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
			gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
			
			gl2.glLoadIdentity();

			
			glu.gluPerspective( 45.0, view_width_firstperson/view_height_firstperson, 0.1f, 500.0 );
			glu.gluLookAt(
					bot.position.x, bot.position.y, bot.height, 
					bot.focus.x, bot.focus.y, bot.height, 
					0d, 0d, 1d);
			
			drawFirstPerson(glautodrawable);
			
			
			{
				Graphics graphics = botView.getGraphics();
				ByteBuffer buffer = GLBuffers.newDirectByteBuffer(view_width_firstperson * view_height_firstperson * 3);				
				gl2.glReadPixels(0, height - view_height_firstperson, view_width_firstperson, view_height_firstperson, GL2.GL_RGB, GL2.GL_BYTE, buffer);
				
				
				for (int h = 0; h < view_height_firstperson; h++) {
	                for (int w = 0; w < view_width_firstperson; w++) {
	                    graphics.setColor(new Color((buffer.get()*2), (buffer.get()*2), (buffer.get()*2)));
	                    graphics.drawRect(w, h, 1, 1);
	                }
	            }
			}
		}
	}
	
	public void drawTrack(GLAutoDrawable glautodrawable)
	{
		GL2 gl2 = glautodrawable.getGL().getGL2();
		
		TrackNode[] nodes = MathTest.track;
		int trackSize = nodes.length;
		
		if (tex_trackRoad != null)
		{
			gl2.glActiveTexture(GL2.GL_TEXTURE0);
			tex_trackRoad.enable(gl2);
			tex_trackRoad.bind(gl2);
		}
		
		//track surface
		gl2.glBegin(GL2.GL_QUAD_STRIP);
		gl2.glColor3f(1f, 1f, 1f);
		for (int i = 0; i < trackSize; i++) {
			Point2D p1 = nodes[i].a;
			Point2D p2 = nodes[i].b;
			if (i % 2 == 0) { gl2.glTexCoord2d(1,0); } else { gl2.glTexCoord2d(1,1); } 
			gl2.glVertex2d(p1.x, p1.y);
			if (i % 2 == 0) { gl2.glTexCoord2d(0,0); } else { gl2.glTexCoord2d(0,1); }
			gl2.glVertex2d(p2.x, p2.y);
		}
		{
			Point2D p1 = nodes[0].a;
			Point2D p2 = nodes[0].b;
			gl2.glTexCoord2d(1,0);
			gl2.glVertex2d(p1.x, p1.y);
			gl2.glTexCoord2d(0,0);
			gl2.glVertex2d(p2.x, p2.y);
		}
		gl2.glEnd();
		
		if (tex_trackRoad != null)
			tex_trackRoad.disable(gl2);
		
		gl2.glColor3f(.9f, .9f, .9f);
		
		//walls inside
		gl2.glBegin(GL2.GL_QUAD_STRIP);
		gl2.glColor3f(.2f, .2f, .2f);
		for (int i = 0; i < trackSize; i++) {
			Point2D p1 = nodes[i].a;
			gl2.glTexCoord2d(1,0);
			gl2.glVertex3d(p1.x, p1.y, .5f);
			gl2.glTexCoord2d(0,0);
			gl2.glVertex2d(p1.x, p1.y);
		}
		{
			Point2D p1 = nodes[0].a;
			gl2.glTexCoord2d(1,0);
			gl2.glVertex3d(p1.x, p1.y, .5f);
			gl2.glTexCoord2d(0,0);
			gl2.glVertex2d(p1.x, p1.y);
		}
		gl2.glEnd();
		
		//walls outside
		gl2.glBegin(GL2.GL_QUAD_STRIP);
		gl2.glColor3f(.2f, .2f, .2f);
		for (int i = 0; i < trackSize; i++) {
			Point2D p1 = nodes[i].b;
			gl2.glTexCoord2d(1,0);
			gl2.glVertex2d(p1.x, p1.y);
			gl2.glTexCoord2d(0,0);
			gl2.glVertex3d(p1.x, p1.y, .5f);
		}
		{
			Point2D p1 = nodes[0].b;
			gl2.glTexCoord2d(1,0);
			gl2.glVertex2d(p1.x, p1.y);
			gl2.glTexCoord2d(0,0);
			gl2.glVertex3d(p1.x, p1.y, .5f);
		}
		gl2.glEnd();
		
	}
	
	public void drawBot(GLAutoDrawable glautodrawable)
	{
		GL2 gl2 = glautodrawable.getGL().getGL2();
		
		if (tex_bot != null)
		{
			gl2.glActiveTexture(GL2.GL_TEXTURE0);
			gl2.glEnable(GL2.GL_BLEND);
			gl2.glBlendFunc(GL2.GL_ONE, gl2.GL_ONE_MINUS_SRC_ALPHA);
			tex_bot.enable(gl2);
			tex_bot.bind(gl2);
			gl2.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE); 
		}
		
		//move to bot class
		float width = 1f, height = .75f;
		
		gl2.glPushMatrix();
		gl2.glTranslatef((float)bot.position.x, (float)bot.position.y, 0f);
		gl2.glRotatef(bot.direction, 0f, 0f, 1f);
		
		gl2.glBegin(GL2.GL_QUADS);
		{
			gl2.glTexCoord2d(1,1);
			gl2.glVertex2d(-(width / 2), -(height/2));
			gl2.glTexCoord2d(1,0);
			gl2.glVertex2d(-(width / 2),  (height/2));
			gl2.glTexCoord2d(0,0);
			gl2.glVertex2d( (width / 2),  (height/2));
			gl2.glTexCoord2d(0,1);
			gl2.glVertex2d( (width / 2), -(height/2));
		}
		gl2.glEnd();
		gl2.glTranslatef(-(float)bot.position.x, -(float)bot.position.y, 0f);
		gl2.glPopMatrix();
		
		if (tex_bot != null)
			tex_bot.disable(gl2);
	}
	
	public void drawFirstPerson(GLAutoDrawable glautodrawable)
	{
		GL2 gl2 = glautodrawable.getGL().getGL2();
		drawTrack(glautodrawable);
	
	}
	
	public static void main(String[] args) {

		TrackView w = new TrackView();
		w.setVisible(true);
		w.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
	}

}
