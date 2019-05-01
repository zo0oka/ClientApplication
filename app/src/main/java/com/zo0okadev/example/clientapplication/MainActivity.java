package com.zo0okadev.example.clientapplication;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int POPUP_REQUEST_CODE = 101;
    private TextView replyText;
    private Intent serviceIntent;
    private Messenger requestMessenger, receiveMessenger;
    private boolean isBound;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: called");
            requestMessenger = new Messenger(service);
            receiveMessenger = new Messenger(new ReceiveHandler());
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: called");
            requestMessenger = null;
            receiveMessenger = null;
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bindButton = findViewById(R.id.bind_service_button);
        bindButton.setOnClickListener(this);
        Button unbindButton = findViewById(R.id.unbind_service_button);
        unbindButton.setOnClickListener(this);
        Button startServiceButton = findViewById(R.id.start_service_button);
        startServiceButton.setOnClickListener(this);
        Button requestPopupMessageButton = findViewById(R.id.request_popup_message_button);
        requestPopupMessageButton.setOnClickListener(this);
        replyText = findViewById(R.id.reply_text_view);

        serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName("com.zo0okadev.example.serverapplication",
                "com.zo0okadev.example.serverapplication.MyService"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bind_service_button:
                bindToService();
                break;
            case R.id.unbind_service_button:
                unbindFonService();
                break;
            case R.id.request_popup_message_button:
                getPopupMessage();
                break;
            case R.id.start_service_button:
                startRemoteService();
                break;
        }
    }

    private void startRemoteService() {
        startService(serviceIntent);
    }

    private void unbindFonService() {
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
            Toast.makeText(this, "Service unbound", Toast.LENGTH_SHORT).show();
        }
    }

    private void bindToService() {
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        Toast.makeText(this, "Service bound", Toast.LENGTH_SHORT).show();
    }

    private void getPopupMessage() {
        if (isBound) {
            Message requestMessage = Message.obtain(null, POPUP_REQUEST_CODE);
            requestMessage.replyTo = receiveMessenger;
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
            try {
                requestMessenger.send(requestMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Service not bound", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serviceConnection = null;
    }

    @SuppressLint("HandlerLeak")
    private class ReceiveHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == POPUP_REQUEST_CODE) {
                Log.d(TAG, "handleMessage: Message being handled");
                String popupText = msg.getData().getString("response");
                replyText.setText(popupText);
            }
            super.handleMessage(msg);
        }
    }
}
