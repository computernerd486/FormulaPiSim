package sim.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import sim.object.Bot;
import sim.object.IndicatorBar.Status;
import sim.object.Track;

public class AIBotUpdater extends BotUpdater {

	public enum State { INIT, STAGE, READY, GO }
	
	protected Track track;
	public int lane;
	public State state;
	public boolean lightStart;
	
	public AIBotUpdater(Bot bot, Track track, int lane) {
		super(bot);
		
		this.track = track;
		this.lane = lane;
		this.state = State.INIT;
		this.lightStart = true;
	}
	
	@Override
	public void start()
	{
		running = true;
		timer = new Timer(20, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				if (lightStart) {
					if (state == State.INIT && track.lights.status == Status.GREEN)
						state = State.STAGE;
					
					if (state == State.STAGE && track.lights.status == Status.RED)
						state = State.READY;
					
					if (state == State.READY && track.lights.status == Status.GREEN)
						state = State.GO;
				}
				
				if (state == State.GO) {
					calcMotors();
					bot.move();
				}
				
				timer.restart();
			}
		});
		timer.start();
	}
	
	float angle = 0f;
	float velocity = 1.0f;
	float o_error = 0f;
	float kp = 0.01f, ki = 0.01f, kd = 0.005f;
	
	public int targetIndex = 1;
	
	private void calcMotors() {
		
		Point2D target = track.nodes[targetIndex].lane[lane];
		Point2D next = track.nodes[(targetIndex + 1) % track.nodes.length].lane[lane];
		
		float distance_t = (float) Math.sqrt(Math.pow(target.x - bot.position.x, 2) + Math.pow(target.y - bot.position.y,2));
		float distance_n = (float) Math.sqrt(Math.pow(next.x - bot.position.x, 2) + Math.pow(next.y - bot.position.y,2));
		
		if (distance_t < 10) {
			target = next;
			targetIndex = (targetIndex + 1) % track.nodes.length;
		}
		
		float e = (float) (Math.atan2(target.y - bot.position.y, target.x - bot.position.x));

		//System.out.println(distance_t + " : " + distance_n + " -- " + e);
		
		bot.setDirection((float)Math.toDegrees(e));
		
		float max_speed = .5f;		

		bot.p_m1 = clamp(1, -max_speed, max_speed);
		bot.p_m2 = clamp(1, -max_speed, max_speed);
	}
	
	private float clamp(float val, float min, float max)
	{
		 return Math.max(min, Math.min(max, val));
	}
	
	public float lerp(float min, float max, float v) {
		 return (1-v)*min + v*max;
	}

}
