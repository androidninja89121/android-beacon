package android.wangridev.ble.osezbeacon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.wangridev.ble.gps.GPSTracker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aprilbrother.aprilbrothersdk.Beacon;
import com.aprilbrother.aprilbrothersdk.BeaconManager;
import com.aprilbrother.aprilbrothersdk.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    private TextView txvStatusConnection;
    private TextView txvGPS;
    private TextView txvBanner;
    private TextView txvStatusConnectionLabel;
    private TextView txvFoundedBeaconLabel;
    private TextView txvGPSLabel;
    private ListView lstBeacons;
    private BeaconAdapter adapter;

    private Timer timer;
    private TimerTask timerTask;

    public GPSTracker gps;

    private boolean isAlreadyAsked = false;


    private BeaconManager beaconManager;
    private ArrayList<Beacon> myBeacons;

    private static final int REQUEST_ENABLE_BT = 1234;
    private static final String TAG = "WI";
    private static final Region ALL_BEACONS_REGION = new Region("apr", null, null, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setGlobalVariables();

        arrangeComponents();
        init_beacon();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!beaconManager.hasBluetooth()) {
            Toast.makeText(this, "Device does not have Bluetooth Low Energy",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (!beaconManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectToService();
        }

        loadGPS();
    }

    @Override
    protected void onStop() {
        try {
            myBeacons.clear();
            beaconManager.stopRanging(ALL_BEACONS_REGION);
        } catch (RemoteException e) {
            Log.d(TAG, "Error while stopping ranging", e);
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();

//        if (timer != null){
//            timer.cancel();
//            timer = null;
//        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                connectToService();
            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG)
                        .show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadGPS(){
        GlobalScope.currentLocation = new Location(getResources().getString(R.string.app_name));

        gps = new GPSTracker(this);

        proceedTracking();

        startGPSTrackingEngine();
    }

    private void startGPSTrackingEngine(){
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                mHandlerForUpdatingCurrentStatus.obtainMessage().sendToTarget();
                transferCurrentStatus();
            }
        };

        timer.schedule(timerTask, 0, 3000);
    }

    private void proceedTracking(){
        // check if GPS enabled
        if(gps.canGetLocation()){
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            GlobalScope.currentLocation.setLatitude(latitude);
            GlobalScope.currentLocation.setLongitude(longitude);
        } else if(isAlreadyAsked == false){
            isAlreadyAsked = true;
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    private void outputLog(String msg){
        Log.e("WI", msg);
    }

    public Handler mHandlerForUpdatingCurrentStatus = new Handler() {
        public void handleMessage(Message msg) {
            if (GlobalScope.currentLocation == null){
                txvGPS.setText("GPS is not enabled.");
            } else {
                txvGPS.setText("Longitude: " + GlobalScope.currentLocation.getLongitude() + "\nLatitude: " + GlobalScope.currentLocation.getLatitude());
            }

            if (beaconManager.isBluetoothEnabled()) {
                connectToService();
            }

            txvStatusConnection.setText(GlobalScope.sConnectionStatus);
            GlobalScope.sConnectionStatus = getResources().getString(R.string.failure);
        }
    };

    private void transferCurrentStatus(){
        double dLongitude = GlobalScope.currentLocation == null ? 0 : GlobalScope.currentLocation.getLongitude();
        double dLatitude = GlobalScope.currentLocation == null ? 0 : GlobalScope.currentLocation.getLatitude();

        int nCount = 0;
        if (GlobalScope.beacons == null)nCount = 0; else nCount = GlobalScope.beacons.size();

        String sRequstUrl = GlobalScope.sServerUrl + "/?pid=" + getDeviceId() + "&n=" + nCount + "&longitude=" + dLongitude + "&latitude=" + dLatitude;

        for (int i = 0 ; i < nCount ; i ++){
            Beacon beacon = GlobalScope.beacons.get(i);
            sRequstUrl += "&mac" + i + "=" + beacon.getMacAddress() + "&uuid" + i + "=" + beacon.getProximityUUID() + "&major" + i + "=" + beacon.getMajor() + "&minor" + i + "=" + beacon.getMinor() + "&mpower" + i + "=" + beacon.getMeasuredPower() + "&rssi" + i + "=" + beacon.getRssi() + "&proximity" + i + "=" + beacon.getProximity() + "&dist" + i + "=" + beacon.getDistance();
        }
        outputLog(sRequstUrl);

        String sResponse = GlobalScope.myHTTPRequest.toString(GlobalScope.myHTTPRequest.responseHTTP(sRequstUrl));
        sResponse = sResponse.trim().equals(getResources().getString(R.string.server_answer)) ? getResources().getString(R.string.success) : getResources().getString(R.string.failure);
        GlobalScope.sConnectionStatus = sResponse;
    }

    private void setGlobalVariables(){
        GlobalScope.mainFont = Typeface.createFromAsset(getAssets(), "atwriter.ttf");
        GlobalScope.beacons = new ArrayList<Beacon>();
    }

    private void arrangeComponents(){
        txvStatusConnection = (TextView)findViewById(R.id.txvStatusConnection);
        txvGPS = (TextView)findViewById(R.id.txvGPS);
        txvBanner = (TextView)findViewById(R.id.txvBanner);
        txvStatusConnectionLabel = (TextView)findViewById(R.id.txvStatusConnectionLabel);
        txvFoundedBeaconLabel = (TextView)findViewById(R.id.txvFoundedBeaconLabel);
        txvGPSLabel = (TextView)findViewById(R.id.txvGPSLabel);

//        txvStatusConnection.setTypeface(GlobalScope.mainFont);
//        txvGPS.setTypeface(GlobalScope.mainFont);
        txvBanner.setTypeface(GlobalScope.mainFont);
        txvStatusConnectionLabel.setTypeface(GlobalScope.mainFont);
        txvFoundedBeaconLabel.setTypeface(GlobalScope.mainFont);
        txvGPSLabel.setTypeface(GlobalScope.mainFont);

        lstBeacons = (ListView)findViewById(R.id.lstBeacons);

        GlobalScope.myHTTPRequest = new MyHTTPRequest();
    }

    private String getDeviceId() {
        try {
            return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {

        }
        return "";
    }

    private void init_beacon(){
        myBeacons = new ArrayList<Beacon>();
        adapter = new BeaconAdapter(this);
        lstBeacons.setAdapter(adapter);

        beaconManager = new BeaconManager(this);
        beaconManager.setForegroundScanPeriod(3000, 0);

        beaconManager.setRangingListener(beaconRangingListener);
        beaconManager.setMonitoringListener(beaconMonitoringListener);
    }

    private BeaconManager.MonitoringListener beaconMonitoringListener = new BeaconManager.MonitoringListener() {
        @Override
        public void onEnteredRegion(Region region, List<Beacon> list) {
            outputLog("Entered Region!");
        }

        @Override
        public void onExitedRegion(Region region) {
            outputLog("Exited Region!");
        }
    };

    private BeaconManager.RangingListener beaconRangingListener = new BeaconManager.RangingListener() {
        @Override
        public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
            myBeacons.clear();
            myBeacons.addAll(beacons);
            if (beacons != null && beacons.size() > 0) {
                for (Beacon beacon : beacons) {
                    Log.d(TAG, "mac = " + beacon.getMacAddress()
                            + "+++major = " + beacon.getMajor()
                            + "+++minor = " + beacon.getMinor());
                    Log.d(TAG, "power = " + beacon.getPower());
                }
                Log.d(TAG, "-------------------------------");
            }

            GlobalScope.beacons = beacons;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.replaceWith(myBeacons);
                }
            });
        }
    };

    private void connectToService() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_BEACONS_REGION);
                } catch (RemoteException e) {

                }
            }
        });
    }
}
