package sim.object;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import sim.util.Point2D;

public class BotModel {
	
	//
	//vertices:
	//center 0,0
	//baseplate
	//-4.2,-12.5,6
	// 4.2,-12.5,6
	// 4.2, 12.5,6
	//-4.2, 12.5,6
	
	//tire centers:
	//-5.25,-4.75
	//-5.25, 4.75
	// 5.25, 4.75
	// 5.25,-4.75;
	
	//tire width:
	//4

	public float tyre_diameter = 8.4f;
	public float tyre_radius = tyre_diameter / 2f;
	public float tyre_width = 4;
	public float base_width = 8.4f;
	public float base_length = 12.5f;

	int rimSteps = 20;

	FloatBuffer vertices_tread;
	FloatBuffer texCoords_tread;
	int treadVCount;
	
	FloatBuffer vertices_rim;
	FloatBuffer texCoords_rim;
	
	FloatBuffer vertices_base;
	
	Texture tex_tyre;
	Texture tex_lid;
	Texture tex_rim;
	
	private static final String fn_tex_tyre = "img/bot_tyre.png";
	private static final String fn_tex_lid = "img/bot_lid.png";
	private static final String fn_tex_rim = "img/bot_rim.png";
	
	float[][] tyre_offset = new float[][] {{5f, 5.25f, 0}, {-5f, 5.25f, 0}, {5f, -5.25f, 180}, {-5f, -5.25f, 180}};
	
