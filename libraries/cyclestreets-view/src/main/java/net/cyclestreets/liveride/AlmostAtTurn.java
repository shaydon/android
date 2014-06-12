package net.cyclestreets.liveride;

import net.cyclestreets.routing.Segment;
import net.cyclestreets.CycleStreetsPreferences;

import net.cyclestreets.routing.Journey;

final class AlmostAtTurn extends MovingState
{
    AlmostAtTurn(final LiveRideState previous, final Journey journey)
    {
        super(previous, CycleStreetsPreferences.startCountdownDistance());

        final Segment segment = journey.segments().get(journey.activeSegmentIndex()+1);
        notify(segment);
    } // AlmostAtTurn

    @Override
    protected LiveRideState transitionState(final Journey journey)
    {
        return new Countdown(this, transitionThreshold());
    } // transitionState
} // class AlmostAtTurn
