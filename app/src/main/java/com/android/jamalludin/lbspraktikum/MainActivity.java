package com.android.jamalludin.lbspraktikum;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements LocationListener {

    private GoogleMap googleMap;
    private LatLng kampussatu = new LatLng(-7.7989312,110.3831042);
    private LatLng kampusdua = new LatLng(-7.8202812, 110.3879458);
    private LatLng kampustiga = new LatLng(-7.8082958, 110.3893301);
    private Document documentHasil;
    private HttpParser httpParser = new HttpParser();
    private ArrayList<LatLng> point=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int state = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if (state != ConnectionResult.SUCCESS) {
            Log.e("Error", "google play service out available");
        } else {
            if (googleMap == null) {
                googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
                if (googleMap != null) {

                    googleMap.addMarker(new MarkerOptions().position(kampussatu).title("Kampus Satu").snippet("Jalan Kapas"));
                    googleMap.addMarker(new MarkerOptions().position(kampusdua).title("Kampus Dua").snippet("Jalan Pramuka"));
                    googleMap.addMarker(new MarkerOptions().position(kampustiga).title("Kampus Tiga").snippet("Umbulharjo"));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(10),2000, null);

                    new AmbilData(kampustiga, kampusdua).execute();
                    new AmbilData(kampussatu, kampusdua).execute();

                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }

                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            LatLng tujuan = marker.getPosition();
                            new AmbilData(kampustiga, kampussatu).execute();

                            return false;
                        }
                    });

                    googleMap.setMyLocationEnabled(true);
                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Criteria criteria = new Criteria();
                    String provider = locationManager.getBestProvider(criteria, true);
                    Location location = locationManager.getLastKnownLocation(provider);
                    if (location != null){

                        double lati = location.getLatitude();
                        double longi = location.getLongitude();
                        LatLng coordinate = new LatLng(lati, longi);
                        googleMap.addMarker(new MarkerOptions().position(coordinate).title("Posisi Saya").snippet("Lelouch"));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 10));
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10),20000, null);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 16));
                        Log.e("Lokasi : ", String.valueOf(coordinate));
                        Toast.makeText(this, "longitude : " + longi + ", Latitude : " + lati, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getBaseContext(),"Lokasi Unknown", Toast.LENGTH_SHORT).show();
                    }
                    locationManager.requestLocationUpdates(provider,20000,0,this);


                }
            }
        }
    }



    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {



    }

    @Override
    public void onProviderEnabled(String provider) {
            Toast.makeText(this, "layanan Ada", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "tidak melayani Anda", Toast.LENGTH_SHORT).show();

    }

    class AmbilData extends AsyncTask<Void,Void,String> {
        LatLng mulai, selesai;

        AmbilData(LatLng mulai, LatLng selesai) {
            this.selesai = selesai;
            this.mulai = mulai;
        }

        @Override
        protected String doInBackground(Void... params) {

            String hasil = null;
            try {
                documentHasil = httpParser.dapatkanDocument(mulai, selesai);
                hasil = httpParser.getDuration(documentHasil);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return hasil;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            point = httpParser.getDirection(documentHasil);

            PolylineOptions points = new PolylineOptions().color(Color.BLACK).width(10);
            for(int i=0;i<point.size();i++){
                points.add(point.get(i));
            }
            polyline = googleMap.addPolyline(points);


            Toast.makeText(getApplicationContext(), "durasi "+s, Toast.LENGTH_SHORT).show();
        }
        }
    private Polyline polyline;
    public void hapusPolyline(ArrayList<LatLng> p){
        p.clear();;
        polyline.remove();
    }
}
