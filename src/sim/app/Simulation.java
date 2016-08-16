/**
* Copyright 2016 Tim Pearce
**/

package sim.app;

import javax.swing.JFrame;

public class Simulation {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TrackView w = new TrackView();
		w.setVisible(true);
		w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		BotView v = new BotView();
		v.setVisible(true);
		w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		v.camera = w.botView;
		v.bot = w.bot;
	}

}
