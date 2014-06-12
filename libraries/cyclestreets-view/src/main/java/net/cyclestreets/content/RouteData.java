package net.cyclestreets.content;

import net.cyclestreets.routing.Waypoints;

public class RouteData 
{
	final String name_;
	final String xml_;
	final Waypoints points_;
	final int waypointNumberOffset_;
	
	public RouteData(final String xml, 
					 final Waypoints points,
					 final String name)
	{
		this(xml, points, name, 0);
	}

	public RouteData(final String xml,
					 final Waypoints points,
					 final String name,
					 final int waypointNumberOffset)
	{
		xml_ = xml;
		points_ = points;
		name_ = name;
		waypointNumberOffset_ = waypointNumberOffset;
	} // RouteData
	
	public String name() { return name_; }
	public String xml() { return xml_; }
	public Waypoints points() { return points_; }
	public int waypointNumberOffset() { return waypointNumberOffset_; }
} // class RouteData
