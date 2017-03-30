package ggr.urbanadventure;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener {

    private GoogleMap mMap;
    private List<AreaLocation> allAreaLocations = new ArrayList<AreaLocation>();
    private HashMap<String, Marker> allMarkers = new HashMap<>();
    private LocationLab locationLab;
    private LocationManager locationManager;
    private String provider;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private Brain brain = Brain.getInstance();

    // For current location
    GoogleApiClient mGoogleApiClient;
    Marker mLocationMarker;
    Location mLastLocation;
    LocationRequest mLocationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Point size = new Point();
        Display display = this.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        int width = size.x;
        Log.e("MapsActivity", "width: " + width);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //populate database with locations
        locationLab = LocationLab.get(this);

        //make sure locations are populated and copied into allLocation array
        if (locationLab.getLocations().size() < 1) {
            populateDatabase();
        } else {
            //make sure not duplicating
            allAreaLocations.clear();
            allAreaLocations.addAll(locationLab.getLocations());
        }


//        setContentView(R.layout.activity_maps);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = null;

        if (provider != null) {
            location = locationManager.getLastKnownLocation(provider);
        }

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            viewToast("Location not available");
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkLocationPermission() {
        // In Android 6.0 and higher you need to request permissions at runtime, and the user has
        // the ability to grant or deny each permission. Users can also revoke a previously-granted
        // permission at any time, so your app must always check that it has access to each
        // permission, before trying to perform actions that require that permission. Here, we’re using
        // ContextCompat.checkSelfPermission to check whether this app currently has the
        // ACCESS_COARSE_LOCATION permission

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                // If your app does have access to COARSE_LOCATION, then this method will return
                // PackageManager.PERMISSION_GRANTED//
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // If your app doesn’t have this permission, then you’ll need to request it by calling
                // the ActivityCompat.requestPermissions method//
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // Request the permission by launching Android’s standard permissions dialog.
                // If you want to provide any additional information, such as why your app requires this
                // particular permission, then you’ll need to add this information before calling
                // requestPermission //
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    // Add the actionbar to the top of the view
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mapview_actionbar, menu);

        String vi = getString(R.string.visited);

        MenuItem vis = menu.findItem(R.id.menu_item_show_subtitle);

//        SpannableString spanString = new SpannableString(vi);
//        spanString.setSpan(new ForegroundColorSpan(Color.GRAY), 0, spanString.length(), 0); // fix the color to gray
//
//        vis.setTitle(spanString);

        return true;
    }

    public boolean getProvider() {
        if (provider == null) {
            // Get the location manager
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            // Define the criteria how to select the locatioin provider -> use
            // default
            Criteria criteria = new Criteria();
            provider = locationManager.getBestProvider(criteria, false);
        }

        return provider != null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.list:
                goToListActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //make sure locations are populated and copied into allLocation array
        if (locationLab.getLocations().size() < 1) {
            populateDatabase();
        } else {
            //make sure not duplicating
            allAreaLocations.clear();
            allAreaLocations.addAll(locationLab.getLocations());
        }

        // Add a marker in WPI and move the camera
        AreaLocation wpi = allAreaLocations.get(0);
        if (wpi != null) {
            mMap.addMarker(new MarkerOptions().position(wpi.getLatLng()).title(wpi.getTitle()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(wpi.getLatLng(), 16));
        }

        populateMap();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                buildGoogleApiClient();
                // Although the user’s location will update automatically on a regular basis, you can also
                // give your users a way of triggering a location update manually. Here, we’re adding a
                // ‘My AreaLocation’ button to the upper-right corner of our app; when the user taps this button,
                // the camera will update and center on the user’s current location//
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        // Use the GoogleApiClient.Builder class to create an instance of the
        // Google Play Services API client//
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        // Connect to Google Play Services, by calling the connect() method//
        mGoogleApiClient.connect();
    }

    private List<AreaLocation> populateDatabase() {
        // WPI Facility Locations
        String wpid = "Worcester Polytechnic Institute was founded in 1865 to create and convey the latest science and " +
                "engineering knowledge in ways that are most beneficial to society. Today, WPI holds firm to its founding " +
                "mission to provide an education that balances theory with practice, a philosophy symbolized by the towers " +
                "of our two original buildings, pictured above. WPI's 14 academic departments offer more than 50 " +
                "undergraduate and graduate degree programs in science, engineering, technology, business, the social " +
                "sciences, and the humanities and arts, leading to bachelor’s, master’s, and doctoral degrees. ";
        AreaLocation wpi = new AreaLocation("Worcester Polytechnic Institute", LocationType.WPIFACILITY, wpid, 42.2746, -71.8063);
        wpi.setWebsiteURL("https://www.wpi.edu");
        wpi.setPhoneNumber("5088315000");
        wpi.setPicturePath("wpi");

        String rec = "The Sports & Recreation Center offers many opportunities for fitness, recreation, weight lifting, cardio " +
                "exercise, swimming, racquet sports, yoga, and aerobics. Spread across four floors, the Sports & Recreation " +
                "Center serves our varsity athletes as well as our community members maintaining their fitness regimen.";
        AreaLocation recCenter = new AreaLocation("Rec Center", LocationType.WPIFACILITY, rec, 42.274205, -71.810708);
        recCenter.setWebsiteURL("https://www.wpi.edu/student-experience/sports-recreation/sports-recreation-center");
        recCenter.setPhoneNumber("5088315046");
        recCenter.setPicturePath("rec");

        String fuller = "The Sports & Recreation Center offers many opportunities for fitness, recreation, weight lifting, " +
                "cardio exercise, swimming, racquet sports, yoga, and aerobics. Spread across four floors, the Sports & " +
                "Recreation Center serves our varsity athletes as well as our community members maintaining their fitness regimen.";
        AreaLocation fullerLabs = new AreaLocation("Fuller Labs", LocationType.WPIFACILITY, fuller, 42.275060, -71.806522);
        fullerLabs.setPicturePath("fuller");

        String lib = "Opened in 1967, George C. Gordon Library is named for one of its benefactors who graduated from WPI " +
                "with an electrical engineering degree in 1895. Gordon generously bequeathed $5 million of his estate to the Institute in 1964. " +
                "Today, Gordon Library is one of the busiest buildings on campus-with an average of 13,000+ visitors per week during the academic year.";
        AreaLocation library = new AreaLocation("Gordon Library", LocationType.WPIFACILITY, lib, 42.274229, -71.806352);
        library.setWebsiteURL("https://web.wpi.edu/academics/library");
        library.setPhoneNumber("15088315410");
        library.setPicturePath("lib");

        String cc = "WPI’s Rubin Campus Center is at the crossroads of the campus. Whether you want to pick up your mail, grab a cup of coffee, " +
                "or host a meeting, the Rubin Campus Center fits all these needs. It’s home to our Bookstore and Food Court, the Office " +
                "of Student Affairs and Campus Life , as well as a comfortable location to study or visit with friends. Enjoy a concert on " +
                "the patio, meet for lunch, catch a comedy show on the stage in the Food Court, reserve a room for a meeting, grab a coffee at " +
                "Dunkin' Donuts, or relax in the Class of 1946 Lounge.";
        AreaLocation campusCenter = new AreaLocation("Campus Center", LocationType.WPIFACILITY, cc, 42.274907, -71.808482);
        campusCenter.setPicturePath("cc");

        String gate = "This growing center of research, innovation, and commerce is located in downtown Worcester just a short walk from" +
                " WPI's main campus. The park began as a joint venture with the Worcester Business Development Corporation to transform a" +
                " blighted and underutilized area in into a clean, thriving, mixed-use facility for a range of academic, research, and " +
                "commercial enterprises. WPI became the sole owner of Gateway Park in 2010 and is pursing the build-out of the park in " +
                "partnership with companies in the region.";
        AreaLocation gateway = new AreaLocation("Gateway Park", LocationType.WPIFACILITY, gate, 42.275387, -71.799020);
        gateway.setPicturePath("gateway");

        // Food Locations
        String bean = "At the Bean Counter Bakery, all of the fine cakes, cupcakes, cookies, tarts, pies, desserts, pastries" +
                " and baked goods are hand made from scratch. Only premium quality fresh all natural ingredients " +
                "and no preservatives or artificial flavors are used. All of the coffee and espresso beans are micro-roasted " +
                "in small batches and shipped several times a week. Only  premium quality roasted beans are selected and " +
                "carefully brewed for every cup in order to meet and exceed the customers’ continued high " +
                "expectations. All beans are bought Direct-Trade single origin.";
        AreaLocation beanCounter = new AreaLocation("The Bean Counter", LocationType.FOOD, bean, 42.271729, -71.807335);
        beanCounter.setPicturePath("bean");

        String boyntonDes = "The Boynton was originally a small tavern in the 1930's most often frequented by Worcester Polytechnic Institute (WPI) professors, " +
                "students and the general neighborhood population. (Not too much different from today!) The name \"Boynton\" seems " +
                "to have been the popular name of choice during a bygone era. Nearby is Boynton Street, Boynton Hall (the main " +
                "administration building of WPI), and Boynton Park. You may or may not know that the frequent use of the name " +
                "\"Boynton\" originates from John Boynton, one of the founding fathers of Worcester Polytechnic Institute. ";
        AreaLocation boynton = new AreaLocation("The Boynton Restaurant", LocationType.FOOD, boyntonDes, 42.270867, -71.807431);
        boynton.setPicturePath("boynton");

        String woo = "WooBerry was founded on simple principles: combining delicious frozen desserts with outstanding " +
                "customer service in a fun and inviting atmosphere. Locally owned and operated on Highland Street in " +
                "Worcester Massachusetts, WooBerry is in close proximity to multiple universities, cultural institutions, " +
                "retail and restaurants. Great pride is taken in being a part of the Worcester community, bringing together " +
                "people of all ages and interests through a universal love of delicious frozen desserts.";
        AreaLocation wooberry = new AreaLocation("Wooberry Frozen Yogurt & Ice Cream", LocationType.FOOD, woo, 42.270724, -71.808211);
        wooberry.setPicturePath("wooberry");

        String fix = "Modern setting for custom burgers served with craft beers, unique cocktails & spiked milkshakes.";
        AreaLocation theFix = new AreaLocation("The Fix", LocationType.FOOD, fix, 42.276723, -71.801415);
        theFix.setPicturePath("fix");

        // Culture Locations
        String moore = "Moore State Park is a 737-acre public recreation area located in the town of Paxton, Massachusetts, portions of " +
                "which were listed on the National Register of Historic Places as the Moore State Park Historic District in 2004.";
        AreaLocation moorePond = new AreaLocation("Moore Pond", LocationType.CULTURE, moore, 42.313249, -71.957684);
        moorePond.setPicturePath("moore");

        String art = "The Worcester Art Museum, also known by its acronym WAM, houses over 35,000 works of art dating from antiquity " +
                "to the present day, representing cultures from all over the world.";
        AreaLocation wam = new AreaLocation("Worcester Art Museum", LocationType.CULTURE, art, 42.273345, -71.801973);
        wam.setPicturePath("wam");

        String newton = "The Newton Hill portion of Elm Park (west of Park Avenue) remains minimally landscaped and contains basketball " +
                "and tennis courts, walking trails and also Doherty Memorial High School, a high school within the Worcester Public " +
                "Schools system.";
        AreaLocation newtonHill = new AreaLocation("Newton Hill", LocationType.CULTURE, newton, 42.267565, -71.819960);
        newtonHill.setPicturePath("newton");

        locationLab.addLocation(wpi);
        locationLab.addLocation(recCenter);
        locationLab.addLocation(beanCounter);
        locationLab.addLocation(fullerLabs);
        locationLab.addLocation(wam);
        locationLab.addLocation(moorePond);
        locationLab.addLocation(library);
        locationLab.addLocation(campusCenter);
        locationLab.addLocation(wooberry);
        locationLab.addLocation(boynton);
        locationLab.addLocation(theFix);
        locationLab.addLocation(gateway);
        locationLab.addLocation(newtonHill);

        //make sure to not add duplicates
        allAreaLocations.clear();
        allAreaLocations.addAll(locationLab.getLocations());

        return locationLab.getLocations();
    }

    // Populate map with markers in allAreaLocations
    public void populateMap() {

        for (AreaLocation loc : allAreaLocations) {
            addLocationMarker(loc);
        }

        mMap.setOnMarkerClickListener(this);
    }

    public void addLocationMarker(AreaLocation loc) {
        float markerColor;
        markerColor = getCorrespondingMarkerColor(loc);

        Marker m = mMap.addMarker(new MarkerOptions()
                .position(loc.getLatLng()).title(loc.getTitle())
                .icon(BitmapDescriptorFactory
                        .defaultMarker(markerColor)));

        allMarkers.put(loc.getTitle(), m);

        m.setTag(loc.getId());
    }

    // Change color of a specified marker on the map.
    public void updateMapMarker(AreaLocation loc) {
        Marker m = allMarkers.get(loc.getTitle());

        if (m != null) {
            // refresh marker with new color
            m.remove();
            addLocationMarker(loc);
        }
    }

    public void vibratePhone() {
        Vibrator v = (Vibrator) this.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
    }

    private float getCorrespondingMarkerColor(AreaLocation loc) {
        LocationType type = loc.getLocationType();

        if (loc.isHasVisited()) {
            return BitmapDescriptorFactory.HUE_CYAN;
        }

        switch (type) {
            case WPIFACILITY:
                return BitmapDescriptorFactory.HUE_RED;
            case CULTURE:
                return BitmapDescriptorFactory.HUE_ORANGE;
            case FOOD:
                return BitmapDescriptorFactory.HUE_GREEN;
            default:
                return BitmapDescriptorFactory.HUE_YELLOW;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        UUID locID = (UUID) marker.getTag();
        return goToLocationDetailActivity(locID);
    }

    public boolean goToListActivity() {
        Intent intent = new Intent(this, LocationListActivity.class);
        intent.setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT); // Reopen activity if already exists.
        startActivity(intent);
        return true;
    }

    public boolean goToLocationDetailActivity(UUID locationID) {
        Intent intent = LocationPagerActivity.newIntent(this, locationID);
        startActivity(intent);
        return true;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Retrieve the user’s last known location//
            if (getProvider()) {
                locationManager.requestLocationUpdates(provider, 400, 1, this);
            }

//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        brain.setCurrentLocation(mLastLocation);

        if (mLocationMarker != null) {
            mLocationMarker.remove();
        }


        checkIfAtLocation();
//        viewToast("changed location");

        // To help preserve the device’s battery life, you’ll typically want to use
        // removeLocationUpdates to suspend location updates when your app is no longer
        // visible onscreen//
        if (mGoogleApiClient != null) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String prov) {
        viewToast("Enabled new provider " + provider);
        provider = prov;
    }

    @Override
    public void onProviderDisabled(String provider) {
        viewToast("Disabled provider " + provider);
    }

    // Custom ratchet geofencing!!!
    private void checkIfAtLocation() {
        Location currentLoc = brain.getCurrentLocation();

        for (AreaLocation areaLocation : allAreaLocations) {
            if (!areaLocation.isHasVisited()) { // if you haven't visited the areaLocation.
                // 0.02 mi is 105 feet
                if (0.02 >= brain.getDistanceBetweenTwo(areaLocation.getLatitude(), areaLocation.getLongitude(), currentLoc.getLatitude(), currentLoc.getLongitude())) {
                    Context context = getApplicationContext();
                    CharSequence text = "Congradulations You've Visited" + areaLocation.getTitle();
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                    // set areaLocation in alllocations as visited:
                    areaLocation.setHasVisited(true);

                    vibratePhone();
                    // update areaLocation in db as visited:
                    locationLab.updateLocation(areaLocation);
                    // updateMapMarker
                    updateMapMarker(areaLocation);
                }
            }
        }
    }

    public void viewToast(String msg) {
        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {

                // If the request is cancelled, the result array will be empty (0)//
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // If the user has granted your permission request, then your app can now perform all its
                    // location-related tasks, including displaying the user’s location on the map//
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // If the user has denied your permission request, then at this point you may want to
                    // disable any functionality that depends on this permission//
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        } catch (IllegalArgumentException e) {
            viewToast(e.toString());
        }

    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }
}
