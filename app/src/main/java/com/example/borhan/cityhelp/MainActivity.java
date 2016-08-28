package com.example.borhan.cityhelp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends FragmentActivity implements LocationListener{

    GoogleMap mGoogleMap;
    Spinner mSprPlaceType;

    String name;
    String vicinity;

    String[] mPlaceType=null;
    String[] mPlaceTypeName=null;

    double mLatitude=0;
    double mLongitude=0;

    int radius = 0;
    String rating="";


    private static SeekBar seek_bar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seek_bar = (SeekBar)findViewById(R.id.seekBar);

        seek_bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        radius = progress;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );



        // Array of place types
        mPlaceType = getResources().getStringArray(R.array.place_type);

        // Array of place type names
        mPlaceTypeName = getResources().getStringArray(R.array.place_type_name);

        // Creating an array adapter with an array of Place types
        // to populate the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, mPlaceTypeName);

        // Getting reference to the Spinner
        mSprPlaceType = (Spinner) findViewById(R.id.spr_place_type);

        // Setting adapter on Spinner to set place types
        mSprPlaceType.setAdapter(adapter);

        Button btnFind;

        // Getting reference to Find Button
        btnFind = ( Button ) findViewById(R.id.btn_find);


        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());


        if(status!=ConnectionResult.SUCCESS){ // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        }else { // Google Play Services are available

            // Getting reference to the SupportMapFragment
            SupportMapFragment fragment = ( SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            // Getting Google Map
            mGoogleMap = fragment.getMap();

            // Enabling MyLocation in Google Map
            mGoogleMap.setMyLocationEnabled(true);


            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 50000,0,this);


            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location From GPS
            Location location = locationManager.getLastKnownLocation(provider);

            //
            Toast.makeText(getApplication().getBaseContext(),"Locaiton changed: "+location, Toast.LENGTH_SHORT).show();


            if(location!=null){
                onLocationChanged(location);

            }

            locationManager.requestLocationUpdates(provider, 20000, 0, this);



            // Setting click event lister for the find button
            btnFind.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {


                    int selectedPosition = mSprPlaceType.getSelectedItemPosition();
                    String type = mPlaceType[selectedPosition];


                    StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/textsearch/json?");
                    sb.append("query="+type);
                    sb.append("&location="+mLatitude+","+mLongitude);
                    sb.append("&sensor=true");
                    sb.append("&radius="+radius);
                    sb.append("&key=AIzaSyC29bA2extNrg1Lt7x7VyBk52lmHZu7LWU");


                    // Creating a new non-ui thread task to download Google place json data
                    PlacesTask placesTask = new PlacesTask();

                    // Invokes the "doInBackground()" method of the class PlaceTask
                    placesTask.execute(sb.toString());


                }
            });


        }


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////



        ////////////////////////////////////////////////////////////////////////////////////////////////////////////



    }





    ///////////////////////////////////////////////
    /*
    final String[] FRUITS = new String[] { "Apple", "Avocado", "Banana",
            "Blueberry", "Coconut", "Durian", "Guava", "Kiwifruit",
            "Jackfruit", "Mango", "Olive", "Pear", "Sugar-apple" };
    ArrayAdapter<String> adapter1;

    public void listViewShow() {
        setContentView(R.layout.activity_result);
        adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,FRUITS);
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(adapter1);
    }
    */

    ///////////////////////////////////////////////



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


    /** A class, to download Google Places */
    private class PlacesTask extends AsyncTask<String, Integer, String>{

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result){
            ParserTask parserTask = new ParserTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }

    }


    String address;


    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String,String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject);


            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }




        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(final List<HashMap<String,String>> list){

            // Clears all the existing markers
            mGoogleMap.clear();



            for(int i=0;i<list.size();i++){

                /*
                placeNode.setName(null);
                placeNode.setLocatoin(null);
                placeNode.setAddress(null);
                placeNode.setPlace_id(null);
                */

                final HashMap<String, String> hmPlace = list.get(i);

                // Creating a marker
                final MarkerOptions markerOptions = new MarkerOptions();



                //popup1 = hmPlace.get("place_name");
                //Collection bori = hmPlace.values();
                //Iterator itr = bori.iterator();




                // only show the places with a rating
                //if(rating!="0"){


                    // Getting latitude of the place
                    double lat = Double.parseDouble(hmPlace.get("lat"));

                    // Getting longitude of the place
                    final double lng = Double.parseDouble(hmPlace.get("lng"));

                    // Getting name
                    name = hmPlace.get("place_name");

                    // Getting vicinity
                    vicinity = hmPlace.get("vicinity");

                    //Getting rating
                    rating = hmPlace.get("rating");

                    address = hmPlace.get("address");

                    final LatLng latLng = new LatLng(lat, lng);



                    ////////////////////////////////////////////////////////////////////////////////

                    //Toast.makeText(getApplication().getBaseContext(),"@i is: "+i+" @PlacesList Name: "+placesList.get(i).getName(), Toast.LENGTH_SHORT).show();











                ////////////////////////////////////////////////////////////////////////////////
                    // Setting the position for the marker
                    markerOptions.position(latLng);

                    // Setting the title for the marker.
                    //This will be displayed on taping the marker
                    markerOptions.title(name);
                    markerOptions.snippet("Rating: "+rating+"\n vicinity: "+vicinity);

                    // Placing a marker on the touched position
                    mGoogleMap.addMarker(markerOptions);


                ///////////////////////////////////////////////////////////////////////////////////////////////////////
                /*
                TextView nm = (TextView) findViewById(R.id.tempText);
                JSONObject json = new JSONObject(hmPlace);
                try {
                    JSONObject openingHours = json.getJSONObject("08b1f7cf2e3a2f1f260787d507ff187a18481677");
                    String placeName = openingHours.toString();
                    nm.setText(openingHours.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                */
                //tempText.setText(list.get(0).get("place_name")+"\n"+list.get(0).get("price_level")+"\n"+list.get(0).get("address")+"\n"+list.get(0).get("phone_number"));
                    //Toast.makeText(getApplication().getBaseContext(),"BORHAN: "+list.get(1).get("place_name"), Toast.LENGTH_SHORT).show();
                ///////////////////////////////////////////////////////////////////////////////////////////////////////



                    mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {


                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            //String popup = marker.getPosition().toString();
                            //popup1 = marker.getPosition().toString();











                            //Toast.makeText(getApplication().getBaseContext(),"Marker: "+marker.getPosition()+"\nLatLng: "+latLng, Toast.LENGTH_SHORT).show();


                            //Toast.makeText(getApplication().getBaseContext(),"Added", Toast.LENGTH_SHORT).show();
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.getPosition(), 14));
                            //hmPlace1.put("place1",marker.getPosition().toString());
                            //String place_id = hmPlace.get("place_id");

                            //setContentView(R.layout.popup);
                            //textViewPop.setText("hellooooooooo");
                            String temp1 = marker.getPosition().toString();


                            //////////////////////////////////////////////////////////////////////////////////////////////






                            //TextView tempText = (TextView) findViewById(R.id.tempText);
                            //tempText.setText(temp1);

                            showPopup(temp1, marker, list);

                            //Intent b1 = new Intent(MainActivity.this, Result.class);
                            //b1.putExtra("place_id", place_id);
                            //startActivity(b1);

                            return true;
                        }
                    });




                //}


            }

        }

    }























    String data1;
    int tempNum = 0;

    public void setData(String str){
        data1 = str;
    }

    public String getData(){
        return data1;
    }



    int i = 0;
    ArrayList<placesNode> placesList = new ArrayList<placesNode>();
    ArrayList<String> added1 = new ArrayList<String>();
    //String[] added = new String[100];
    TextView textViewPop;
    Button Close;
    Button Create;
    Button Delete;
    private PopupWindow pw;
    private void showPopup(final String n, final Marker marker, final List<HashMap<String,String>> list) {
        try {

            // We need to get the instance of the LayoutInflater
            final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup,(ViewGroup) findViewById(R.id.popup_1));
            pw = new PopupWindow(layout, 500, 700, true);



            pw.showAtLocation(layout, Gravity.CENTER, 0, 520);


            Close = (Button) layout.findViewById(R.id.close_popup);
            Create = (Button) layout.findViewById(R.id.add_popup);
            Delete = (Button) layout.findViewById(R.id.remove_popup);


            Create.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
                    //Toast.makeText(getApplication().getBaseContext(), n, Toast.LENGTH_SHORT).show();
                    //added1.add(n);

                    LatLng markerLatLng = marker.getPosition();
                    placesNode placeNode = new placesNode();

                    for(int num=0; num<list.size();num++) {

                        // Getting latitude of the place
                        double lat1 = Double.parseDouble(list.get(num).get("lat"));
                        // Getting longitude of the place
                        double lng1 = Double.parseDouble(list.get(num).get("lng"));
                        LatLng latLng = new LatLng(lat1, lng1);


                        if (markerLatLng.equals(latLng)) {
                            placeNode.setName(list.get(num).get("place_name"));
                            placeNode.setLocatoin(latLng.toString());
                            placeNode.setAddress(list.get(num).get("address"));
                            placeNode.setPlace_id("134324324");
                            placeNode.setRating(list.get(num).get("rating"));
                            break;

                            //Toast.makeText(getApplication().getBaseContext(), placeNode.getName(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    placesList.add(tempNum, placeNode);
                    tempNum++;

                    Toast.makeText(getApplication().getBaseContext(), "added: "+placesList.get(0).getName()+"\n"+tempNum, Toast.LENGTH_SHORT).show();

                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    pw.dismiss();

                }
            });


            Delete.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Boolean deleted = false;



                    LatLng markerLatlng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);

                    for(int num=0; num < placesList.size();num++) {

                        String temp = placesList.get(num).getLocatoin().split("\\(")[1];
                        Double ListLat = Double.parseDouble(temp.split(",")[0]);
                        String temp1 = temp.split(",")[1];
                        String temp2 = temp1.split("\\)")[0];
                        Double ListLng = Double.parseDouble(temp2);
                        LatLng ListLatLng = new LatLng(ListLat, ListLng);


                        if (ListLatLng.equals(markerLatlng)) {
                            Toast.makeText(getApplication().getBaseContext(), placesList.get(num).getName()+" successfully deleted!", Toast.LENGTH_SHORT).show();
                            placesList.remove(placesList.get(num));
                            deleted = true;
                            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            tempNum--;
                        }
                    }

                    if(deleted == false) {
                        Toast.makeText(getApplication().getBaseContext(), "Uppppps, nothing found like that!", Toast.LENGTH_SHORT).show();
                    }
                    pw.dismiss();

                }
            });


            Close.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
                    //Toast.makeText(getApplication().getBaseContext(), "Close", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, Result.class);
                    //Bundle bundle = new Bundle();

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    //intent.putExtra("added1", added1);
                    //bundle.putSerializable("added1", added1);

                    //listViewShow();
                    intent.putParcelableArrayListExtra("placesList1",placesList);
                    //intent.putExtras(bundle);
                    //intent.putExtra("added", added);
                    startActivity(intent);
                    pw.dismiss();



                    //Disable GPS
                    //Intent intent1 = new Intent("android.location.GPS_ENABLED_CHANGE");
                    //intent.putExtra("enabled", false);
                    //sendBroadcast(intent1);

                }
            });



            //Create.setOnClickListener(create_button);
            //Delete.setOnClickListener(delete_button);
            //Close.setOnClickListener(cancel_button);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class addButton implements OnClickListener
    {
        String n;
        public addButton(String n) {
            this.n = n;
        }

        @Override
        public void onClick(View v)
        {
            Toast.makeText(getApplication().getBaseContext(),"Added", Toast.LENGTH_SHORT).show();
        }

    };



    private View.OnClickListener cancel_button = new View.OnClickListener() {
        public void onClick(View v) {
            pw.dismiss();
        }
    };



    private View.OnClickListener create_button = new View.OnClickListener() {
        public void onClick(View v) {
            //Toast.makeText(getApplication().getBaseContext(), mPlaceTypeName[0].toString(), Toast.LENGTH_SHORT).show();

        }
    };



    private View.OnClickListener delete_button = new View.OnClickListener() {
        public void onClick(View v) {
            Toast.makeText(getApplication().getBaseContext(),"Removed", Toast.LENGTH_SHORT).show();

        }
    };



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }



    @Override
    public void onLocationChanged(Location location) {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            LatLng latLng = new LatLng(mLatitude, mLongitude);

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(14));

    }


    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

}
