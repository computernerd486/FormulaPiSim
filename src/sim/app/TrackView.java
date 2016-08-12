package sim.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jogamp.opengl.GL;
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

import sim.app.panel.VideoSettings;
import sim.object.*;
import sim.util.*;
import sim.util.io.stream.*;

/**
 * TrackView
 * 
 * This is the heavy lifting class of this project, it is an extension of a JFrame
 * which has an opengl displays and a JPanel area for settings.
 * 
 * The OpenGL output is making use of JOGL bindings, and displays two views.
 * One is an overhead view of the track, the other is the first person perspective
 * from the bot.
 * 
 * @author Tim Pearce
 *
 */
public class TrackView extends JFrame implements WindowListener, GLEventListener{
	
	private static final long serialVersionUID = 1L;
	
	//OpenGL objects
	Animator anim;
	GLCanvas glcanvas;
	GLU glu;
	
	//Easy access size definitions for the main breaks of the screen
	int width = 1280, height = 800;
	int settings_width = 200;
	int view_width_overhead = 800, view_height_overhead = 600;
	int view_width_firstperson = 400, view_height_firstperson = 300;
	
	//These probably should be loaded out of a config file, but
	//hard codes win here for now.
	private static final String fn_tex_track = "img/track_v3.png";
	private static final String fn_tex_bot = "img/bot.png";
	private static final String track_default = "final.trk";
	
	//Texture Section, this should be in a loader
	Texture tex_trackRoad;
	Texture tex_bot;

	//Bot
	public Bot bot;
	BotUpdater botUpdater;
	Track track;
	
	//This is for output
	public BufferedImage botView;
	
	//Vertex and Coordinate Buffers
	FloatBuffer vertices_track;
	FloatBuffer texCoords_track;
	
	FloatBuffer vertices_innerwall;
	FloatBuffer vertices_outerwall;
	
	//Counter variable, use to control how often the fps is printed
	int framecounter = 0;
	
	//IO for video and motor control
	VideoStreamer videoStream;
	VideoSettings vs;
	
	public TrackView() {
	
		botView = new BufferedImage(view_width_firstperson, view_height_firstperson, BufferedImage.TYPE_INT_RGB);
		
		GLCapabilities capabilities = new GLCapabilities(GLProfile.getDefault());
		glcanvas = new GLCanvas(capabilities);
		glcanvas.addGLEventListener(this);
		
		anim = new Animator(glcanvas);
		anim.setUpdateFPSFrames(10, null);
		anim.start();
		
		glu = new GLU();


		//this.setSize(width, height);
		this.glcanvas.setPreferredSize(new Dimension(width - settings_width, height));
		this.getContentPane().add(glcanvas, BorderLayout.CENTER);
		
		//Settings Area
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(settings_width, height));
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		this.getContentPane().add(p, BorderLayout.EAST);
		
		{
			JPanel h = new JPanel();
			h.setBackground(Color.LIGHT_GRAY);
			h.add(new JLabel("Video Stream Settings", JLabel.CENTER));
			p.add(h);
		}
		
		vs = new VideoSettings();
		p.add(vs);
		
		{
			JPanel h = new JPanel();
			h.setBackground(Color.LIGHT_GRAY);
			h.add(new JLabel("PiBorg Control", JLabel.CENTER));
			p.add(h);
		}

		track = new Track();
		track.load(track_default);
		
		TrackNode start = track.nodes[0];
		
		bot = new Bot(new Point2D(start.p.x, start.p.y), 180f);
		botUpdater = new BotUpdater(bot);
		botUpdater.start();
		
		
		try {
			videoStream = new RTSPStreamer();
			videoStream.video = botView;

			((RTSPStreamer)videoStream).setupRTSPStreamer();
			((RTSPStreamer)videoStream).bot = bot;
			videoStream.run();

		} catch (Exception e) {
			videoStream = null;
			System.err.println("Unable to setup video streamer");
			e.printStackTrace(System.err);
		}
		
