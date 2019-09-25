package com.notepoint4ugmail.bluetoothmessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static com.notepoint4ugmail.bluetoothmessenger.ConnectionReceiver.INTENT_STRING;

public class BroadCastActivity extends AppCompatActivity {

    Button button;
    ConnectionReceiver receiver;
    IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broad_cast);

        button = findViewById(R.id.button);

        receiver = new ConnectionReceiver();
        intentFilter = new IntentFilter(INTENT_STRING);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(INTENT_STRING);
                sendBroadcast(intent);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }
}
