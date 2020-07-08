package com.lsr.sensordatacollection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class SensorSettingsActivity extends AppCompatActivity {

    public static final String OPTIONS = "SENSOR_OPTIONS";

    private Button startRecording;

    private Switch accSwitch;
    private Switch gyroSwitch;
    private Switch magSwitch;
    private Switch wifiSwitch;

    private EditText readingsLocationArea;
    private EditText buildingTagArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_settings);

        startRecording = findViewById(R.id.start_recording_button);

        accSwitch = findViewById(R.id.acc_switch);
        gyroSwitch = findViewById(R.id.gyro_switch);
        magSwitch = findViewById(R.id.mag_switch);
        wifiSwitch = findViewById(R.id.wifi_switch);

        readingsLocationArea = findViewById(R.id.readings_location_input);
        buildingTagArea = findViewById(R.id.building_tag_input);

        startRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // We do nothing if the building where the data collection takes place is not mentioned
                String building = buildingTagArea.getText().toString();
                if (building.equals("")) {
                    Toast.makeText(SensorSettingsActivity.this, "No building was selected!", Toast.LENGTH_LONG).show();
                    return;
                }

                boolean recordAcc = accSwitch.isChecked();
                boolean recordGyro = gyroSwitch.isChecked();
                boolean recordMag = magSwitch.isChecked();
                boolean recordWifi = wifiSwitch.isChecked();

                // No switches selected, we do nothing
                if (!recordAcc && !recordGyro && !recordMag && !recordWifi) {
                    Toast.makeText(SensorSettingsActivity.this, "No sensors selected!", Toast.LENGTH_LONG).show();
                }

                // Create SensorReadingsOptions object and fire the SensorGraphsActivity
                else {

                    String readingsLocation = readingsLocationArea.getText().toString();
                    if (readingsLocation.equals("")) {
                        readingsLocation = readingsLocationArea.getHint().toString();
                    }

                    SensorReadingsOptions options =
                            new SensorReadingsOptions(recordAcc, recordGyro,
                                    recordMag, recordWifi, readingsLocation, building);

                    Intent intent = new Intent(SensorSettingsActivity.this, SensorGraphsActivity.class);
                    intent.putExtra(OPTIONS, options);
                    startActivity(intent);
                }
            }
        });

    }
}