package com.example.coronaindia;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    static String[] cities ={"Katihar","Purnea","Bhagalpur","Araria"};
    static String city="Katihar";
    static String state="Bihar";
    static String info,info2,info3;
    static String active,confirmed,deceased,recovered;


    static String[] states ={"Andaman and Nicobar Islands","Andhra Pradesh","Arunachal Pradesh","Assam", "Bihar",
            "Chandigarh","Chhattisgarh","Delhi","Dadra and Nagar Haveli and Daman and Diu", "Goa","Gujarat","Haryana", "Himachal Pradesh","Jammu and Kashmir ",
            "Jharkhand", "Karnataka", "Kerala","Ladakh", "Lakshadweep", "Madhya Pradesh", "Maharashtra",
            "Manipur", "Meghalaya", "Mizoram", "Nagaland","Odisha","Puducherry","Punjab","Rajasthan",
            "Sikkim","Tamil Nadu","Telangana","Tripura", "Uttar Pradesh","Uttarakhand", "Dehradun","West Bengal"};

    static AutoCompleteTextView inputtext;
    static AutoCompleteTextView statesName;
    static TextView information;
    static Button  searchButton;

    public static class DownloadTask extends AsyncTask<String ,Void,String>{

        @Override
        protected String doInBackground(String... urls) {
            String result="";
            URL url;
            HttpURLConnection connection;

            try{
                url = new URL(urls[0]);
                connection =(HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data!=-1){
                    char current= (char) data;
                    result+=current;
                    data= reader.read();
                }
                return  result;

            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            searchButton.setEnabled(true);

            try{
                JSONObject jsonObject= new JSONObject(s);
                 info = jsonObject.getString(state);
//                Log.i(statesName.getText().toString(),info);
//                information.setText(info);

                JSONObject newJsonObj = new JSONObject(info);
                info2 = newJsonObj.getString("districtData");
//                information.setText(info2);

                JSONObject cityoutput = new JSONObject(info2);
                info3 = cityoutput.getString(city);
//                information.setText(info3);

                JSONObject finalout = new JSONObject(info3);
                active = finalout.getString("active");
                confirmed = finalout.getString("confirmed");
                deceased = finalout.getString("deceased");
                recovered = finalout.getString("recovered");


                information.setText(state+":"+city+"\nActive : "+active+"\nConfirmed : "+confirmed+"\nDeceased : "+deceased+"\nRecovered :"+recovered);







            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void onButtonClick(View view){

        state = statesName.getText().toString();
        city = inputtext.getText().toString();
        DownloadTask task = new DownloadTask();
        task.execute("https://api.covid19india.org/state_district_wise.json");
        searchButton.setEnabled(false);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statesName = (AutoCompleteTextView)findViewById(R.id.stateName);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,states);
        statesName.setAdapter(adapter2);
        statesName.setThreshold(1);
        inputtext =(AutoCompleteTextView)findViewById(R.id.autoCompleteTextView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,cities);
        inputtext.setAdapter(adapter);
        inputtext.setThreshold(1);
        information = (TextView)findViewById(R.id.information);
        searchButton= (Button)findViewById(R.id.button);




    }
}