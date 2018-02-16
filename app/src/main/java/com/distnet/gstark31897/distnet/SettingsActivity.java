package com.distnet.gstark31897.distnet;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {
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

    AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database")
                .allowMainThreadQueries().fallbackToDestructiveMigration().build();

        interfaceInput = (EditText) findViewById(R.id.interface_input);
        interfaceSubmit = (Button) findViewById(R.id.interface_submit);
        interfaceSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.interfaceDao().insertAll(new Interface(interfaceInput.getText().toString()));

                ArrayList<String> args = new ArrayList<String>();
                args.add(interfaceInput.getText().toString());
                Intent intent = new Intent("com.distnet.gstark31897.distnet.ACTIVITY");
                intent.putExtra("type","add_interface");
                intent.putExtra("args", args);
                sendBroadcast(intent);

                interfaceAdapter.notifyDataSetChanged();
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
                database.peerDao().insertAll(new Peer(peerInput.getText().toString()));

                ArrayList<String> args = new ArrayList<String>();
                args.add(peerInput.getText().toString());
                Intent intent = new Intent("com.distnet.gstark31897.distnet.ACTIVITY");
                intent.putExtra("type","add_peer");
                intent.putExtra("args", args);
                sendBroadcast(intent);

                peerAdapter.notifyDataSetChanged();
                peerInput.setText("");
            }
        });
        peerAdapter = new PeerAdapter(database);
        peerRecycler = (RecyclerView) findViewById(R.id.peer_recycler);
        peerRecycler.setAdapter(peerAdapter);
        peerLayoutManager = new LinearLayoutManager(this);
        peerLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        peerRecycler.setLayoutManager(peerLayoutManager);

        /*identityInput = (EditText)findViewById(R.id.identity_input);
        submitButton = (Button)findViewById(R.id.identity_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(MainActivity.NEW_CONTACT_IDENTITY, identityInput.getText().toString());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });*/
    }
}
