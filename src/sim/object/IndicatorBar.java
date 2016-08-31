package sim.object;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.GLBuffers;

import sim.util.Point2D;

public class IndicatorBar {
	
	public enum Status {OFF, RED, GREEN}

	public Status status = Status.OFF;
	public Track track;
	
	FloatBuffer vertices;
	FloatBuffer colors;
	ShortBuffer index;
	IntBuffer ib;
	
	int vHandle;
	int cHandle;
	int iHandle;
	
	
	Point2D center;
	float trackDiameter;
	
	public IndicatorBar(Point2D center, Float radius) {
		this.center = center;
		this.trackDiameter = radius;
	}
	
	public void prepIndicatorBuffer(GLAutoDrawable glautodrawable){
		
		GL2 gl2 = glautodrawable.getGL().getGL2();
		
		// 5--6------7--8
		// 3--4------9-10
		// |  |      |  |
		// |  |      |  |
		// 1--2     11-12
		
		float height_over = 38f, height = 6f;
		float y1 = (float) (center.y + trackDiameter), y2 = (float) (center.y - trackDiameter);
		
		FloatBuffer vLights = GLBuffers.newDirectFloatBuffer(12 * 3 * GLBuffers.SIZEOF_FLOAT);
		FloatBuffer cLights = GLBuffers.newDirectFloatBuffer(12 * 3 * GLBuffers.SIZEOF_FLOAT);
		ShortBuffer iLights = GLBuffers.newDirectShortBuffer(10 * 3 * GLBuffers.SIZEOF_SHORT);
		
		vLights.put((float)center.x).put(y2 - height).put(0f);
		vLights.put((float)center.x).put(y2).put(0f);
		vLights.put((float)center.x).put(y2 - height).put(height_over);
		vLights.put((float)center.x).put(y2).put(height_over);
		vLights.put((float)center.x).put(y2 - height).put(height_over + height);
		vLights.put((float)center.x).put(y2).put(height_over + height);
		
		vLights.put((float)center.x).put(y1).put(height_over + height);
		vLights.put((float)center.x).put(y1 + height).put(height_over + height);
		vLights.put((float)center.x).put(y1).put(height_over);
		vLights.put((float)center.x).put(y1 + height).put(height_over);
		vLights.put((float)center.x).put(y1).put(0f);
		vLights.put((float)center.x).put(y1 + height).put(0f);
		
		cLights.put(0f).put(0f).put(0f);
		cLights.put(0f).put(0f).put(0f);
		cLights.put(0f).put(0f).put(0f);
		cLights.put(0f).put(0f).put(0f);
		cLights.put(0f).put(0f).put(0f);
		cLights.put(0f).put(0f).put(0f);
		
		cLights.put(0f).put(0f).put(0f);
		cLights.put(0f).put(0f).put(0f);
		cLights.put(0f).put(0f).put(0f);
		cLights.put(0f).put(0f).put(0f);
		cLights.put(0f).put(0f).put(0f);
		cLights.put(0f).put(0f).put(0f);
				
		iLights.put((short)2).put((short)0).put((short)1);
		iLights.put((short)2).put((short)1).put((short)3);
		iLights.put((short)4).put((short)2).put((short)3);
		iLights.put((short)4).put((short)3).put((short)5);
		
		iLights.put((short)5).put((short)3).put((short)8);
		iLights.put((short)5).put((short)8).put((short)6);
		
		iLights.put((short)6).put((short)8).put((short)9);
		iLights.put((short)6).put((short)9).put((short)7);
		iLights.put((short)8).put((short)10).put((short)11);
		iLights.put((short)8).put((short)11).put((short)9);
		
		vLights.flip();
		cLights.flip();
		iLights.flip();
		
		vertices = vLights; 
		colors = cLights;
		index = iLights;
		
		ib = GLBuffers.newDirectIntBuffer(3);
		gl2.glGenBuffers(3, ib);
		
		vHandle = ib.get(0);
		cHandle = ib.get(1);
		iHandle = ib.get(2);

		
		/**
		//rememerber, the Y axis splits the track
		vLights.put((float)center.x).put(y1 + height).put(0f);
		vLights.put((float)center.x).put(y1).put(0f);
		vLights.put((float)center.x).put(y1 + height).put(height_over);
		vLights.put((float)center.x).put(y1).put(height_over);
		
		vLights.put((float)center.x).put(y1 + height).put(height_over + height);
		vLights.put((float)center.x).put(y1).put(height_over + height);
		
		vLights.put((float)center.x).put(y1).put(height_over);
		vLights.put((float)center.x).put(y2).put(height_over);
		vLights.put((float)center.x).put(y1).put(height_over + height);
		vLights.put((float)center.x).put(y2).put(height_over + height);
		
		vLights.put((float)center.x).put(y2 - height).put(height_over + height);
		vLights.put((float)center.x).put(y2).put(height_over + height);
		
		vLights.put((float)center.x).put(y2 - height).put(0f);
		vLights.put((float)center.x).put(y2).put(0f);
		vLights.put((float)center.x).put(y2 - height).put(height_over);
		vLights.put((float)center.x).put(y2).put(height_over);
		
		vLights.flip();
		
		vertices_indicator = vLights;
		**/
	}
	