		this.addWindowListener(this);
		this.pack();
	}
	
	private void tearDown()
	{
		if (videoStream != null)
			videoStream.stop();
	}
	
	public void loadTextures(GLAutoDrawable glautodrawable)
	{
		GL2 gl2 = glautodrawable.getGL().getGL2();
		gl2.glEnable(GL2.GL_CULL_FACE);
		gl2.glCullFace(GL2.GL_BACK);
		gl2.glFrontFace(GL2.GL_CW);
		
		 try {
			 Texture t;
			 //use the getResource when in jar file
			 //using GL_NEAREST_MIPMAP_LINEAR will give a slight bit 
			 //of blur close up, this may be desirable
			 
			 t = TextureIO.newTexture(Files.newInputStream(Paths.get(fn_tex_track)), true, ".png");
			 //t = TextureIO.newTexture(this.getClass().getResource(fn_tex_track), true, ".png");
			 //t.setTexParameterf(gl2, gl2.GL_TEXTURE_MIN_FILTER, gl2.GL_NEAREST_MIPMAP_LINEAR);
			 //t.setTexParameterf(gl2, gl2.GL_TEXTURE_MAG_FILTER, gl2.GL_NEAREST_MIPMAP_LINEAR);
			 t.setTexParameterf(gl2, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
			 t.setTexParameterf(gl2, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
			 t.setTexParameterf(gl2, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
			 t.setTexParameterf(gl2, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
			 tex_trackRoad = t;
			 
			 //t = TextureIO.newTexture(this.getClass().getResource(fn_tex_bot), false, ".png");
			 t = TextureIO.newTexture(Files.newInputStream(Paths.get(fn_tex_bot)), true, ".png");
			 t.setTexParameterf(gl2, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			 t.setTexParameterf(gl2, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			 t.setTexParameterf(gl2, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
			 t.setTexParameterf(gl2, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
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
			gl2.glColor3f(1f, 1f, 1f);
			gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
			
			gl2.glMatrixMode(GL2.GL_PROJECTION);
			gl2.glLoadIdentity();
			gl2.glOrtho(0, track.bounds.x, 0, track.bounds.y, -1, 1);
			
			drawTrack(glautodrawable);
			drawBot(glautodrawable);
		}
		
		//Draw the First Person/Robot View
		{
			gl2.glViewport(0, height - view_height_firstperson, view_width_firstperson, view_height_firstperson);
			gl2.glScissor(0, height - view_height_firstperson, view_width_firstperson, view_height_firstperson);
			
			gl2.glClearColor(.8f, .8f, .8f, 1.0f);
			gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
			
			gl2.glLoadIdentity();
			
			glu.gluPerspective( 45.0, view_width_firstperson/view_height_firstperson, 0.01f, 2000.0 );
			glu.gluLookAt(
					bot.position.x, bot.position.y, bot.height, 
					bot.focus.x, bot.focus.y, bot.focus.z, 
					0d, 0d, 1d);
			
			drawFirstPerson(glautodrawable);
		}
		
		gl2.glFlush();
	}
	
	/**
	 * Sets up vertex buffers for rendering
	 */
	public void prepTrackBuffers()
	{
		int nNodes = track.nodes.length;
		TrackNode[] nodes = track.nodes;
		
		FloatBuffer vTrack = GLBuffers.newDirectFloatBuffer((nNodes + 1) * 2 * 2);
		FloatBuffer cTrack = GLBuffers.newDirectFloatBuffer((nNodes + 1) * 2 * 2);
		
		FloatBuffer vInnerWall = GLBuffers.newDirectFloatBuffer((nNodes + 1) * 3 * 2);
		FloatBuffer vOuterWall = GLBuffers.newDirectFloatBuffer((nNodes + 1) * 3 * 2);
		
		for (TrackNode tn : nodes) {
			vInnerWall.put((float)tn.a.x).put((float)tn.a.y).put(track.wallHeight);
			vInnerWall.put((float)tn.a.x).put((float)tn.a.y).put(0f);
			
			vTrack.put((float)tn.a.x).put((float)tn.a.y);
			vTrack.put((float)tn.b.x).put((float)tn.b.y);
			
			vOuterWall.put((float)tn.b.x).put((float)tn.b.y).put(0f);
			vOuterWall.put((float)tn.b.x).put((float)tn.b.y).put(track.wallHeight);
		}
		
		vInnerWall.put((float)nodes[0].a.x).put((float)nodes[0].a.y).put(track.wallHeight);
		vInnerWall.put((float)nodes[0].a.x).put((float)nodes[0].a.y).put(0f);
		
		vTrack.put((float)nodes[0].a.x).put((float)nodes[0].a.y);
		vTrack.put((float)nodes[0].b.x).put((float)nodes[0].b.y);
		
		vOuterWall.put((float)nodes[0].b.x).put((float)nodes[0].b.y).put(0f);
		vOuterWall.put((float)nodes[0].b.x).put((float)nodes[0].b.y).put(track.wallHeight);
		
		vInnerWall.flip();
		vTrack.flip();
		vOuterWall.flip();

		for (int i = 0; i < nNodes; i++) {
			if (i % 2 == 0) { cTrack.put(1).put(0); } else { cTrack.put(1).put(1); } 
			if (i % 2 == 0) { cTrack.put(0).put(0); } else { cTrack.put(0).put(1); }
		}
		cTrack.put(1).put(0);
		cTrack.put(0).put(0);
		cTrack.flip();

		vertices_track = vTrack;
		texCoords_track = cTrack;
		
		vertices_innerwall = vInnerWall;
		vertices_outerwall = vOuterWall;
	}
	
	/**
	 * Uses the buffers setup in prepTrackBuffers to draw the track
	 * @param glautodrawable
	 */
	public void drawTrack(GLAutoDrawable glautodrawable)
	{
		GL2 gl2 = glautodrawable.getGL().getGL2();
		
		TrackNode[] nodes = track.nodes;
		int drawCount = (nodes.length + 1) * 2;
		
		gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl2.glColor3f(1f, 0f, 0f);

		if (tex_trackRoad != null) {
			gl2.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
			gl2.glActiveTexture(GL2.GL_TEXTURE0);
			tex_trackRoad.enable(gl2);
			tex_trackRoad.bind(gl2);
			gl2.glTexCoordPointer(2, GL.GL_FLOAT, 0, texCoords_track);
		} 

		gl2.glVertexPointer(2, GL.GL_FLOAT, 0, vertices_track);
		gl2.glDrawArrays(GL2.GL_QUAD_STRIP, 0, drawCount);

		if (tex_trackRoad != null) {
			tex_trackRoad.disable(gl2);
			gl2.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		}
		
		//Set Color to black, and draw walls
		gl2.glColor3f(.1f, .1f, .1f);
		
		gl2.glVertexPointer(3, GL.GL_FLOAT, 0, vertices_innerwall);
		gl2.glDrawArrays(GL2.GL_QUAD_STRIP, 0, drawCount);
		
		gl2.glVertexPointer(3, GL.GL_FLOAT, 0, vertices_outerwall);
		gl2.glDrawArrays(GL2.GL_QUAD_STRIP, 0, drawCount);
		
		gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
	}
	
	/**
	 * Draws the bot using fixed coordinates, no need for buffers
	 * @param glautodrawable
	 */
	public void drawBot(GLAutoDrawable glautodrawable)
	{
		GL2 gl2 = glautodrawable.getGL().getGL2();
		
		if (tex_bot != null)
		{
			gl2.glActiveTexture(GL2.GL_TEXTURE0);
			gl2.glEnable(GL2.GL_BLEND);
			gl2.glBlendFunc(GL2.GL_ONE, GL2.GL_ONE_MINUS_SRC_ALPHA);
			tex_bot.enable(gl2);
			tex_bot.bind(gl2);
			gl2.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE); 
		}
		
		//move to bot class
		float bot_width = (float)bot.dimensions.x, bot_height = (float)bot.dimensions.y;
		
		gl2.glPushMatrix();
		gl2.glTranslatef((float)bot.position.x, (float)bot.position.y, 0f);
		gl2.glRotatef(bot.direction, 0f, 0f, 1f);
		
		gl2.glBegin(GL2.GL_QUADS);
		{
			gl2.glTexCoord2d(1,1);
			gl2.glVertex2d(-(bot_width / 2), -(bot_height/2));
			gl2.glTexCoord2d(1,0);
			gl2.glVertex2d(-(bot_width / 2),  (bot_height/2));
			gl2.glTexCoord2d(0,0);
			gl2.glVertex2d( (bot_width / 2),  (bot_height/2));
			gl2.glTexCoord2d(0,1);
			gl2.glVertex2d( (bot_width / 2), -(bot_height/2));
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
		
		ByteBuffer botViewBuffer = GLBuffers.newDirectByteBuffer(view_width_firstperson * view_height_firstperson * 3);
		gl2.glReadPixels(0, height - view_height_firstperson, view_width_firstperson, view_height_firstperson, GL2.GL_RGB, GL2.GL_BYTE, botViewBuffer);
		
		for (int y = 0; y < view_height_firstperson; y++) {
            for (int x = 0; x < view_width_firstperson; x++) {
            	botView.setRGB(x, y, ((botViewBuffer.get()*2) << 16) | ((botViewBuffer.get()*2) << 8) | (botViewBuffer.get()*2));
            }
        }
	}
	


	//WindowListener
	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		tearDown();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}

	
	//GL Event Listener Methods
	@Override
	public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {}
	
	@Override
	public void init(GLAutoDrawable glautodrawable ) {
		loadTextures(glautodrawable);
		prepTrackBuffers();
	}
	
	@Override
	public void dispose(GLAutoDrawable glautodrawable ) {}
	
	@Override
	public void display(GLAutoDrawable glautodrawable ) {
		 draw(glautodrawable);
	}
	
	/**
	 * Runs the main part of the simulation, the track view
	 * @param args
	 */
	public static void main(String[] args) {

		TrackView w = new TrackView();
		w.setVisible(true);
		w.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
	}
}
