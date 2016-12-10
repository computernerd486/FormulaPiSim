/**
* Copyright 2016 Tim Pearce
**/

package sim.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import sim.app.panel.BotSettings;
import sim.app.panel.StartSettings;
import sim.app.panel.VideoSettings;
import sim.object.*;
import sim.util.*;
import sim.util.AIBotUpdater.State;
import sim.util.io.stream.*;
import sim.util.io.stream.RTSPStreamer.Format;

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
	
	private static final String VERSION = "RELEASE - 1.1.0";
	
	//OpenGL objects
	Animator anim;
	GLCanvas glcanvas;
	GLOffscreenAutoDrawable fpbuffer;
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
	private static final String fn_tex_botOverview = "img/bot_overview.png";
	private static final String track_default = "final.trk";
	
	//Texture Section, this should be in a loader
	Texture tex_trackRoad;
	Texture tex_bot;
	Texture tex_bot_overview;

	//Bot
	public Bot bot;
	BotModel botModel;
	BotUpdater botUpdater;
	int userLane;
	
	Track track;
	boolean isFlipped = false;
	
	public Bot aiBots[];
	BotUpdater aiUpdaters[];
	
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
	
	//Settings Panels
	VideoSettings vs;
	StartSettings ss;
	BotSettings bs;
	
	//For Drawing opengl text
	TextRenderer tr;
	ByteBuffer botViewBuffer;
	
	public TrackView() {
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int monitor_width = gd.getDisplayMode().getWidth();
		int monitor_height = gd.getDisplayMode().getHeight();
		System.out.println("Screen: [" + monitor_width + "x" + monitor_height + "]");
	
		botView = new BufferedImage(view_width_firstperson, view_height_firstperson, BufferedImage.TYPE_INT_RGB);
		
		GLCapabilities capabilities = new GLCapabilities(GLProfile.getDefault());
		glcanvas = new GLCanvas(capabilities);
		glcanvas.addGLEventListener(this);
						
		anim = new Animator(glcanvas);
		anim.setUpdateFPSFrames(10, null);
		anim.start();
		
		glu = new GLU();

		this.glcanvas.setPreferredSize(new Dimension(width - settings_width, height));
		this.getContentPane().add(glcanvas, BorderLayout.CENTER);
		
		initSettingsPanel();

		track = new Track();
		track.load(track_default);
		
		//TrackNode start = track.nodes[0];
		userLane = Math.round(track.lanes / 2);
		Point2D start = track.startPositions[userLane];
		bot = new Bot(new Point2D(start.x, start.y), 180f); //TODO: Set bot offset from half width.
		bot.laptimer = new LapTimer(track, bot);
		
		botModel = new BotModel();
		
		//Startup one per lane, we'll just skip whichever one is on your starting lane for drawing.
		aiBots = new Bot[track.lanes];
		aiUpdaters = new AIBotUpdater[track.lanes];
		for (int i = 0; i < track.lanes; i++) {
			Point2D sp = track.startPositions[i];
			aiBots[i] = new Bot(new Point2D(sp.x, sp.y), 180f);
			aiUpdaters[i] = new AIBotUpdater(aiBots[i], track, i);
			aiUpdaters[i].start();
		}
		
		bs.accel.setValue(bot.m1.accel_rate);
		bs.deccel.setValue(bot.m1.decel_rate);
		bs.refVoltage.setValue(bot.m_ref_volt);
		bs.refRPM.setValue(bot.m_ref_rpm);
		bs.maxVoltage.setValue(bot.m_run_volt);
		bs.maxRPM.setText(String.valueOf(Math.round(bot.m_run_rpm)));
		
		for (int i = 1; i <= track.lanes; i++) {
			ss.lane.addItem(i);
		}
		ss.lane.setSelectedIndex(Math.round(track.lanes / 2));
		
		botUpdater = new BotUpdater(bot);
		botUpdater.start();
		
		tr = new TextRenderer(new Font("SansSerif", Font.PLAIN, 16));
		
		try {
			videoStream = new RTSPStreamer(30, new Dimension(640, 480));
			videoStream.video = botView;

			RTSPStreamer stream = ((RTSPStreamer) videoStream);
			stream.bot = bot;
			stream.loadProperties();
			
			vs.resX.setText(String.valueOf(stream.size.width));
			vs.resY.setText(String.valueOf(stream.size.height));
			vs.port.setText(String.valueOf(stream.port));

		} catch (Exception e) {
			videoStream = null;
			System.err.println("Unable to setup video streamer");
			e.printStackTrace(System.err);
		}
				
		initSettingsControls();
		
		this.addWindowListener(this);
		this.pack();
		
		/**
		Timer t = new Timer(true);
		t.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				writeMemUsage();
			}
		}, 0, 10000);
		**/
	}
	
	private void tearDown()
	{
		if (videoStream != null)
		{
			videoStream.stop();				
		}
	}
	
	private void loadTextures(GLAutoDrawable glautodrawable)
	{
		GL2 gl2 = glautodrawable.getGL().getGL2();
		gl2.glEnable(GL2.GL_CULL_FACE);
		gl2.glCullFace(GL2.GL_NONE);
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
			 

			//t = TextureIO.newTexture(this.getClass().getResource(fn_tex_bot), false, ".png");
			 t = TextureIO.newTexture(Files.newInputStream(Paths.get(fn_tex_botOverview)), true, ".png");
			 t.setTexParameterf(gl2, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			 t.setTexParameterf(gl2, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			 t.setTexParameterf(gl2, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
			 t.setTexParameterf(gl2, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
			 tex_bot_overview = t;
			 
		 } catch (Exception e) {
			 e.printStackTrace(System.err);
		 }
	}
	
	private void setupLighting(GLAutoDrawable glautodrawable) {
		
		GL2 gl2 = glautodrawable.getGL().getGL2();
		
		float[] lightPos = { 2000,3000,2000,1 };        // light position
		float[] noAmbient = { 0.2f, 0.2f, 0.2f, 1f };     // low ambient light
		float[] diffuse = { 1f, 1f, 1f, 1f };        // full diffuse colour
	    
	    gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, noAmbient, 0);
	    gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuse, 0);
	    gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
	}
	
	private int TEXTOFFSET = 20;
	private void draw(GLAutoDrawable glautodrawable){
		
		GL2 gl2 = glautodrawable.getGL().getGL2();
		gl2.glLineWidth(2);		
		gl2.glEnable(GL2.GL_SCISSOR_TEST);
				
		gl2.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);		
		gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);

		//Draw the overhead view
		{
			gl2.glViewport(glcanvas.getWidth() - view_width_overhead, 0, view_width_overhead, view_height_overhead);
			gl2.glScissor(glcanvas.getWidth() - view_width_overhead, 0, view_width_overhead, view_height_overhead);
			gl2.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			gl2.glColor3f(1f, 1f, 1f);
			gl2.glClearDepth(1.0f);
			gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
			
			gl2.glMatrixMode(GL2.GL_PROJECTION);
			gl2.glLoadIdentity();
			gl2.glOrtho(0, track.bounds.x, 0, track.bounds.y, -50, 50);
			
			drawTrack(glautodrawable);
			drawLap(glautodrawable, bot);
			drawBot(glautodrawable);
			drawBot3D(glautodrawable, bot);
		}
		
		//Draw the First Person/Robot View
		{
			gl2.glViewport(0, height - view_height_firstperson, view_width_firstperson, view_height_firstperson);
			gl2.glScissor(0, height - view_height_firstperson, view_width_firstperson, view_height_firstperson);
			
			gl2.glEnable(GL2.GL_DEPTH_TEST);
			gl2.glDepthFunc(GL.GL_LEQUAL);
			
			gl2.glClearColor(.8f, .8f, .8f, 1.0f);
			gl2.glClearDepth(1.0f);
			gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
			
			gl2.glLoadIdentity();
			
			glu.gluPerspective( 55, (float)view_width_firstperson/(float)view_height_firstperson, 1f, 2000.0f );
			
			
			glu.gluLookAt(
					bot.camera.x, bot.camera.y, bot.camera.z, 
					bot.focus.x, bot.focus.y, bot.focus.z, 
					0d, 0d, (isFlipped) ? -1d : 1d);
			
			/*
			glu.gluLookAt(
					bot.position.x + 20, bot.position.y, bot.height + 10, 
					bot.position.x, bot.position.y, bot.height, 
					0d, 0d, 1d);
			*/
			
			drawFirstPerson(glautodrawable);
			
			gl2.glDisable(GL2.GL_DEPTH_TEST);
			
		}
		
		//Bot overview for motor control
		{
			gl2.glViewport(view_width_firstperson, height - 200, 200, 200);
			gl2.glScissor(view_width_firstperson, height - 200, 200, 200);
			gl2.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			gl2.glColor3f(1f, 1f, 1f);
			gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
			
			gl2.glMatrixMode(GL2.GL_PROJECTION);
			gl2.glLoadIdentity();
			gl2.glOrtho(0, 100, 0, 100, -1, 1);
			
			drawOverview(glautodrawable);
		}
		
		//Alpha Warning Text
		{
			gl2.glViewport(0, 0, glcanvas.getWidth(), glcanvas.getHeight());
			gl2.glScissor(0, 0,  glcanvas.getWidth(), glcanvas.getHeight());
			
			gl2.glMatrixMode(GL2.GL_PROJECTION);
			gl2.glLoadIdentity();
			gl2.glOrtho(0, glcanvas.getWidth(), 0, glcanvas.getHeight(), -1, 1);
			
			tr.beginRendering(glcanvas.getWidth(), glcanvas.getHeight());
			tr.setColor(1f, 1f, 1f, .6f);
			tr.draw(VERSION, 8, 10);
			
			//So these need to be 20px diffrence
			
			int currentOffset = 0;
			tr.draw("Laps:", 8,  view_height_firstperson - (currentOffset += TEXTOFFSET));
			
			for (int l = 0; l < bot.laptimer.laps.size(); l++) { 
				tr.draw(bot.laptimer.laps.get(l).toString(), 8,  view_height_firstperson - (currentOffset += TEXTOFFSET));
			}
			
			tr.draw("---------", 8,  view_height_firstperson - (currentOffset += TEXTOFFSET));
			
			if (bot.laptimer.currentLap != null) {
				tr.draw(bot.laptimer.currentLap.toString(), 8,  view_height_firstperson - (currentOffset += TEXTOFFSET));
			}
			tr.endRendering();
		}

		gl2.glFlush();
	}
	
	/**
	 * Sets up vertex buffers for rendering
	 */
	private void prepTrackBuffers()
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
	private void drawTrack(GLAutoDrawable glautodrawable)
	{
		GL2 gl2 = glautodrawable.getGL().getGL2();
		gl2.glClearDepthf(0f);
		
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
		gl2.glColor3f(.15f, .15f, .15f);
		
		gl2.glVertexPointer(3, GL.GL_FLOAT, 0, vertices_innerwall);
		gl2.glDrawArrays(GL2.GL_QUAD_STRIP, 0, drawCount);
		
		gl2.glVertexPointer(3, GL.GL_FLOAT, 0, vertices_outerwall);
		gl2.glDrawArrays(GL2.GL_QUAD_STRIP, 0, drawCount);

		//Draw light bar
		track.lights.draw(glautodrawable);
		
		gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
	}
	
	/**
	 * Draws the bot using fixed coordinates, no need for buffers
	 * @param glautodrawable
	 */
	private void drawBot(GLAutoDrawable glautodrawable)
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
		//gl2.glTranslatef(-(float)bot.position.x, -(float)bot.position.y, 0f);
		gl2.glPopMatrix();
		
		if (tex_bot != null)
			tex_bot.disable(gl2);
	}
	
	private void drawLap(GLAutoDrawable glautodrawable, Bot bot)
	{
		GL2 gl2 = glautodrawable.getGL().getGL2();

		gl2.glLineWidth(2f);
		
		gl2.glColor3f(.36f, .7f, .9f);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		{
			for (Point2D p : bot.tracker.lastLap)
				gl2.glVertex3d(p.x, p.y, 1);
		}
		gl2.glEnd();
		
		gl2.glColor3f(.9f, .62f, .32f);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		{
			for (Point2D p : bot.tracker.currentLap)
				gl2.glVertex3d(p.x, p.y, 1);
		}
		gl2.glEnd();
	}
	
	private void drawBot3D(GLAutoDrawable glautodrawable, Bot bot)
	{
		botModel.draw(glautodrawable, bot, null);
		
		for (int i = 0; i < aiBots.length; i++) {
			if (((AIBotUpdater)aiUpdaters[i]).lane != userLane)
				botModel.draw(glautodrawable, aiBots[i], null);
		}
	}
	
	private void drawFirstPerson(GLAutoDrawable glautodrawable)
	{
		GL2 gl2 = glautodrawable.getGL().getGL2();	
		drawTrack(glautodrawable);
		drawBot3D(glautodrawable, bot);
		
		botViewBuffer.clear();
		gl2.glReadPixels(0, height - view_height_firstperson, view_width_firstperson, view_height_firstperson, GL2.GL_RGB, GL2.GL_BYTE, botViewBuffer);
		
		for (int y = 0; y < view_height_firstperson; y++) {
            for (int x = 0; x < view_width_firstperson; x++) {
            	botView.setRGB(x, y, ((botViewBuffer.get()*2) << 16) | ((botViewBuffer.get()*2) << 8) | (botViewBuffer.get()*2));
            }
        }
	}
	
	private void drawOverview(GLAutoDrawable glautodrawable) 
	{
		GL2 gl2 = glautodrawable.getGL().getGL2();
		
		if (tex_bot_overview != null)
		{
			gl2.glActiveTexture(GL2.GL_TEXTURE0);
			gl2.glEnable(GL2.GL_BLEND);
			gl2.glBlendFunc(GL2.GL_ONE, GL2.GL_ONE_MINUS_SRC_ALPHA);
			tex_bot_overview.enable(gl2);
			tex_bot_overview.bind(gl2);
			gl2.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE); 
		}
		
		
		gl2.glPushMatrix();
		gl2.glTranslatef(50f,50f,0f);


		gl2.glBegin(GL2.GL_QUADS);
		{
			gl2.glTexCoord2d(0,0);
			gl2.glVertex2d(-40, -40);
			gl2.glTexCoord2d(0,1);
			gl2.glVertex2d(-40,  40);
			gl2.glTexCoord2d(1,1);
			gl2.glVertex2d( 40,  40);
			gl2.glTexCoord2d(1,0);
			gl2.glVertex2d( 40, -40);
		}
		gl2.glEnd();
		
		if (tex_bot_overview != null)
			tex_bot_overview.disable(gl2);
		
		
		float m1 = bot.p_m1, m2 = bot.p_m2;
		float r,g;
		
		float[] c_pos = {1, 1, 0};
		float[] c_act = {0, .76f, 1};
				
		c_pos[0] = r = (m1 < 0) ? 1 : 0;;
		c_pos[1] = g = (m1 > 0) ? 1 : 0;
		
		gl2.glBegin(GL2.GL_QUADS);
		{
			gl2.glColor3fv(c_act, 0); gl2.glVertex2d(-39, (40 * Math.min(bot.m1.spd_act, 0)));
			gl2.glColor3fv(c_act, 0); gl2.glVertex2d(-49, (40 * Math.min(bot.m1.spd_act, 0)));
			gl2.glColor3fv(c_act, 0); gl2.glVertex2d(-49, (40 * Math.max(bot.m1.spd_act, 0)));
			gl2.glColor3fv(c_act, 0); gl2.glVertex2d(-39, (40 * Math.max(bot.m1.spd_act, 0)));

			gl2.glColor3fv(c_pos, 0); gl2.glVertex2d(-41, (40 * Math.min(bot.m1.spd_sig, 0)));
			gl2.glColor3fv(c_pos, 0); gl2.glVertex2d(-47, (40 * Math.min(bot.m1.spd_sig, 0)));
			gl2.glColor3fv(c_pos, 0); gl2.glVertex2d(-47, (40 * Math.max(bot.m1.spd_sig, 0)));
			gl2.glColor3fv(c_pos, 0); gl2.glVertex2d(-41, (40 * Math.max(bot.m1.spd_sig, 0)));
		}
		gl2.glEnd();
		
		c_pos[0] = r = (m2 < 0) ? 1 : 0;;
		c_pos[1] = g = (m2 > 0) ? 1 : 0;
		
		gl2.glBegin(GL2.GL_QUADS);
		{
			gl2.glColor3fv(c_act, 0); gl2.glVertex2d(39, (40 * Math.max(bot.m2.spd_act, 0)));
			gl2.glColor3fv(c_act, 0); gl2.glVertex2d(49, (40 * Math.max(bot.m2.spd_act, 0)));
			gl2.glColor3fv(c_act, 0); gl2.glVertex2d(49, (40 * Math.min(bot.m2.spd_act, 0)));
			gl2.glColor3fv(c_act, 0); gl2.glVertex2d(39, (40 * Math.min(bot.m2.spd_act, 0)));

			gl2.glColor3fv(c_pos, 0); gl2.glVertex2d(41, (40 * Math.max(bot.m2.spd_sig, 0)));
			gl2.glColor3fv(c_pos, 0); gl2.glVertex2d(47, (40 * Math.max(bot.m2.spd_sig, 0)));
			gl2.glColor3fv(c_pos, 0); gl2.glVertex2d(47, (40 * Math.min(bot.m2.spd_sig, 0)));
			gl2.glColor3fv(c_pos, 0); gl2.glVertex2d(41, (40 * Math.min(bot.m2.spd_sig, 0)));
		}
		gl2.glEnd();
		
		gl2.glPointSize(12);
		gl2.glBegin(GL2.GL_POINTS);
		{
			gl2.glColor3f(0f, 0f, 0f);
			gl2.glVertex3f(10, -20, 0.1f);
		}
		gl2.glEnd();
		
		if (bot.light) {
			gl2.glPointSize(8);
			gl2.glBegin(GL2.GL_POINTS);
			{
				gl2.glColor3f(0f, 1f, 0f);
				gl2.glVertex3f(10, -20, 0.1f);
			}
			gl2.glEnd();
		}
		
		gl2.glTranslatef(-50, -50, 0f);
		gl2.glPopMatrix();
		
		if (tex_bot_overview != null)
			tex_bot_overview.disable(gl2);
	}

	private void initSettingsPanel() {
		//Settings Area
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(settings_width, height));
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		

		this.getContentPane().add(p, BorderLayout.EAST);
	
		vs = new VideoSettings();
		p.add(vs);
		
		ss = new StartSettings();
		p.add(ss);
		
		bs = new BotSettings();
		p.add(bs);
		
	}

	private void initSettingsControls() {
		//Start with Server
		vs.server_stop.setEnabled(false);
		
		vs.server_start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try{
					int x = Integer.parseInt(vs.resX.getText());
					int y = Integer.parseInt(vs.resY.getText());
					int port = Integer.parseInt(vs.port.getText());
					
					vs.server_start.setEnabled(false);
					vs.server_stop.setEnabled(true);
					vs.resX.setEnabled(false);
					vs.resY.setEnabled(false);
					vs.port.setEnabled(false);
					vs.formatJPG.setEnabled(false);
					vs.formatPNG.setEnabled(false);
					vs.runningStatus.setText("Running");

					((RTSPStreamer)videoStream).setupRTSPStreamer(new Dimension(x, y), port);
					videoStream.run();
					
				} catch (Exception ex) {
					vs.runningStatus.setText("Error Starting");
					ex.printStackTrace(System.err);
				}
				
			}
		});
		
		vs.server_stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				vs.server_start.setEnabled(true);
				vs.server_stop.setEnabled(false);
				vs.resX.setEnabled(true);
				vs.resY.setEnabled(true);
				vs.port.setEnabled(true);
				vs.formatJPG.setEnabled(true);
				vs.formatPNG.setEnabled(true);
				vs.runningStatus.setText("Stopped");
				videoStream.stop();
			}
		});
		
		vs.flipVideo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				isFlipped = vs.flipVideo.isSelected();
			}
		});
		
		vs.formatJPG.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				RTSPStreamer streamer = (RTSPStreamer)videoStream;
				streamer.outputFormat = Format.JPG;
			}
		});
		
		vs.formatPNG.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				RTSPStreamer streamer = (RTSPStreamer)videoStream;
				streamer.outputFormat = Format.PNG;
			}
		});
		
		//Next with the bot controls
		
		bs.bot_start.setEnabled(false);
		
		bs.bot_start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bs.bot_start.setEnabled(false);
				bs.bot_stop.setEnabled(true);
				botUpdater.start();
			}
		});
		
		bs.bot_stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bs.bot_start.setEnabled(true);
				bs.bot_stop.setEnabled(false);
				botUpdater.stop();
			}
		});
		
		bs.bot_reset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				userLane = ss.lane.getSelectedIndex();
				Point2D start = track.startPositions[userLane];
				bot.p_m1 = 0f;
				bot.p_m2 = 0f;
				bot.light = false;
				bot.m1.spd_act = 0f;
				bot.m1.spd_sig = 0f;
				bot.m2.spd_act = 0f;
				bot.m2.spd_sig = 0f;
				bot.position = new Point2D(start.x, start.y);
				bot.setDirection( 180f);
				
				for (BotUpdater bu : aiUpdaters) {
					((AIBotUpdater)bu).state = State.INIT;
				}
				
				for (int i = 0; i < track.lanes; i++) {
					Point2D sp = track.startPositions[i];
					aiBots[i].position = new Point2D(sp.x, sp.y);
					aiBots[i].direction = 180f;
				}

			}
		});
		
		bs.accel.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				float accel = (float)bs.accel.getValue();
				bot.m1.accel_rate = accel;
				bot.m2.accel_rate = accel;
			}
		});
		
		bs.deccel.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				float deccel = (float)bs.deccel.getValue();
				bot.m1.decel_rate = deccel;
				bot.m2.decel_rate = deccel;
				
			}
		});
		
		bs.refVoltage.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				bot.m_ref_volt = (float)bs.refVoltage.getValue();
				bot.recalc();
				bs.maxRPM.setText(String.valueOf(Math.round(bot.m_run_rpm)));
			}
		});
		
		bs.refRPM.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				bot.m_ref_rpm = (float)bs.refRPM.getValue();
				bot.recalc();
				bs.maxRPM.setText(String.valueOf(Math.round(bot.m_run_rpm)));
			}
		});
		
		bs.maxVoltage.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				bot.m_run_volt = (float)bs.maxVoltage.getValue();
				bot.recalc();
				bs.maxRPM.setText(String.valueOf(Math.round(bot.m_run_rpm)));
			}
		});
		
		ss.lightsOff.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ss.lightsOff.setEnabled(false);
				ss.lightsGreen.setEnabled(true);
				ss.lightsRed.setEnabled(true);
				track.lights.status = IndicatorBar.Status.OFF;
			}
		});
		
		ss.lightsGreen.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ss.lightsOff.setEnabled(true);
				ss.lightsGreen.setEnabled(false);
				ss.lightsRed.setEnabled(true);
				track.lights.status = IndicatorBar.Status.GREEN;
				
			}
		});
		
		ss.lightsRed.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ss.lightsOff.setEnabled(true);
				ss.lightsGreen.setEnabled(true);
				ss.lightsRed.setEnabled(false);
				track.lights.status = IndicatorBar.Status.RED;
			}
		});
		
		ss.autoAI.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ss.autoAI.setEnabled(false);
				ss.startAI.setEnabled(true);
				ss.stopAI.setEnabled(true);
				for (AIBotUpdater updr : (AIBotUpdater[])aiUpdaters) {
					updr.lightStart = true;
					updr.state = State.INIT;
				}
			}
		});
		
		ss.startAI.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ss.autoAI.setEnabled(true);
				ss.startAI.setEnabled(false);
				ss.stopAI.setEnabled(true);
				for (AIBotUpdater updr : (AIBotUpdater[])aiUpdaters) {
					updr.lightStart = false;
					updr.state = State.GO;
				}
			}
		});
		
		ss.stopAI.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ss.autoAI.setEnabled(true);
				ss.startAI.setEnabled(true);
				ss.stopAI.setEnabled(false);
				for (AIBotUpdater updr : (AIBotUpdater[])aiUpdaters) {
					updr.lightStart = false;
					updr.state = State.INIT;
				}
			}
		});

	}
	
	private void writeMemUsage()
	{
		int mb = 1024 * 1024; 
		 
		// get Runtime instance
		Runtime instance = Runtime.getRuntime();
 
		System.out.println("***** Heap usage [MB] *****");
		System.out.printf("%1$6s %2$6s %3$6s %4$6s", "Total", "Free", "Used", "Max");
		System.out.println();
		System.out.printf("%1$6s %2$6s %3$6s %4$6s", 
				(instance.totalMemory() / mb), 
				(instance.freeMemory() / mb),
				((instance.totalMemory() - instance.freeMemory()) / mb), 
				(instance.maxMemory() / mb));
		System.out.println();
		System.out.println();
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
		track.lights.loadTextures(glautodrawable);
		botModel.loadTextures(glautodrawable);
		
		prepTrackBuffers();
		track.lights.prepIndicatorBuffer(glautodrawable);
		botModel.setupBuffer();
		
		botViewBuffer = GLBuffers.newDirectByteBuffer(view_width_firstperson * view_height_firstperson * 3);
		
		setupLighting(glautodrawable);
	}
	
	@Override
	public void dispose(GLAutoDrawable glautodrawable ) {
		track.lights.cleanup(glautodrawable);
		botModel.cleanup(glautodrawable);
	}
	
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

/**
 * Notes:
 * 
 * Desaturate:
 * f = 0.2; // desaturate by 20%
 * L = 0.3*r + 0.6*g + 0.1*b;
 * new_r = r + f * (L - r);
 * new_g = g + f * (L - g);
 * new_b = b + f * (L - b);
 * 
 * Lumanace:
 * Y = (R+R+R+B+G+G+G+G)>>3
 */
