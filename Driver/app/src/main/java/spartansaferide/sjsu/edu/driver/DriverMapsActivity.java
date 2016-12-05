package spartansaferide.sjsu.edu.driver;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.net.Uri;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class DriverMapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    Thread thread;
    boolean stop=false;
    LocationManager locationManager;
    String provider;
    static Context context;
    static ArrayList<StopInformation> stops = new ArrayList<StopInformation>(8);
    ListView rides;
    List<Address> listAddresses;
    ArrayList<String> stoparr = new ArrayList<String>();
    LatLng current_location;
    String authCode;
    Bitmap smallMarker;
    String refreshedToken;
    static String baseUrl = "http://saferide.nagkumar.com/";
//    String notification = "{\"path\":\n" +
//            "[{\"eta\":0,\"user\":\"driver\",\"latLng\":{\"lng\":-121.879737,\"lat\":37.333163}},\n" +
//            "\t{\"ride_id\":10,\"eta\":135,\"type\":\"pick\",\"user\":{\"latitude\":null,\"sjsu_id\":\"010095345\",\"last_name\":\"Arkalgud\",\"id\":1,\"first_name\":\"Nagkumar\",\"longitude\":null},\"latLng\":{\"lng\":-121.879737,\"lat\":37.333163}},{\"ride_id\":4,\"eta\":237,\"type\":\"drop\",\"user\":{\"latitude\":null,\"sjsu_id\":\"1111111\",\"last_name\":\"gupta\",\"id\":3,\"first_name\":\"balaji\",\"longitude\":null},\"latLng\":{\"lng\":-121.873226,\"lat\":37.331983}},{\"ride_id\":3,\"eta\":237,\"type\":\"pick\",\"user\":{\"latitude\":null,\"sjsu_id\":\"10101010\",\"last_name\":\"jayadev\",\"id\":2,\"first_name\":\"varsha\",\"longitude\":null},\"latLng\":{\"lng\":-121.883569,\"lat\":37.327444}}]}";
//    static String baseUrl = "http://saferide.nagkumar.com/";

   static JSONObject authObj;
    String sid;
    String sname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);

        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Status", "Refreshed token: " + refreshedToken);

        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        context = getApplicationContext();

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
            try {
                authObj = new JSONObject(authCode);
                JSONObject params = new JSONObject();
                params.put("token", refreshedToken);
                Log.i("Status", "API Token: " + refreshedToken);

                //Call API to update device token
                makePostCall(params, authObj, "update_device_token/");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

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

//            parseNotifcation(notification);

        }
        createthread();
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
        super.onBackPressed();
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

        SharedPreferences preferences = getSharedPreferences("spartansaferide.sjsu.edu.driver", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove("authcode");
        editor.clear();
        editor.commit();

        Log.i("Preferences", "After Clear: " + getApplicationContext().getSharedPreferences("spartansaferide.sjsu.edu.driver", Context.MODE_PRIVATE).getString("authcode", ""));
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

        //Call API to update Driver Location
        authCode = getApplicationContext().getSharedPreferences("spartansaferide.sjsu.edu.driver", Context.MODE_PRIVATE).getString("authcode", "");
        Log.d("Status", "Auth Code in MapsActivity is" + authCode);
        try {

            authObj = new JSONObject(authCode);
            JSONObject params = new JSONObject();
            params.put(new String("latitude"), String.valueOf(current_location.latitude));
            params.put(new String("longitude"), String.valueOf(current_location.longitude));

            //Update shuttle location to the server
            makePostCall(params, authObj, "ride/update_driver_location/");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                //LatLng position = marker.getPosition();
                for (StopInformation s : stops) {
                    if (s.student_id == sid) {
                        if (s.type.equals("pick")) {
                            Intent barcodeScanner = new Intent(DriverMapsActivity.this, BarcodeScannerActivity.class);
                            barcodeScanner.putExtra("id", sid);
                            barcodeScanner.putExtra("name", s.name);
                            startActivityForResult(barcodeScanner, 1);
                        } else if (s.type.equals("drop")) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(DriverMapsActivity.this).setTitle("Confirm Drop-off")
                                    .setMessage("Dropped " + sname + "?").setCancelable(true)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            //Add API
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // if this button is clicked, just close the dialog box and do nothing
                                            dialog.cancel();
                                        }
                                    });
                            ;
                            final AlertDialog dialog = alert.create();
                            dialog.show();

                        }
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //parse JSON
        stops = new ArrayList<StopInformation>();
        String s = data.getStringExtra("response");
        try {
            JSONObject response = new JSONObject(data.getStringExtra("response"));
            JSONArray path = response.getJSONArray("route");
            for (int i = 1; i < path.length(); i++) {
                //new StopInformation
                StopInformation stop_obj = new StopInformation();

                JSONObject obj = path.getJSONObject(i);
                stop_obj.type = obj.getString("type");
                Double location_lat = obj.getJSONObject("latLng").getDouble("lat");
                Double location_lng = obj.getJSONObject("latLng").getDouble("lng");

                stop_obj.location = new LatLng(location_lat, location_lng);

                stop_obj.name = obj.getJSONObject("user").getString("first_name") + " " + obj.getJSONObject("user").getString("last_name");
                stop_obj.student_id = obj.getJSONObject("user").getString("sjsu_id");
                stops.add(stop_obj);
            }
            updateStops();
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
        //updateStops();

        if (location != null) {
            onLocationChanged(location);
        }
    }

    public void updateStops() {
        //To populate this list from the MapQuest JSON response
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        stoparr=new ArrayList<String>();

        for (StopInformation i : stops) {
            try {
                listAddresses = geocoder.getFromLocation(i.location.latitude, i.location.longitude, 1);

                stoparr.add((i.type.toUpperCase() + "," + i.student_id + "," + i.name.toUpperCase()));

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
                //  Toast.makeText(DriverMapsActivity.this, "Option Selected: " + item, Toast.LENGTH_LONG).show();

                sid = stops.get(position).student_id;
//                sname=stops.get(position).name;
//                trip_type=stops.get(position).type;

                displayPoints(position);
            }
        });

        rides.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                LatLng navigate_loc = stops.get(i).location;

                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + navigate_loc.latitude + "," + navigate_loc.longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

                return true;
            }
        });
    }

    private void displayPoints(int position) {
        LatLng dest = new LatLng(stops.get(position).location.latitude, stops.get(position).location.longitude);
        mMap.clear();

        MarkerOptions options = new MarkerOptions();

        //Display Shuttle Location and Zoom in to the shuttle position
        mMap.addMarker(new MarkerOptions().position(current_location).title("Shuttle Location").icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current_location));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current_location, 15));

        //Display Marker for next stop
        if ((stops.get(position).type).equalsIgnoreCase("pick")) {
            Marker m = mMap.addMarker(new MarkerOptions().position(dest).title("PickUp")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        } else if ((stops.get(position).type).equalsIgnoreCase("drop")) {
            Marker m = mMap.addMarker(new MarkerOptions().position(dest).title("drop")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }

        //Draw the route to the pickup point
        //drawMarker(dest);

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(current_location, dest);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    @Override
    protected void onResume() {

        Log.d("In", "On Resume");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //Send location updates every 5 seconds and for every change in 100 meters in distance
        locationManager.requestLocationUpdates(provider, 5000, 100, this);
        super.onResume();
    }

    @Override
    protected void onPause() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
        super.onPause();
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

        // Executes in UI thread, after the execution of doInBackground()
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

    public void makePutCall(JSONObject params, JSONObject authObj, String api) throws JSONException, UnsupportedEncodingException {

        AsyncHttpClient client = new AsyncHttpClient();
        StringEntity entity = new StringEntity(params.toString());
        client.addHeader("Authorization", "Token " + authObj.getString("token"));
        client.addHeader("Content-Type", "application/json");
        client.addHeader("Accept", "application/json");

        client.put(context, baseUrl + api, entity, "application/json", new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                Log.d("Return Status", "Status Code: b.b@sjsu.edu    " + statusCode);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void makePostCall(JSONObject params, JSONObject authObj, String api) throws JSONException, UnsupportedEncodingException {

        AsyncHttpClient client = new AsyncHttpClient();
        StringEntity entity = new StringEntity(params.toString());

        client.addHeader("Authorization", "Token " + authObj.getString("token"));
        client.addHeader("Content-Type", "application/json");
        client.addHeader("Accept", "application/json");

        client.post(context, baseUrl + api, entity, "application/json", new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("Return Status", "Status Code: " + statusCode);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void createthread(){
        thread=new Thread(new Runnable() {
            public void run() {
                stops = new ArrayList<StopInformation>();
                Log.d("parseNotifcation:  ",Notification.getInstance().message);

                while(!stop && Notification.getInstance().message!=""){
                    // Log.i("herer","response");
                    try {
                        String mesg=Notification.getInstance().message;
                        Notification.getInstance().message="";
                        JSONObject newNotification = new JSONObject(mesg);
                        JSONArray path = newNotification.getJSONArray("path");
                        for (int i = 1; i < path.length(); i++) {
                            //new StopInformation
                            StopInformation stop_obj = new StopInformation();

                            JSONObject obj = path.getJSONObject(i);
                            stop_obj.type = obj.getString("type");
                            Double location_lat = obj.getJSONObject("latLng").getDouble("lat");
                            Double location_lng = obj.getJSONObject("latLng").getDouble("lng");

                            stop_obj.location = new LatLng(location_lat, location_lng);

                            stop_obj.name = obj.getJSONObject("user").getString("first_name") + " " + obj.getJSONObject("user").getString("last_name");
                            stop_obj.student_id = obj.getJSONObject("user").getString("sjsu_id");
                            stops.add(stop_obj);
                        }
                        DriverMapsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                updateStops();

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

}
