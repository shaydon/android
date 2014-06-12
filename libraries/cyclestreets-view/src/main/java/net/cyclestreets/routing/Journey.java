package net.cyclestreets.routing;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.util.Collections;

import org.osmdroid.util.GeoPoint;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

public class Journey 
{
  private Waypoints waypoints_;
  private Segments segments_;
  private int activeSegment_;
  private int waypointNumberOffset_;
    
  static public final Journey NULL_JOURNEY;
  static {
    NULL_JOURNEY = new Journey();
    NULL_JOURNEY.activeSegment_ = -1;
  }

  private Journey() 
  {
    waypoints_ = new Waypoints();
    segments_ = new Segments();
    activeSegment_ = 0;   
    waypointNumberOffset_ = 0;
  } // PlannedRoute
  
  private Journey(final Waypoints waypoints, final int waypointNumberOffset)
  {
    this();
    if(waypoints != null)
      waypoints_ = waypoints;
    waypointNumberOffset_ = waypointNumberOffset;
  } // Journey

  public boolean isEmpty() { return segments_.isEmpty(); }
  public Segments segments() { return segments_; }
  
  private Segment.Start s() { return segments_.first(); }
  private Segment.End e() { return segments_.last(); }

  public Waypoints waypoints() { return waypoints_; }
    
  public int waypointNumberOffset() { return waypointNumberOffset_; }
  
  public String url() { return "http://cycle.st/j" + itinerary(); }
  public int itinerary() { return s().itinerary(); }
  public String name() { return s().name(); }
  public String plan() { return s().plan(); }
  public int speed() { return s().speed(); }
  public int total_distance() { return e().total_distance(); }

  /////////////////////////////////////////
  public void setActiveSegmentIndex(int index) { activeSegment_ = index; }
  public void setActiveSegment(final Segment seg) 
  {
    for(int i = 0; i != segments_.count(); ++i)
      if(seg == segments_.get(i))
      {
        setActiveSegmentIndex(i);
        break;
      }
  } // setActiveSegment
  public int activeSegmentIndex() { return activeSegment_; }
  
  public Segment activeSegment() { return activeSegment_ >= 0 ? segments_.get(activeSegment_) : null; }
  public Segment nextSegment() 
  {
    if(atEnd())
      return activeSegment();
    return segments_.get(activeSegment_+1);
  } // nextSegment
  
  public boolean atStart() { return activeSegment_ <= 0; }
  public boolean atWaypoint() { return activeSegment() instanceof Segment.Waymark; }
  public boolean atEnd() { return activeSegment_ == segments_.count()-1; }
  
  public void regressActiveSegment() 
  { 
    if(!atStart()) 
      --activeSegment_; 
  } // regressActiveSegment
  public void advanceActiveSegment() 
  { 
    if(!atEnd()) 
      ++activeSegment_; 
  } // advanceActiveSegment

  /**
   * Returns all segments up to and including the active segment.
   */
  public Segments passedSegments()
  {
    final Segments passed = new Segments();
    for(int i = 0; i <= activeSegment_; ++i)
      passed.add(segments_.get(i));
    return passed;
  }
  
  /**
   * Returns all segments beyond the active segment.
   */
  public Segments upcomingSegments()
  {
    final Segments upcoming = new Segments();
    for(int i = activeSegment_ + 1; i < segments_.count(); ++i)
      upcoming.add(segments_.get(i));
    return upcoming;
  } // upcomingSegments

  /**
   * Returns segments up to the first routing waypoint beyond the active segment.
   * 
   * Includes the waypoint segment.
   */
  public Segments segmentsUpToNextWaypoint()
  {
    final Segments segments = passedSegments();
    for(final Segment seg : upcomingSegments()) {
      segments.add(seg);
      if (seg.isWaypoint()) break;
    }
    return segments;
  } // segmentsUpToNextWaypoint
  
  public Iterator<GeoPoint> points()
  {
    return segments_.pointsIterator();
  } // points
  
  /**
   * Returns all routing waypoints beyond the active segment.
   * 
   * Includes the end and possibly even the start (if there's no active segment yet).
   */
  public Waypoints upcomingWaypoints()
  {
    final Iterator<GeoPoint> waypoints = waypoints_.reversed().iterator();
    final Waypoints upcoming = new Waypoints();
    
    for(final Segment seg : upcomingSegments().reversed())
      if (seg.isWaypoint() && waypoints.hasNext())
        upcoming.add(waypoints.next());
    
    return upcoming.reversed();
  } // upcomingWaypoints
  
