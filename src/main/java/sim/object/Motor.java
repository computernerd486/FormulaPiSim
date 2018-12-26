package sim.object;

public class Motor {
	
	//acceleration rate, percent/20ms
	public float accel_rate = 0.05f;
	public float decel_rate = -0.01f;

	public float spd_sig;
	public float spd_act;
	
	public Motor(){
		spd_act = 0;
		spd_sig = 0;
	}
	
	public void tick() {
		spd_act += Math.min(Math.max((spd_sig - spd_act), decel_rate), accel_rate);
	}
	
	@Override
	public String toString() {
		return "Motor [" + spd_sig + " : " + spd_act + "]"; 
	}
}
