package com.distnet.gstark31897.distnet;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String NEW_CONTACT_IDENTITY = "com.example.myfirstapp.NEW_CONTACT_IDENTITY";
    public static final int NEW_CONTACT_REQUEST_CODE = 0;
    public static final int SETTINGS_REQUEST_CODE = 1;

    NavigationView navigationView;
    RecyclerView messageRecycler;
    LinearLayoutManager layoutManager;

    EditText messageEdit;
    Button sendButton;

    MessageAdapter messageAdapter;

    AppDatabase database;
    Intent serviceIntent;

    IntentFilter filter;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database")
                .allowMainThreadQueries().fallbackToDestructiveMigration().build();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        for (Contact contact: database.contactDao().getAll())
            navigationView.getMenu().add(R.id.contacts_group, 0, 0, contact.getIdentity());

        //database.messageDao().insertAll(new Message("ident2", false, 1, "second message"));
        //database.messageDao().insertAll(new Message("ident2", true, 2, "third message"));
        messageAdapter = new MessageAdapter(database);

        messageRecycler = (RecyclerView) findViewById(R.id.message_recycler);
        messageRecycler.setAdapter(messageAdapter);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(true);
        messageRecycler.setLayoutManager(layoutManager);

        messageEdit = (EditText) findViewById(R.id.message_edit);
        sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.distnet.gstark31897.distnet.ACTIVITY");
                intent.putExtra("type","send_msg");
                ArrayList<String> args = new ArrayList<String>();
                args.add(messageEdit.getText().toString());
                sendBroadcast(intent);
            }
        });

        filter = new IntentFilter("com.distnet.gstark31897.distnet.SERVICE");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String value =  intent.getExtras().getString("value");
                System.out.println("got the intent: " + value);
            }
        };
        registerReceiver(receiver, filter);

        serviceIntent = new Intent(this, NodeService.class);
        this.startService(serviceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, SETTINGS_REQUEST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        int gid = item.getGroupId();

        if (gid == R.id.contacts_group) {
            messageAdapter.setContact(item.getTitle().toString());
        } else if (id == R.id.add_contact) {
            Intent intent = new Intent(this, NewContactActivity.class);
            startActivityForResult(intent, NEW_CONTACT_REQUEST_CODE);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_CONTACT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String newContact = data.getStringExtra(NEW_CONTACT_IDENTITY);
            if (database.contactDao().countIdentity(newContact) > 0)
                return;
            database.contactDao().insertAll(new Contact(newContact));
            ArrayList<String> args = new ArrayList<String>();
            args.add(newContact);
            Intent intent = new Intent("com.distnet.gstark31897.distnet.ACTIVITY");
            intent.putExtra("type","add_contact");
            intent.putExtra("args", args);
            sendBroadcast(intent);
            navigationView.getMenu().add(R.id.contacts_group, 0, 0, newContact);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
