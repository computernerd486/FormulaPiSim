package sim.object;

import java.nio.FloatBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.GLBuffers;

import sim.util.Point2D;

public class IndicatorBar {
	
	public enum Status {OFF, RED, GREEN}

	public Status status = Status.OFF;
	public Track track;
	
	FloatBuffer vertices_indicator;
	Point2D center;
	float trackDiameter;
	
	public IndicatorBar(Point2D center, Float radius) {
		this.center = center;
		this.trackDiameter = radius;
	}
	
	public void prepIndicatorBuffer(){
		
		float height_over = 38f, height = 6f;
		float y1 = (float) (center.y + trackDiameter), y2 = (float) (center.y - trackDiameter);
		
		FloatBuffer vLights = GLBuffers.newDirectFloatBuffer((16) * 3 * 2);
		
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
	}
	
	public void draw(GLAutoDrawable glautodrawable) {
		GL2 gl2 = glautodrawable.getGL().getGL2();
		
		gl2.glColor3f(1f, .1f, .1f);
		gl2.glVertexPointer(3, GL.GL_FLOAT, 0, vertices_indicator);
		gl2.glDrawArrays(GL2.GL_QUAD_STRIP, 0, 16);
	}
}
