/**
* Copyright 2016 Tim Pearce
**/

package sim.app;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

import sim.util.*;
import sim.object.*;

public class BotView extends JFrame implements KeyListener {

	int width = 400, height = 500;
	int camera_width = 0, camera_height = 0;
	
	
	GLCanvas glcanvas;
	GLU glu;
	TextRenderer tr;
	
	//Bit hacky, so that it can be set equal
	public BufferedImage camera;
	public BufferedImage processed;
	public Bot bot;
	
	Timer aiRoutine;
	Point2D target;
	Texture view;
	
	public float step_speed = .05f; //5 percent increase
	public float step_angle = .01f;	
	
	float velocity = 0f;
	float angle = 0f;
	
	float o_error = 0;
	
	boolean running = false;
	
	public BotView(){
		GLCapabilities capabilities = new GLCapabilities(GLProfile.getDefault());
		glcanvas = new GLCanvas(capabilities);
		glcanvas.addGLEventListener(new GLEventListener() {
			
			@Override
			public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {}
			
			@Override
			public void init(GLAutoDrawable glautodrawable ) {
				//loadTextures(glautodrawable);
			}
			
			@Override
			public void dispose(GLAutoDrawable glautodrawable ) {}
			
			@Override
			public void display(GLAutoDrawable glautodrawable ) {
				 draw(glautodrawable);
			}
		});
		
		glcanvas.addKeyListener(this);
		this.addKeyListener(this);
		
		tr = new TextRenderer(new Font("SansSerif", Font.PLAIN, 24));
		Animator anim = new Animator(glcanvas);
		anim.start();
		
		glu = new GLU();

		target = new Point2D(0, 0);
		aiSetup();

		this.setSize(width, height);
		this.getContentPane().add(glcanvas, BorderLayout.CENTER);
	}
	
	public void draw(GLAutoDrawable glautodrawable){

		GL2 gl2 = glautodrawable.getGL().getGL2();
		
		gl2.glViewport(0, 0, width, height);
		gl2.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
		gl2.glColor3f(1f, 1f, 1f);
		
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		gl2.glOrtho(0, width, 0, height, -1, 1);
		
		if (camera != null)
		{
			view = AWTTextureIO.newTexture(gl2.getGLProfile(), camera, false);
			camera_width = camera.getWidth();
			camera_height = camera.getHeight();

			gl2.glPushMatrix();
			gl2.glTranslatef(0, height - camera_height, 0f);
			
			
			gl2.glActiveTexture(GL2.GL_TEXTURE0);
			view.enable(gl2);
			view.bind(gl2);
			
			gl2.glBegin(GL2.GL_QUADS);
			{
				gl2.glTexCoord2d(0,0);
				gl2.glVertex2d(0,0);
				gl2.glTexCoord2d(0,1);
				gl2.glVertex2d(0, camera_height);
				gl2.glTexCoord2d(1,1);
				gl2.glVertex2d(camera_width, camera_height);
				gl2.glTexCoord2d(1,0);
				gl2.glVertex2d(camera_width, 0);
			}
			gl2.glEnd();
			
			view.disable(gl2);
			view.destroy(gl2);
			
			gl2.glColor3f(1f, 1f, 0.2f);
			gl2.glPointSize(10);
			
			gl2.glBegin(GL2.GL_POINTS);
			{
				gl2.glVertex2d(target.x, target.y);
			}
			gl2.glEnd();
			
			
			gl2.glPopMatrix();
			
		}

		tr.beginRendering(width, height);
		tr.setColor(1f, 1f, 1f, 1f);
		tr.draw("Motor 1: " + (int)(bot.p_m1 * 100) + "%", 20, 100);
		tr.draw("Target: " + target, 20, 75);
		tr.endRendering();
		
		
	}
	
	
	//Bot Control Via keyboard
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		if (bot != null)
		{
			switch (e.getKeyCode())
			{
			case KeyEvent.VK_LEFT: angle += step_angle; break;
			case KeyEvent.VK_RIGHT: angle -= step_angle; break;
			case KeyEvent.VK_UP: velocity = Math.min(velocity + step_speed, 1f); break;
			case KeyEvent.VK_DOWN: velocity = Math.max(velocity - step_speed, -1f); break;
			case KeyEvent.VK_ENTER: running = !running; break;
			}
		}
	}
	
	private void aiSetup()
	{
		aiRoutine = new Timer();
		
		aiRoutine.scheduleAtFixedRate(new TimerTask() {
			@Override
			
			public void run() {
				
				if (camera != null && running)
				{
					int lineNum = (int)(camera.getHeight() * .25f);
					int[] rgbData = camera.getRGB(0, lineNum, camera_width, 1, null, 0, camera_width).clone();
					
					int highest_red_color = 0;
					int highest_green_color = 0;
					int last_red_postion = 0;
					int first_green_position = 0;
					
					for (int i = 0; rgbData != null && i < rgbData.length; i++ )
					{
						int r = (rgbData[i] >> 16) & 0x000000FF;
						int g = (rgbData[i] >> 8 ) & 0x000000FF;
						int b = rgbData[i] & 0x000000FF;
						
						if (r >= highest_red_color)
						{
							highest_red_color = r;
							last_red_postion = i;
						}
						
						if (g > highest_green_color)
						{
							highest_green_color = g;
							first_green_position = i;
						}
					}
					
					int avg = (int)((first_green_position - last_red_postion) / 2.0f); 
					target = new Point2D(last_red_postion + avg, lineNum);
					
					
					//right<->left align
					int max_fix = 2;
					
					int half = camera_width / 2;
					int e = (int) (half - target.x);
					
					float kp = 0.001f, ki = 0.001f, kd = 0.0001f;
					
					float i = ki + e;
					float d = (e - o_error);
					angle = kp * e + ki * i + kd * d;
					o_error = e;
					
					float adjust = angle = (float) Math.atan2(Math.sin(angle), Math.cos(angle));
					//v = .5 * (vl + vr) / r
					
					bot.p_m1 = (float) (-Math.sin(adjust) + velocity);
					bot.p_m2 = (float) (Math.sin(adjust) + velocity);
					
					/**
					float velocityR = m_dist_peroid * m2.spd_act;
					float velocityL = m_dist_peroid * m1.spd_act;
					
					float angle = (float) Math.toRadians(direction);
					angle = (velocityR - velocityL) / (bot_radius * 2);
					**/
					
					//int max_deviation = 25;
					//float fix_percent =  diff/max_deviation;
					//float fix = (int) (Math.min(1f, fix_percent) * max_fix);
					
					//bot.direction += fix;
					
					
					//float max_speed_change = -.15f;
					//float speed_diff = Math.max(-.05f, Math.min(.02f, (1 - fix_percent) * 2)) / .1f;
					
					//float speed_change = speed_diff * max_speed_change;
					//bot.p_m1 = Math.min(Math.max(bot.p_m1 - speed_change, -1f), 1f);
					
				}				
			}
		}, 0, 32);
	}
		
}
