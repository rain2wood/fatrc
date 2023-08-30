package xyz.rain2wood.fatrc;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "MainActivity";
    private static final String DEVICE_ADDRESS = "98:D3:61:F6:FC:B3"; // Replace with your HC-05 address

    private BluetoothFactory bluetoothFactory;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor rotationSensor;
    private float[] accelerometerValues = new float[3];
    private float[] rotationMatrix = new float[9];

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;

    private Handler bluetoothHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // Handle Bluetooth messages if needed
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothFactory = BluetoothFactory.getInstance();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        if (!bluetoothFactory.isBluetoothSupported()) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!bluetoothFactory.isBluetoothEnabled()) {
            requestBluetoothEnable();
        } else {
            checkLocationPermission();
        }

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            connectToDevice();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                connectToDevice();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void connectToDevice() {
        if (bluetoothFactory.connectToDevice(DEVICE_ADDRESS)) {
            Toast.makeText(this, "Connected to device", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to connect to device", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendCommand(String command) {
        bluetoothFactory.sendCommand(command);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        }

        int orientation = OrientationFactory.getOrientation(accelerometerValues, rotationMatrix);

        switch (orientation) {
            case OrientationFactory.ORIENTATION_FRONT:
                sendCommand("F");
                break;
            case OrientationFactory.ORIENTATION_BACK:
                sendCommand("B");
                break;
            case OrientationFactory.ORIENTATION_LEFT:
                sendCommand("L");
                break;
            case OrientationFactory.ORIENTATION_RIGHT:
                sendCommand("R");
                break;
            default:
                // No recognized orientation or no change in orientation
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}