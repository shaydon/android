package net.cyclestreets.liveride;

import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Waypoints;

import org.osmdroid.util.GeoPoint;

final class ReplanFromHere extends LiveRideState
      implements Route.Listener
{
  private LiveRideState next_;

  ReplanFromHere(final LiveRideState previous, final Journey journey, final GeoPoint whereIam)
  {
    super(previous);
    notify("Too far away. Re-planning the journey.");

    next_ = this;

    // Reroute through unvisited waypoints, preserving speed, journey type and waypoint numbering.
    
    final Waypoints waypoints = new Waypoints();
    waypoints.add(whereIam);
    for(final GeoPoint point : journey.upcomingWaypoints())
      waypoints.add(point);
    
    Route.softRegisterListener(this);
    Route.PlotRoute(journey.plan(),
                    journey.speed(),
                    context(),
                    waypoints,
                    journey.waypointNumberOffset()
                  + journey.waypoints().count()
                  - waypoints.count());
  } // ReplanFromHere
  
  @Override
  public final boolean stationaryUpdates() { return true; }
  
  @Override
  public LiveRideState update(Journey journey, GeoPoint whereIam, int accuracy)
  {
    return next_;
  } // update

  @Override
  public boolean isStopped() { return false; }

  @Override
  public boolean arePedalling() { return true; }

  @Override
  public void onNewJourney(Journey journey, Waypoints waypoints)
  {
    next_ = new HuntForSegment(this);
    Route.unregisterListener(this);
  } // onNewJourney

  @Override
  public void onResetJourney()
  {
  } // onResetJourney
} // class ReplanFromHere
