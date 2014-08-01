package cat.xojan.sporttracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import cat.xojan.sporttracker.controller.MapController;
import cat.xojan.sporttracker.controller.TimeController;

public class MainActivity extends FragmentActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private NotificationManager mNotificationManager;
    private Button button;
    private TimeController timeController;
    private MapController mapController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeController = new TimeController(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mapController = new MapController(this, locationManager, timeController);

        initViews();
        mapController.initializeMap();
        mapController.getProviderAndPosition();
        mapController.updateMapView();
    }

    private void initViews() {
        button = (Button) findViewById(R.id.start_activity_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (button.getText().equals("Start Activity")) {
                    start();
                } else {
                    stop();
                }
            }
        });
    }

    private void start() {
        requestLocationUpdates();
        initStartVars();
        mapController.updateMapView();
        mapController.startGPS();
        notificationOn();
        timeController.startActivity();
    }

    private void initStartVars() {
        mapController.clearMap();
        changeButton(button, R.color.red, "Stop Activity");
    }

    private void requestLocationUpdates() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location arg0) {
                mapController.onLocationChanged();
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

    private void notificationOn() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 56, notificationIntent, 0);

        Notification.Builder mBuilder = new Notification.Builder(this);
        mBuilder.setSmallIcon(android.R.drawable.star_big_on)
                .setContentTitle("Sports GPS Tracker is running")
                .setContentText("Press here to see your time and route")
                .setContentIntent(pendingIntent);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(56, mBuilder.build());

    }

    public void changeButton(Button button, int buttonColor, String buttonText) {
        if (button == null) {
            return;
        }
        button.setBackgroundResource(buttonColor);
        button.setText(buttonText);
    }

    private void stop() {
        timeController.stopActivity();
        mapController.updateLastDistance();
        mapController.updateDistance();
        mapController.addFinishMarker();
        notificationOff();
        locationManager.removeUpdates(locationListener);
        initStopVars();
    }

    private void initStopVars() {
        changeButton(button, R.color.green, "Start Activity");
        mapController.stopTracking();
    }

    private void notificationOff() {
        mNotificationManager.cancel(56);
    }
}
