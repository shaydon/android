package net.cyclestreets.liveride;

import net.cyclestreets.routing.Segment;
import net.cyclestreets.CycleStreetsPreferences;

import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Segments;

final class NearingTurn extends MovingState
{
  NearingTurn(final LiveRideState previous, final Journey journey)
  {
    super(previous, CycleStreetsPreferences.turnNowDistance());

    final Segments segments = journey.segments();
    if (CycleStreetsPreferences.verboseVoiceGuidance())
    {
      if (journey.lastWarnedSegmentIndex() > journey.activeSegmentIndex())
        return;
      final StringBuilder instructions = new StringBuilder();
      for (int i = journey.activeSegmentIndex() + 1, n = 1;
           i < segments.count() && n <= 3;
           i++, n++)
      {
        Segment segment = segments.get(i);
        if (n > 1) instructions.append(", then ");
        appendTurnInstruction(instructions, segment);
        journey.setLastWarnedSegmentIndex(i);
        log("Warning segment no. " + i);
        if (segment.numericDistance() >= CycleStreetsPreferences.coalesceWarningsDistance())
          break;
      }
      notify("Coming up: " + instructions.append(".").toString());
    }
    else
    {
      final Segment segment = journey.segments().get(journey.activeSegmentIndex()+1);
      notify("Get ready to " + segment.turn());
    }
  } // NearingEnd

  @Override
  protected LiveRideState transitionState(final Journey journey)
  {
    return new AdvanceToSegment(this, journey);
  } // transitionStatue
} // class NearingTurn
