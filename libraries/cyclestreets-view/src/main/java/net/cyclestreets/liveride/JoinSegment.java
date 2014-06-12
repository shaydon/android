package net.cyclestreets.liveride;

import android.media.ToneGenerator;

import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Segment;

import org.osmdroid.util.GeoPoint;

final class JoinSegment extends LiveRideState
{
  JoinSegment(final LiveRideState previous,
              final Journey journey,
              final Segment segment)
  {
    super(previous);
    if (journey.segmentIndex(segment) < journey.activeSegmentIndex())
      journey.setLastWarnedSegmentIndex(-1);
    journey.setActiveSegment(segment);
    playTone(ToneGenerator.TONE_DTMF_4, 1000);
  } // JoinSegment
  
  @Override
  public boolean stationaryUpdates() { return false; }
  
  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy)
  {
    Segment seg = journey.activeSegment();
    notify(speakableStreet(seg) + ": head " + compassBearing(seg.nearestBearing(whereIam)) + " " + seg.distanceFromEnd(whereIam) + "m.");
    if(journey.atWaypoint())
      return new PassingWaypoint(this);
    if(journey.atEnd())
      return new Arrivee(this);
    
    return new OnTheMove(this);
  } // update

  private static String[] BEARINGS_ =
    {"north", "north-east", "east", "south-east", "south", "south-west", "west", "north-west"};

  private String compassBearing(int bearing)
  {
    while(bearing < 0) bearing += 360;
    return BEARINGS_[Math.round((float) bearing / 45.0F) % 8];
  }

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return true; }
}
