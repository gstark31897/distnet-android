package com.distnet.gstark31897.distnet;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {
    String identity;
    EditText identityInput;
    Button identitySubmit;

    InterfaceAdapter interfaceAdapter;
    EditText interfaceInput;
    Button interfaceSubmit;
    RecyclerView interfaceRecycler;
    LinearLayoutManager interfaceLayoutManager;

    PeerAdapter peerAdapter;
    EditText peerInput;
    Button peerSubmit;
    RecyclerView peerRecycler;
    LinearLayoutManager peerLayoutManager;

    SharedPreferences settings;
    AppDatabase database;

    IntentFilter filter;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle(R.string.settings_title);

        settings = getSharedPreferences("distnet", 0);

        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database")
                .allowMainThreadQueries().fallbackToDestructiveMigration().build();

        identityInput = (EditText) findViewById(R.id.identity_input);
        identityInput.setText(settings.getString("identity", ""));
        identitySubmit = (Button) findViewById(R.id.identity_submit);
        identitySubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeIntent("set_identity", identityInput.getText().toString());
            }
        });

        interfaceInput = (EditText) findViewById(R.id.interface_input);
        interfaceSubmit = (Button) findViewById(R.id.interface_submit);
        interfaceSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeIntent("add_interface", interfaceInput.getText().toString());
                interfaceInput.setText("");
            }
        });
        interfaceAdapter = new InterfaceAdapter(database);
        interfaceRecycler = (RecyclerView) findViewById(R.id.interface_recycler);
        interfaceRecycler.setAdapter(interfaceAdapter);
        interfaceLayoutManager = new LinearLayoutManager(this);
        interfaceLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        interfaceRecycler.setLayoutManager(interfaceLayoutManager);

        peerInput = (EditText) findViewById(R.id.peer_input);
        peerSubmit = (Button) findViewById(R.id.peer_submit);
        peerSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeIntent("add_peer", peerInput.getText().toString());
                peerInput.setText("");
            }
        });
        peerAdapter = new PeerAdapter(database);
        peerRecycler = (RecyclerView) findViewById(R.id.peer_recycler);
        peerRecycler.setAdapter(peerAdapter);
        peerLayoutManager = new LinearLayoutManager(this);
        peerLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        peerRecycler.setLayoutManager(peerLayoutManager);

        filter = new IntentFilter("com.distnet.gstark31897.distnet.SERVICE");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String type =  intent.getExtras().getString("type");

                if (type.equals("interface_update")) {
                    interfaceAdapter.notifyDataSetChanged();
                } else if (type.equals("peer_update")) {
                    peerAdapter.notifyDataSetChanged();
                }
            }
        };
        registerReceiver(receiver, filter);
    }

    public void makeIntent(String type, String... args)
    {
        ArrayList<String> argList = new ArrayList<String>();
        for (String arg: args) {
            argList.add(arg);
        }

        Intent intent = new Intent("com.distnet.gstark31897.distnet.ACTIVITY");
        intent.putExtra("type", type);
        intent.putExtra("args", argList);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
