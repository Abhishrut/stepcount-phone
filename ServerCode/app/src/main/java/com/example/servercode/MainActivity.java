package com.example.servercode;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Thread serverThread;
    private String TAG="ABHI";

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Abhi","Error");
        } // for now eat exceptions
        return "Can't get IP";
    }

    private PrintWriter output;
    private BufferedReader input;

    private ServerSocket serverSocket;
    private int PORT = 4321;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        To forcefully block UI thread

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
         */
        TextView textView = findViewById(R.id.textView);
        textView.setText(getIPAddress(true));

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Button button = findViewById(R.id.Button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new ServerListen().execute("");
            }
        });

        //create threads
    }

    class ServerListen extends AsyncTask {
        @Override
        protected Object doInBackground(Object... arg0) {
            Socket socket;
            try {
                //new socket created
                Log.i(TAG, "Waiting for new client");
                socket = serverSocket.accept(); //blocking
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Log.i(TAG, "Connected");

                //start a new thread for additional connections
                //new Thread(new Thread1()).start();

                //send message to server
                /*
                String message = "Hello from server";
                output.write(message);
                output.flush();
                */

                //receive message from server
                String messageFromClient;
                while(!socket.isClosed())
                {
                    messageFromClient = input.readLine();
                    Log.i(TAG, "From Client: " + messageFromClient);
                    Thread.sleep(1000);
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}