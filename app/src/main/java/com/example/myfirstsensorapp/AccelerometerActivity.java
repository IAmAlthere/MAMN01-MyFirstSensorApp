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
import android.widget.TextView;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float[] mLastAccelerometer = new float[3];
    private boolean haveSensor;
    private TextView x_coords;
    private TextView y_coords;
    private TextView z_coords;
    private TextView phoneInfo;
    private boolean hasVibrated = false;
    private boolean focused = false;
    private MediaPlayer cat_sound;
    private float gravity = SensorManager.GRAVITY_EARTH;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        x_coords = (TextView) findViewById(R.id.X_coords);
        y_coords = (TextView) findViewById(R.id.Y_coords);
        z_coords = (TextView) findViewById(R.id.Z_coords);
        phoneInfo = (TextView) findViewById(R.id.PhoneInfo);
        cat_sound = MediaPlayer.create(this, R.raw.cat_sound);


        start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
        }

        x_coords.setText("X-Coords: " + mLastAccelerometer[0]);
        y_coords.setText("Y-Coords: " + mLastAccelerometer[1]);
        z_coords.setText("Z-Coords: " + mLastAccelerometer[2]);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        StringBuilder phoneRotation = new StringBuilder();
        if(mLastAccelerometer[0] < -2){
            phoneRotation.append("Mobilen är roterad åt höger \n");
        }else if(mLastAccelerometer[0] > 2){
            phoneRotation.append("Mobilen är roterad åt vänster \n");
        } else {
            phoneRotation.append("Mobilen är inte roterad \n");
        }
        if(mLastAccelerometer[1] < -2){
            phoneRotation.append("Mobilen är lutad neråt \n");
        }else if(mLastAccelerometer[1] > 2){
            phoneRotation.append("Mobilen är lutad uppåt\n");
        } else {
            phoneRotation.append("Mobilen är ligger plant \n");
        }
        if(mLastAccelerometer[2] < -2){
            phoneRotation.append("Mobilen är riktad neråt \n");
            hasVibrated = false;
        }else if(mLastAccelerometer[2] > 2){
            phoneRotation.append("Mobilen är riktad uppåt \n");
            if (focused && !hasVibrated) {
                v.vibrate(100);
                hasVibrated = true;
                cat_sound.start();
            }
        } else {
            phoneRotation.append("Mobilen är upprät \n");
            hasVibrated = false;
        }
        //Calculates the hexadecimal number of the values from the accelerometer
        String hexa = calcToHexa(mLastAccelerometer);
        hexa = hexa.length() <= 5 ? "#0" + hexa :  "#" + hexa;
        phoneInfo.setText(phoneRotation.toString() + hexa);
        //Calculates the color based on the values from the accelerometer
        int[] color = calcColor(mLastAccelerometer);
        this.getWindow().getDecorView().setBackgroundColor(Color.argb(255,
                color[0] <= 255 ? color[0] : 255,
                color[1] <= 255 ? color[1] : 255,
                color[2] <= 255 ? color[2] : 255));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void start() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            noSensorsAlert();
        } else {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            haveSensor = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        if (haveSensor) {
            mSensorManager.unregisterListener(this, mAccelerometer);
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

    /**
     * Calculates the hexadecimal value of a float array for usage as colors. Based on gravity
     * @param values float vector from an accelerometer
     * @return string corresponding to the hexadecimal value of the float array as a color
     */
    private String calcToHexa(float[] values) {
        double redDouble = Math.ceil(Math.abs(values[0]/gravity)*255);
        double greenDouble = Math.ceil(Math.abs(values[1]/gravity)*255);
        double blueDouble = Math.ceil(Math.abs(values[2]/gravity)*255);
        double result = Math.pow(255.0,2.0)*redDouble + 255*greenDouble + blueDouble;
        return Integer.toString(Double.valueOf(result).intValue(), 16);
    }

    /**
     * Converts a float array range 0 - Earths gravity to a int vector range 0 - 255 corresponding to a color
     * @param values float array from an accelerometer
     * @return int array corresponding to a color
     */
    private int[] calcColor(float[] values) {
        int redDouble = Double.valueOf(Math.ceil(Math.abs(values[0]/gravity)*255)).intValue();
        int greenDouble = Double.valueOf(Math.ceil(Math.abs(values[1]/gravity)*255)).intValue();
        int blueDouble = Double.valueOf(Math.ceil(Math.abs(values[2]/gravity)*255)).intValue();
        return new int[]{redDouble, greenDouble, blueDouble};
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
