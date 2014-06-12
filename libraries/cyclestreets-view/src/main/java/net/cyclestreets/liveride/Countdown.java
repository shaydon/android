package net.cyclestreets.liveride;

import android.media.ToneGenerator;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.routing.Journey;

final class Countdown extends MovingState
{
    Countdown(final LiveRideState previous, final int previousTransitionThreshold)
    {
        super(previous, previousTransitionThreshold - CycleStreetsPreferences.countdownStepDistance());

        playTone(ToneGenerator.TONE_DTMF_4, 200);
    } // Countdown

    @Override
    protected LiveRideState transitionState(final Journey journey)
    {
      if (transitionThreshold() <= CycleStreetsPreferences.endCountdownDistance())
        return new AdvanceToSegment(this, journey);
      else
        return new Countdown(this, transitionThreshold());
    } // transitionState
} // class Countdown
