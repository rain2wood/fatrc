package xyz.rain2wood.fatrc;

import android.hardware.SensorManager;
import android.util.Log;

public class OrientationFactory {

    // hack: save lastPitch and lastRoll so we can easily determine orientation
    private static float lastPitch = 0.0f;
    private static float lastRoll = 0.0f;

    static int threshold = 35;

    public static final int ORIENTATION_FRONT = 0;
    public static final int ORIENTATION_BACK = 1;
    public static final int ORIENTATION_LEFT = 2;
    public static final int ORIENTATION_RIGHT = 3;

    public static int getOrientation(float[] accelerometerValues, float[] rotationMatrix) {
        float[] orientation = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientation);

        float pitch = orientation[1] * (180 / (float) Math.PI);
        float roll = orientation[2] * (180 / (float) Math.PI);

        if (pitch > lastPitch + threshold) {
            lastPitch = pitch;
            return ORIENTATION_FRONT;
        } else if (pitch < lastPitch - threshold) {
            lastPitch = pitch;
            return ORIENTATION_BACK;
        } else if (roll > lastRoll + threshold) {
            lastRoll = roll;
            return ORIENTATION_RIGHT;
        } else if (roll < lastRoll - threshold) {
            lastRoll = roll;
            return ORIENTATION_LEFT;
        }

        return -1;
    }

}