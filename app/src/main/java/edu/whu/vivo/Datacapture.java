package edu.whu.vivo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import edu.whu.vivo.sensorfusion.observer.SensorSubject;
import edu.whu.vivo.sensorfusion.sensor.FSensor;
import edu.whu.vivo.sensorfusion.sensor.acceleration.AccelerationSensor;
import edu.whu.vivo.sensorfusion.sensor.acceleration.KalmanLinearAccelerationSensor;
import edu.whu.vivo.sensorfusion.sensor.acceleration.LinearAccelerationSensor;
import edu.whu.vivo.sensorfusion.sensor.gyroscope.KalmanGyroscopeSensor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.SENSOR_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;


/**
 * created by Terence
 * on 2022/3/16
 */
class Datacapture  {
    private FSensor fusion_acc, fusion_gyr;
    private Context mContext;
    private SensorManager sensorManager;
    private int SAMPLEDELAY = 5000;
    public boolean collecting= false ;
    private float[] gyrValues;
    public String name;
    private float[] accValues;

    private int COUNT_MAX = 100; // 对于一次数据采集为100次
    public int count;
    private CallBack callback=null;
    private static final String TAG = "Datacapture";
    private float[] acclx, accly, acclz,acclnorm;
    private float[]  gyrx, gyry, gyrz;
    private List < float[] > res;


    Datacapture(Context context){
        this.mContext=context;
    }

    public void init_v(){
        count = 0;
        gyrValues = new float[3] ;
        accValues = new float[3] ;
        acclx = new float[100]; accly = new float[100]; acclz = new float[100]; acclnorm = new float[100];
        gyrx = new float[100]; gyry = new float[100]; gyrz = new float[100];
        res = new ArrayList<>();
    }

    private SensorSubject.SensorObserver acc_sensorObserver = new SensorSubject.SensorObserver() {
        @Override
        public void onSensorChanged(float[] values) {
            accValues = values.clone();
            setting_v();
        }
    };

    private SensorSubject.SensorObserver gyr_sensorObserver = new SensorSubject.SensorObserver() {
        @Override
        public void onSensorChanged(float[] values) {
            gyrValues = values.clone();
        }
    };

    public void start(){
        fusion_acc = new KalmanLinearAccelerationSensor(mContext);
        fusion_gyr= new KalmanGyroscopeSensor(mContext);
        fusion_acc.register(acc_sensorObserver);
        fusion_gyr.register(gyr_sensorObserver);
        fusion_acc.setSensorDelay(SAMPLEDELAY);
        fusion_gyr.setSensorDelay(SAMPLEDELAY);
        init_v();
        fusion_acc.reset();
        fusion_gyr.reset();
    }

    public void stop(){
        fusion_acc.unregister(acc_sensorObserver);
        fusion_gyr.unregister(gyr_sensorObserver);
        fusion_acc.stop();
        fusion_gyr.stop();
        save_batch_stfts();

        if (callback!=null) {
            callback.onComplete(res);
        }

        String lineo = "";
        for (int i = 0; i < COUNT_MAX; i++) {
            lineo +=gyrx[i]+",";
        }
        Log.e(TAG, lineo);
        init_v();
    }


//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        if (collecting && count < COUNT_MAX) {
//            data_collecting(event);
//        }
//        else if (collecting && count == COUNT_MAX){
//            stop();
//        }
//    }

//    public void data_collecting(SensorEvent event){
//        switch (event.sensor.getType()) {
//            case Sensor.TYPE_LINEAR_ACCELERATION:
//                accValues = event.values.clone();
//                setting_v();
//                break;
//
//            case Sensor.TYPE_MAGNETIC_FIELD:
//                magValues = event.values.clone();
//                break;
//
//            case Sensor.TYPE_GYROSCOPE:
//                gyrValues = event.values.clone();
//                break;
//        }
//    }

    public void setting_v(){
        if (count < COUNT_MAX && gyrValues[0]!=0) {
            acclx[count] = accValues[0];
            accly[count] = accValues[1];
            acclz[count] = accValues[2];
            acclnorm[count] = (float) Math.sqrt( accValues[0] *  accValues[0] +  accValues[1]* accValues[1] + accValues[2]*accValues[2]);
            gyrx[count] = gyrValues[0];
            gyry[count] = gyrValues[1];
            gyrz[count] = gyrValues[2];
            count+=1;
        }
        else if (count == COUNT_MAX){
            stop();
        }
    }

    public void save_stftfeature(double[][] stftfeatures,  String filepath, String filename){
        for (double[] r : stftfeatures){
            StringBuilder line_write = new StringBuilder();
            for (double c: r){
                line_write.append(String.format( "%.5f,", c));
            }
            String des_path = filepath + "/" + filename + ".txt";
            new SDFileUtils(des_path).writeToSDFile(line_write + "\n");
        }
    }

    public String Get_path(){
        String filepath = mContext.getFilesDir().getPath() + "/" +  String.valueOf(System.currentTimeMillis()/100);
        if  (!new File(filepath).exists() )
        {
            new File(filepath).mkdirs();
        }
        return filepath;
    }

    public void save_batch_stfts(){
        String filepath = Get_path();

        Stft stft = new Stft();
        save_stftfeature(stft.extractSTFTFeatures(acclx), filepath, "acclx");
        save_stftfeature(stft.extractSTFTFeatures(accly), filepath, "accly");
        save_stftfeature(stft.extractSTFTFeatures(acclz), filepath, "acclz");
        save_stftfeature(stft.extractSTFTFeatures(acclnorm), filepath, "acclnorm");
        save_stftfeature(stft.extractSTFTFeatures(gyrx), filepath, "gyrx");
        save_stftfeature(stft.extractSTFTFeatures(gyry), filepath, "gyry");
        save_stftfeature(stft.extractSTFTFeatures(gyrz), filepath, "gyrz");
    }

    public CallBack getCallback() {
        return callback;
    }

    public void setCallback(CallBack callback) {
        this.callback = callback;
    }

    public static interface CallBack{
        void onComplete(List data);
    }
}
