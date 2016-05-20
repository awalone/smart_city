package com.awalone.smartcity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;


public class Laporan_all_Fragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, LocationListener {
    private static final String TAG = "DaengSampah";
    private ListView listView;
    private FeedListAdapter listAdapter;
    private List<FeedItem> feedItems;
    SwipeRefreshLayout swipeRefreshLayout;
    View x;
    ArrayList<HashMap<String, String>> list_data;

    MapView mMapView;
    GoogleMap googleMap;

    MaterialDialog mMaterialDialog, mapDialog;
    String namanya, waktu, keluhan, gambar;
    Double lati, longi;

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        x = inflater.inflate(R.layout.lapor_all_layout, container, false);

        final View n = LayoutInflater.from(getActivity()).inflate(R.layout.map_popup, container, false);

        listView = (ListView) x.findViewById(R.id.list);
        swipeRefreshLayout = (SwipeRefreshLayout) x.findViewById(R.id.swipe_refresh_layout);
        feedItems = new ArrayList<FeedItem>();

        listAdapter = new FeedListAdapter(getActivity(), feedItems);
        listView.setAdapter(listAdapter);

        mMapView = (MapView) n.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();
        googleMap.setMyLocationEnabled(true);


        //fungsi ketika nama penyakit di klik
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    final int position, long id) {

                namanya = list_data.get(position).get("nama");
                lati = Double.valueOf(list_data.get(position).get("lat"));
                longi = Double.valueOf(list_data.get(position).get("long"));
                waktu = list_data.get(position).get("waktu");
                gambar = list_data.get(position).get("gambar");
                keluhan = list_data.get(position).get("keluhan");


                mMaterialDialog = new MaterialDialog(getActivity())
                        .setTitle("Informasi")
                        .setMessage("Apakah anda ingin melihat laporan " + namanya + "?")
                        .setPositiveButton("Iya", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMaterialDialog.dismiss();

                                if (n.getParent() != null) {
                                    ((ViewGroup) n.getParent()).removeView(n);
                                }

                                LatLng latLng = new LatLng(lati, longi);

                                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lati, longi)).zoom(15).build();
                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                googleMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                        .title(gambar)
                                        .snippet(waktu + "%" + namanya + "%" + keluhan)
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

                                mapDialog = new MaterialDialog(getActivity())
                                        .setView(n);
                                mapDialog.show();
                            }
                        })
                        .setNegativeButton("Tidak", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMaterialDialog.dismiss();

                            }
                        });

                mMaterialDialog.show();
            }

        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()

                                                {
                                                    @Override
                                                    public void onRefresh() {
                                                        // Your code to refresh the list here.
                                                        // Make sure you call swipeContainer.setRefreshing(false)
                                                        // once the network request has completed successfully.
                                                        freshReq();
                                                    }
                                                }

        );
        swipeRefreshLayout.post(new

                                        Runnable() {
                                            @Override
                                            public void run() {
                                                swipeRefreshLayout.setRefreshing(true);
                                                freshReq();
                                            }
                                        }

        );


        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).

                getState()

                == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).

                        getState()

                        == NetworkInfo.State.CONNECTED)

        {
            //we are connected to a network
            connected = true;
        } else
            connected = false;
        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(Config.NEWS_FEED);
        if (entry != null && !connected)

        {
            // fetch the data from cache
            try {
                String data = new String(entry.data, "UTF-8");
                try {
                    parseJsonFeed(new JSONObject(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } else

        {
            freshReq();
        }

        return x;
    }

    public void freshReq() {
        // making fresh volley request and getting json
        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                Config.NEWS_FEED, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                VolleyLog.d(TAG, "Response: " + response.toString());
                if (response != null) {
                    parseJsonFeed(response);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });

        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(jsonReq);
    }

    /**
     * Parsing json reponse and passing the data to feed view list adapter
     */

    private void parseJsonFeed(JSONObject response) {
        try {
            if (response.length() > 0) {
                JSONArray feedArray = response.getJSONArray("keluhan");
                feedItems.clear();
                list_data = new ArrayList<HashMap<String, String>>();

                for (int i = 0; i < feedArray.length(); i++) {

                    JSONObject feedObj = (JSONObject) feedArray.get(i);

                    FeedItem item = new FeedItem();
                    item.setId(feedObj.getInt("id_keluhan"));
                    item.setNama(feedObj.getString("nama"));

                    // Image might be null sometimes
                    String image = feedObj.isNull("gambar") ? null : feedObj
                            .getString("gambar");
                    String img_url_clear = image.replace("\\", "");
                    item.setGambar(img_url_clear);
                    item.setKomentar(feedObj.getString("keluhan"));
                    //item.setProfilePic(feedObj.getString("profilePic"));
                    item.setWaktu(feedObj.getString("waktu"));

                    // url might be null sometimes
                    String feedUrl = feedObj.isNull("url") ? null : feedObj
                            .getString("url");
                    //item.setUrl(feedUrl);

                    // Create HashMap to store row values
                    HashMap<String, String> recordData =
                            new HashMap<String, String>();

                    recordData.put("nama", feedObj.getString("nama"));
                    recordData.put("lat", feedObj.getString("latitude"));
                    recordData.put("long", feedObj.getString("longitude"));
                    recordData.put("waktu", feedObj.getString("waktu"));
                    recordData.put("gambar", feedObj.getString("gambar"));
                    recordData.put("keluhan", feedObj.getString("keluhan"));
                    list_data.add(recordData);


                    feedItems.add(item);
                }

                // notify data changes to list adapater
                listAdapter.notifyDataSetChanged();
            }
            // stopping swipe refresh
            swipeRefreshLayout.setRefreshing(false);
        } catch (JSONException e) {
            e.printStackTrace();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        freshReq();
    }


    @Override
    public void onLocationChanged(Location location) {
//        latitude = location.getLatitude();
//        longitude = location.getLongitude();
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lati, longi)).zoom(15).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    // @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    //    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    //    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
}
