/**
* Copyright 2016 Tim Pearce
**/

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Tester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pattern p = Pattern.compile("([a-z0-9]+=[0-9.]+)");
		Matcher m = p.matcher("/?m1=1&m2=.5");
		
		System.out.println(m.matches());
	}

}
