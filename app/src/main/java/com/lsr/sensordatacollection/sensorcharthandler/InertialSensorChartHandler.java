package com.lsr.sensordatacollection.sensorcharthandler;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.lsr.sensordatacollection.SensorType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

/***
 * Class which handles data from the phone's inertial sensors
 * (accelerometer, magnetometer, gyroscope)
 *
 * This handling involves updating the "sensorDataChart" so that
 * the user can see a plot of the data in real-time, and buffering
 * the data in a pre-determined format that fits in an .xml file.
 */
public class InertialSensorChartHandler extends SensorChartHandler implements SensorEventListener {

    private LineChart sensorDataChart;
    private SensorType sensorType;

    private float sensorMaxRange;
    private String xSetLabel = "x";
    private String ySetLabel = "y";
    private String zSetLabel = "z";

    public InertialSensorChartHandler(LineChart chartToUpdate, SensorType sensorType,
                                      float maxRange, BufferedWriter logWriter) {

        super(logWriter);

        sensorDataChart = chartToUpdate;
        this.sensorType = sensorType;
        this.sensorMaxRange = maxRange;

        // Create data line for this graph
        LineData lineData = new LineData();
        lineData.setValueTextColor(Color.WHITE);
        sensorDataChart.setData(lineData);

        setOptionsForChart();

        // Create data sets to be plotted for the x,y,z axis
        createAndAddDataSets();
    }

    private void setOptionsForChart() {

        // Define Y axis graphical properties
        YAxis leftAxis = sensorDataChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);
        leftAxis.setEnabled(true);

        sensorDataChart.getDescription().setEnabled(true);
        String description = "";
        switch (this.sensorType) {
            case ACC:
                description = "Accelerometer - m/s^2";
                leftAxis.setAxisMaximum(20f);
                leftAxis.setAxisMinimum(-20f);
                break;

            case GYRO:
                description = "Gyroscope - rad/s";
                leftAxis.setAxisMaximum(8f);
                leftAxis.setAxisMinimum(-8f);
                break;

            case MAG:
                description = "Magnetometer - uT";
                leftAxis.setAxisMaximum(20f);
                leftAxis.setAxisMinimum(-20f);
        }

        // Define chart options
        sensorDataChart.getDescription().setText(description);

        sensorDataChart.setTouchEnabled(false);
        sensorDataChart.setDragEnabled(false);
        sensorDataChart.setScaleEnabled(false);
        sensorDataChart.setPinchZoom(false);
        sensorDataChart.setBackgroundColor(Color.WHITE);

        // Define X Axis properties
        XAxis xl = sensorDataChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        // Disable right Y axis graphic
        YAxis rightAxis = sensorDataChart.getAxisRight();
        rightAxis.setEnabled(false);

        sensorDataChart.getAxisLeft().setDrawGridLines(false);
        sensorDataChart.getXAxis().setDrawGridLines(false);
        sensorDataChart.setDrawBorders(false);


    }

    private void setOptionsForDataSet(LineDataSet set, int displayColor) {

        set.setColor(displayColor);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setLineWidth(1.5f);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
    }

    /**
     * Function that sets all display options and information
     * for 3 data sets representing the x,y,z axis
     */
    private void createAndAddDataSets() {

        LineDataSet setX = new LineDataSet(null, xSetLabel);
        LineDataSet setY = new LineDataSet(null, ySetLabel);
        LineDataSet setZ = new LineDataSet(null, zSetLabel);

        setOptionsForDataSet(setX, Color.BLUE);
        setOptionsForDataSet(setY, Color.GREEN);
        setOptionsForDataSet(setZ, Color.RED);

        // Add all three data sets to the LineData object
        // so that they can be displayed on the graphs
        LineData lineData = sensorDataChart.getLineData();
        if (lineData != null) {
            lineData.addDataSet(setX);
            lineData.addDataSet(setY);
            lineData.addDataSet(setZ);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        LineData sensorData = sensorDataChart.getLineData();
        if (sensorData != null) {

            ILineDataSet xSet = sensorData.getDataSetByLabel(xSetLabel, false);
            ILineDataSet ySet = sensorData.getDataSetByLabel(ySetLabel, false);
            ILineDataSet zSet = sensorData.getDataSetByLabel(zSetLabel, false);

            sensorData.addEntry(new Entry(xSet.getEntryCount(), sensorEvent.values[0]), sensorData.getIndexOfDataSet(xSet));
            sensorData.addEntry(new Entry(ySet.getEntryCount(), sensorEvent.values[1]), sensorData.getIndexOfDataSet(ySet));
            sensorData.addEntry(new Entry(zSet.getEntryCount(), sensorEvent.values[2]), sensorData.getIndexOfDataSet(zSet));
            sensorData.notifyDataChanged();

            sensorDataChart.notifyDataSetChanged();
            sensorDataChart.setVisibleXRangeMaximum(150);
            sensorDataChart.moveViewToX(sensorData.getEntryCount());

            try {
                sensorDataToXMLEntry(sensorEvent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @Override
    public void sensorDataToXMLEntry(SensorEvent event) throws IOException {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:MM");
        String timestamp = sdf.format(System.currentTimeMillis());

        switch (this.sensorType) {
            case ACC:
                getBuffWrite().write("<a x=\"" + String.valueOf(event.values[0]) +
                        "\" y=\"" + String.valueOf(event.values[1]) + "\" z=\"" + String.valueOf(event.values[2]) +
                        "\" st=\"" + timestamp + "\" />\n");
                break;

            case GYRO:
                getBuffWrite().write("<g x=\"" + String.valueOf(event.values[0]) +
                        "\" y=\"" + String.valueOf(event.values[1]) + "\" z=\"" + String.valueOf(event.values[2]) +
                        "\" st=\"" + timestamp + "\" />\n");
                break;

            case MAG:
                getBuffWrite().write("<m x=\"" + String.valueOf(event.values[0]) +
                        "\" y=\"" + String.valueOf(event.values[1]) + "\" z=\"" + String.valueOf(event.values[2]) +
                        "\" st=\"" + timestamp + "\" />\n");
                break;
        }
    }
}
