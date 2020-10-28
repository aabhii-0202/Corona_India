package com.example.coronaindia;

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static String[] cities ={"Araria","Arwal","Aurangabad","Banka","Begusarai", "Bhagalpur","Bhojpur","Buxar","Darbhanga","East Champaran",
            "Gaya","Gopalganj", "Jamui", "Jehanabad","Kaimur","Katihar","Khagaria","Kishanganj","Lakhisarai", "Madhepura",
            "Madhubani","Munger", "Muzaffarpur","Nalanda","Nawada","Patna","Purnia","Rohtas","Saharsa","Samastipur","Saran",
            "Sheikhpura","Sheohar","Sitamarhi","Siwan","Supaul","Vaishali","West Champaran"};
    static String city="Katihar";
    static String state="Bihar";
    static String info,info2,info3;
    static String active,confirmed,deceased,recovered;
    public int time =1000*60*15;


    static String[] states ={"Andaman and Nicobar Islands","Andhra Pradesh","Arunachal Pradesh","Assam", "Bihar",
            "Chandigarh","Chhattisgarh","Delhi","Dadra and Nagar Haveli and Daman and Diu", "Goa","Gujarat","Haryana",
            "Himachal Pradesh","Jammu and Kashmir ", "Jharkhand", "Karnataka", "Kerala","Ladakh", "Lakshadweep",
            "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland","Odisha","Puducherry","Punjab",
            "Rajasthan", "Sikkim","Tamil Nadu","Telangana","Tripura", "Uttar Pradesh","Uttarakhand", "Dehradun","West Bengal"};

    static AutoCompleteTextView inputtext;
    static AutoCompleteTextView statesName;
    static TextView information;
    static SharedPreferences rawdata;


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
        rawdata= getApplicationContext().getSharedPreferences("com.example.coronaindia",MODE_PRIVATE);
        schedulejob();

    }

    public void schedulejob(){
        ComponentName componentName = new ComponentName(this,goodmorningupdatedata.class);
        JobInfo info = new JobInfo.Builder(123,componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(time).build();

        JobScheduler scheduler =(JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.schedule(info);
        int resultCode =scheduler.schedule(info);
        if (resultCode==JobScheduler.RESULT_SUCCESS){
            Log.i("ALERT","Job Scheduled");
        }else{
            Log.i("ALERT","Job Scheduling Failed");
        }
    }


    public static class goodmorningupdatedata extends JobService {

        public boolean jobCanceled = false;


        @Override
        public boolean onStartJob(JobParameters params) {
            Log.i("ALERT","Job Started");
            morningCall();
            return false;
        }

        @Override
        public boolean onStopJob(JobParameters params) {
            Log.i("ALERT","Job Cancelled before completition");
            jobCanceled = true;
            return true;
        }
    }
    public static void morningCall(){
        Log.i("ALERT","Downloading Started");
        DownloadTask task = new DownloadTask();
        task.execute("https://api.covid19india.org/state_district_wise.json");
    }
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

            storeData(s);
            Log.i("Information","The downloading process is done");

        }
    }
    public static void storeData(String morningdata){
        rawdata.edit().clear().apply();
        rawdata.edit().putString("morningData",morningdata).apply();
    }
    public void onButtonClick(View view){

        state = statesName.getText().toString();
        city = inputtext.getText().toString();

        try{
            getDetails();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void getDetails() throws JSONException {

        JSONObject jsonObject= new JSONObject(rawdata.getString("morningData",""));
        info = jsonObject.getString(state);
        JSONObject newJsonObj = new JSONObject(info);
        info2 = newJsonObj.getString("districtData");
        JSONObject cityoutput = new JSONObject(info2);
        info3 = cityoutput.getString(city);
        JSONObject finalout = new JSONObject(info3);
        active = finalout.getString("active");
        confirmed = finalout.getString("confirmed");
        deceased = finalout.getString("deceased");
        recovered = finalout.getString("recovered");
        information.setText(state+":"+city+"\nActive : "+active+"\nConfirmed : "+confirmed+"\nDeceased : "+deceased+"\nRecovered :"+recovered);

    }
}