package net.cyclestreets.liveride;

import net.cyclestreets.routing.Journey;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.media.ToneGenerator;
import android.speech.tts.TextToSpeech;

final class LiveRideStart extends LiveRideState
{
  LiveRideStart(final Context context, final TextToSpeech tts, ToneGenerator toneGenerator)
  {
    super(context, tts, toneGenerator);
    notify("Live Ride", "Starting Live Ride");
  } // LiveRideStart
  
  @Override
  public final boolean stationaryUpdates() { return true; }
  
  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy)
  {
    log("itinerary:" + journey.itinerary());
    notify("Live Ride", "Live Ride");
    journey.setActiveSegmentIndex(0);
    journey.setLastWarnedSegmentIndex(-1);
    notify(journey.activeSegment());
    return new HuntForSegment(this);
  } // update

  @Override
  public boolean isStopped() { return false; }
  @Override
  public boolean arePedalling() { return false; }
} // class LiveRideStart
