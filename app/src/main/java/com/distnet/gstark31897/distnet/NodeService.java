package com.distnet.gstark31897.distnet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NodeService extends Service {
    AppDatabase database;
    SharedPreferences settings;

    private NodeRunner runner;
    BroadcastReceiver receiver;

    boolean showNotification = false;

    NotificationChannel notificationChannel;
    NotificationManager notificationManager;
    String currentContact = "";
    int currentNotification = 0;
    Map<String, Integer> activeNotifications;

    @Override
    public void onCreate() {
        Uri alarmSound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ringtone);
        AudioAttributes att = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        notificationChannel = new NotificationChannel("messages", "distnet", NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.CYAN);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{0, 75, 25, 75, 150, 150});
        notificationChannel.setSound(alarmSound, att);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        activeNotifications = new HashMap<>();

        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database")
                .allowMainThreadQueries().fallbackToDestructiveMigration().build();

        System.loadLibrary("distnet-core");
        System.loadLibrary("sodium");

        settings = getSharedPreferences("distnet", 0);
        String public_key = settings.getString("public_key", "");
        String secret_key = settings.getString("secret_key", "");
        System.out.println("public key: " + public_key);
        System.out.println("secret key: " + secret_key);
        if (public_key.length() == 0 || secret_key.length() == 0) {
            Intent intent = new Intent(this, SettingsActivity.class);
            String key[] = generateKeypair().split(":");
            public_key = key[0];
            secret_key = key[1];
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("public_key", public_key);
            editor.putString("secret_key", secret_key);
            editor.commit();
        }
        nodeStart(0, public_key, secret_key);

        database.interfaceDao().deleteTemporary();

        for (Interface inter: database.interfaceDao().getAll()) {
            nodeAddInterface(0, inter.getUri());
        }

        for (Peer peer: database.peerDao().getAll()) {
            nodeAddPeer(0, peer.getUri());
        }

        runner = new NodeRunner();
        runner.start();

        IntentFilter filter = new IntentFilter("com.distnet.gstark31897.distnet.ACTIVITY");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getExtras() == null) {
                    return;
                }
                String type =  intent.getExtras().getString("type");
                ArrayList<String> args = intent.getStringArrayListExtra("args");
                if (type == null || args == null) {
                    return;
                } else if (type.equals("main_activity")) {
                    mainActivity(args.get(0));
                } else if (type.equals("set_identity")) {
                    setIdentity(args.get(0), args.get(1));
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

    public void mainActivity(String status) {
        if (status.equals("open")) {
            showNotification = false;
            notificationManager.cancelAll();
            activeNotifications.clear();
        } else {
            showNotification = true;
        }
    }

    public void setIdentity(String publicKey, String secretKey) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("public_key", publicKey);
        editor.putString("secret_key", secretKey);
        editor.commit();
        nodeSetIdentity(0, publicKey, secretKey);
    }

    public void sendMessage(String identity, String message) {
        database.messageDao().insertAll(new Message(identity, true, message));
        makeIntent("msg_update");
        nodeSendMsg(0, identity, message);
    }

    public void messageCallback(String sender, String message) {
        database.messageDao().insertAll(new Message(sender, false, message));
        makeIntent("msg_update");

        if (!showNotification)
            return;

        int id = 0;
        if (activeNotifications.containsKey(sender)) {
            id = activeNotifications.get(sender).intValue();
        } else {
            id = currentNotification++;
        }
        PendingIntent notificationIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification n  = new Notification.Builder(this)
                .setContentTitle(sender)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setChannelId("messages")
                .setContentIntent(notificationIntent).build();
        notificationManager.notify(id, n);
        activeNotifications.put(sender, id);
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

    public native void nodeStart(int node_id, String public_key, String secret_key);
    public native void nodeRun(int node_id);
    public native void nodeSetIdentity(int node_id, String public_key, String secret_key);
    public native void nodeAddInterface(int node_id, String uri);
    public native void nodeRemoveInterface(int node_id, String uri);
    public native void nodeAddPeer(int node_id, String uri);
    public native void nodeDiscover(int node_id, String identity);
    public native void nodeSendMsg(int node_id, String identity, String message);
    public native void nodeStop(int node_id);
    public native String generateKeypair();
}