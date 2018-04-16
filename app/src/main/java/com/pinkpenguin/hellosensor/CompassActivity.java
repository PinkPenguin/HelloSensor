package com.pinkpenguin.hellosensor;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    ImageView compass_img;
    TextView txt_azimuth;
    Vibrator vibrator;
    int azimuth;
    private SensorManager sensorManager;
    private Sensor rotationV, accelerometer, magnetometer;
    boolean hasSensor = false, hasSensor2 = false;
    float[] rotationMatrix = new float[9];
    float[] orientation = new float[3];
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;
    static final float ALPHA = 0.25f;

    float[] prevRotationMatrix = new float[9];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        compass_img = (ImageView) findViewById(R.id.img_compass);
        txt_azimuth = (TextView) findViewById(R.id.txt_azimuth);

        start();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

            rotationMatrix = lowPassFilter(prevRotationMatrix, rotationMatrix);
            azimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientation)[0]) + 360) % 360;

            prevRotationMatrix = rotationMatrix;
        }
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;

        }
        else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            lastMagnetometerSet = true;
        }
        if(lastMagnetometerSet && lastAccelerometerSet){
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            //Why do getOrientation twice?
            SensorManager.getOrientation(rotationMatrix, orientation);
            azimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientation)[0]) + 360) % 360;
        }

        azimuth = Math.round(azimuth);
        compass_img.setRotation(-azimuth);

        String cDirection = "ERROR";

        if(azimuth >= 350 || azimuth <= 10){
            cDirection = "N";

            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            else{
                vibrator.vibrate(500);
            }
        }
        else if(azimuth < 350 && azimuth > 280){
            cDirection = "NW";
        }else if(azimuth < 280 && azimuth > 260){
            cDirection = "W";
        }else if(azimuth <= 260 && azimuth > 190){
            cDirection = "SW";
        }else if(azimuth <= 190 && azimuth > 170){
            cDirection = "S";
        }else if(azimuth <= 170 && azimuth > 100){
            cDirection = "SE";
        }else if(azimuth <= 100 && azimuth > 80){
            cDirection = "E";
        }else if(azimuth <= 80 && azimuth > 10){
            cDirection = "NE";
        }

       txt_azimuth.setText(azimuth + "° " + cDirection);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void start(){
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null){
            if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null ||
                    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null){
                noSensorAlert();
            }
            else{
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

                hasSensor = sensorManager.registerListener(this, accelerometer, sensorManager.SENSOR_DELAY_UI);
                hasSensor2 = sensorManager.registerListener(this, magnetometer,sensorManager.SENSOR_DELAY_UI);

            }
        }
        else{
            rotationV = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            hasSensor = sensorManager.registerListener(this, rotationV, sensorManager.SENSOR_DELAY_UI);

        }
    }

    private void noSensorAlert() {
        AlertDialog.Builder alertDialogue = new AlertDialog.Builder(this);
        alertDialogue.setMessage("Your device does not support this feature.");
        alertDialogue.setCancelable(false);
        alertDialogue.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        alertDialogue.show();
    }

    public void stop(){
        if(hasSensor && hasSensor2){
            sensorManager.unregisterListener(this, accelerometer);
            sensorManager.unregisterListener(this, magnetometer);
        }
        else{
            if(hasSensor){
                sensorManager.unregisterListener(this, rotationV);
            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        stop();
    }

    @Override
    protected void onResume(){
        super.onResume();
        start();
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
}

