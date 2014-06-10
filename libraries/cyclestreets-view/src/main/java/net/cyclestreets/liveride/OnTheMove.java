package net.cyclestreets.liveride;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.routing.Journey;

final class OnTheMove extends MovingState
{
  OnTheMove(final LiveRideState previous) 
  {
    super(previous, CycleStreetsPreferences.nearingTurnDistance());
  } // OnTheMove
  
  @Override
  protected LiveRideState transitionState(final Journey journey)
  {
    if(CycleStreetsPreferences.verboseVoiceGuidance())
      return new NearingTurnVerbose(this, journey);
    else
      return new NearingTurn(this, journey);
  } // transitionState
} // class OnTheMove
