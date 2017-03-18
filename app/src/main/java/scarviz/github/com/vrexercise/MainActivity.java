package scarviz.github.com.vrexercise;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG =MainActivity.class.getSimpleName();
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView)findViewById(R.id.ipadress);

        UARTHelper.getInstance().open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Loopback Destroyed");

        UARTHelper.getInstance().close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tv.setText(getIPAddress());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    private String getIPAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String strIPAddess =
                ((ipAddress >> 0) & 0xFF) + "." +
                        ((ipAddress >> 8) & 0xFF) + "." +
                        ((ipAddress >> 16) & 0xFF) + "." +
                        ((ipAddress >> 24) & 0xFF);
        Log.v("IP Address", strIPAddess);
        return strIPAddess;
    }
}
