import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class BotUpdater{

	private Bot bot;
	private Timer timer;
	
	
	public BotUpdater(Bot bot){
		this.bot = bot;
	}
	
	
	
	public void start()
	{
		
		timer = new Timer(16, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				bot.move();
				timer.restart();
			}
		});
		timer.start();
	}
	
	public void stop()
	{
		timer.stop();
	}
	
	
	
}