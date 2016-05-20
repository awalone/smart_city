package com.awalone.smartcity;


import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import me.drakeet.materialdialog.MaterialDialog;


public class Laporan_map_Fragment extends Fragment implements LocationListener {

    MapView mMapView;
    GoogleMap googleMap;
    private int PROXIMITY_RADIUS = 5000;
    private static final String GOOGLE_API_KEY = "AIzaSyD66rFJ9-lOCbOCT8uOSCAG7frV0gNMJvE";
    double latitude = 0;
    double longitude = 0;
    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    MaterialDialog mMaterialDialog;

    String image_url, nama, keluhan, waktu;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View x = inflater.inflate(R.layout.lapor_map_layout, container, false);
        mMapView = (MapView) x.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();
        googleMap.setMyLocationEnabled(true);
        // latitude and longitude

        // adding marker
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // getting GPS status
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled || !isNetworkEnabled) {
            // no network provider is enabled
            mMaterialDialog = new MaterialDialog(getActivity())
                    .setTitle("Peringatan!")
                    .setMessage("GPS Anda Tidak Aktif")
                    .setPositiveButton("Pengaturan", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                            mMaterialDialog.dismiss();
                        }
                    })
                    .setNegativeButton("Batal", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mMaterialDialog.dismiss();

                        }
                    });
            mMaterialDialog.show();
        } else {
            googleMap.setMyLocationEnabled(true);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(bestProvider);
//            latitude = location.getLatitude();
//            longitude = location.getLongitude();
//            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(15).build();
//            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            if (location != null) {
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);

            //Search Nearby
            new readFromGooglePlaceAPI()
                    .execute(Config.NEWS_FEED
                    );
        }

        return x;
    }

    public String readJSON(String URL) {
        StringBuilder sb = new StringBuilder();
        HttpGet httpGet = new HttpGet(URL);
        HttpClient client = new DefaultHttpClient();

        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } else {
                Log.e("JSON", "Couldn't find JSON file");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public class readFromGooglePlaceAPI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... param) {
            return readJSON(param[0]);
        }

        protected void onPostExecute(String str) {
            Log.d("Cekname", str);
//  myArrayList = new ArrayList<GetterSetter>();
            try {
                JSONObject root = new JSONObject(str);
                JSONArray results = root.getJSONArray("keluhan");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject arrayItems = results.getJSONObject(i);
                    // JSONObject geometry = arrayItems.getJSONObject("geometry");
                    // JSONObject location = geometry.getJSONObject("location");


//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

                    double lat = Double.valueOf(arrayItems.getString("latitude").toString());
                    double lng = Double.valueOf(arrayItems.getString("longitude").toString());
                    nama = arrayItems.getString("nama").toString();
                    keluhan = arrayItems.getString("keluhan").toString();
                    waktu = arrayItems.getString("waktu").toString();
                    image_url = arrayItems.getString("gambar").toString();
                    LatLng latLng = new LatLng(lat, lng);

//                    markerOptions.getIcon();
//                    markerOptions.position(latLng);
//                    markerOptions.title("Waktu\t: " + waktu + "\n"+"Nama\t: "+ nama +"\n"+"Keluhan\t:"+keluhan);
//                    googleMap.addMarker(markerOptions);
                    googleMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .title(image_url)
                            .snippet(waktu + "%" + nama + "%" + keluhan)
                            .position(latLng));


                    googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                        @Override
                        public View getInfoWindow(Marker arg0) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker arg0) {

                            View v = getActivity().getLayoutInflater().inflate(R.layout.custom_info_content, null);

                            ImageLoader imageLoader = AppController.getInstance().getImageLoader();
                            String img_url = arg0.getTitle();
                            String img_url_clear = img_url.replace("\\", "");

                            String sinppet = arg0.getSnippet();
                            String[] separated = sinppet.split("%");


                            final ImageView image = ((ImageView) v.findViewById(R.id.foto));
                            imageLoader.get(img_url_clear, ImageLoader.getImageListener(image, R.mipmap.ic_launcher, R.mipmap.failed));

                            TextView TvWaktu = ((TextView) v.findViewById(R.id.waktu));
                            TvWaktu.setText("Waktu\t: " + separated[0]);

                            TextView TvNama = ((TextView) v.findViewById(R.id.nama));
                            TvNama.setText("Nama\t: " + separated[1]);

                            TextView TvKeluhan = ((TextView) v.findViewById(R.id.keluhan));
                            TvKeluhan.setText("Keluhan\t: " + separated[2]);


                            return v;

                        }
                    });

//                    Log.d("Before", myArrayList.toString());

                }

            } catch (Exception e) {

            }
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(15).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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