	public void draw(GLAutoDrawable glautodrawable) {
		GL2 gl2 = glautodrawable.getGL().getGL2();
		
		//gl2.glColor3f(1f, .1f, .1f);
		//gl2.glVertexPointer(3, GL.GL_FLOAT, 0, vertices_indicator);
		//gl2.glDrawArrays(GL2.GL_QUAD_STRIP, 0, 16);
		
		//IntBuffer ib = GLBuffers.newDirectIntBuffer(3);

		//gl2.glGenBuffers(3, ib);
		//int vHandle = ib.get(0);
		//int cHandle = ib.get(1);
		//int iHandle = ib.get(2);

		//gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		//gl2.glEnableClientState(GL2.GL_COLOR_ARRAY);

		/**
		gl2.glBindVertexArray(vHandle);
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vHandle);
		gl2.glBufferData(GL2.GL_ARRAY_BUFFER, vertices.capacity() * GLBuffers.SIZEOF_FLOAT, vertices, GL2.GL_STATIC_DRAW);
		gl2.glVertexPointer(3, GL2.GL_FLOAT, 3 << 2, 0L);

		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, cHandle);
		gl2.glBufferData(GL2.GL_ARRAY_BUFFER, colors.capacity() * GLBuffers.SIZEOF_FLOAT, colors, GL2.GL_STATIC_DRAW);
		gl2.glColorPointer(3, GL2.GL_FLOAT, 3 << 2, 0L);

		gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, iHandle);
		gl2.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, index.capacity() * GLBuffers.SIZEOF_SHORT, index, GL2.GL_STATIC_DRAW);
		
		gl2.glDrawElements(GL2.GL_TRIANGLES, index.capacity(), GL2.GL_UNSIGNED_SHORT, 0L);

		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);

		gl2.glDisableClientState(GL2.GL_COLOR_ARRAY);
		//gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);

		// cleanup VBO handles
		gl2.glDeleteBuffers(3, ib);
		**/
		
		gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl2.glEnableClientState(GL2.GL_COLOR_ARRAY);

		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vHandle);
		gl2.glBufferData(GL2.GL_ARRAY_BUFFER, vertices.capacity() * GLBuffers.SIZEOF_FLOAT, vertices, GL2.GL_STATIC_DRAW);
		gl2.glVertexPointer(3, GL2.GL_FLOAT, 3, 0);
		
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, cHandle);
		gl2.glBufferData(GL2.GL_ARRAY_BUFFER, colors.capacity() * GLBuffers.SIZEOF_FLOAT, colors, GL2.GL_STATIC_DRAW);
		gl2.glColorPointer(3, GL2.GL_FLOAT, 3, 0);
		
		gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, iHandle);
		gl2.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, index.capacity() * GLBuffers.SIZEOF_SHORT, index, GL2.GL_STATIC_DRAW);
		gl2.glDrawElements(GL2.GL_TRIANGLES, 10, GL.GL_SHORT, 0);
		
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		gl2.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
	}
	
	public void cleanup(GLAutoDrawable glautodrawable) {
		GL2 gl2 = glautodrawable.getGL().getGL2();
		
		ib.put(0, vHandle);
		ib.put(1, cHandle);
		ib.put(2, iHandle);
		gl2.glDeleteBuffers(3, ib);
	}
}