  ////////////////////////////////////////////////////////////////
  static private GeoPoint pD(final GeoPoint a1, final GeoPoint a2)
  {
    return a1 != null ? a1 : a2;
  } // pD

  static Journey loadFromXml(final String xml,
                             final Waypoints points,
                             final String name,
                             final int waypointNumberOffset)
    throws Exception
  {
    final JourneyFactory factory = factory(points, name, waypointNumberOffset);
    
    try {
      Xml.parse(xml, factory.contentHandler());
    } // try
    catch(final Exception e) {
      throw new RuntimeException(e);
    } // catch
      
    return factory.get();
  } // loadFromXml
  
  ////////////////////////////////////////////////////////////////////////////////
  /*
As at 16 October 2012
<?xml version="1.0" encoding="UTF-8"?>
<markers xmlns:cs="http://www.cyclestreets.net/schema/xml/">
  <marker start="Forest Road" finish="Wake Green Road, B4217" startBearing="0" startSpeed="0" 
          start_longitude="-1.878426" start_latitude="52.445953" 
          finish_longitude="-1.880228" finish_latitude="52.443996" crow_fly_distance="315" 
          event="depart" whence="2012-10-16 20:05:15" speed="24" clientRouteId="0" plan="balanced" 
          note="" length="456" time="99" busynance="1203" quietness="38" 
          signalledJunctions="0" signalledCrossings="0" south="52.444034576416" west="-1.88161" 
          north="52.446396" east="-1.87845826148987" name="Forest Road to Wake Green Road, B4217" 
          walk="0" leaving="2012-10-16 20:05:15" arriving="2012-10-16 20:06:54" 
          coordinates="-1.87845826148987,52.4459228515625 -1.879563,52.446396 -1.88050925731659,52.4455528259277 -1.88161,52.444572 -1.881184,52.444214 -1.88020920753479,52.444034576416" 
          grammesCO2saved="85" calories="11" itinerary="5268412" type="route" />
  <waypoint longitude="-1.878426" latitude="52.445953" sequenceId="1" />
  <waypoint longitude="-1.880539" latitude="52.445568" sequenceId="2" />
  <waypoint longitude="-1.880228" latitude="52.443996" sequenceId="3" />
  <marker name="Forest Road" legNumber="1" distance="92" time="20" busynance="157" flow="against" walk="0" provisionName="Residential street" signalledJunctions="0" signalledCrossings="0" turn="" startBearing="305" color="#000000" points="-1.87845826148987,52.4459228515625 -1.879563,52.446396" distances="0,92" elevations="148,149" type="segment" />
  <marker name="Anderton Park Road" legNumber="1" distance="114" time="24" busynance="295" flow="against" walk="0" provisionName="Minor road" signalledJunctions="0" signalledCrossings="0" turn="turn left" startBearing="214" color="#33aa33" points="-1.879563,52.446396 -1.88050925731659,52.4455528259277" distances="0,114" elevations="149,150" type="segment" />
  <marker name="Anderton Park Road" legNumber="2" distance="132" time="32" busynance="398" flow="against" walk="0" provisionName="Minor road" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="214" color="#33aa33" points="-1.88050925731659,52.4455528259277 -1.88161,52.444572" distances="0,132" elevations="150,152" type="segment" />
  <marker name="Wake Green Road, B4217" legNumber="2" distance="49" time="14" busynance="207" flow="with" walk="0" provisionName="Main road" signalledJunctions="0" signalledCrossings="0" turn="turn left" startBearing="144" color="#0000ff" points="-1.88161,52.444572 -1.881184,52.444214" distances="0,49" elevations="152,153" type="segment" />
  <marker name="Wake Green Road, B4217" legNumber="2" distance="69" time="9" busynance="146" flow="with" walk="0" provisionName="Main road" signalledJunctions="0" signalledCrossings="0" turn="bear left" startBearing="107" color="#0000ff" points="-1.881184,52.444214 -1.88020920753479,52.444034576416" distances="0,69" elevations="153,152" type="segment" />
</markers>
   */
  
  static private JourneyFactory factory(final Waypoints waypoints,
                                        final String name,
                                        final int waypointNumberOffset) 
  { 
    return new JourneyFactory(waypoints, name, waypointNumberOffset);
  } // factory
  
  static private class JourneyFactory 
  {    
    private final Journey journey_;
    private final String name_;
    private final int waypointNumberOffset_;
    private int total_time = 0;
    private int total_distance = 0;
    private int itinerary_ = 0;
    private int grammesCO2saved_ = 0;
    private int calories_ = 0;
    private String plan_;
    private int speed_;
    private String start_;
    private String finish_;
    private int leg_ = 1;

