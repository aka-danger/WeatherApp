package Weather;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.weather.weather.WeatherActivity;

/**
 * Created by Brendon on 2017/08/31.
 */

public class Tracker extends Service implements LocationListener {
//--fields
    private Context context;
    private LocationManager locationManager;
    private Location location;
    private String provider;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final String TAG = Tracker.class.getSimpleName();
//--constructor
    public Tracker(Context context){
        this.context = context;
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria,false);


    }
//--methods

    //<editor-fold defaultstate="collapsed" desc="Location Methods">
    private boolean checkPermissions(){
        int permissionState = ActivityCompat.checkSelfPermission(this , Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressWarnings("MissingPermission")
    private Location getLastKnownLocation(){
        return locationManager.getLastKnownLocation(provider);
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions((Activity)context,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale((Activity)context,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            // Request permission
            startLocationPermissionRequest();

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    //used for the on start method
    public void startLocationTracking(){
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            location = getLastKnownLocation();
        }
    }
    // </editor-fold>

    //<editor-fold defaultstate="collapsed" desc="LocationListener Overrides">
    @Override
    public void onLocationChanged(Location location) {

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
    //</editor-fold>


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