	public void loadTextures(GLAutoDrawable glautodrawable) {
		GL2 gl2 = glautodrawable.getGL().getGL2();
		Texture t;
		
		try {
			tex_tyre = textureLoad(gl2, fn_tex_tyre);
			tex_lid = textureLoad(gl2, fn_tex_lid);
			tex_rim = textureLoad(gl2, fn_tex_rim);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setupBuffer() {
		
		ArrayList<Point2D> edge = new ArrayList<>();
		//wheel
		int step = 360 / rimSteps;
		for (int a = 0; a < 360; a += step) {	
			edge.add(new Point2D(
					Math.cos(Math.toRadians(a)) * tyre_radius, 
					Math.sin(Math.toRadians(a)) * tyre_radius
				));
		}
		treadVCount = (edge.size() + 1) * 2;
		
		for (Point2D p : edge) {
			System.out.println(p);
		}
		
		//Segements * sides * coord * float size
		FloatBuffer vTread = GLBuffers.newDirectFloatBuffer((treadVCount + 1) * 3 * GLBuffers.SIZEOF_FLOAT * 2);
		FloatBuffer cTread = GLBuffers.newDirectFloatBuffer((treadVCount + 1) * 2 * GLBuffers.SIZEOF_FLOAT * 2);
		
		for (int i = 0; i < edge.size(); i++) {
			Point2D p1 = edge.get(i);
			vTread.put((float)p1.x).put(tyre_width).put((float)p1.y);
			vTread.put((float)p1.x).put(0f).put((float)p1.y);
		}
		
		Point2D p = edge.get(0);
		vTread.put((float)p.x).put(tyre_width).put((float)p.y);
		vTread.put((float)p.x).put(0f).put((float)p.y);

		for (int i = 0; i < edge.size(); i++) {
			if (i % 2 == 0) { cTread.put(1).put(0); } else { cTread.put(1).put(1); } 
			if (i % 2 == 0) { cTread.put(0).put(0); } else { cTread.put(0).put(1); }
		}
		cTread.put(1).put(0);
		cTread.put(0).put(0);
		
		vTread.flip();
		cTread.flip();
		
		vertices_tread = vTread;
		texCoords_tread = cTread;

		double anglePerStep = 2.0 * Math.PI / rimSteps;
		float x;
		float y;
		
		FloatBuffer vRim = GLBuffers.newDirectFloatBuffer(rimSteps * 4 * 2 * 3 * GLBuffers.SIZEOF_FLOAT);
		FloatBuffer cRim = GLBuffers.newDirectFloatBuffer(rimSteps * 4 * 2 * 2 * GLBuffers.SIZEOF_FLOAT);
		
		// Outside rim
		for (int i = 0; i < rimSteps; ++i) {
			x = -tyre_radius * (float)(Math.sin(i * anglePerStep));
			y = tyre_radius * (float)(Math.cos(i * anglePerStep));
			vRim.put(0).put(tyre_width).put(0); 
			vRim.put(x).put(tyre_width).put(y);
			
			x = -tyre_radius * (float)(Math.sin((i+1) * anglePerStep));
			y = tyre_radius * (float)(Math.cos((i+1) * anglePerStep)); 
			vRim.put(x).put(tyre_width).put(y);
			vRim.put(0).put(tyre_width).put(0); 
			
			x = -0.25f * (float)(Math.sin(i * anglePerStep)) + 0.25f;
			y = 0.5f * (float)(Math.cos(i * anglePerStep)) + 0.5f; 
			cRim.put(0.25f).put(0.5f);
			cRim.put(x).put(y);
			x = -0.25f * (float)(Math.sin((i+1) * anglePerStep)) + 0.25f;
			y = 0.5f * (float)(Math.cos((i+1) * anglePerStep)) + 0.5f; 
			cRim.put(x).put(y);
			cRim.put(0.25f).put(0.5f);
		}

		// Inside rim
		for (int i = 0; i < rimSteps; ++i) {
			x = -tyre_radius * (float)(Math.sin(i * anglePerStep));
			y = tyre_radius * (float)(Math.cos(i * anglePerStep));
			vRim.put(0).put(0).put(0); 
			vRim.put(x).put(0).put(y);
			
			x = -tyre_radius * (float)(Math.sin((i+1) * anglePerStep));
			y = tyre_radius * (float)(Math.cos((i+1) * anglePerStep)); 
			vRim.put(x).put(0).put(y);
			vRim.put(0).put(0).put(0); 
			
			x = -0.25f * (float)(Math.sin(i * anglePerStep)) + 0.75f;
			y = 0.5f * (float)(Math.cos(i * anglePerStep)) + 0.5f; 
			cRim.put(0.75f).put(0.5f);
			cRim.put(x).put(y);
			x = -0.25f * (float)(Math.sin((i+1) * anglePerStep)) + 0.75f;
			y = 0.5f * (float)(Math.cos((i+1) * anglePerStep)) + 0.5f; 
			cRim.put(x).put(y);
			cRim.put(0.75f).put(0.5f);
		}

		/*vRim.put( tyre_radius).put(0f).put(-tyre_radius);
		vRim.put(-tyre_radius).put(0f).put(-tyre_radius);
		vRim.put(-tyre_radius).put(0f).put( tyre_radius);
		vRim.put( tyre_radius).put(0f).put( tyre_radius);
		
		vRim.put(-tyre_radius).put(tyre_width).put(-tyre_radius);
		vRim.put( tyre_radius).put(tyre_width).put(-tyre_radius);
		vRim.put( tyre_radius).put(tyre_width).put( tyre_radius);
		vRim.put(-tyre_radius).put(tyre_width).put( tyre_radius);
		
		cRim.put(.5f).put(0);
		cRim.put(1).put(0);
		cRim.put(1).put(1);
		cRim.put(.5f).put(1);
		
		cRim.put(0).put(0);
		cRim.put(.5f).put(0);
		cRim.put(.5f).put(1);
		cRim.put(0).put(1);*/

		vRim.flip();
		cRim.flip();
		
		vertices_rim = vRim;
		texCoords_rim = cRim;
		
		FloatBuffer vBase = GLBuffers.newDirectFloatBuffer(20 * 3 * GLBuffers.SIZEOF_FLOAT);
		
		float half_bl = base_length / 2;
		float half_bw = base_width / 2;
		float tyre_sl = half_bw + tyre_width;
		float tyre_sw = 2f;
		float tyre_sh = 3f;
				
		vBase.put(half_bl).put(tyre_sl).put(0);
		vBase.put(half_bl).put(-tyre_sl).put(0);
		vBase.put(half_bl).put(tyre_sl).put(-tyre_sh);
		vBase.put(half_bl).put(-tyre_sl).put(-tyre_sh);
		vBase.put(half_bl - tyre_sw).put(tyre_sl).put(-tyre_sh);
		vBase.put(half_bl - tyre_sw).put(-tyre_sl).put(-tyre_sh);
		vBase.put(half_bl - tyre_sw).put(tyre_sl).put(0);
		vBase.put(half_bl - tyre_sw).put(-tyre_sl).put(0);
		vBase.put(half_bl).put(tyre_sl).put(0);
		vBase.put(half_bl).put(-tyre_sl).put(0);
		
		vBase.put(-half_bl).put(tyre_sl).put(0);
		vBase.put(-half_bl).put(-tyre_sl).put(0);
		vBase.put(-half_bl).put(tyre_sl).put(-tyre_sh);
		vBase.put(-half_bl).put(-tyre_sl).put(-tyre_sh);
		vBase.put(-half_bl + tyre_sw).put(tyre_sl).put(-tyre_sh);
		vBase.put(-half_bl + tyre_sw).put(-tyre_sl).put(-tyre_sh);
		vBase.put(-half_bl + tyre_sw).put(tyre_sl).put(0);
		vBase.put(-half_bl + tyre_sw).put(-tyre_sl).put(0);
		vBase.put(-half_bl).put(tyre_sl).put(0);
		vBase.put(-half_bl).put(-tyre_sl).put(0);

		
		vBase.flip();
		vertices_base = vBase;
		
		
	}
	
	public void cleanup(GLAutoDrawable glautodrawable) {}
	
	/**
	 * 
	 * @param glautodrawable
	 * @param bot
	 * @param lidOverride, nullable, pass if you want a diffrent lid drawn
	 */
	public void draw(GLAutoDrawable glautodrawable, Bot bot, Texture lidOverride) {
		GL2 gl2 = glautodrawable.getGL().getGL2();
		
		gl2.glEnable(GL2.GL_BLEND);
		gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl2.glDisable(GL.GL_CULL_FACE);
		gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		
		gl2.glPushMatrix();
		gl2.glTranslatef((float)bot.position.x, (float)bot.position.y, 0);
		gl2.glRotatef(bot.direction, 0f, 0f, 1f);
		
		// Chassis
		gl2.glPushMatrix();
		gl2.glTranslatef(0,0,6f);
		gl2.glColor3f(.8f, .8f, .85f);
		gl2.glVertexPointer(3, GL.GL_FLOAT, 0, vertices_base);
		gl2.glDrawArrays(GL2.GL_QUAD_STRIP, 0, 10);
		gl2.glDrawArrays(GL2.GL_QUAD_STRIP, 10, 10);
		gl2.glPopMatrix();

		//Tyres
		gl2.glColor3f(0f, 1f, 1f);
		
		for (float[] pos : tyre_offset )
		{
			gl2.glPushMatrix();
			gl2.glTranslatef(pos[0], pos[1], tyre_radius);
			gl2.glRotatef(pos[2], 0f, 0f, 1f);
			
			setTex(gl2, tex_tyre, texCoords_tread);
			gl2.glVertexPointer(3, GL.GL_FLOAT, 0, vertices_tread);
			gl2.glDrawArrays(GL2.GL_QUAD_STRIP, 0, treadVCount);
			unsetTex(gl2, tex_tyre);
			
			gl2.glPopMatrix();
		}

		//baseplate		
		gl2.glColor3f(1f, 0f, 1f);
		gl2.glPushMatrix();
		setTex(gl2, (lidOverride == null) ? tex_lid : lidOverride, null);
		gl2.glTranslatef(0,0,6f);
		gl2.glBegin(GL2.GL_QUADS);
		{
			gl2.glTexCoord2d(1,0);
			gl2.glVertex3d(-(base_length / 2), -(base_width/2), .25d);
			gl2.glTexCoord2d(0,0);
			gl2.glVertex3d(-(base_length / 2),  (base_width/2), .25d);
			gl2.glTexCoord2d(0,1);
			gl2.glVertex3d( (base_length / 2),  (base_width/2), .25d);
			gl2.glTexCoord2d(1,1);
			gl2.glVertex3d( (base_length / 2), -(base_width/2), .25d);
		}
		gl2.glEnd();
		unsetTex(gl2, (lidOverride == null) ? tex_lid : lidOverride);
		
		gl2.glPopMatrix();
		

		//Rims
		gl2.glColor3f(0f, 1f, 1f);
		for (float[] pos : tyre_offset )
		{
			gl2.glPushMatrix();
			gl2.glTranslatef(pos[0], pos[1], tyre_radius);
			gl2.glRotatef(pos[2], 0f, 0f, 1f);
					
			setTex(gl2, tex_rim, texCoords_rim);
			gl2.glVertexPointer(3, GL.GL_FLOAT, 0, vertices_rim);
			gl2.glDrawArrays(GL2.GL_QUADS, 0, 8 * rimSteps);
			unsetTex(gl2, tex_rim);
			
			gl2.glPopMatrix();
		}

		//bot matrix
		gl2.glPopMatrix();
		gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
	}
	
	private void setTex(GL2 gl2, Texture tex, FloatBuffer texCoords) {
		if (tex != null) {
			gl2.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
			gl2.glActiveTexture(GL2.GL_TEXTURE0);
			tex.enable(gl2);
			tex.bind(gl2);
			if (texCoords != null) gl2.glTexCoordPointer(2, GL.GL_FLOAT, 0, texCoords);
		} 
	}
	
	private void unsetTex(GL2 gl2, Texture tex) {
		if (tex != null) {
			tex.disable(gl2);
			gl2.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		}
	}
	
	private Texture textureLoad(GL2 gl2, String filename) throws GLException, IOException {
		Texture t = TextureIO.newTexture(Files.newInputStream(Paths.get(filename)), true, ".png");
		t.setTexParameterf(gl2, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		t.setTexParameterf(gl2, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		t.setTexParameterf(gl2, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		t.setTexParameterf(gl2, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
		return t;
	}
	
}
