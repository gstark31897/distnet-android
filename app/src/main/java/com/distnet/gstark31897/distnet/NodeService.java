package com.distnet.gstark31897.distnet;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class NodeService extends IntentService {
    public NodeService() {
        super("nodeservice");
        System.out.println("making intent service");
        System.loadLibrary("native-lib");
        nodeStart(0, "ident1");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();
    }


    //public native String stringFromJNI();
    public native void nodeStart(int nodeId, String identity);
    public native void nodeRun(int nodeId);
    public native String nodeAddInterface();
    public native String nodeAddPeer();
    public native String nodeDiscover();
    public native String nodeSendMsg();
    public native String nodeStop(int nodeId);
}
