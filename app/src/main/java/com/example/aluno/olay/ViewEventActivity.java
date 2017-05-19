package com.example.aluno.olay;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ViewEventActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String jsonResult;
    int lat=0, lon=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    class JsonReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(params[0]);
            try {
                HttpResponse response = httpclient.execute(httppost);
                //recebe a lista de produtos como json
                jsonResult = inputStreamToString(
                        response.getEntity().getContent()).toString();
            }

            catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private StringBuilder inputStreamToString(InputStream is) {
            String rLine = "";
            StringBuilder answer = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            try {
                while ((rLine = rd.readLine()) != null) {
                    answer.append(rLine);
                }
            }

            catch (IOException e) {
                mostrarToast("Error..." + e.toString());
            }
            return answer;
        }

        @Override
        protected void onPostExecute(String result) {
            try{
                ListDrawer();
            }catch(Exception e){
                mostrarToast("Não foi possivel conectar ao banco");
            }
        }
    }

    public void accessWebService() {
        JsonReadTask task = new JsonReadTask();
        task.execute(new String[] { "http://192.168.0.104/android/pegaproduto.php" });
    }

    public void ListDrawer() {

        try {
            JSONObject jsonResponse = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("evento");
            //cria 3 vetores, para id,nome e preço
            final String[] idEvento=new String[jsonMainNode.length()];
            final String[] localizacaoEvento = new String[jsonMainNode.length()];
            final String[] linkImageEvento = new String[jsonMainNode.length()];
            final String[] dataEvento = new String[jsonMainNode.length()];
            final String[] nomeEvento = new String[jsonMainNode.length()];
            final String[] descricaoEvento = new String[jsonMainNode.length()];
            final String[] horaEvento = new String[jsonMainNode.length()];

            for (int i = 0; i < jsonMainNode.length(); i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                //cada vetor recebe seu valor respectivo
                idEvento [i]=jsonChildNode.getString("idEvento");
                localizacaoEvento [i]=jsonChildNode.getString("localizacaoEvento");
                linkImageEvento [i]=jsonChildNode.getString("linkImageEvento");
                dataEvento [i]=jsonChildNode.getString("dataEvento");
                nomeEvento [i]=jsonChildNode.getString("nomeEvento");
                descricaoEvento [i]=jsonChildNode.getString("descricaoEvento");
                horaEvento [i]=jsonChildNode.getString("horaEvento");
                //irá
                //posicao[i]=items[i]+","+items3[i];
            }

            List<String> list = new ArrayList<String>();

			for(int i=0;i<localizacaoEvento.length;i++)
        	{
			    //no spinner irá somente nomes
                String[] vetLocal= new String [2];
                vetLocal=localizacaoEvento[i].split(";");
                //0 lat 1 long
                lat=Integer.parseInt(vetLocal[0]);
                lon=Integer.parseInt(vetLocal[1]);
                onMapReady(mMap);
        	}

			/*transforma em string o nome do produdo
            String item=spnproduto.getSelectedItem().toString();
            //pega o preço/id equivalente ao produto selecionado, baseado na posição e seta na text "preço"

            String[] idVar=posicao[spnproduto.getSelectedItemPosition()].split(",");
            Preco.setText("R$ "+precoVar[1]);
            idProduto=idVar[0];*/


        } catch (JSONException e) {
            mostrarToast("Error" + e.toString());
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng local = new LatLng(lat, lon);
        googleMap.addMarker(new MarkerOptions().position(local).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    void mostrarToast(String texto){
        Toast.makeText(getApplicationContext(), texto, Toast.LENGTH_SHORT).show();
    }
}
