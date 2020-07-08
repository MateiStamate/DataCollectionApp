package com.lsr.sensordatacollection;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.lsr.sensordatacollection.sensorcharthandler.InertialSensorChartHandler;
import com.lsr.sensordatacollection.sensorcharthandler.WiFiChartHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;


public class SensorGraphsActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private Sensor mAccSensor;
    private Sensor mGyroSensor;
    private Sensor mMagSensor;

    private InertialSensorChartHandler accChartHandler;
    private InertialSensorChartHandler gyroChartHandler;
    private InertialSensorChartHandler magChartHandler;
    private WiFiChartHandler wifiHandler;

    private boolean recordData = false;

    private BufferedWriter recordingWriter = null;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_graphs);

        Context appContext = getApplicationContext();

        LinearLayout sensorsScrollView = findViewById(R.id.scrollContainer);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Intent intent = getIntent();
        SensorReadingsOptions options = intent.getParcelableExtra(SensorSettingsActivity.OPTIONS);

        // Create readings directory
        File readingsDirectory = new File(getExternalFilesDir(null) + "/localization");
        if (!readingsDirectory.exists()) {
            readingsDirectory.mkdir();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH-mm-ss");
        String date = sdf.format(System.currentTimeMillis());

        assert options != null;

        // Create the readings file and write its header
        String readingsLocation = options.getRecordingsLocation() + date + ".xml";
        String building = options.getBuildingTag();
        try {
            recordingWriter = new BufferedWriter(new FileWriter(readingsDirectory.getAbsolutePath() + "/" + readingsLocation));
            recordingWriter.write("<data phone=\"" +
                    Settings.Secure.getString(appContext.getContentResolver(), Settings.Secure.ANDROID_ID) +
                    "\" building=\"" + building + "\" >\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Check which sensors we record
        boolean recordAcc = options.isRecordAcc();
        boolean recordGyro = options.isRecordGyro();
        boolean recordMag = options.isRecordMag();
        boolean recordWifi = options.isRecordWifi();

        LayoutInflater vi = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LineChart accChart;
        LineChart gyroChart;
        LineChart magChart;

        // Accelerometer
        if (recordAcc) {
            mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

            View acc_graph_view = vi.inflate(R.layout.acc_graph_view, null);
            accChart = acc_graph_view.findViewById(R.id.AccSensorChart);
            sensorsScrollView.addView(acc_graph_view);

            float accMaxRange = mAccSensor.getMaximumRange();
            accChartHandler = new InertialSensorChartHandler(accChart, SensorType.ACC, accMaxRange, recordingWriter);
        }

        // Gyroscope
        if (recordGyro) {
            mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

            View gyro_graph_view = vi.inflate(R.layout.gyro_graph_view, null);
            gyroChart = gyro_graph_view.findViewById(R.id.GyroSensorChart);
            sensorsScrollView.addView(gyro_graph_view);

            float gyroMaxRange = mGyroSensor.getMaximumRange();
            gyroChartHandler = new InertialSensorChartHandler(gyroChart, SensorType.GYRO, gyroMaxRange, recordingWriter);
        }

        // Magnetometer
        if (recordMag) {
            View mag_graph_view = vi.inflate(R.layout.mag_graph_view, null);

            magChart = mag_graph_view.findViewById(R.id.MagSensorChart);
            mMagSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorsScrollView.addView(mag_graph_view);

            float magMaxRange = mMagSensor.getMaximumRange();
            magChartHandler = new InertialSensorChartHandler(magChart, SensorType.MAG, magMaxRange, recordingWriter);
        }

        // Wi-Fi
        if (recordWifi) {

            TableLayout wifiScanTable = (TableLayout) vi.inflate(R.layout.wifi_table_view, null);

            // Add table to scrollview
            sensorsScrollView.addView(wifiScanTable);

            // Create wifi handler
            wifiHandler = new WiFiChartHandler(appContext, wifiScanTable, recordingWriter);
        }

        final Button startRecordingBtn = findViewById(R.id.record_button);
        startRecordingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!recordData) {

                    startRecordingBtn.setText("End");

                    if (mAccSensor != null) {
                        mSensorManager.registerListener(accChartHandler, mAccSensor, SensorManager.SENSOR_DELAY_UI);
                    }

                    if (mGyroSensor != null) {
                        mSensorManager.registerListener(gyroChartHandler, mGyroSensor, SensorManager.SENSOR_DELAY_UI);
                    }

                    if (mMagSensor != null) {
                        mSensorManager.registerListener(magChartHandler, mMagSensor, SensorManager.SENSOR_DELAY_UI);
                    }

                    if (wifiHandler != null) {
                        wifiHandler.registerScanListener();
                    }

                    recordData = true;
                }
                else {

                    if (mAccSensor != null) {
                        mSensorManager.unregisterListener(accChartHandler);
                    }

                    if (mGyroSensor != null) {
                        mSensorManager.unregisterListener(gyroChartHandler);
                    }

                    if (mMagSensor != null) {
                        mSensorManager.unregisterListener(magChartHandler);
                    }

                    if (wifiHandler != null) {
                        wifiHandler.unregisterScanListener();
                    }

                    recordData = false;

                    startRecordingBtn.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {

        if (recordingWriter != null) {

            try {
                recordingWriter.write("</data>");
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    recordingWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            recordingWriter = null;
        }

        super.onDestroy();
    }
}