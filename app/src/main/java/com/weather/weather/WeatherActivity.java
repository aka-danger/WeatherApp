package com.weather.weather;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Weather.WebServiceClient;

public class WeatherActivity extends AppCompatActivity implements LocationListener {
//--fields

    //<editor-fold defaultstate="collapsed" desc="fields">
    private TextView txtLocation;
    private TextView txtDate;
    private TextView txtMax;
    private TextView txtMin;
    private LocationManager locationManager;
    private String provider;
    private static final String TAG = WeatherActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private Location location;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Android Life cycle">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        txtLocation = (TextView) findViewById(R.id.txtLocation);
        txtDate = (TextView) findViewById(R.id.txtDate);
        txtMax = (TextView) findViewById(R.id.txtMax);
        txtMin = (TextView) findViewById(R.id.txtMin);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!enabled){
            Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
            startActivity(intent);
        }

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria,false);

        if(location != null){
            Log.d(TAG,"provider: " + provider);
            onLocationChanged(location);
        }else {
            showSnackbar("Location cannot be found. make sure that all your setting are correct and your internet is active.");
        }

    }

    @Override
    @SuppressWarnings("MissingPermission")
    protected void onResume(){
        super.onResume();

        if(provider != null){
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        }

    }

    @Override
    protected void onPause(){
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            location = getLastKnownLocation();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Location Methods"
    private boolean checkPermissions(){
        int permissionState = ActivityCompat.checkSelfPermission(this ,Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressWarnings("MissingPermission")
    private Location getLastKnownLocation(){
        return locationManager.getLastKnownLocation(provider);
    }


    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(WeatherActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar("This app needs your location.", android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            startLocationPermissionRequest();
        }
    }

    public void print(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_LONG);
    }


    private void showSnackbar(final String text) {
        View container = findViewById(R.id.WeatherActivity);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    private void showSnackbar(String message , final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="LocationListener"
    @Override
    public void onLocationChanged(Location location) {
        RestAPI tmp = new RestAPI(location.getLongitude(),location.getLatitude());
        tmp.execute();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //nothing
    }

    @Override
    public void onProviderEnabled(String provider) {
        print("provide enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        print("provide disabled");
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="AsyncTask RestAPI">
    private class RestAPI extends AsyncTask<String,String,String>{

        private int lon = 0;
        private int lat = 0;
        private double dblLon = 0.0;
        private double dblLat = 0.0;
        private final String http = "http://api.openweathermap.org/data/2.5/weather";
        private final String apiKey = "1cc298607dea103d80657daade0f741e";
        private String response ;

        private String min;
        private String max;

        public RestAPI(double longitude, double  latitude){
            dblLat = latitude;
            dblLon = longitude;
            lon = (int) longitude;
            lat = (int) latitude;
        }

        @Override
        protected String doInBackground(String... params) {

            if(lat != 0 && lat != 0){

                ArrayList<Pair<String,String>> p = new ArrayList<>();
                p.add(new Pair<>("lat",String.valueOf(lat)));
                p.add(new Pair<>("lon",String.valueOf(lon)));
                p.add(new Pair<>("appid",apiKey));
                p.add(new Pair<>("units","metric"));

                response = WebServiceClient.get(http,p);

                System.out.println("response: "+ response);

                if(response != null){
                    try {
                        JSONObject obj = new JSONObject(response);
                        JSONObject mainObj = obj.getJSONObject("main");
                        min = mainObj.getString("temp_min");
                        max = mainObj.getString("temp_max");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            if(lat != 0 && lon != 0 && response != null){
                txtDate.setText("Today: "+ new Date().toString());
                txtMin.setText("Min Temperature: "+ min +" C");
                txtMax.setText("Max Temperature: "+ max+ " C");

                //address
                String resultAddress = null;
                Geocoder geocoder;
                List<Address> addresses = null;
                geocoder = new Geocoder(WeatherActivity.this, Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(dblLat, dblLon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(addresses != null){
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();
                    resultAddress = String.format("Address: %s \n City: %s \n Province: %s \n Country: %s \n Postal Code: %s",address,city,state,country,postalCode);
                }else resultAddress = null;


                txtLocation.setText(resultAddress);
            }else {
                showSnackbar("Check your internet connection and location settings.");
            }

        }
    }
    //</editor-fold>

}
