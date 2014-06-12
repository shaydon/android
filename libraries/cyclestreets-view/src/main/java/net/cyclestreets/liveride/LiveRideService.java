package net.cyclestreets.liveride;

import org.osmdroid.util.GeoPoint;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class LiveRideService extends Service
  implements LocationListener
{
  private IBinder binder_;
  private LocationManager locationManager_;
  private Location lastLocation_;
  private LiveRideState stage_;

  private static int updateDistance = 5;  // metres
  private static int updateTime = 500;    // milliseconds

  @Override
  public void onCreate()
  {
    binder_ = new Binding();
    locationManager_ = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    stage_ = LiveRideState.StoppedState(this);
  } // onCreate

  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId)
  {
    return Service.START_NOT_STICKY;
  } // onStartCommand

  @Override
  public void onDestroy()
  {
    stopRiding();
    super.onDestroy();
  } // onDestroy

  @Override
  public IBinder onBind(final Intent intent)
  {
    return binder_;
  } // onBind

  public void startRiding()
  {
    if(!stage_.isStopped())
      return;
    
    stepStage(LiveRideState.InitialState(this));
  } // startRiding

  public void stopRiding()
  {
    stepStage(LiveRideState.StoppedState(this));
  } // stopRiding

  public boolean areRiding()
  {
    return stage_.arePedalling();
  } // onRide
  
  public Location lastLocation()
  {
    return lastLocation_;
  } // lastLocation

  private void stepStage(final LiveRideState nextStage)
  {
    if(nextStage.isStopped())
    {
      locationManager_.removeUpdates(this);
    }
    else if(stage_.isStopped() || (stage_.stationaryUpdates() != nextStage.stationaryUpdates()))
    {
      locationManager_.removeUpdates(this);  // In case it's possible to be multiply registered.
      locationManager_.requestLocationUpdates(
        LocationManager.GPS_PROVIDER, updateTime, nextStage.stationaryUpdates() ? 0 : updateDistance, this);
    }
    stage_ = nextStage;
  } // stepStage
  
  public class Binding extends Binder
  {
    private LiveRideService service() { return LiveRideService.this; }
    public void startRiding() { service().startRiding(); }
    public void stopRiding() { service().stopRiding(); }
    public boolean areRiding() { return service().areRiding(); }
    public String stage() { return stage_.getClass().getSimpleName(); }
    public Location lastLocation() { return service().lastLocation(); }
  } // class LocalBinder

  // ///////////////////////////////////////////////
  // location listener
  @Override
  public void onLocationChanged(final Location location)
  {
    if(!Route.available())
    {
      stopRiding();
      return;
    } // if ...

    lastLocation_ = location;
    
    final GeoPoint whereIam = new GeoPoint(location);
    final float accuracy = location.hasAccuracy() ? location.getAccuracy() : 2;

    final Journey journey = Route.journey();
    
    if(CycleStreetsPreferences.verboseVoiceGuidance())
    {
      LiveRideState previous;
      do
      {
        previous = stage_;
        stepStage(stage_.update(journey, whereIam, (int) accuracy));
      } while (stage_ != previous);
    }
    else
    {
      stepStage(stage_.update(journey, whereIam, (int) accuracy));
    }
  } // onLocationChanged

  @Override
  public void onProviderDisabled(String arg0) { }
  @Override
  public void onProviderEnabled(String arg0) { }
  @Override
  public void onStatusChanged(String arg0, int arg1, Bundle arg2) { }
} // class LiveRideService
