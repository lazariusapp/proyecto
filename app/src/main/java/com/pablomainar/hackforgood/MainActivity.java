package com.pablomainar.hackforgood;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.pablomainar.hackforgood.Clases.buscar;


public class MainActivity extends Activity {


    public static Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Esto del activity es una cutrez que uso luego, basicamente guardo el contexto este para poder saltar en
        //la AsyncTask de actividad
        activity = this;
    }


    //Aqui entra cuando se pulsa el boton de Papelera
    public void pasarABuscarPapelera(View view) {
        //Esta es la URL que nos han dado los de telefonica
        String url_s = "http://hackforgood.fiware.org:1026/NGSI10/queryContext";
        //Con esto se empieza el AsyncTask
        new buscar().execute(url_s);
    }


}
