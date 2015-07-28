package com.example.mukundthakur.fblogin;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class MapActivity extends ActionBarActivity {

    private GoogleMap googleMap;
    private String postURL = "http://192.168.2.5:8080/employee/create";
    private String getURL = "http://192.168.2.5:8080/employee/getNear/23";
    private double latitude;
    private double longitude;
    private Marker marker = null;
    private Map<String, Marker> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        list = new HashMap<String, Marker>();
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        googleMap = fm.getMap();
        googleMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);
        UpdateMapPos(location);

        LocationListener locationlistener =new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {

                UpdateMapPos(loc);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0, 0, locationlistener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0, 0, locationlistener);
        Button button = (Button) findViewById(R.id.rideButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button buttonl = (Button) findViewById(R.id.ridelButton);

        buttonl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void UpdateMapPos(Location location){

        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            googleMap.setMyLocationEnabled(true);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            if(marker != null){
                marker.setPosition(latLng);
            }
            else{
                marker = googleMap.addMarker(new MarkerOptions().title("azhar").position(latLng));
            }
            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute();
        }

    }

    public void PostUserInfo() throws IOException, JSONException {
        Thread t = new Thread(new Runnable() {

            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost;
                JSONObject json = new JSONObject();

                try {
                    HttpResponse response;
                    httpPost = new HttpPost(postURL);
                    json.put("firstName", "azhar");
                    json.put("sourceLat", Double.toString(latitude));
                    json.put("sourceLong", Double.toString(longitude));

                    StringEntity se = new StringEntity(json.toString());
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    httpPost.setEntity(se);
                    response = httpClient.execute(httpPost);


                    int code =  response.getStatusLine().getStatusCode(); //Get the data in the entity

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        t.start();
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Void, Map<String, LatLng>> implements com.example.mukundthakur.AsyncTaskRunner {
        @Override
        protected Map<String, LatLng> doInBackground(Void... params) {

            Map<String, LatLng> map = new HashMap<String, LatLng>();

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response;
                HttpGet get = new HttpGet(getURL);
                response = httpClient.execute(get);

                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

                String line = "";
                line = rd.readLine();
                JSONArray obj = new JSONArray(line);
                int i = 0;

                while (i < obj.length()) {
                    double lat = obj.getJSONObject(i).getDouble("sourceLat");
                    double longit = obj.getJSONObject(i).getDouble("sourceLong");
                    String name = (String) obj.getJSONObject(i).getString("firstName");

                    LatLng latLng = new LatLng(lat, longit);

                    map.put(name, latLng);
                    i++;
                }
            } catch(Exception e){
                e.printStackTrace();
            }
            return map;

        }

        @Override
        protected void onPostExecute(Map<String, LatLng> result) {

            Set<String> keys = result.keySet();
            for (String key : keys) {
                if(list.get(key) != null){
                    list.get(key).setPosition(result.get(key));
                }
                else{
                    Marker newmarker = googleMap.addMarker(new MarkerOptions().title(key).position(result.get(key)));
                    list.put(key, newmarker);
                }
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }


}