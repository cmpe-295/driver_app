package spartansaferide.sjsu.edu.driver;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DriverMapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    String provider;
    Location location;
    static Context context;
    ArrayList<LatLng> stops = new ArrayList<LatLng>(8);
    ListView rides;
    List<Address> listAddresses;
    ArrayList<String> stoparr = new ArrayList<String>(8);
    //LatLng destination=new LatLng(37.333286, -121.879909);
    LatLng current_location;
    String authCode;
    Bitmap smallMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("TokenID", "Refreshed token: " + refreshedToken);

        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        context = getApplicationContext();
        TextView notification_field = (TextView) findViewById(R.id.notification);


        //sample data
        LatLng loc1 = new LatLng(37.338716, -121.879794);
        stops.add(loc1);
        loc1 = new LatLng(37.341877, -121.887195);
        stops.add(loc1);
        loc1 = new LatLng(37.329346, -121.871347);
        stops.add(loc1);
        rides = (ListView) findViewById(R.id.rides);


        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.bus_icon);
        Bitmap b = bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, 75, 75, false);

        //Get Google Play Services
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        } else {
            //Fetch the authCode stored in SharedPreferences
            authCode = getApplicationContext().getSharedPreferences("spartansaferide.sjsu.edu.driver", Context.MODE_PRIVATE).getString("authcode", "");
            Log.d("Status", "Auth Code in MapsActivity is" + authCode);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            //new
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            //Check if Location is enabled. If location services are turned off, then this function requests the user to turn them on
            checkGPSStatus();

            //1. Initialize the locationManager and the provider
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            provider = locationManager.getBestProvider(new Criteria(), false); //To return only enabled providers
        }
    }

    private void checkGPSStatus() {
        LocationManager locationManager = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(DriverMapsActivity.this);
            dialog.setMessage("GPS is not enabled. Tap on OK to Enable the GPS");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //this will navigate user to the device location settings screen
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            AlertDialog alert = dialog.create();
            alert.show();
        }
    }

    @Override
    public void onBackPressed() {
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.logout) {
            logOut();
            Intent logout = new Intent(DriverMapsActivity.this, DriverLoginActivity.class);
            startActivity(logout);
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logOut() {
        //To make API Call for logging out the user
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.i("Status", "In onLocationChanged");

        //2. Get the current location
        final Double lat = location.getLatitude();
        final Double lng = location.getLongitude();
        current_location = new LatLng(lat, lng);

        Log.i("Status", "Latitude is: " + lat.toString());
        Log.i("Status", "Longitude is: " + lng.toString());

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.addMarker(new MarkerOptions().position(current_location).title("Shuttle Location").icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current_location));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current_location, 15));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                Intent barcodeScanner = new Intent(DriverMapsActivity.this, BarcodeScannerActivity.class);
                startActivity(barcodeScanner);

            }
        });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        Log.i("Status", "In onMapReady");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        //populate list of stops
        updateStops();

        if (location != null) {
            onLocationChanged(location);
        }
    }

    public void updateStops() {
        //To populate this list from the MapQuest JSON response
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        for (LatLng i : stops) {
            try {
                listAddresses = geocoder.getFromLocation(i.latitude, i.longitude, 1);

                stoparr.add((listAddresses.get(0).getAddressLine(0).toString()) + "," + (listAddresses.get(0).getAddressLine(1).toString()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                stoparr);

        rides.setAdapter(arrayAdapter);
        rides.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String item = (String) parent.getItemAtPosition(position);
                Log.i("Position", "Item Clicked" + id);
                Toast.makeText(DriverMapsActivity.this, "Option Selected: " + item, Toast.LENGTH_LONG).show();

                displayPoints(id);

            }
        });

    }

    private void displayPoints(long id) {
        LatLng dest = new LatLng(stops.get((int) id).latitude, stops.get((int) id).longitude);
        mMap.clear();

        //Display Shuttle Location and Zoom in to the shuttle position
        mMap.addMarker(new MarkerOptions().position(current_location).title("Shuttle Location").icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current_location));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current_location, 15));

        //Display Marker for Pickup Location
        mMap.addMarker(new MarkerOptions().position(dest).title("PickUp"));

        //Draw the route to the pickup point
        drawMarker(dest);

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(current_location, dest);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //Send location updates every 5 seconds and for every change in 100 meters in distance
        locationManager.requestLocationUpdates(provider, 5000, 100, this);

        //Call "update_driver_location" API to update the location to the server
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);

    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        //String str_dest = "destination=37.339089,-121.893048";
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("err", "Exception downloading url" + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * A class to download data from Google Directions URL
     */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Directions in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);
            }

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }

    private void drawMarker(LatLng point) {
        // Creating MarkerOptions
        MarkerOptions options = new MarkerOptions();

        // Setting the position of the marker
        options.position(point);
        // Add new marker to the Google Map Android API V2

        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMap.addMarker(options);
    }
}
