
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Label;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class TrackView extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	GLCanvas glcanvas;
	GLU glu;
	
	int width = 1280, height = 800;
	int view_width_overhead = 800, view_height_overhead = 600;
	int view_width_firstperson = 400, view_height_firstperson = 300;
	
	Texture tex_trackRoad;
	Texture tex_bot;
	
	public TrackView() {
	
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
		glu = new GLU();


		this.setSize(width, height);
		this.getContentPane().add(glcanvas, BorderLayout.CENTER);
		
		JPanel p = new JPanel();
		p.add(new Label("Overview:"));
		p.setPreferredSize(new Dimension(view_width_firstperson, 100));
		
		this.getContentPane().add(p, BorderLayout.EAST);

		
		//DrawTest(null);
		//this.add(new ViewPort());
	}
	
	public void loadTextures(GLAutoDrawable glautodrawable)
	{
		GL2 gl2 = glautodrawable.getGL().getGL2();
		gl2.glEnable(GL2.GL_CULL_FACE);
		gl2.glCullFace(GL2.GL_BACK);
		gl2.glFrontFace(GL2.GL_CW);
		
		 try {
			 Texture t;
			 t = TextureIO.newTexture(this.getClass().getResource("track.png"), false, ".png");
			 t.setTexParameterf(gl2, gl2.GL_TEXTURE_MIN_FILTER, gl2.GL_LINEAR);
			 t.setTexParameterf(gl2, gl2.GL_TEXTURE_MAG_FILTER, gl2.GL_LINEAR);
			 t.setTexParameterf(gl2, gl2.GL_TEXTURE_WRAP_S, gl2.GL_CLAMP_TO_EDGE);
			 t.setTexParameterf(gl2, gl2.GL_TEXTURE_WRAP_T, gl2.GL_CLAMP_TO_EDGE);
			 
			 tex_trackRoad = t;
			 
		 } catch (Exception e) {
			 e.printStackTrace(System.err);
		 }
	}
	
	
	public void draw(GLAutoDrawable glautodrawable){
		
		GL2 gl2 = glautodrawable.getGL().getGL2();
		gl2.glLineWidth(2);		
		
		//Draw the overhead view
		{
			gl2.glViewport(glcanvas.getWidth() - view_width_overhead, 0, view_width_overhead, view_height_overhead);
			gl2.glMatrixMode(GL2.GL_PROJECTION);
			gl2.glLoadIdentity();
			gl2.glOrtho(0, 30, 0, 20, -1, 1);
			
			drawTrack(glautodrawable);
		}
		
		//Draw the First Person/Robot View
		{
			gl2.glViewport(0, height - view_height_firstperson, view_width_firstperson, view_height_firstperson);
			gl2.glLoadIdentity();
			
			TrackNode start = MathTest.track[0];
			
			glu.gluPerspective( 45.0, view_width_firstperson/view_height_firstperson, 0.1f, 500.0 );
			glu.gluLookAt(start.p.x, start.p.y, 1d, start.p.x - 5, start.p.y, 1d, 0d, 0d, 1d);
			
			drawFirstPerson(glautodrawable);
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
		
		gl2.glBegin(GL2.GL_QUAD_STRIP);
		gl2.glColor3f(1f, 1f, 1f);
		for (int i = 0; i < trackSize; i++) {
			Point2D p1 = nodes[i].a;
			Point2D p2 = nodes[i].b;
			gl2.glTexCoord2d(1,0);
			gl2.glVertex2d(p1.x, p1.y);
			gl2.glTexCoord2d(0,0);
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
		
		
		gl2.glBegin(GL.GL_LINE_LOOP);
		gl2.glColor3f(0f, 1f, 0f);
		for (int i = 0; i < trackSize; i++) {
			Point2D p1 = nodes[i].p;
			//Point2D p2 = nodes[(i == trackSize - 1) ? 0 : i + 1].p;
			gl2.glVertex2d(p1.x, p1.y);
		}
		gl2.glEnd();
		 
		//glautodrawable.swapBuffers();

	}
	
	public void drawFirstPerson(GLAutoDrawable glautodrawable)
	{
		GL2 gl2 = glautodrawable.getGL().getGL2();
		//gl2.glTranslatef(-(float)start.p.x, -(float)start.p.y, 1f);
		drawTrack(glautodrawable);
	
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TrackView w = new TrackView();
		w.setVisible(true);
		w.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
	}

}