    public JourneyFactory(final Waypoints waypoints,
                          final String name,
                          final int waypointNumberOffset) 
    {
      journey_ = new Journey(waypoints, waypointNumberOffset);
      name_ = name;
      waypointNumberOffset_ = waypointNumberOffset;
    } // JourneyFactory
    
    private ContentHandler contentHandler()
    {
      Segment.formatter = DistanceFormatter.formatter(CycleStreetsPreferences.units());

      final RootElement root = new RootElement("markers");
      final Element marker = root.getChild("marker");
      marker.setStartElementListener(new StartElementListener() {
        @Override
        public void start(final Attributes attr)
        {
          final String type = s(attr, "type");
          final String name = s(attr, "name");
          
          if(type.equals("segment"))
          {
            final String packedPoints = s(attr, "points");
            
            final String turn = s(attr, "turn");
             
            final int distance = i(attr, "distance");
            final int time = i(attr, "time");
            final boolean shouldWalk = "1".equals(s(attr, "walk"));
            final int currentLeg = i(attr, "legNumber");
            
            final List<GeoPoint> points = pointsList(packedPoints);
            
            if(currentLeg != leg_) 
            {
              journey_.segments_.add(new Segment.Waymark(leg_ + waypointNumberOffset_, total_distance, points.get(0)));
              leg_ = currentLeg;
            } // if ...              

            total_time += time;
            total_distance += distance;
            final Segment seg = new Segment.Step(name,
                                                 turn,
                                                 shouldWalk,
                                                 total_time,
                                                 distance,
                                                 total_distance,
                                                 points);
            journey_.segments_.add(seg);
          } // if ...
          if(type.equals("route"))
          {
            grammesCO2saved_ = i(attr, "grammesCO2saved");
            calories_ = i(attr, "calories");
            plan_ = s(attr, "plan");
            speed_ = i(attr, "speed");
            itinerary_ = i(attr, "itinerary");
            start_ = s(attr, "name");
            finish_ = s(attr, "finish");
          } // if ...
        } // start

        private String s(final Attributes attr, final String name) { return attr.getValue(name); }
        private int i(final Attributes attr, final String name) 
        { 
          final String v = s(attr, name);
          return v != null ? Integer.parseInt(v) : 0; 
        } // i
      });
      
      if(journey_.waypoints().count() == 0)
        root.getChild("waypoint").setStartElementListener(new StartElementListener() {
          @Override
          public void start(final Attributes attr)
          {
            final double lat = d(attr, "latitude");
            final double lon = d(attr, "longitude");
            
            journey_.waypoints().add(lat, lon);
          } // start
          
          private double d(final Attributes attr, final String name) 
          { 
            final String v = attr.getValue(name);
            return v != null ? Double.parseDouble(v) : 0; 
          } // i
        });
      
      root.setEndElementListener(new EndElementListener() {
        @Override
        public void end()
        {
          final GeoPoint from = journey_.waypoints().first();
          final GeoPoint to = journey_.waypoints().last();

          final GeoPoint pstart = journey_.segments_.startPoint();
          final GeoPoint pend = journey_.segments_.finishPoint();
          final Segment startSeg = new Segment.Start(itinerary_,
                                 name_ != null ? name_ : start_,
                                 plan_, 
                                 speed_,
                                 total_time, 
                                 total_distance, 
                                 calories_,
                                 grammesCO2saved_,
                                 Collections.list(pD(from, pstart), pstart));
          final Segment endSeg = new Segment.End(finish_, 
                               total_time, 
                               total_distance, 
                               Collections.list(pend, pD(to, pend)));
          journey_.segments_.add(startSeg);
          journey_.segments_.add(endSeg);
        } // end
      });

      return root.getContentHandler();
    } // contentHandler
    
    public Journey get()
    {
      return journey_;
    } // get
    
    private List<GeoPoint> pointsList(final String points)
    {
      final List<GeoPoint> pl = new ArrayList<GeoPoint>();
      final String[] coords = points.split(" ");
      for (final String coord : coords) 
      {
        final String[] yx = coord.split(",");
        final GeoPoint p = new GeoPoint(Double.parseDouble(yx[1]), Double.parseDouble(yx[0]));
        pl.add(p);
      } // for ...
      return pl;
    } // points

  } // class JourneyFactory
  
} // class Journey
