package spartansaferide.sjsu.edu.driver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import android.view.MenuItem;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DriverMapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback, LocationListener,GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    String provider;
    Location location;
    static Context context;
    ArrayList<LatLng> stops=new ArrayList<LatLng>(8);
    ListView rides;
    List<Address> listAddresses;
    ArrayList<String> stoparr= new ArrayList<String >(8);
    LatLng destination=new LatLng(37.333286, -121.879909);
    LatLng current_location;
    String url="https://maps.googleapis.com/maps/api/directions/json?origin=37.333540,-121.884589&destination=37.338716,-121.879794&waypoints=37.341877,-121.887195|37.329346,-121.871347";
    String authCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = getApplicationContext();
        LatLng loc1 = new LatLng(37.338716, -121.879794);
        stops.add(loc1);
        loc1 = new LatLng(37.341877, -121.887195);
        stops.add(loc1);
        loc1 = new LatLng (37.329346, -121.871347);
        stops.add(loc1);
        rides = (ListView) findViewById(R.id.rides);



//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        //Fetch the authCode stored in SharedPreferences
        authCode = getApplicationContext().getSharedPreferences("spartansaferide.sjsu.edu.driver",Context.MODE_PRIVATE).getString("authcode","");
        Log.d("Status","Auth Code in MapsActivity is"+authCode);

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

    private void checkGPSStatus() {
        LocationManager locationManager = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if ( locationManager == null ) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex){}
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex){}
        if ( !gps_enabled && !network_enabled ){
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.logout) {

            logOut();
            Intent logout = new Intent(DriverMapsActivity.this,DriverLoginActivity.class);
            startActivity(logout);
            Toast.makeText(this,"Logged Out",Toast.LENGTH_SHORT).show();

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

        Log.i("Status","In onLocationChanged");

        //2. Get the current location
        Double lat = location.getLatitude();
        Double lng = location.getLongitude();
        current_location=new LatLng(lat, lng);

        Log.i("Status", "Latitude is: "+ lat.toString());
        Log.i("Status", "Longitude is: "+ lng.toString());

        LatLng myLocation = new LatLng(lat,lng);

       // mMap.clear(); //Clears any previous markers that were set when the location is updated

        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.bus_icon);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 75, 75, false);

        mMap.addMarker(new MarkerOptions().position(myLocation).title("Shuttle Location")
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),15));


        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this);


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

        Log.i("Status","In onMapReady");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        //populate list of stops
        updateStops();

        if(location != null){
            onLocationChanged(location);
        }
    }

    public void updateStops(){
        Geocoder geocoder = new Geocoder(getApplicationContext(),Locale.getDefault());

        for(LatLng i : stops) {
            try {
                listAddresses = geocoder.getFromLocation(i.latitude, i.longitude, 1);
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(i.latitude, i.longitude))
                        .title("H"));
                stoparr.add(listAddresses.get(0).getAddressLine(0).toString());

                //Check if we have atleast gt one address

                if (listAddresses != null && listAddresses.size() > 0) {
                    //Get the results

                    Log.i("Place Info ", listAddresses.get(0).getAddressLine(0).toString());
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                stoparr );

        rides.setAdapter(arrayAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(this);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Intent barcodeScanner = new Intent(DriverMapsActivity.this,BarcodeScannerActivity.class);
        startActivity(barcodeScanner);

        return true;
    }
}
