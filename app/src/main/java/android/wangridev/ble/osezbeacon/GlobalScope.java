package android.wangridev.ble.osezbeacon;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.location.Location;
import android.wangridev.ble.gps.GPSTracker;
import android.widget.TextView;

import com.aprilbrother.aprilbrothersdk.Beacon;

import java.util.List;

/**
 * Created by Administrator on 7/24/2015.
 */
public class GlobalScope extends Application {
    public static Typeface mainFont;
    public static Location currentLocation;
    public static String sServerUrl = "http://morafig.com/request.php";
    public static MyHTTPRequest myHTTPRequest;
    public static String sConnectionStatus = "";
    public static List<Beacon> beacons;
}
