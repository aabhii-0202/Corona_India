package com.example.coronaindia;

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;



public class MainActivity extends AppCompatActivity {

    static String[] states ={"Andaman and Nicobar Islands","Andhra Pradesh","Arunachal Pradesh","Assam", "Bihar",
            "Chandigarh","Chhattisgarh","Delhi","Dadra and Nagar Haveli and Daman and Diu", "Goa","Gujarat","Haryana",
            "Himachal Pradesh","Jammu and Kashmir ", "Jharkhand", "Karnataka", "Kerala","Ladakh", "Lakshadweep",
            "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland","Odisha","Puducherry","Punjab",
            "Rajasthan", "Sikkim","Tamil Nadu","Telangana","Tripura", "Uttar Pradesh","Uttarakhand", "Dehradun","West Bengal"};

    static String[] cities ={"Araria","Arwal","Aurangabad","Banka","Begusarai", "Bhagalpur","Bhojpur","Buxar","Darbhanga",
            "Gaya","Gopalganj", "Jamui", "Jehanabad","Kaimur","Katihar","Khagaria","Kishanganj","Lakhisarai", "Madhepura",
            "Madhubani","Munger", "Muzaffarpur","Nalanda","Nawada","Patna","Purnia","Rohtas","Saharsa","Samastipur","Saran",
            "Sheikhpura","Sheohar","Sitamarhi","Siwan","Supaul","Vaishali","West Champaran","Belagavi","Bengaluru Rural","Bengaluru Urban",
            "Central Delhi","East Delhi","New Delhi","North Delhi","North East Delhi","North West Delhi", "Shahdara",
            "South Delhi","South East Delhi","South West Delhi","West Delhi","North Goa","South Goa","Other State","East Champaran"};
    static String city;
    static String state;
    static String info,info2,info3;
    static String active,confirmed,deceased,recovered;
    public int timeinmin =60*5;

    static AutoCompleteTextView inputtext;
    static AutoCompleteTextView statesName;
    static TextView information;
    static SharedPreferences rawdata;
    static JobScheduler scheduler;


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

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);
        if (firstStart) {
            schedulejob();
        }


    }

    public void schedulejob(){
        ComponentName componentName = new ComponentName(this,goodmorningupdatedata.class);
        JobInfo info = new JobInfo.Builder(123,componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(timeinmin*1000*60).build();

        scheduler =(JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.schedule(info);
        int resultCode =scheduler.schedule(info);
        if (resultCode==JobScheduler.RESULT_SUCCESS){
            Log.i("ALERT","Job Scheduled");
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstStart", false);
            editor.apply();
        }else{
            Log.i("ALERT","Job Scheduling Failed");
        }
    }



    public static void morningCall(){
        Log.i("ALERT","Downloading Started");
        DownloadTask task = new DownloadTask();
        task.execute("https://api.covid19india.org/state_district_wise.json");
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
        }catch(JSONException a){
            information.setText("\n\nUpdating Data\nTry after 5 minutes");
        }
        catch (Exception e){
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