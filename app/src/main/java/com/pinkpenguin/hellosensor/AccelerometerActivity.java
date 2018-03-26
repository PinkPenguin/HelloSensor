package com.pinkpenguin.hellosensor;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.math.BigDecimal;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener{

    TextView txt_accX;
    TextView txt_accY;
    TextView txt_accZ;

    Sensor accelerometer;

    SensorManager sManager;

    float[] accArray = new float[3];

    final static float ALPHA = 0.125f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        txt_accX = (TextView) findViewById(R.id.txt_accX);
        txt_accY = (TextView) findViewById(R.id.txt_accY);
        txt_accZ = (TextView) findViewById(R.id.txt_accZ);

        if(sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null){
            noSensorAlert();

        }
        else {
            accelerometer = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sManager.registerListener(this, accelerometer, sManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            //System.arraycopy(event.values, 0, accArray, 0, event.values.length);
            /* Still dont know if this is the right way to do this*/
            accArray = lowPassFilter(event.values.clone(), accArray);

            for(int i = 0; i<accArray.length; i++) {
                accArray[i] = round(accArray[i], 2);
            }

            txt_accX.setText("X: " + accArray[0]);
            txt_accY.setText("Y: " + accArray[1]);
            txt_accZ.setText("Z: " + accArray[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void noSensorAlert(){
        AlertDialog.Builder alertDialogue = new AlertDialog.Builder(this);
        alertDialogue.setMessage("Your device does not support this feature.");
        alertDialogue.setCancelable(false);
        alertDialogue.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();;
            }
        });
        alertDialogue.show();
    }

    protected float[] lowPassFilter(float[] input, float[] output){
        if(output == null){
            return input;
        }

        for(int i=0; i<input.length; i++){
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;

    }

    public float round(float value, int dPlaces){
        BigDecimal bigD = new BigDecimal(Float.toString(value));
        bigD = bigD.setScale(dPlaces, BigDecimal.ROUND_HALF_UP);
        return bigD.floatValue();
    }
}
