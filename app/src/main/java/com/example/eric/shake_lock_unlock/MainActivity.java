package com.example.eric.shake_lock_unlock;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button b_enable, b_lock;

    static final int RESULT_ENABLE = 1;
    DevicePolicyManager devicePolicyManager;
    ComponentName componentName;


    private SensorManager sm;
    private float acelVal; // Current acceleration value and gravity.
    private float acellast; // Last Acceleration value and Gravity.
    private float shake;  // Acceleration value different from gravity.

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(sensorListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

       acelVal = SensorManager.GRAVITY_EARTH;
       acellast = SensorManager.GRAVITY_EARTH;
       shake = 0.00f;

       b_enable = (Button) findViewById(R.id.b_enable);
       b_lock = (Button) findViewById(R.id.b_lock);

       devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
       componentName = new ComponentName(this, Controller.class);

       boolean active = devicePolicyManager.isAdminActive(componentName);

       if (active){
           b_enable.setText("Disable");
           b_lock.setVisibility(View.VISIBLE);
       }
       else{
           b_enable.setText("ENABLE");
           b_lock.setVisibility(View.GONE);
       }


       b_enable.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               boolean active = devicePolicyManager.isAdminActive(componentName);
               if(active){
                   devicePolicyManager.removeActiveAdmin(componentName);
                   b_enable.setText("ENABLE");
                   b_lock.setVisibility(View.GONE);
               }
               else{
                   Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                   intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,componentName);
                   intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "You should enable the app!");
                   startActivityForResult(intent, RESULT_ENABLE);
               }
           }
       });

       b_lock.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
              // Toast toast = Toast.makeText(getApplicationContext(), "b_lock: onClick!!!!", Toast.LENGTH_LONG);
              // toast.show();

               boolean active = devicePolicyManager.isAdminActive(componentName);

               if(active){
                   Toast toast = Toast.makeText(getApplicationContext(), "b_lock: onClick got permission!!!", Toast.LENGTH_LONG);
                   toast.show();
               }else{
                   Toast toast = Toast.makeText(getApplicationContext(), "b_lock: onClick did not have permission!!!", Toast.LENGTH_LONG);
                   toast.show();
               }

               devicePolicyManager.lockNow();
           }
       });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case RESULT_ENABLE:
            if(resultCode == Activity.RESULT_OK){
                b_enable.setText("DISABLE");
                b_lock.setVisibility(View.VISIBLE);
            } else{
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            acellast = acelVal;
            acelVal= (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = acelVal - acellast;
            shake = shake * 0.9f + delta;

            if (shake > 12) {
                // DO something
                //example:
                Toast toast = Toast.makeText(getApplicationContext(), "DO NOT SHAKE ME, PLEASE!!", Toast.LENGTH_LONG);
                toast.show();
                devicePolicyManager.lockNow();
            }
       }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
}
