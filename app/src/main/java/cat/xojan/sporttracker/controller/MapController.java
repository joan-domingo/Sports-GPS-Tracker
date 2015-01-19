package cat.xojan.sporttracker.controller;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import cat.xojan.sporttracker.R;
import cat.xojan.sporttracker.kml.KmlGenerator;

public class MapController {

    private final TimeController timeController;
    private GoogleMap map;
    private Activity activity;
    private LatLng mCurrentPosition;
    private LatLngBounds.Builder mBoundsBuilder = null;
    private boolean tracking = false;
    private List<LatLng> path;
    private LatLng mOldPosition;
    private double fDistance = 0, tDistance = 0;
    private int mKm = 0;
    private double km_2achieve;
    private TextView km;
    private LinearLayout timeList;
    private KmlGenerator kmlGenerator;
    private LocationListener locationListener;
    private LocationManager locationManager;

    public MapController(Activity activity, TimeController timeController) {
        this.activity = activity;
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        this.timeController = timeController;
        kmlGenerator = new KmlGenerator();
        path = new ArrayList<LatLng>();
    }

    public void initializeMap() {
        // Get a handle to the Map Fragment
        map = ((MapFragment) activity.getFragmentManager().findFragmentById(R.id.map)).getMap();
        km = (TextView) activity.findViewById(R.id.kilometres);
        timeList = (LinearLayout) activity.findViewById(R.id.time_list);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(true);
    }

    public void updateMapView() {
        if (mBoundsBuilder != null) {
            LatLngBounds mBounds = mBoundsBuilder.build();
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(mBounds, 80));
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 15));
        }
    }

    private void locationChanged() {
        LatLng mOldPosition = mCurrentPosition;

        //get current location and center camera to it
        getProviderAndPosition();

        //create polyline with last location
        if (tracking) {
            map.addPolyline(new PolylineOptions()
                    .geodesic(true)
                    .add(mOldPosition)
                    .add(mCurrentPosition)
                    .width(6)
                    .color(Color.BLACK));

            tDistance = gps2km(mOldPosition.latitude, mOldPosition.longitude, mCurrentPosition.latitude, mCurrentPosition.longitude);
            mBoundsBuilder.include(mCurrentPosition);
            updateDistance();
            updatePoly();
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 15));
        }
        updateMapView();
    }

    public void getProviderAndPosition() {
        // getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSEnabled) {
            Toast.makeText(activity, "Enable GPS", Toast.LENGTH_SHORT).show();
        } else {
            try {
                // if GPS Enabled get lat/long using GPS Services
                Location mCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                mCurrentPosition = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                String coordinate = String.valueOf(mCurrentLocation.getLongitude()) + "," + String.valueOf(mCurrentLocation.getLatitude());
                kmlGenerator.addCoordinate(coordinate);
                mOldPosition = mCurrentPosition;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startGPS() {
        tracking = true;
        getProviderAndPosition();
        updatePoly();
        map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .position(mCurrentPosition)
                .title("START"));
        kmlGenerator.addMarker("START", mCurrentPosition.longitude + "," + mCurrentPosition.latitude);

        mBoundsBuilder = new LatLngBounds.Builder().include(mCurrentPosition);
    }

    public void clearMap() {
        timeList.removeAllViews();
        km.setText("0,00 km"); //quilometres
        //km.setText("0.00 Mi"); //milles
        km_2achieve = 1.00;
        map.clear();
    }

    public void addMarker() {
        map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(mCurrentPosition)
                .title(mKm + " km")); //quilometres
                //.title(mKm + " Mi"));
        kmlGenerator.addMarker(mKm + " km", mCurrentPosition.longitude + "," + mCurrentPosition.latitude); //quilometres
        //kmlGenerator.addMarker(mKm + " Mi", mCurrentPosition.longitude + "," + mCurrentPosition.latitude); //milles
    }

    public void addFinishMarker() {
        map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .position(mCurrentPosition)
                .title("END"));
        kmlGenerator.addMarker("END", mCurrentPosition.longitude + "," + mCurrentPosition.latitude);
        kmlGenerator.createFile();
    }

    private double gps2km(double lat_a, double lng_a, double lat_b, double lng_b) {
        double pk = 180 / 3.14169;

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        double metres = 6366000 * tt;
        return metres / 1000; //quilometres
        //return (metres / 1000) * 0.62137; //milles
    }

    public void stopTracking() {
        tracking = false;
        mBoundsBuilder = null;
        fDistance = 0;
    }

    protected void updatePoly() {
        path.add(mCurrentPosition);
    }

    public void updateDistance() {
        fDistance = fDistance + tDistance;

        DecimalFormat fm = new DecimalFormat("#.##");
        fm.setRoundingMode(RoundingMode.DOWN);
        double auxDist = Double.valueOf(fm.format(fDistance).replace(",", "."));

        String dist = String.format("%.2f", fDistance) + " km";//quilometres
        //String dist = String.format("%.2f", fDistance) + " Mi";//milles

        km.setText(dist);

        if (auxDist >= km_2achieve) {
            updateKmMarker();
            km_2achieve++;
        }
    }

    private void updateKmMarker() {
        //update last kilometer
        timeController.setLastKm(timeList);

        mKm = (int) km_2achieve; //update number km
        timeController.setPace(mKm);//update pace

        addMarker();
    }

    public void updateLastDistance() {
        tDistance = gps2km(mOldPosition.latitude, mOldPosition.longitude, mCurrentPosition.latitude, mCurrentPosition.longitude);
    }

    public void requestLocationUpdates() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location arg0) {
                locationChanged();
            }

            @Override
            public void onProviderDisabled(String provider) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {}

        });
    }

    public void removeUpdates() {
        locationManager.removeUpdates(locationListener);
    }
}