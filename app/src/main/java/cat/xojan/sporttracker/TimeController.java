package cat.xojan.sporttracker;

import android.app.Activity;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;


public class TimeController {

    private TextView lastKm, pace;
    private Chronometer chrono;
    private long lastTimeElapsed = 0;
    private int km;
    private Activity activity;

    public TimeController(Activity activity) {
        this.activity = activity;
    }

    public void startActivity() {
        initViews();

        chrono.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long t = SystemClock.elapsedRealtime() - cArg.getBase();
                int h = (int) (t / 3600000);
                int m = (int) (t - h * 3600000) / 60000;
                int s = (int) (t - h * 3600000 - m * 60000) / 1000;
                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";
                cArg.setText(hh + ":" + mm + ":" + ss);
            }
        });

        chrono.setBase(SystemClock.elapsedRealtime());
        chrono.start();
    }

    private void initViews() {
        String initialText = "——:——";
        km = 0;
        lastTimeElapsed = 0;

        chrono = (Chronometer) activity.findViewById(R.id.chronometer);
        chrono.setText("00:00:00");

        lastKm = (TextView) activity.findViewById(R.id.lastkm);
        lastKm.setText("Last km: " + initialText);

        pace = (TextView) activity.findViewById(R.id.pace);
        pace.setText("Pace: " + initialText);
    }

    public void stopActivity() {
        chrono.stop();
    }

    public void setLastKm(LinearLayout timeList) {
        km++;
        long timeElapsed = SystemClock.elapsedRealtime() - chrono.getBase();

        TextView textView = new TextView(activity);
        textView.setTextColor(activity.getResources().getColor(R.color.black));
        textView.setBackgroundColor(activity.getResources().getColor(R.color.grey));

        if (lastTimeElapsed == 0) {
            int hours = (int) (timeElapsed / 3600000);
            int minutes = (int) (timeElapsed - hours * 3600000) / 60000;
            int seconds = (int) (timeElapsed - hours * 3600000 - minutes * 60000) / 1000;

            lastKm.setText("Last km: " + hours + ":" + minutes + ":" + seconds);

            textView.setText("Km" + km + ": " + hours + ":" + minutes + ":" + seconds);
            timeList.addView(textView);
        } else {
            Long newTimeElapsed = timeElapsed - lastTimeElapsed;
            int hours = (int) (newTimeElapsed / 3600000);
            int minutes = (int) (newTimeElapsed - hours * 3600000) / 60000;
            int seconds = (int) (newTimeElapsed - hours * 3600000 - minutes * 60000) / 1000;

            lastKm.setText("Last km: " + hours + ":" + minutes + ":" + seconds);

            textView.setText("Km" + km + ": " + hours + ":" + minutes + ":" + seconds);
            timeList.addView(textView);
        }
        lastTimeElapsed = timeElapsed;
    }

    public void setPace(int numKm) {
        long timeElapsed = SystemClock.elapsedRealtime() - chrono.getBase();
        long pacetimeElapsed = timeElapsed / numKm;

        int hours = (int) (pacetimeElapsed / 3600000);
        int minutes = (int) (pacetimeElapsed - hours * 3600000) / 60000;
        int seconds = (int) (pacetimeElapsed - hours * 3600000 - minutes * 60000) / 1000;

        pace.setText("Pace: " + hours + ":" + minutes + ":" + seconds);
    }
}
