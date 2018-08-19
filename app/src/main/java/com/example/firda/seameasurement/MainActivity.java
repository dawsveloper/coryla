package com.example.firda.seameasurement;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    Button btnConnect;
    TextView valGaram, valPh;
    LineChart mChart;

    Handler mHandler;
    int xGaram, xTemp;
    Runnable refreshTimer;

    static final String URL_GARAM = "http://things.ubidots.com/api/v1.6/devices/demo/dht/lv?token=A1E-7itZt4MQIEu1EyqthCbaykJl6u7pxl";
    static final String URL_TEMPERATURE = "http://things.ubidots.com/api/v1.6/devices/demo/temperature/lv?token=A1E-7itZt4MQIEu1EyqthCbaykJl6u7pxl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = findViewById(R.id.btnConnect);
        valGaram = findViewById(R.id.valGaram);
        valPh = findViewById(R.id.valPh);
        mChart = findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);
        mChart.getDescription().setEnabled(true);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
//        mChart.setBackground(Color.LTGRAY);

        LineData dataGaram = new LineData();
        dataGaram.setValueTextColor(Color.BLACK);
        mChart.setData(dataGaram);

        LineData dataTemp = new LineData();
        dataTemp.setValueTextColor(Color.BLACK);
        mChart.setData(dataTemp);

        Legend l = mChart.getLegend();

        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
//


        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        mHandler = new Handler();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChart.clearValues();
                try{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true){
                                try {
                                    Thread.sleep(1);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            readData read = new readData();
                                            read.execute();
                                        }
                                    });
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public String sendGetRequest(String requestURL){
        StringBuilder sb = new StringBuilder();
        try{
            URL url = new URL(requestURL);
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

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    public class readData extends AsyncTask<Void, Void, String>{

        LineData dataGaram = mChart.getData();
        LineData dataTemp = mChart.getData();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... voids) {
            String dataGaram = sendGetRequest(URL_GARAM);
            String dataTemperature = sendGetRequest(URL_TEMPERATURE);
            String res = dataGaram + "@" + dataTemperature;

            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            final String[] res = s.split("@");

            valGaram.setText(res[0]);
            valPh.setText(res[1]);

//            seriesGaram = new LineGraphSeries<DataPoint>(new DataPoint[]{
//                    new DataPoint(xGaram, Float.valueOf(res[0]))
//            });
//
//            seriesTemp = new LineGraphSeries<>(new DataPoint[]{
//                    new DataPoint(xTemp, Float.valueOf(res[1]))
//            });

            if (dataGaram != null){
                ILineDataSet set = dataGaram.getDataSetByIndex(0);

                if(set == null){
                    set = createSet();
                    dataGaram.addDataSet(set);
                }

                dataGaram.addEntry(new Entry(set.getEntryCount(), Float.parseFloat(res[0])), 0);
                dataGaram.notifyDataChanged();

                // let the chart know it's data has changed
                mChart.notifyDataSetChanged();

                // limit the number of visible entries
                mChart.setVisibleXRangeMaximum(120);
                // mChart.setVisibleYRange(30, AxisDependency.LEFT);

                // move to the latest entry
                mChart.moveViewToX(dataGaram.getEntryCount());

            }
            if (dataTemp != null){
                ILineDataSet set = dataTemp.getDataSetByIndex(0);

                if(set == null){
                    set = createSet();
                    dataTemp.addDataSet(set);
                }

                dataTemp.addEntry(new Entry(set.getEntryCount(), Float.parseFloat(res[0])), 0);
                dataTemp.notifyDataChanged();

                // let the chart know it's data has changed
                mChart.notifyDataSetChanged();

                // limit the number of visible entries
                mChart.setVisibleXRangeMaximum(120);
                // mChart.setVisibleYRange(30, AxisDependency.LEFT);

                // move to the latest entry
                mChart.moveViewToX(dataTemp.getEntryCount());

            }
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }
}
