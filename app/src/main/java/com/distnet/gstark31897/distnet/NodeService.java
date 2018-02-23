package com.distnet.gstark31897.distnet;

import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.*;

import java.util.ArrayList;

public class NodeService extends Service {
    AppDatabase database;
    SharedPreferences settings;

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

                if (type.equals("set_identity")) {
                    setIdentity(args.get(0));
                } else if (type.equals("add_interface")) {
                    addInterface(args.get(0));
                } else if (type.equals("remove_interface")) {
                    removeInterface(args.get(0));
                } else if (type.equals("add_peer")) {
                    addPeer(args.get(0));
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

        settings = getSharedPreferences("distnet", 0);

        System.loadLibrary("distnet-core");
        nodeStart(0, settings.getString("identity", ""));

        database.interfaceDao().deleteTemporary();

        System.out.println("adding interfaces");
        for (Interface inter: database.interfaceDao().getAll()) {
            nodeAddInterface(0, inter.getUri());
        }

        System.out.println("adding peers");
        for (Peer peer: database.peerDao().getAll()) {
            nodeAddPeer(0, peer.getUri());
        }

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

    public void setIdentity(String identity) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("identity", identity);
        editor.commit();
        nodeSetIdentity(0, identity);
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

    public void addInterface(String uri) {
        database.interfaceDao().insertAll(new Interface(uri, true));
        makeIntent("interface_update");
        nodeAddInterface(0, uri);
    }

    public void removeInterface(String uri) {
        nodeRemoveInterface(0, uri);
    }

    public void interfaceCallback(String uri, boolean add) {
        System.out.println("interface cb");
        if (add) {
            database.interfaceDao().insertAll(new Interface(uri, false));
        } else {
            database.interfaceDao().remove(uri);
        }
        makeIntent("interface_update");
    }

    public void addPeer(String uri) {
        database.peerDao().insertAll(new Peer(uri));
        makeIntent("peer_update");
        nodeAddPeer(0, uri);
    }

    public native void nodeStart(int node_id, String identity);
    public native void nodeRun(int node_id);
    public native void nodeSetIdentity(int node_id, String identity);
    public native void nodeAddInterface(int node_id, String uri);
    public native void nodeRemoveInterface(int node_id, String uri);
    public native void nodeAddPeer(int node_id, String uri);
    public native void nodeDiscover(int node_id, String identity);
    public native void nodeSendMsg(int node_id, String identity, String message);
    public native void nodeStop(int node_id);
}