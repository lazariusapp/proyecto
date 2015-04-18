package com.pablomainar.hackforgood.Clases;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.pablomainar.hackforgood.MainActivity;
import com.pablomainar.hackforgood.Mapa;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class buscar extends AsyncTask<String, Void, String> {

    String id;
    String valor;

    protected String doInBackground(String ...url_s) {
        try {
            //Lo primero que hace es lo más complicado pero para que sea más legible el código, he hecho
            //un método que se llama POST al que se le pasa el url al que hacer la consulta y devuelve
            //el String devuelto por el servidor. Este String tiene formato JSON, así que hago el objeto
            //JSONObject.
            JSONObject json = new JSONObject(POST(url_s[0]));
            //Una vez con el JSONObject, empiezo a quitar "mierda", o sea, saco el JSONArray de dentro,
            //luego cada JSONObject de dentro del array y de cada uno saco el id de la basura y las
            //coordenadas. Eso de contextResponses y contextElement es sólo parte del formato con el que
            //me mandan el JSON. Las ids y las cordenadas se guardan en dos vectores (se guardan todas las
            //que se reciben, por si se recibe más de una)
            JSONArray json_a = json.getJSONArray("contextResponses");
            String ids[] = new String[json_a.length()];
            String valores[] = new String[json_a.length()];
            for (int i=0; i<json_a.length();i++) {
                json = json_a.getJSONObject(i);
                json = json.getJSONObject("contextElement");
                id = json.getString("id");
                JSONArray json_ar = json.getJSONArray("attributes");
                json = json_ar.getJSONObject(0);
                valor = json.getString("value");
                ids[i] = id;
                valores[i] = valor;
            }
            //Ahora en ids y valores tenemos todos los ids y localizaciones de las papeleras que superan el filtro.
            //Ahora separo las latitudes de las longitudes y las guardo en dos vectores diferentes.
            double lat[] = new double[json_a.length()];
            double lon[] = new double[json_a.length()];
            String valoress[] = new String[2];
            for (int i=0;i<ids.length;i++) {
                valoress = valores[i].split(",");
                lat[i] = Double.parseDouble(valoress[0]);
                lon[i] = Double.parseDouble(valoress[1]);
            }
            //Ultra cutre esto de aqui abajo pero en fin, eran las 4 de la mañana, es para poder cambiar de actividad desde aquí.
            //A la nueva actividad le paso los vectores con las ids, las latitudes y las longitudes.
            Intent i = new Intent(MainActivity.activity, Mapa.class);
            i.putExtra("lats",lat);
            i.putExtra("lons",lon);
            i.putExtra("ids",ids);
            MainActivity.activity.startActivity(i);

        }catch(Exception e) { }
        return "ei";
    }

    @Override
    protected void onPostExecute(String result) {   }


    public static String POST(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            //Creo el HttpClient y el HttpPost con la url que se le ha pasado
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            //Escribo el body del mensaje http para filtrar las papeleras y sólo me devuelvan
            //las que estén a menos de un radio de 500 metros de la latiutd y longitud que he puesto.
            //Esto habrá que cambiarlo para que coja la latitud y longitud de la posición actual del gps
            //y quizás cambiar dinámicamente el radio también pero por ahora se queda así
            String json = "{\n" +
                    "  \"entities\": [\n" +
                    "{\n" +
                    "\"type\": \"bin\", \"isPattern\": \"true\", \"id\": \".*\"\n" +
                    "}\n" +
                    "], \"restriction\": {\n" +
                    "\"scopes\": [\n" +
                    "{\n" +
                    "\"type\" : \"FIWARE_Location\", \"value\" : {\n" +
                    "\"circle\": {\n" +
                    "\"centerLatitude\": \"39.479710\", \"centerLongitude\": \"-0.342995\", \"radius\": \"500\"\n" +
                    "\n" +
                    "} }\n" +
                    "}\n" +
                    "] } }";


            //Creo el StringEntity, que es algo así como el body del mensaje, y lo seteo como Entidad del
            //HttpPost
            StringEntity se = new StringEntity(json);
            httpPost.setEntity(se);

            //Pongo algunas cabeceras que son necesarias para que el Fiware me responda con lo que quiero
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Fiware-Service", "smartbins");

            //Ejecuto el post
            HttpResponse httpResponse = httpclient.execute(httpPost);

            //Recibo la respuesta del servidor
            inputStream = httpResponse.getEntity().getContent();

            //Convierto el inputStream en String para obtener el resultado
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("POST", e.getLocalizedMessage());
        }
        //Finalmente, devuelvo el String con el resultado de la consulta
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        inputStream.close();
        return result;
    }

}
