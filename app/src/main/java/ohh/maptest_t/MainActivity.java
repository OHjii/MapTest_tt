package ohh.maptest_t;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import java.util.ArrayList;

import static ohh.maptest_t.R.id.mapview;

public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    private Context mContext;
    //private boolean m_bTrackingMod;
    private boolean m_bTrackingMod = true;

    private TMapData tmapdata ;
    private TMapGpsManager tmapgps;
    private TMapView tmapview;
    private TMapPoint tPointStart, tPointEnd, tsetPoint;
    private TMapPolyLine tpolyLine;

    private static String mApikey = "b1914c83-23f8-383b-989f-784d06066b6f";
    private int mMarkerId;

    private ArrayList<TMapPoint> m_tmapPoint = new ArrayList<TMapPoint>();
    private ArrayList<String> mArrayMarkerID = new ArrayList<String>();
    private ArrayList<MapPoint> m_mapPoint = new ArrayList<MapPoint>();

    private String address;
    private Double lat = null;
    private Double lon = null;

    private Button bt_find,btn_gps;
    private Switch pathshowSwitch;
    private LinearLayout linearLayout;

    private TextView tvDestination, tvDeparture;
    private  View dialogView;

    private String strDeparture, strDestination;
    private double departureLat, departureLon;
    private String strDepartureName;

    TextView editDeparture,editDestination;



    @Override
    public void onLocationChange(Location location) {
        if (m_bTrackingMod) {
            tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMap();

        tmapdata = new TMapData();
        mContext = this;
        tpolyLine = new TMapPolyLine();
        bt_find = (Button) findViewById(R.id.bt_find);
        tvDeparture = (TextView) findViewById(R.id.tvDeparture);
        tvDestination = (TextView) findViewById(R.id.tvDestination);

        btn_gps = (Button)findViewById(R.id.gps);

        bt_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchAddress();
            }
        });

        btn_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsI();
            }
        });

    }

    public void gpsI(){
        Intent gpsIntent = new Intent(this,Gps_find.class);
        startActivity(gpsIntent);
    }

    public void initMap() {
        linearLayout = (LinearLayout) findViewById(mapview);
        tmapview = new TMapView(this);
        linearLayout.addView(tmapview);
        tmapview.setSKPMapApiKey(mApikey);
        tmapview.setIconVisibility(true);
        tmapview.setZoomLevel(15);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setTrackingMode(true);
        tmapview.setSightVisible(true);

        tmapgps = new TMapGpsManager(MainActivity.this);
        tmapgps.setMinTime(100);
        tmapgps.setMinDistance(5);
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER);
        tmapgps.OpenGps();
    }

    public void searchAddress() {
        tmapview.removeAllMarkerItem(); //다시 검색할 때 이전 마커들이 남지 않게.
        dialogView = (View) View.inflate(MainActivity.this, R.layout.dialog1, null);
        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);

        dlg.setTitle("주소검색");
        dlg.setIcon(R.mipmap.ic_launcher);
        dlg.setView(dialogView);

        editDeparture = (TextView) dialogView.findViewById(R.id.editDeparture);
        editDestination = (TextView) dialogView.findViewById(R.id.editDestination);

        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                tvDeparture.setText(editDeparture.getText());
                tvDestination.setText(editDestination.getText());

                strDeparture = editDeparture.getText().toString();
                strDestination = editDestination.getText().toString();

             tmapdata.findAddressPOI(strDeparture, new TMapData.FindAddressPOIListenerCallback() {
                    @Override
                    public void onFindAddressPOI(ArrayList<TMapPOIItem> poiItem) {
                            TMapPOIItem item = poiItem.get(0);
                            Log.d("주소로 검색 ", "POI Name: " + item.getPOIName().toString() + ", " +
                                    "Address: " + item.getPOIAddress().replace("null", "") + ", " +
                                    "Point: " + item.getPOIPoint().toString());

                             departureLat = item.getPOIPoint().getLatitude();
                             departureLon = item.getPOIPoint().getLongitude();
                             tPointStart = new TMapPoint(departureLat, departureLon);
                             //tPointStart = tmapgps.getLocation();
                             tsetPoint = tPointStart;
                             showMarker();
                    }
                });

               tmapdata.findAddressPOI(strDestination, new TMapData.FindAddressPOIListenerCallback() {
                   @Override
                   public void onFindAddressPOI(ArrayList<TMapPOIItem> poiItem) {
                           TMapPOIItem item = poiItem.get(0);
                           Log.d("주소로 검색 ", "POI Name: " + item.getPOIName().toString() + ", " +
                                   "Address: " + item.getPOIAddress().replace("null", "") + ", " +
                                   "Point: " + item.getPOIPoint().toString());
                           departureLat = item.getPOIPoint().getLatitude();
                           departureLon = item.getPOIPoint().getLongitude();
                           tPointEnd = new TMapPoint(departureLat, departureLon);
                           tsetPoint = tPointEnd;
                           showMarker();
                       }
               });
                pathSwitch(); // addPoint()와 pathSwitch()만 사용하면 스위치로 on/off해서 사용 가능.
            }
        });
        dlg.setNegativeButton("취소", null);
        dlg.show();

    }


    public void showMarker(){ //검색한 장소에 마커를 찍어줌
        TMapMarkerItem titem = new TMapMarkerItem();
        titem.setTMapPoint(tsetPoint);
        titem.setVisible(TMapMarkerItem.VISIBLE);

        titem.setCanShowCallout(true);
        titem.setAutoCalloutVisible(true);

        Bitmap bitmap;
        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ico_spot);
        titem.setIcon(bitmap);
        String strID = String.format("pmarker%d", mMarkerId++);
        tmapview.addMarkerItem(strID, titem);
        mArrayMarkerID.add(strID);
    }


    public void pathSwitch() {
        pathshowSwitch = (Switch) findViewById(R.id.pathshowSwitch);
        pathshowSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (pathshowSwitch.isChecked()) {
                    pathfind();
                    Toast.makeText(MainActivity.this, "경로ON", Toast.LENGTH_SHORT).show();
                } else {
                    tmapview.removeTMapPath();
                    Toast.makeText(MainActivity.this, "경로OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void pathfind() {  ////경로에 선을 그어줌
        tmapdata.findPathData(tPointStart, tPointEnd, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tpolyLine) {
                tmapview.addTMapPath(tpolyLine);
            }
        });
    }

    private void searchRoute(TMapPoint start, TMapPoint end){
        TMapData data = new TMapData();
        tmapdata.findPathData(start, end, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(final TMapPolyLine path) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        path.setLineWidth(5);
                        path.setLineColor(Color.RED);
                        tmapview.addTMapPath(path);
                    }
                });
            }
        });
    }

    public void clickButton() { //포인트마커를 클릭하면 상세주소를 알려줌
        tmapview.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(TMapMarkerItem markerItem) {
                lat = markerItem.latitude;
                lon = markerItem.longitude;

                tmapdata.convertGpsToAddress(lat, lon, new TMapData.ConvertGPSToAddressListenerCallback() {
                    @Override
                    public void onConvertToGPSToAddress(String strAddress) {
                        address = strAddress;
                    }
                });
                Toast.makeText(MainActivity.this, "주소 : " + address, Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void addPoint() {
        m_tmapPoint.add(tPointStart);
        m_tmapPoint.add(tPointEnd);
    }


}



