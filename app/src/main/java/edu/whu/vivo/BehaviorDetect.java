package edu.whu.vivo;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class BehaviorDetect extends Service{
    private boolean  res;
    private String data = "default";
    private static final String TAG = "BehaviorDetect";
    public Datacapture datacapture;


    public BehaviorDetect() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind: ");
        datacapture = new Datacapture(this);
//        collecting = true;
        return new Binder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
//        collecting = false;
        datacapture.collecting = false;
        return super.onUnbind(intent);
    }

    public class Binder extends android.os.Binder{
        public void setData(boolean res) {
            BehaviorDetect.this.res = res;
        }
        public BehaviorDetect getService(){
            return BehaviorDetect.this;
        }
    }

    public void reset(){

    }

}