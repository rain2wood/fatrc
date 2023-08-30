package xyz.rain2wood.fatrc;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import xyz.rain2wood.fatrc.OrientationFactory;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor rotationVector;
    private boolean isAccelerometerRegistered = false;
    private boolean isRotationVectorRegistered = false;
    private float[] accelerometerValues = new float[3];
    private float[] rotationMatrix = new float[9];
    private TextView orientationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        orientationTextView = findViewById(R.id.oriText);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSensorListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterSensorListeners();
    }

    private void registerSensorListeners() {
        if (accelerometer != null && !isAccelerometerRegistered) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            isAccelerometerRegistered = true;
            Log.d(TAG, "Accelerometer listener registered");
        }
        if (rotationVector != null && !isRotationVectorRegistered) {
            sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_NORMAL);
            isRotationVectorRegistered = true;
            Log.d(TAG, "Rotation vector listener registered");
        }
    }

    private void unregisterSensorListeners() {
        if (isAccelerometerRegistered) {
            sensorManager.unregisterListener(this, accelerometer);
            isAccelerometerRegistered = false;
            Log.d(TAG, "Accelerometer listener unregistered");
        }
        if (isRotationVectorRegistered) {
            sensorManager.unregisterListener(this, rotationVector);
            isRotationVectorRegistered = false;
            Log.d(TAG, "Rotation vector listener unregistered");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer) {
            System.arraycopy(event.values, 0, accelerometerValues, 0, event.values.length);
        } else if (event.sensor == rotationVector) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        }

        if (accelerometerValues != null && rotationMatrix != null) {
            int orientation = OrientationFactory.getOrientation(accelerometerValues, rotationMatrix);

            switch (orientation) {
                case OrientationFactory.ORIENTATION_FRONT:
                    updateOrientationText("Front");
                    break;
                case OrientationFactory.ORIENTATION_BACK:
                    updateOrientationText("Back");
                    break;
                case OrientationFactory.ORIENTATION_LEFT:
                    updateOrientationText("Left");
                    break;
                case OrientationFactory.ORIENTATION_RIGHT:
                    updateOrientationText("Right");
                    break;
            }

            // Log.d(TAG, "Phone orientation: " + orientation);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    private void updateOrientationText(String orientation) {
        orientationTextView.setText(orientation);
    }
}