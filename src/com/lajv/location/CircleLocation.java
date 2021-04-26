package com.lajv.location;

import peersim.config.Configuration;

public class CircleLocation implements Location, Cloneable {

	private static final String CIRCLE_RADIUS = "radius"; // radius of the circle area where to position the nodes
	
	public static int radius;
	
	double x;
	double y;
	String prefix;
	
	public CircleLocation(String prefix) {
		this.prefix = prefix;
		radius = Configuration.getInt(prefix + "." + CIRCLE_RADIUS, 100);
	}

	@Override
	public void randomize() {

		double a = Math.random() * 2 * Math.PI;
		double r = radius * Math.sqrt(Math.random());
		
		// In cartesian coordinates
		this.x = r * Math.cos(a);
		this.y = r * Math.sin(a);	
	}

	@Override
	public double latency(Location otherLocation) {
		CircleLocation other = (CircleLocation) otherLocation;
		double xdiff = other.x - x;
		double ydiff = other.y - y;
		return Math.sqrt(xdiff * xdiff + ydiff * ydiff);
	}

	@Override
	public Object clone() {
		CircleLocation clone = null;
		try {
			clone = (CircleLocation) super.clone();
		} catch (CloneNotSupportedException e) {
			// Never happens
		}
		return clone;
	}

	@Override
	public double getUploadCapacity() {
		return 0;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}


}
