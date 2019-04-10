package com.example.myfirstsensorapp;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    ImageView compass_img;
    TextView txt_compass;
    int mAzimuth;
    private SensorManager mSensorManager;
    private Sensor mRotationV, mAccelerometer, mMagnetometer;
    boolean haveSensor = false, haveSensor2 = false;
    float[] rMat = new float[9];
    float[] orientation = new float[3];
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private boolean hasVibrated = false;
    private boolean focused = false;
    private MediaPlayer cat_sound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        compass_img = (ImageView) findViewById(R.id.img_compass);
        txt_compass = (TextView) findViewById(R.id.txt_azimuth);
        cat_sound = MediaPlayer.create(this, R.raw.cat_sound);

        start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        mAzimuth = Math.round(mAzimuth);
        compass_img.setRotation(-mAzimuth);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        String where = "NW";

        if (mAzimuth >= 345 || mAzimuth <= 15) {
            where = "N";
            if (focused && !hasVibrated) {
                v.vibrate(100);
                hasVibrated = true;
                cat_sound.start();

            }
            this.getWindow().getDecorView().setBackgroundColor(getColor(R.color.red));
        }
        if (mAzimuth < 345 && mAzimuth > 285) {
            where = "NW";
            hasVibrated = false;
        }
        if (mAzimuth <= 285 && mAzimuth > 255){
            where = "W";
            hasVibrated = false;
            this.getWindow().getDecorView().setBackgroundColor(getColor(R.color.blue));
        }
        if (mAzimuth <= 255 && mAzimuth > 195) {
            where = "SW";
            hasVibrated = false;
        }
        if (mAzimuth <= 195 && mAzimuth > 165) {
            where = "S";
            hasVibrated = false;
            this.getWindow().getDecorView().setBackgroundColor(getColor(R.color.green));
        }
        if (mAzimuth <= 165 && mAzimuth > 105) {
            where = "SE";
            hasVibrated = false;
        }
        if (mAzimuth <= 105 && mAzimuth > 75) {
            where = "E";
            hasVibrated = false;
            this.getWindow().getDecorView().setBackgroundColor(getColor(R.color.yellow));
        }
        if (mAzimuth <= 75 && mAzimuth > 15) {
            where = "NE";
            hasVibrated = false;
        }


        txt_compass.setText(mAzimuth + "° " + where);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void start() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if ((mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null)) {
                noSensorsAlert();
            }
            else {
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                haveSensor = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                haveSensor2 = mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        }
        else{
            mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            haveSensor = mSensorManager.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void noSensorsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Your device doesn't support the Compass.")
                .setCancelable(false)
                .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        alertDialog.show();
    }

    public void stop() {
        if(haveSensor && haveSensor2){
            mSensorManager.unregisterListener(this,mAccelerometer);
            mSensorManager.unregisterListener(this,mMagnetometer);
        }
        else{
            if(haveSensor)
                mSensorManager.unregisterListener(this,mRotationV);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        focused = false;
        stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        focused = true;
        start();
    }
}
