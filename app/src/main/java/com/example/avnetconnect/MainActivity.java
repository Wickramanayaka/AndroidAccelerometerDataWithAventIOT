package com.example.avnetconnect;

import androidx.appcompat.app.AppCompatActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.iotconnectsdk.IoTConnectSDK;
import com.iotconnectsdk.interfaces.IotSDKCallback;
import com.iotconnectsdk.webservices.responsebean.HubToSdkDataBean;
import com.iotconnectsdk.webservices.responsebean.SyncServiceResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements IotSDKCallback, SensorEventListener {

    IoTConnectSDK iotConnect;
    String  cpId = ""; // cpId get from Avnet IOTConnect
    String uniqueId = "2";
    String env = "Avnet";
    private SensorManager sensorManager;
    double ax,ay,az ;
    TextView textViewAx, textViewAy, textViewAz;

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iotConnect = new IoTConnectSDK(MainActivity.this,cpId, uniqueId,
                MainActivity.this, env);

        // Accelorometer
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),sensorManager.SENSOR_DELAY_NORMAL);
    }

    private boolean checkValidation() {
        if (cpId.isEmpty()) {
            Toast.makeText(MainActivity.this, "cpId can not be blank.", Toast.LENGTH_LONG).show();
            return false;
        } else if (uniqueId.isEmpty()) {
            Toast.makeText(MainActivity.this, "uniqueId can not be blank.", Toast.LENGTH_LONG).show();
            return false;
        }else if (env.isEmpty()) {
            Toast.makeText(MainActivity.this, "env can not be blank.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onReceiveMsg(HubToSdkDataBean dataBean) {
        if (dataBean != null) {
            Log.e("onReceiveMsg", dataBean.getValue());

        }
    }

    @Override
    public void attributeData(List<SyncServiceResponse.DBeanXX.AttBean> attributesBeanList) {
        for(int i=0; i<attributesBeanList.size(); i++){
            Log.e("Attribute",attributesBeanList.get(i).getD().get(0).getLn());
        }
    }

    @Override
    public void onConnectionStateChange(boolean isConnected) {
        if (isConnected) {
            Log.e("onConnectionStateChange", "Device connected");
        } else {
            Log.e("onConnectionStateChange", "Device disconnected");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iotConnect != null) {
            iotConnect.disconnectSDK();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
            Log.i("Sensor","AX:" + ax + " AY:" + ay + " AZ:" + az);

            textViewAx = (TextView) findViewById(R.id.ax);
            textViewAy = (TextView) findViewById(R.id.ay);
            textViewAz = (TextView) findViewById(R.id.az);

            textViewAx.setText(String.valueOf(ax));
            textViewAy.setText(String.valueOf(ay));
            textViewAz.setText(String.valueOf(az));

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    String sendTeledata = "[{\'data\': {\'X\': \'"+ax+"\',\'Y\': \'"+ay+"\',\'Z\':\'"+az+"\'},\'uniqueId\':\'2\',\'time\' : \'"+getCurrentTime()+"\'}]";

                    if (iotConnect != null) {
                        iotConnect.sendData(sendTeledata);
                        Log.e("json", "" + sendTeledata);
                    }
                }
            }, 1000);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}