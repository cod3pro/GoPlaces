package com.example.borhan.cityhelp;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Direction extends FragmentActivity {

    GoogleMap map;
    ArrayList<LatLng> markerPoints;
    ArrayList<placesNode> addedPlaces = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.direction);


        //get the placesList from Result.java
        Intent intent = getIntent();
        final ArrayList<placesNode> placesList = intent.getParcelableArrayListExtra("placesList");


        // Initializing
        markerPoints = new ArrayList<LatLng>();

        // Getting reference to SupportMapFragment of the activity_main
        SupportMapFragment fm = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);

        // Getting Map for the SupportMapFragment
        map = fm.getMap();

        if(map!=null){

            // Enable MyLocation Button in the Map
            map.setMyLocationEnabled(true);

            // Setting onclick event listener for the map
            //map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {


                //@Override
                //public void onMapClick(LatLng point) {
            List<LatLng> point = new ArrayList<LatLng>();

            for(int plNum = 0; plNum < placesList.size(); plNum++) {
                //point.add(new LatLng(34.2202607056577, -118.58170174062252));
                //point.add(new LatLng(34.2278379939567767, -118.57547800987957));
                //point.add(new LatLng(34.200496337122345, -118.56625590473412));
                String temp = placesList.get(plNum).getLocatoin().split("\\(")[1];
                Double ListLat = Double.parseDouble(temp.split(",")[0]);
                String temp1 = temp.split(",")[1];
                String temp2 = temp1.split("\\)")[0];
                Double ListLng = Double.parseDouble(temp2);
                LatLng ListLatLng = new LatLng(ListLat, ListLng);

                point.add(ListLatLng);

            }

            for(int p=0;p<point.size();p++) {


                // Already two locations
                if (markerPoints.size() > 20) {
                    markerPoints.clear();
                    map.clear();
                }

                // Adding new item to the ArrayList
                markerPoints.add(point.get(p));


                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(point.get(p));


                /**
                 * For the start location, the color of marker is GREEN and
                 * for the end location, the color of marker is RED.
                 */
                if (markerPoints.size() == 1) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }


                // Add new marker to the Google Map Android API V2
                //map.addMarker(options);
                map.addMarker(options).setTitle(options.getPosition() + "");


                for (int i = 0; i < markerPoints.size() - 1; i++) {

                    // Checks, whether start and end locations are captured
                    if (markerPoints.size() >= 3) {
                        LatLng origin = markerPoints.get(i);
                        LatLng dest = markerPoints.get(i + 1);

                        // Getting URL to the Google Directions API
                        String url = getDirectionsUrl(origin, dest);

                        DownloadTask downloadTask = new DownloadTask();

                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);
                    }
                }

                onLocationChanged(point);

            }
                //}
            //});

        }


            /*
            MarkerOptions options1 = new MarkerOptions();
            LatLng temp = new LatLng(34.245605756158206,-118.55340275913477);
            options1.position(temp);
            map.addMarker(options1).showInfoWindow();
            map.getUiSettings().setMapToolbarEnabled(true);
            */

    }




    //@Override
    public void onLocationChanged(List<LatLng> location) {
        double smallLat = 90;
        double bigLat = -90;
        double smallLng = 180;
        double bigLng = -180;
        double mLatitude = 0;
        double mLongitude = 0;

        for(int num=0; num < location.size();num++){
            if(location.get(num).latitude < smallLat){
                smallLat = location.get(num).latitude;
            }
            if(location.get(num).latitude > bigLat){
                bigLat = location.get(num).latitude;
            }
            if(location.get(num).longitude < smallLng){
                smallLng = location.get(num).longitude;
            }
            if(location.get(num).longitude > bigLng){
                bigLng = location.get(num).longitude;
            }
        }

        mLatitude = (smallLat+bigLat)/2;
        mLongitude = (smallLng+bigLng)/2;

        //Toast.makeText(getApplication().getBaseContext(),mLatitude +" "+mLongitude, Toast.LENGTH_SHORT).show();

        LatLng latLng = new LatLng(mLatitude, mLongitude);

        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(10));

    }



    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;


        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }



    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }




    ////////////////////////////////////////////////////////////////////////////////////////////


    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));


        TextView ttvv = (TextView) findViewById(R.id.textView);
        ttvv.setText("");

        ttvv.append("Radius Value" + valueResult + "\nKM  " + kmInDec
                + "\nMeter   " + meterInDec);

        return Radius * c;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////



    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {


            int pathColor = 0; ////////////////////////////////
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);



                    points.add(position);

                }



                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(15);
                lineOptions.color(Color.BLUE);

            }

            // Drawing polyline in the Google Map for the i-th route
            map.addPolyline(lineOptions);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}