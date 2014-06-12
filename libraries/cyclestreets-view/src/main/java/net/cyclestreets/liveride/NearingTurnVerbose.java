package net.cyclestreets.liveride;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.routing.Segments;

final class NearingTurnVerbose extends MovingState
{
  NearingTurnVerbose(final LiveRideState previous, final Journey journey)
  {
    super(previous, CycleStreetsPreferences.almostAtTurnDistance());

    if(journey.lastWarnedSegmentIndex() > journey.activeSegmentIndex())
      return;
    final Segments segments = journey.segments();
    final StringBuilder instructions = new StringBuilder();
    for(int i = journey.activeSegmentIndex() + 1, n = 1;
        i < segments.count() && n <= 3;
        i++, n++)
    {
      Segment segment = segments.get(i);
      if (n > 1) instructions.append(", then ");
      appendTurnAndStreet(instructions, segment);
      journey.setLastWarnedSegmentIndex(i);
      log("Warning segment no. " + i);
      if(segment.numericDistance() >= CycleStreetsPreferences.coalesceWarningsDistance())
        break;
    }
    notify("Coming up: " + instructions.append(".").toString());
  } // NearingTurnVerbose

  @Override
  protected LiveRideState transitionState(final Journey journey)
  {
    return new AlmostAtTurn(this, journey);
  } // transitionState
} // class NearingTurnVerbose
