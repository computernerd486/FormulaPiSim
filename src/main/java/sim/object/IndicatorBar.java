package sim.object;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import sim.util.Point2D;

public class IndicatorBar {
	
	public enum Status {OFF, RED, GREEN}

	public Status status = Status.OFF;
	public Track track;
	
	FloatBuffer vertices;
	FloatBuffer texCoords_off, texCoords_red, texCoords_green;

	
	Point2D center;
	float trackDiameter;
	Color indicator = Color.decode("0x411a1b");
	
	Texture tex_bar;
	
	private static final String fn_tex_track = "img/lightbar_soft.png"; //"img/lightbar_hard.png";
	
	
	public IndicatorBar(Point2D center, Float radius) {
		this.center = center;
		this.trackDiameter = radius;
	}
	
	public void loadTextures(GLAutoDrawable glautodrawable) {
		GL2 gl2 = glautodrawable.getGL().getGL2();
		Texture t;

		try {
			t = TextureIO.newTexture(Files.newInputStream(Paths.get(fn_tex_track)), true, ".png");
			//t.setTexParameterf(gl2, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
			//t.setTexParameterf(gl2, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
			t.setTexParameterf(gl2, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			t.setTexParameterf(gl2, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			t.setTexParameterf(gl2, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
			t.setTexParameterf(gl2, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
			tex_bar = t;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void prepIndicatorBuffer(GLAutoDrawable glautodrawable) {
		
		GL2 gl2 = glautodrawable.getGL().getGL2();
		
		// 5--6------7--8
		// |  4------9  |
		// |  |      |  |
		// |  |      |  |
		// 1--2     11-12
		
		float height_over = 38f, height = 6f, marker_over = 20f;
		float y1 = (float) (center.y + trackDiameter), y2 = (float) (center.y - trackDiameter);
		
		//vertex buffers for lights
		FloatBuffer vLights = GLBuffers.newDirectFloatBuffer(20 * 3 * GLBuffers.SIZEOF_FLOAT);
		vLights.put((float)center.x).put(y1).put(height_over); //9
		vLights.put((float)center.x).put(y2).put(height_over); //4
		vLights.put((float)center.x).put(y2).put(height_over + height); //6
		vLights.put((float)center.x).put(y1).put(height_over + height); //7text
		
		vLights.put((float)center.x).put(y2).put(0f); //2
		vLights.put((float)center.x).put(y2 - height).put(0f); //1
		vLights.put((float)center.x).put(y2 - height).put(height_over + height); //5
		vLights.put((float)center.x).put(y2).put(height_over + height); //6
		
		vLights.put((float)center.x).put(y1 + height).put(0f); //12
		vLights.put((float)center.x).put(y1).put(0f); //11
		vLights.put((float)center.x).put(y1).put(height_over + height); //7
		vLights.put((float)center.x).put(y1 + height).put(height_over + height); //8
		
		vLights.put((float)center.x).put(y2 - height).put(height_over + height); //5
		vLights.put((float)center.x - height).put(y2 - height).put(height_over + height); //5
		vLights.put((float)center.x - height).put(y1 + height).put(height_over + height);
		vLights.put((float)center.x).put(y1 + height).put(height_over + height);
		
		vLights.put((float)center.x).put(y1).put(marker_over); //9
		vLights.put((float)center.x).put(y2).put(marker_over); //4
		vLights.put((float)center.x).put(y2).put(height_over); //4
		vLights.put((float)center.x).put(y1).put(height_over); //9
		
		vLights.flip();
		vertices = vLights;
		
		//buffers for the texture locations
		//4 points per set, 2 coords per point, 3 sets
		FloatBuffer tLights_red = GLBuffers.newDirectFloatBuffer(4 * 2 *  GLBuffers.SIZEOF_FLOAT);
		tLights_red.put(1f).put(0f);
		tLights_red.put(0f).put(0f);
		tLights_red.put(0f).put(.25f);
		tLights_red.put(1f).put(.25f);
		tLights_red.flip();
		texCoords_red = tLights_red;
		
		FloatBuffer tLights_green = GLBuffers.newDirectFloatBuffer(4 * 2 * GLBuffers.SIZEOF_FLOAT);	
		tLights_green.put(1f).put(.25f);
		tLights_green.put(0f).put(.25f);
		tLights_green.put(0f).put(.5f);
		tLights_green.put(1f).put(.5f);
		tLights_green.flip();
		texCoords_green = tLights_green;
		
		FloatBuffer tLights_off = GLBuffers.newDirectFloatBuffer(4 * 2 * GLBuffers.SIZEOF_FLOAT);	
		tLights_off.put(1f).put(.75f);
		tLights_off.put(0f).put(.75f);
		tLights_off.put(0f).put(1f);
		tLights_off.put(1f).put(1f);
		tLights_off.flip();
		texCoords_off = tLights_off;
		
		/**
		tLights.put(1f).put(.5f);
		tLights.put(0f).put(.5f);
		tLights.put(0f).put(.75f);
		tLights.put(1f).put(.75f);
		
		tLights.put(1f).put(.75f);
		tLights.put(0f).put(.75f);
		tLights.put(0f).put(1f);
		tLights.put(1f).put(1f);
		**/


	}
	
	public void draw(GLAutoDrawable glautodrawable) {
		GL2 gl2 = glautodrawable.getGL().getGL2();
		
		
		//draw sides, no need for texture
		gl2.glColor3f(.1f, .1f, .1f);
		gl2.glVertexPointer(3, GL.GL_FLOAT, 0, vertices);
		gl2.glDrawArrays(GL2.GL_QUADS, 4, 12);
		
		//System.out.println(indicator.getRed() + " " + indicator.getGreen() + " " + indicator.getBlue());
		gl2.glColor3ub((byte)indicator.getRed(), (byte)indicator.getGreen(), (byte)indicator.getBlue());
		gl2.glVertexPointer(3, GL.GL_FLOAT, 0, vertices);
		gl2.glDrawArrays(GL2.GL_QUADS, 16, 4);
		
		
		//setup texture and draw actual bar
		if (tex_bar != null) {
			gl2.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
			gl2.glActiveTexture(GL2.GL_TEXTURE0);
			tex_bar.enable(gl2);
			tex_bar.bind(gl2);
			
			//use correct set per state
			switch (status) {
			case OFF: gl2.glTexCoordPointer(2, GL.GL_FLOAT, 0, texCoords_off); break;
			case GREEN: gl2.glTexCoordPointer(2, GL.GL_FLOAT, 0, texCoords_green); break;
			case RED: gl2.glTexCoordPointer(2, GL.GL_FLOAT, 0, texCoords_red); break;
			}
				
		} 

		gl2.glColor3f(1f, 1f, 1f);
		gl2.glVertexPointer(3, GL.GL_FLOAT, 0, vertices);
		gl2.glDrawArrays(GL2.GL_QUADS, 0, 4);
		
		if (tex_bar != null) {
			tex_bar.disable(gl2);
			gl2.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		}
		
		

	}
	
	public void cleanup(GLAutoDrawable glautodrawable) {}
}
