/**
* Copyright 2016 Tim Pearce
**/

package sim.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import sim.object.Bot;
import sim.object.LapTimer;

public class BotUpdater{

	protected Bot bot;
	protected Timer timer;
	protected LapTimer laptimer;
	protected boolean running;
	
	
	public BotUpdater(Bot bot){
		this.bot = bot;
		this.laptimer = bot.laptimer;
		running = false;
	}

	public void start()
	{
		running = true;
		timer = new Timer(20, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				bot.move();
				if (laptimer != null)
					laptimer.check();
				
				timer.restart();
			}
		});
		timer.start();
	}
	
	public void stop()
	{
		timer.stop();
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}
	
}
