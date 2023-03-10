package edu.whu.vivo.sensorfusion.sensor.acceleration;


/*
 * Copyright 2018, Kircher Electronics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import edu.whu.vivo.sensorfusion.observer.SensorSubject;
import edu.whu.vivo.sensorfusion.sensor.FSensor;

public class AccelerationSensor implements FSensor {
    private static final String TAG = AccelerationSensor.class.getSimpleName();

    private final SensorManager sensorManager;
    private final SimpleSensorListener listener;
    private float startTime = 0;
    private int count = 0;

    private float[] acceleration = new float[3];
    private float[] output = new float[4];

    private int sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;

    private final SensorSubject sensorSubject;

    private int sensorType = Sensor.TYPE_ACCELEROMETER;

    public AccelerationSensor(Context context) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.listener = new SimpleSensorListener();
        this.sensorSubject = new SensorSubject();
    }

    @Override
    public void start() {
        startTime = 0;
        count = 0;

        registerSensors(sensorDelay);
    }

    @Override
    public void stop() {
        unregisterSensors();
    }

    @Override
    public void register(SensorSubject.SensorObserver sensorObserver) {
        sensorSubject.register(sensorObserver);
    }

    @Override
    public void unregister(SensorSubject.SensorObserver sensorObserver) {
        sensorSubject.unregister(sensorObserver);
    }

    /**
     * Set the gyroscope sensor type.
     * @param sensorType must be Sensor.TYPE_GYROSCOPE or Sensor.TYPE_GYROSCOPE_UNCALIBRATED
     */
    public void setSensorType(int sensorType) {
        if(sensorType != Sensor.TYPE_ACCELEROMETER && sensorType != Sensor.TYPE_ACCELEROMETER_UNCALIBRATED && sensorType != Sensor.TYPE_LINEAR_ACCELERATION) {
            throw new IllegalStateException("Sensor Type must be Sensor.TYPE_ACCELEROMETER or Sensor.TYPE_ACCELEROMETER_UNCALIBRATED or sensorType != Sensor.TYPE_LINEAR_ACCELERATION");
        }

        this.sensorType = sensorType;
    }

    /**
     * Set the sensor frequency.
     * @param sensorDelay Must be SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_NORMAL or SensorManager.SENSOR_DELAY_UI
     */
    public void setSensorDelay(int sensorDelay) {
        this.sensorDelay = sensorDelay;
    }

    public void reset() {
        stop();
        acceleration = new float[3];
        output = new float[4];
        start();
    }

    private float calculateSensorFrequency() {
        // Initialize the start time.
        if (startTime == 0) {
            startTime = System.nanoTime();
        }

        long timestamp = System.nanoTime();

        // Find the sample period (between updates) and convert from
        // nanoseconds to seconds. Note that the sensor delivery rates can
        // individually vary by a relatively large time frame, so we use an
        // averaging technique with the number of sensor updates to
        // determine the delivery rate.

        return (count++ / ((timestamp - startTime) / 1000000000.0f));
    }

    private void processAcceleration(float[] acceleration) {
        System.arraycopy(acceleration, 0, this.acceleration, 0, this.acceleration.length);
    }

    private void registerSensors(int sensorDelay) {
        // Register for sensor updates.
        sensorManager.registerListener(listener, sensorManager
                        .getDefaultSensor(sensorType),
                sensorDelay);
    }

    private void unregisterSensors() {
        sensorManager.unregisterListener(listener);
    }

    private void setOutput(float[] value) {
        System.arraycopy(value, 0, output, 0, value.length);
        output[3] = calculateSensorFrequency();
        sensorSubject.onNext(output);
    }

    private class SimpleSensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == sensorType) {

                processAcceleration(event.values);
                setOutput(acceleration);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
