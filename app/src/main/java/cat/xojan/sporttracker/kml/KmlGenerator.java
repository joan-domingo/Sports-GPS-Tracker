package cat.xojan.sporttracker.kml;

import android.os.Environment;
import android.text.format.Time;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Joan on 01/08/2014.
 */
public class KmlGenerator {

    private String body;
    private String markers;

    public KmlGenerator() {
        this.body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
                "<Document>\n" +
                "  <Placemark>\n" +
                "   <LineString>\n" +
                "    <altitudeMode>relative</altitudeMode>\n" +
                "    <coordinates>\n";
        markers = "";
    }

    public void createFile() {
        body = body + "</coordinates>\n" +
                "   </LineString>\n" +
                "  </Placemark>\n" +
                markers +
                " </Document>\n" +
                "</kml>";
        File root = new File(Environment.getExternalStorageDirectory(), "Sport GPS Tracker Routes");
        if (!root.exists()) {
            root.mkdirs();
        }
        File file = new File(root, getFileName());
        try {
            FileWriter writer = new FileWriter(file);
            writer.append(body);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileName() {
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        return today.monthDay + "-" + today.month + "-" + today.year + "-" + today.format("%k-%M-%S") + "GpsTrack.kml";
    }

    public void addCoordinate(String coordinate) {
        body = body + coordinate + "\n";
    }

    public void addMarker(String text, String coordinate) {
        markers = markers +
                "<Placemark>\n" +
                "    <name>" + text + "</name>\n" +
                "    <Point>\n" +
                "      <coordinates>" + coordinate + "</coordinates>\n" +
                "    </Point>\n" +
                "  </Placemark>\n";
    }
}
