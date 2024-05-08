package com.project.seniorpal.skill.service.util;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class OneTimeConnection implements ServiceConnection {
    private BiConsumer<ComponentName, IBinder> callback;

    public OneTimeConnection(BiConsumer<ComponentName, IBinder> callback) {
        this.callback = callback;
    }

    public OneTimeConnection() {
    }

    public void setCallback(BiConsumer<ComponentName, IBinder> callback) {
        this.callback = callback;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        callback.accept(name, service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
