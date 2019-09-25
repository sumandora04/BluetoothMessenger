package com.notepoint4ugmail.bluetoothmessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class ConnectionReceiver extends BroadcastReceiver {
    public static final String INTENT_STRING = "com.notepoint4ugmail.bluetoothmessenger.SOME_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(INTENT_STRING)){
            Toast.makeText(context, "SOME_ACTION received", Toast.LENGTH_SHORT).show();
        }else {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            boolean isConnected = activeNetwork!=null && activeNetwork.isConnected();

            if (isConnected){
                Toast.makeText(context, "Network is connected", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(context, "Network not available", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
