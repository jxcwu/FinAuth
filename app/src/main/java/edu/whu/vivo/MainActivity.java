package edu.whu.vivo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class MainActivity extends AppCompatActivity {
    private BehaviorDetect.Binder binderSen;
    private ImageView imageFingerscan;
    private EditText name;
    public String FILEPATH ;
    private BehaviorDetect mservice;
    private static final String TAG = "MainActivity";

    private ServiceConnection serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                binderSen = (BehaviorDetect.Binder) service;
                mservice = binderSen.getService();
            }
        public void onServiceDisconnected(ComponentName className) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }


    @SuppressLint("ClickableViewAccessibility")
    public void init(){
        FILEPATH = MainActivity.this.getFilesDir().getPath();
        bindService(new Intent(MainActivity.this, BehaviorDetect.class), serviceConnection, Context.BIND_AUTO_CREATE);
        imageFingerscan = findViewById(R.id.imageFingerscan);
        name = findViewById(R.id.editText);
        imageFingerscan.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    Log.e(TAG, "ACTION_DOWN: ");
                    mservice.datacapture.name =  name.getText().toString();
                    mservice.datacapture.start();
                }
                return true;
            }

        });
    }

}