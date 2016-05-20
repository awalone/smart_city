package com.awalone.smartcity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;


public class Laporan_Fragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    Button Camera, Kirim;
    ImageView HasilJepret;
    TextView Waktu, Lokasi;
    EditText ETKomentar;

    double latitude, longitude;
    String alamat = "Nama alamat tidak ditemukan";
    String komentar;
    String waktulapor;

//Location

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    String encodedString;
    String fileName;
    List<Address> addresses;

    private String KEY_KTP = "ktp";
    private String KEY_IMAGE = "image";
    private String KEY_LATITUDE = "latitude";
    private String KEY_LONGITUDE = "longitude";
    private String KEY_WAKTU = "waktu";
    private String KEY_KELUHAN = "keluhan";
    private String KEY_NAMAIMG = "namagambar";


    private static final int REQ_CAMERA = 0x01;
    private static final int REQ_CROP = 0x02;

    private String mTempPhotoPath;
    private String mCurrentPhotoPath;

    // flag for network status
    boolean isNetworkEnabled = false;
    boolean isGPSEnabled = false;

    MaterialDialog mMaterialDialog;
    View rootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.lapor_layout, container, false);
        Camera = (Button) rootView.findViewById(R.id.buttonkamera);
        HasilJepret = (ImageView) rootView.findViewById(R.id.imageView);
        Waktu = (TextView) rootView.findViewById(R.id.laporan_waktu);
        Lokasi = (TextView) rootView.findViewById(R.id.laporan_lokasi);
        ETKomentar = (EditText) rootView.findViewById(R.id.laporan_keluhan);
        Kirim = (Button) rootView.findViewById(R.id.button_kirim);

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }

        //Cek Gps dan internet
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

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
                            getActivity().finish();
                            ;
                        }
                    });
            mMaterialDialog.show();
        } else {

            displayLocation();


            //Ganteng dikit cekrek
            Camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Log.d("cek", "1");
                    if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        Log.d("cek", "2");
                        File photoFile = createTempFile();
                        if (photoFile != null) {
                            Log.d("cek", "3");
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile));
                            startActivityForResult(takePictureIntent, REQ_CAMERA);
                        }
                    }
                }
            });

            Kirim.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //cek harus terisi
                    kirim();
                }
            });
        }
        return rootView;
    }

    private File createTempFile() {
        File dirTemp = new File(Environment.getExternalStorageDirectory(), ".temp");
        if (!dirTemp.exists()) dirTemp.mkdirs();
        File temp = new File(dirTemp, "cropFile");
        try {
            (new File(dirTemp, ".nomedia")).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mTempPhotoPath = temp.getAbsolutePath();
        return temp;
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "image_" + timeStamp + ".jpg";
        File storageDir = new File(Environment.getExternalStorageDirectory(), "smartcity");
        if (!storageDir.exists()) storageDir.mkdirs();
        File image = new File(storageDir, imageFileName);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
//        if (data != null) return;

        if (requestCode == REQ_CAMERA) {
            final File rotateFIle = fixRotate();
            Log.d("cek", String.valueOf(rotateFIle));
            if (rotateFIle != null)
                startCrop(rotateFIle.getAbsolutePath());
            Log.d("cek1", String.valueOf(rotateFIle.getAbsolutePath()));
        } else if (requestCode == REQ_CROP) {
            galleryAddPic(getActivity(), mCurrentPhotoPath);
            setPic(HasilJepret, mCurrentPhotoPath);
        }
    }

    private File fixRotate() {

        Bitmap bitmap = BitmapFactory.decodeFile(mTempPhotoPath);
        try {
            ExifInterface exif = new ExifInterface(mTempPhotoPath);
            final int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                default:
                case ExifInterface.ORIENTATION_NORMAL:
                    bitmap = rotateBitmap(bitmap, 0f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateBitmap(bitmap, 90f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateBitmap(bitmap, 180f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateBitmap(bitmap, 270f);
                    break;
                //...
            }
            File fileRotate = new File(mTempPhotoPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(fileRotate));
            bitmap.recycle();
            return fileRotate;

        } catch (IOException e) {
            return null;
        }
    }

    Bitmap rotateBitmap(Bitmap source, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void startCrop(final String pathImage) {
        // bisa dibuang asal outputX, outputY tentuin manual
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 5;
        BitmapFactory.decodeFile(pathImage, options);


        final int outputX = options.outWidth;//
        final int outputY = options.outHeight;//

        final Uri saveCrop = Uri.fromFile(createImageFile());
        CropImageIntentBuilder cropImage = new CropImageIntentBuilder(
                outputX, outputY, saveCrop)
                .setScale(true)
                .setScaleUpIfNeeded(true)
                .setOutputFormat("JPEG")
                .setDoFaceDetection(false)
                .setSourceImage(Uri.fromFile(new File(pathImage)));

        startActivityForResult(cropImage.getIntent(getActivity()), REQ_CROP);
    }

    private void setPic(final ImageView imageView, final String pathImage) {

        Bitmap bitmap = BitmapFactory.decodeFile(pathImage);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        float scaleHt = (float) width / bitmap.getWidth();
        Log.e("Scaled percent ", " " + scaleHt);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, (int) (bitmap.getWidth() * scaleHt), true);


        imageView.setImageBitmap(scaled);

        //Waktu Lapor
        waktulapor = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Waktu.setText("Tanggal/Waktu : " + waktulapor);

        //Location
//            String lokasi = String.valueOf(latitude) + " " + String.valueOf(longitude);


        //Convert Photo
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        encodedString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        String fileNameSegments[] = mCurrentPhotoPath.split("/");
        fileName = fileNameSegments[fileNameSegments.length - 1];
        if (latitude != 0 && longitude != 0) {
            Geocoder gcd = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
            try {
                addresses = gcd.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                // TODO Auto-generated catch block

                addresses = null;
                e.printStackTrace();
            }

            if (addresses != null) {
                alamat = addresses.get(0).getThoroughfare();
            } else {
                alamat = "Alamat tidak ditemukan";
            }
        } else {
            alamat = "Lokasi tidak ditemukan";
        }
        Lokasi.setText("Lokasi : " + alamat);
    }

    private void galleryAddPic(final Context context, final String pathFile) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(pathFile);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }

    public void kirim() {
        komentar = ETKomentar.getText().toString();


        if (((mCurrentPhotoPath == null) || (mCurrentPhotoPath.equals(""))) || (komentar.equals(""))) {
            Snackbar snackbar = Snackbar
                    .make(rootView, "Foto,Keluhan  tidak boleh kosong", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        else {

            uploadImage();
        }
    }


    private void uploadImage() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(getActivity(), "Mengirim data ke server", "Harap Tunggu...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.FILE_UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        Snackbar snackbar = Snackbar
                                .make(getView(), "Berhasil Upload", Snackbar.LENGTH_LONG);
                        snackbar.show();

                        Reset();

                        Log.d("cekidot", s);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Snackbar snackbar = Snackbar
                                .make(getView(), "Error", Snackbar.LENGTH_LONG);

                        snackbar.show();
                        Reset();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = encodedString;


                SharedPreferences prefProfil = getActivity().getSharedPreferences("Profil",
                        Context.MODE_PRIVATE);
                //menyiapkan value untuk di kirim ke server
                String ktp = prefProfil.getString("ktp", null);
                String lat = String.valueOf(latitude);
                String lon = String.valueOf(longitude);
                String wak = waktulapor;
                String kel = komentar;
                String imgname = fileName;

                //Creating parameters
                Map<String, String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put(KEY_IMAGE, image);
                params.put(KEY_KTP, ktp);
                params.put(KEY_LATITUDE, lat);
                params.put(KEY_LONGITUDE, lon);
//                params.put(KEY_ALAMAT, almt);
                params.put(KEY_WAKTU, wak);
                params.put(KEY_KELUHAN, kel);
                params.put(KEY_NAMAIMG, imgname);
                //returning parameters
                Log.d("cek1", image);
                Log.d("cek2", ktp);
                Log.d("cek3", lat);
                Log.d("cek4", lon);
                Log.d("cek5", wak);
                Log.d("cek6", kel);
                Log.d("cek7", fileName);


                return params;
            }
        };


        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        //Try to fix Double data
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);


        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void Reset() {
        Waktu.setText("Tanggal/Waktu : ");
        Lokasi.setText("Lokasi : ");
        ETKomentar.setText("");
        // ETAlamat.setText("");
        HasilJepret.setImageDrawable(null);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        stopLocationUpdates();
        if (mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Method to toggle periodic location updates
     */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            // Changing the button text
//            btnStartLocationUpdates
//                    .setText(getString(R.string.btn_stop_location_updates));

            mRequestingLocationUpdates = true;

            // Starting the location updates
            startLocationUpdates();

            Log.d("TAG", "Periodic location updates started!");

        } else {
            // Changing the button text
//            btnStartLocationUpdates
//                    .setText(getString(R.string.btn_start_location_updates));

            mRequestingLocationUpdates = false;

            // Stopping the location updates
            stopLocationUpdates();

            Log.d("TAG", "Periodic location updates stopped!");
        }
    }

    /**
     * Creating google api client object
     */
    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Snackbar snackbar = Snackbar
                        .make(getView(), "Google Play Service tidak tersedia", Snackbar.LENGTH_LONG);
                snackbar.show();
                getActivity().finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     */
    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, (LocationListener) getActivity());

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("TAG", "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

//        Toast.makeText(getApplicationContext(), "Location changed!",
//                Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        displayLocation();
    }

    public void displayLocation() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            Log.d("cek1", String.valueOf(latitude));
            Log.d("cek2", String.valueOf(longitude));
//            lblLocation.setText(latitude + ", " + longitude);

        } else {
//            latitude = mLastLocation.getLatitude();
//            longitude = mLastLocation.getLongitude();
//            lblLocation
//                    .setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }
}
