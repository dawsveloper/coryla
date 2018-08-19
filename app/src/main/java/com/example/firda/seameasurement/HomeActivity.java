package com.example.firda.seameasurement;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import me.tankery.lib.circularseekbar.CircularSeekBar;

import static java.lang.Math.min;


public class HomeActivity extends AppCompatActivity {

    CircularSeekBar fuzzyCSB;
    TextView fuzzyVal, phVal, saliVal;
    Button btnConnect, btnDisconnect;

    boolean doubleBackToExitPressedOnce;
    int click;
    float fuzzyRes;
    String[] finalRes;

    Handler mHandler;
    Runnable refresh;
    static String deviceAPI = "demo";
    static String variableAPI1 = "dht";
    static String variableAPI2 = "temperature";
    static String APITOKEN = "A1E-7itZt4MQIEu1EyqthCbaykJl6u7pxl";

    static final String URL_GARAM = "http://things.ubidots.com/api/v1.6/devices/" + deviceAPI + "/" + variableAPI1 + "/lv?token=A1E-7itZt4MQIEu1EyqthCbaykJl6u7pxl";
    static final String URL_PH = "http://things.ubidots.com/api/v1.6/devices/"+ deviceAPI + "/" + variableAPI2 + "/lv?token=A1E-7itZt4MQIEu1EyqthCbaykJl6u7pxl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setTitle("Coryla");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        fuzzyCSB = findViewById(R.id.fuzzyCSB);
        fuzzyVal = findViewById(R.id.fuzzyVal);
        phVal = findViewById(R.id.phVal);
        saliVal = findViewById(R.id.saliVal);
        btnConnect = findViewById(R.id.btnConnect);
        btnDisconnect = findViewById(R.id.btnDisconnect);

        fuzzyCSB.setProgress(0);
        mHandler = new Handler();
        click = 0;
        btnDisconnect.setVisibility(View.GONE);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click = 1;
                repeatRequest();
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click = 0;
                fuzzyCSB.setProgress(0);
                fuzzyVal.setText("0");
                phVal.setText("0");
                saliVal.setText("0");
                btnDisconnect.setVisibility(View.GONE);
                btnConnect.setVisibility(View.VISIBLE);
            }
        });
    }

    public Runnable refreshData = new Runnable() {
        @Override
        public void run() {
            if (click == 1){
                readData read = new readData();
                read.execute();
                mHandler.postDelayed(refreshData, 2000);
            }
            else if(click == 0){
                mHandler.removeCallbacks(this);
                stopRequest();
            }
        }
    };

    void repeatRequest(){
        refreshData.run();
    }

    void stopRequest(){
        mHandler.removeCallbacks(refreshData);
        mHandler.removeMessages(0);
        mHandler.removeCallbacksAndMessages(null);
    }

    public String requestData(String urlRequest){
        StringBuilder sb = new StringBuilder();

        try{
            URL url = new URL(urlRequest);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String s;
            while((s = bufferedReader.readLine()) != null){
                sb.append(s + "\n");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return sb.toString();
    }

    public class readData extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... voids) {
            String dataGaram = requestData(URL_GARAM);
            String dataPh = requestData(URL_PH);
            String res = dataGaram + "@" + dataPh;
            res = res.replace("\n", "").replace("\r","");
            Log.v("result", res);

            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            finalRes = s.split("@");

            if(click == 1){
//                Log.v("res", finalRes[0] + ":" + finalRes[1]);
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(1);

                saliVal.setText(String.valueOf(df.format(Float.parseFloat(finalRes[0]))));
                phVal.setText(String.valueOf(df.format(Float.parseFloat(finalRes[1]))));
                fuzzyRes = myFuzzy(Float.parseFloat(finalRes[0]),Float.parseFloat(finalRes[1]));

                Log.v("my fuzzy", String.valueOf(fuzzyRes));
                fuzzyVal.setText(String.valueOf(df.format(fuzzyRes)));
                fuzzyCSB.setProgress(fuzzyRes);

//            fuzzyCSB.setProgress();
                btnConnect.setVisibility(View.GONE);
                btnDisconnect.setVisibility(View.VISIBLE);
            }
            else if(click == 0){
                fuzzyCSB.setProgress(0);
                btnDisconnect.setVisibility(View.GONE);
                btnConnect.setVisibility(View.VISIBLE);
            }
        }
    }

    float myFuzzy(float garam, float ph){
        float res;
        float aGaram = 0, bGaram = 0, aPH = 0, bPH = 0, a1 = 0, a2 = 0, z1 = 0, z2 = 0;
        String strGaramA = "", strGaramB = "", strPHA = "", strPHB = "";

        if((garam >= 25) && (garam <= 31)){
            aGaram = (31 - garam)/(31-25);
            strGaramA = "rendah";
            bGaram = (garam - 25)/(31-25);
            strGaramB = "normal";
        }
        else if((garam >=31) && (garam <= 37)){
            aGaram = (37 - garam)/(37-31);
            strGaramA = "normal";
            bGaram = (garam - 31)/(37-31);
            strGaramB = "tinggi";
        }

        if((ph >= 5) && (ph <= 7)){
            aPH = (7 - ph)/(7 - 5);
            strPHA = "asam";
            bPH = (ph - 5)/(7 - 5);
            strPHB = "netral";
        }
        else if((ph >= 7) && (ph <= 9)){
            aPH = (9 - ph)/(9 - 7);
            strPHA = "netral";
            bPH = (ph - 7)/(9 - 7);
            strPHB = "basa";
        }


        a1 = min(aGaram, aPH);
        a2 = min(bGaram, bPH);

        z1 = ruleFuzzy(strGaramA, strPHA);
        z2 = ruleFuzzy(strGaramB, strPHB);

        Log.v("strA", strGaramA + ":" + strPHA);
        Log.v("strB", strGaramB + ":" + strPHB);

        res =((a1*z1) + (a2*z2))/(a1+a2);

        return res;
    }

    int ruleFuzzy(String garam, String ph){
        int res = 0;

        if((garam.equals("rendah")) && (ph.equals("asam"))){
            res = 25;
        }
        else if((garam.equals("rendah")) && (ph.equals("netral"))){
            res = 28;
        }
        else if((garam.equals("rendah")) && (ph.equals("basa"))){
            res = 28;
        }
        else if((garam.equals("normal")) && (ph.equals("asam"))){
            res = 28;
        }
        else if((garam.equals("normal")) && (ph.equals("netral"))){
            res = 34;
        }
        else if((garam.equals("normal")) && (ph.equals("basa"))){
            res = 34;
        }
        else if((garam.equals("tinggi")) && (ph.equals("asam"))){
            res = 28;
        }
        else if((garam.equals("tinggi")) && (ph.equals("netral"))){
            res = 28;
        }
        else if((garam.equals("tinggi")) && (ph.equals("basa"))){
            res = 25;
        }

        return res;
    }

    @Override
    public void onBackPressed() {
        if(doubleBackToExitPressedOnce){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            System.exit(0);
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
