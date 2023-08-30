package xyz.rain2wood.fatrc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothFactory {
    private static final String TAG = "BluetoothFactory";
    private static final UUID HC05_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // generic uuid should work fine

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private static BluetoothFactory instance;

    private BluetoothFactory() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static synchronized BluetoothFactory getInstance() {
        if (instance == null) {
            instance = new BluetoothFactory();
        }
        return instance;
    }

    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    public boolean connectToDevice(String deviceAddress) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(HC05_UUID);
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to connect to device: " + e.getMessage());
            closeConnection();
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing Bluetooth connection: " + e.getMessage());
        }
    }

    public void sendCommand(String command) {
        try {
            outputStream.write(command.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error sending command: " + e.getMessage());
        }
    }
}
