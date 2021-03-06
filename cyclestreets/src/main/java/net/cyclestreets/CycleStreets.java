package net.cyclestreets;

import net.cyclestreets.routing.Route;
import net.cyclestreets.util.MapPack;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TabHost;

public class CycleStreets extends MainTabbedActivity
{
	public void onCreate(final Bundle savedInstanceState)	{
    switchMapFile();
    super.onCreate(savedInstanceState);

    final Uri launchUri = getIntent().getData();
    if (launchUri == null)
      return;

    final int itinerary = findItinerary(launchUri);
    if (itinerary == -1)
      return;

    Route.FetchRoute(CycleStreetsPreferences.routeType(),
        itinerary,
        CycleStreetsPreferences.speed(),
        this);
    final SharedPreferences.Editor edit = prefs().edit();
    edit.putString("TAB", "");
    edit.commit();
  } // onCreate

  private int findItinerary(final Uri launchUri) {
    try {
      final String itinerary = extractItinerary(launchUri);
      return Integer.parseInt(itinerary);
    } catch(Exception whatever) {
      return -1;
    } // catch
  } // finddItinerary

  private String extractItinerary(final Uri launchUri) {
    final String host = launchUri.getHost();

    if ("cycle.st".equals(host))
      return launchUri.getPath().substring(2);

    if ("m.cyclestreets.net".equals(host)) {
      final String frag = launchUri.getFragment();
      return frag.substring(0, frag.indexOf('/'));
    }

    final String path = launchUri.getPath().substring(8);
    return path.replace("/", "");
  } // extractItinerary

  protected void addTabs(final TabHost tabHost) {
    addTab("Route Map", R.drawable.ic_tab_planroute, RouteMapFragment.class);
    addTab("Itinerary", R.drawable.ic_tab_itinerary, ItineraryFragment.class);
    addTab("Photomap", R.drawable.ic_tab_photomap, PhotoMapFragment.class);
    addTab("More ...", R.drawable.ic_tab_more, MoreFragment.class);
  } // addTabs

	private void switchMapFile() {
	  final String mappackage = getIntent().getStringExtra("mapfile");
	  if(mappackage == null)
	    return;
	  final MapPack pack = MapPack.findByPackage(mappackage);
	  if(pack == null)
	    return;
	  CycleStreetsPreferences.enableMapFile(pack.path());
	} // switchMapFile

	public void showMap()	{
		setCurrentTab(0);
	} // showMap
} // class CycleStreets
