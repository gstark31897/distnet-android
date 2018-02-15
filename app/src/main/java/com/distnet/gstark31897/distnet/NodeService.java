package com.distnet.gstark31897.distnet;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.os.Process;
import android.widget.Toast;

import java.util.ArrayList;

public class NodeService extends Service {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    BroadcastReceiver receiver;

    @Override
    public void onCreate() {
        // To avoid cpu-blocking, we create a background handler to run our service
        HandlerThread thread = new HandlerThread("NodeService", Process.THREAD_PRIORITY_BACKGROUND);
        // start the new handler thread
        thread.start();

        IntentFilter filter = new IntentFilter("com.distnet.gstark31897.distnet.ACTIVITY");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String type =  intent.getExtras().getString("type");
                ArrayList<String> args = intent.getStringArrayListExtra("args");
                showToast("Got intent: " + type);

                if (type == "add_interface") {
                    nodeAddInterface(0, args.get(0));
                } else if (type == "add_peer") {
                    nodeAddPeer(0, args.get(0));
                } else if (type == "discover") {
                    nodeDiscover(0, args.get(0));
                } else if (type == "send_msg") {
                    nodeSendMsg(0, args.get(0), args.get(1));
                } else if (type == "stop") {
                    nodeStop(0);
                }
            }
        };
        registerReceiver(receiver, filter);

        System.loadLibrary("native-lib");
        nodeStart(0, "ident");
        //nodeRun(0);

        //mServiceLooper = thread.getLooper();
        // start the service using the background handler
        //mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);

        System.out.println("destroying intent");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Starting node service", Toast.LENGTH_SHORT).show();

        // call a new service handler. The service ID can be used to identify the service
        //android.os.Message message = mServiceHandler.obtainMessage();
        //message.arg1 = startId;
        //mServiceHandler.sendMessage(message);

        return START_STICKY;
    }

    protected void showToast(final String msg){
        //gets the main thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // run this code in the main thread
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Object responsible for
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            showToast("Finishing TutorialService, id: " + msg.arg1);
            // the msg.arg1 is the startId used in the onStartCommand,
            // so we can track the running sevice here.

            Intent intent = new Intent("com.distnet.gstark31897.distnet.SERVICE");
            intent.putExtra("value","test");
            sendBroadcast(intent);

            // stopSelf(msg.arg1);

            nodeRun(0);

            stopSelf(msg.arg1);
        }
    }

    public native void nodeStart(int node_id, String identity);
    public native void nodeRun(int node_id);
    public native void nodeAddInterface(int node_id, String uri);
    public native void nodeAddPeer(int node_id, String uri);
    public native void nodeDiscover(int node_id, String identity);
    public native void nodeSendMsg(int node_id, String identity, String message);
    public native void nodeStop(int node_id);
}