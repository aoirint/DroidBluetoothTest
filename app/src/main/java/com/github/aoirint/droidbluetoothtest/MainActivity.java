package com.github.aoirint.droidbluetoothtest;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public final String TAG = getClass().toString();
    public final int REQUEST_CODE_ENABLE_BT = 8000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button clientButton = findViewById(R.id.client_button);
        clientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter == null) {
                    Log.d(TAG, "No bluetooth support.");
                    return;
                }

                if (! adapter.isEnabled()) {
                    Log.d(TAG, "Bluetooth is disabled.");

                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BT);

                    return; // onActivityResult省略
                }

                final String SERVER_DEVICE_UUID = "00:1B:DC:F8:7D:58";
                final String SERVER_SERVICE_UUID = "ef7ce24a-a1eb-45d4-9208-f896b0ae8336";

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        BluetoothDevice server = adapter.getRemoteDevice(SERVER_DEVICE_UUID);
                        final BluetoothSocket socket;
                        try {
                            socket = server.createRfcommSocketToServiceRecord(UUID.fromString(SERVER_SERVICE_UUID));
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }

                        try {
                            socket.connect();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Unable to connect.");

                            try {
                                socket.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                Log.d(TAG, "Unable to close socket.");
                                return;
                            }

                            return;
                        }

                        final OutputStream os;
                        try {
                            os = socket.getOutputStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Unable to create output stream.");

                            try {
                                socket.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                Log.d(TAG, "Unable to close socket.");
                                return;
                            }
                            return;
                        }

                        try {
                            os.write("Hello World from Android\n".getBytes(Charset.forName("ascii")));
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Unable to write to output stream.");
                        }

                        try {
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Unable to close output stream.");
                        }

                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Unable to close socket.");
                        }

                    }
                }).run();

            }
        });

        Button serverButton = findViewById(R.id.server_button);
    }

}
