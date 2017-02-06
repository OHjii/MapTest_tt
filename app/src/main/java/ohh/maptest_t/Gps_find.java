package ohh.maptest_t;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Gps_find extends MainActivity {
    Button startButton;
    TextView txtLatitude, txtLongitude;
    double latitude;
    double longitude;
    boolean isGetLocation = false;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) throws SecurityException {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_gps);
        getPermission();

        startButton = (Button) findViewById(R.id.btn_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationService();
                if(isGetLocation) {
                    showLocation(latitude, longitude);
                }
                else{
                    settingGps();
                }
            }
        });

    }

    private void showLocation(double lat, double lon) {
        txtLatitude = (TextView) findViewById(R.id.latitude);
        txtLongitude = (TextView) findViewById(R.id.longitude);
        txtLatitude.setText(String.valueOf(lat));
        txtLongitude.setText(String.valueOf(lon));
        insert();
    }

    private void getPermission() {

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(Gps_find.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(Gps_find.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();
    }

    public Location startLocationService() throws SecurityException {
        boolean isGPSEnabled = false;
        boolean isNetworkEnabled = false;
        long minTime = 5000;
        float minDistance = 0;

        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        isGPSEnabled= manager.isProviderEnabled(manager.GPS_PROVIDER);
        isNetworkEnabled=manager.isProviderEnabled(manager.NETWORK_PROVIDER);
        GpsInformation listener = new GpsInformation();

        if(isNetworkEnabled) {
            this.isGetLocation = true;
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, listener);
            if(manager != null) {
                location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
        }
        if(isGPSEnabled) {
            this.isGetLocation = true;
            if(location == null) {
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, listener);
                if(manager != null) {
                    location = manager.getLastKnownLocation(manager.GPS_PROVIDER);
                    if(location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
        }
        return location;
    }

    public void settingGps(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Gps_find.this);
        alertDialog.setTitle("GPS사용유무셋팅");
        alertDialog.setMessage("GPS 셋팅이 되지 않았을수도 있습니다.\n 설정창으로 가시겠습니까?\n");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        Gps_find.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    class GpsInformation implements LocationListener {

        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            showLocation(latitude, longitude);
        }

        @Override
        public void onProviderDisabled(String provider) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    public void insert() {
        String latitude = txtLatitude.getText().toString();
        String longitude = txtLongitude.getText().toString();

        insertToDatabase(latitude, longitude);
    }

    private void insertToDatabase(String latitude, String longitude){

        class InsertData extends AsyncTask<String, Void, String>{
            @Override
            protected void onPreExecute() {
            }

            @Override
            protected void onPostExecute(String s) {
                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(String... params) {

                try{
                    String latitude = (String)params[0];
                    String longitude = (String)params[1];

                    String link="http://203.153.146.53/insert.php";
                    String data  = URLEncoder.encode("latitude", "UTF-8") + "=" + URLEncoder.encode(latitude, "UTF-8");
                    data += "&" + URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode(longitude, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write( data );
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while((line = reader.readLine()) != null)
                    {
                        sb.append(line);
                        break;
                    }
                    return sb.toString();
                }
                catch(Exception e){
                    return new String("Exception: " + e.getMessage());
                }

            }
        }

        InsertData task = new InsertData();
        task.execute(latitude, longitude);
    }


}