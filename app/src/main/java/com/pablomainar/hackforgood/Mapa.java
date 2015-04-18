package com.pablomainar.hackforgood;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;



public class Mapa extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    double lat[], lon[];
    String id[];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        //Cojo los datos del intent que he pasado antes. Son tres arrays de ids, latitudes y longitudes
        //de papeleras
        Intent i = getIntent();
        lat = i.getDoubleArrayExtra("lats");
        lon = i.getDoubleArrayExtra("lons");
        id = i.getStringArrayExtra("ids");
        //Inicializo el GoogleApiClient y me conecto
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //Aquí se guarda la última localización del gps para poder iniciar la navegación
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        //Este array de floats se hace porque para coger la distancia entre dos puntos, android proporciona
        //un método que se llama distanceBetween de la clase Location, y lo hay que pasarle el array y guarda
        //el resultado en este array.
        float dist[] = new float[1];
        float distancias[] = new float[lat.length];
        if (mCurrentLocation != null) {
            LatLng myLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            for (int i=0; i<lat.length;i++) {
                Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, lat[i],lon[i],dist);
                distancias[i] = dist[0];
            }
            //Aquí se haya qué papelera está a distancia mínima
            float minValue = distancias[0];
            int minId = 0;
            for(int i=1;i<distancias.length;i++){
                if(distancias[i] < minValue){
                    minValue = distancias[i];
                    minId = i;
                }
            }
            //Se guarda la latitud y longitud de la papelera definitiva
            double latitud = lat[minId];
            double longitud = lon[minId];

            //Esto es para que salte a la navegación de google.
            //El parámetro q son las coordenadas de donde hay que ir (la papelera más cercana)
            //y el parámetro mode es para decir que de indicaciones para ir andando (w).
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitud+","+longitud+"&mode=w");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);

        }
    }


    @Override
    public void onConnectionSuspended(int cause) {    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mapa, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
