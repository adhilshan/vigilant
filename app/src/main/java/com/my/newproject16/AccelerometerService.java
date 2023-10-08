package com.my.newproject16;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.content.Context;
import androidx.annotation.Nullable;

public class AccelerometerService extends Service implements SensorEventListener {
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private static final float NS2S = 1.0f / 1e9f;
	private long lastTimestamp = 0;
	private float velocity = 0; // Velocity in m/s
	
	
	public static final String VELOCITY_UPDATE_ACTION = "com.my.newproject16.velocity_update";
	private static final float ACCELERATION_THRESHOLD = 0.1f; // Adjust this threshold as needed
	private static final float ALPHA = 0.8f; // Smoothing factor (adjust as needed)
	private float smoothedVelocity = 0; // Initial velocity
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		if (accelerometer != null) {
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (accelerometer != null) {
			sensorManager.unregisterListener(this);
		}
	}
	@Override
public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        long currentTimestamp = System.nanoTime();
        if (lastTimestamp != 0) {
            float dt = (currentTimestamp - lastTimestamp) * NS2S;
            float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

            // Apply the low-pass filter
            smoothedVelocity = ALPHA * smoothedVelocity + (1 - ALPHA) * (acceleration * dt);

            // Broadcast smoothed velocity updates to any registered components
            Intent velocityIntent = new Intent(VELOCITY_UPDATE_ACTION);
            velocityIntent.putExtra("VELOCITY_KM_HR", smoothedVelocity * 3.6f);
            sendBroadcast(velocityIntent);
        }

        lastTimestamp = currentTimestamp;
    }
}

	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Handle changes in sensor accuracy if needed (you can leave it empty if not required)
	}
	
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
