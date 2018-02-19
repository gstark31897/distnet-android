package com.distnet.gstark31897.distnet;

import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;

import java.util.ArrayList;

public class NodeService extends Service {
    AppDatabase database;

    private NodeRunner runner;
    BroadcastReceiver receiver;

    @Override
    public void onCreate() {
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database")
                .allowMainThreadQueries().fallbackToDestructiveMigration().build();

        IntentFilter filter = new IntentFilter("com.distnet.gstark31897.distnet.ACTIVITY");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String type =  intent.getExtras().getString("type");
                ArrayList<String> args = intent.getStringArrayListExtra("args");

                if (type.equals("add_interface")) {
                    nodeAddInterface(0, args.get(0));
                } else if (type.equals("add_peer")) {
                    nodeAddPeer(0, args.get(0));
                } else if (type.equals("discover")) {
                    nodeDiscover(0, args.get(0));
                } else if (type.equals("send_msg")) {
                    sendMessage(args.get(0), args.get(1));
                } else if (type.equals("stop")) {
                    nodeStop(0);
                }
            }
        };
        registerReceiver(receiver, filter);

        System.loadLibrary("distnet-core");
        nodeStart(0, "ident");

        runner = new NodeRunner();
        runner.start();
    }

    @Override
    public void onDestroy() {
        nodeStop(0);
        try {
            runner.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        unregisterReceiver(receiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private final class NodeRunner extends Thread {
        public void run() {
            System.out.println("sending message");
            nodeSendMsg(0, "ident", "test");

            System.out.println("running");
            nodeRun(0);
        }
    }

    public void makeIntent(String type, String... args)
    {
        ArrayList<String> argList = new ArrayList<String>();
        for (String arg: args) {
            argList.add(arg);
        }

        Intent intent = new Intent("com.distnet.gstark31897.distnet.SERVICE");
        intent.putExtra("type", type);
        intent.putExtra("args", argList);
        sendBroadcast(intent);
    }

    public void sendMessage(String identity, String message) {
        database.messageDao().insertAll(new Message(identity, true, message));
        makeIntent("msg_update");
        nodeSendMsg(0, identity, message);
    }

    public void messageCallback(String sender, String message) {
        database.messageDao().insertAll(new Message(sender, false, message));
        makeIntent("msg_update");
    }

    public native void nodeStart(int node_id, String identity);
    public native void nodeRun(int node_id);
    public native void nodeAddInterface(int node_id, String uri);
    public native void nodeAddPeer(int node_id, String uri);
    public native void nodeDiscover(int node_id, String identity);
    public native void nodeSendMsg(int node_id, String identity, String message);
    public native void nodeStop(int node_id);
}