package com.stropa.gyros;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.format.Formatter;

import com.google.gson.Gson;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private String serverAddress = "";
    private String gyrosEndpoint = "";
    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        client.setConnectTimeout(100, TimeUnit.MILLISECONDS);
        shortFindServer();
        if (serverAddress.isEmpty()) longFindServer();
        gyrosEndpoint = "http://" + serverAddress + ":2137/gyros";
        System.out.println("Server address: " + serverAddress);
        System.out.println("Gyros endpoint URL: " + gyrosEndpoint);
        registerGyroSensorListener();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    int[] getDeviceAddress() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return Arrays.stream(ip.split("\\."))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    void shortFindServer() {
        int[] address = getDeviceAddress();
        String prefix = address[0] + "." + address[1] + "." + address[2] + ".";
        for (int i = 1; i < 256; i++) {
            if (!serverAddress.isEmpty()) return;
            sendSync(prefix + i);
        }
    }

    void longFindServer() {
        int[] address = getDeviceAddress();
        String prefix = address[0] + "." + address[1] + ".";
        for (int i = 0; i < 256; i++) {
            for (int y = 1; y < 256; y++) {
                if (!serverAddress.isEmpty()) return;
                sendSync(prefix + i + "." + y);
            }
        }
    }

    void sendSync(String address){
        Request request = new Request.Builder()
                .url("http://" + address + ":2137/sync")
                .get()
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) {
                if (response.isSuccessful()) {
                    serverAddress = address;
                } else System.out.println(response);
            }
        });
    }

    void registerGyroSensorListener() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        SensorEventListener gyroListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor sensor, int acc) {}
            public void onSensorChanged(SensorEvent event) {
                sendGyros(event.values);
            }
        };

        sensorManager.registerListener(gyroListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    void sendGyros(float[] data) {
        Request request = new Request.Builder()
                .url(gyrosEndpoint)
                .post(RequestBody.create(MediaType.parse("application/json"), gson.toJson(new GyrosDTO(data))))
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response){
                if (!response.isSuccessful()) System.out.println(response);
            }
        });
    }
}