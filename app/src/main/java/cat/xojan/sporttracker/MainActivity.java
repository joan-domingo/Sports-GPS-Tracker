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
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import cat.xojan.sporttracker.controller.MapController;
import cat.xojan.sporttracker.controller.TimeController;

public class MainActivity extends FragmentActivity {

    private NotificationManager mNotificationManager;
    private Button button;
    private TimeController timeController;
    private MapController mapController;
    private LocationListener locationListener;
    private LocationManager locationManager;

    private Handler firstLocation = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
                initButtonView();
                mapController.getProviderAndPosition();
                mapController.updateMapView();
                locationManager.removeUpdates(locationListener);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeController = new TimeController(this);
        mapController = new MapController(this, timeController);

        mapController.initializeMap();
        getFirstLocation();
    }

    private void getFirstLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location arg0) {
                firstLocation.sendEmptyMessage(0);
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

    private void initButtonView() {
        button = (Button) findViewById(R.id.start_activity_button);
        button.setText("Start Activity");
        button.setBackgroundColor(getResources().getColor(R.color.green));
        button.setTextColor(getResources().getColor(R.color.black));
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
        if (isGPSEnabled()) {
            mapController.requestLocationUpdates();
            initStartVars();
            mapController.updateMapView();
            mapController.startGPS();
            notificationOn();
            timeController.startActivity();
        }
    }

    private boolean isGPSEnabled() {
        boolean result = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!result) {
            Toast.makeText(this, "Enable GPS", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    private void initStartVars() {
        mapController.clearMap();
        changeButton(button, R.color.red, "Stop Activity");
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
        mapController.removeUpdates();
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
