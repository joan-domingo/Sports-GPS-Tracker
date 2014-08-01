package cat.xojan.sporttracker.controller;

import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
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

/**
 * Created by Joan on 28/07/2014.
 */
public class MapController {

    private final LocationManager locationManager;
    private final TimeController timeController;
    private GoogleMap map;
    private Activity activity;
    private LatLng mCurrentPosition;
    private LatLngBounds.Builder mBoundsBuilder = null;
    private boolean tracking = false;
    private List<LatLng> path = new ArrayList<LatLng>();
    private LatLng mOldPosition;
    private double fDistance = 0, tDistance = 0;
    private int mKm = 0;
    private double km_2achieve;
    private TextView km;
    private LinearLayout timeList;
    private KmlGenerator kmlGenerator;

    public MapController(Activity activity, LocationManager locationManager, TimeController timeController) {
        this.activity = activity;
        this.locationManager = locationManager;
        this.timeController = timeController;
        kmlGenerator = new KmlGenerator();
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

    public void onLocationChanged() {
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
        Location mCurrentLocation;
        if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
            mCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else {
            mCurrentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Toast.makeText(activity, "Enable GPS", Toast.LENGTH_SHORT).show();
        }
        mCurrentPosition = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        String coordinate = String.valueOf(mCurrentLocation.getLongitude()) + "," +  String.valueOf(mCurrentLocation.getLatitude());
        kmlGenerator.addCoordinate(coordinate);
        mOldPosition = mCurrentPosition;
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
        km.setText("0,00 km");
        km_2achieve = 1.00;
        map.clear();
    }

    public void addMarker() {
        map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(mCurrentPosition)
                .title(mKm + " km"));
        kmlGenerator.addMarker(mKm + " km", mCurrentPosition.longitude + "," + mCurrentPosition.latitude);
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
        return metres / 1000;
    }

    public void stopTracking() {
        tracking = false;
        mBoundsBuilder = null;
        path = new ArrayList<LatLng>();
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

        String dist = String.format("%.2f", fDistance) + " km";
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
}