package com.distnet.gstark31897.distnet;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String NEW_CONTACT_IDENTITY = "com.example.myfirstapp.NEW_CONTACT_IDENTITY";
    public static final String NEW_CONTACT_NICKNAME = "com.example.myfirstapp.NEW_CONTACT_NICKNAME";
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

    String identity;
    SharedPreferences settings;

    IntentFilter filter;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceIntent = new Intent(this, NodeService.class);
        this.startService(serviceIntent);

        setTitle(R.string.app_name);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database")
                .allowMainThreadQueries().fallbackToDestructiveMigration().build();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        for (Contact contact: database.contactDao().getAll()) {
            addContactItem(contact.getIdentity(), contact.getNickname());
            makeIntent("discover", contact.getIdentity());
        }

        messageAdapter = new MessageAdapter(database);
        messageRecycler = (RecyclerView) findViewById(R.id.message_recycler);
        messageRecycler.setAdapter(messageAdapter);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(true);
        messageRecycler.setLayoutManager(layoutManager);
        messageRecycler.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                messageRecycler.scrollToPosition(0);
            }
        });

        messageEdit = (EditText) findViewById(R.id.message_edit);
        sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messageEdit.getText().toString().length() == 0)
                    return;

                makeIntent("send_msg", messageAdapter.getContact(), messageEdit.getText().toString());

                messageEdit.setText("");
            }
        });

        settings = getSharedPreferences("distnet", 0);
        String currentContact = settings.getString("currentContact", "");
        if (currentContact.length() != 0) {
            switchContact(currentContact);
        }

        filter = new IntentFilter("com.distnet.gstark31897.distnet.SERVICE");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String type =  intent.getExtras().getString("type");
                System.out.println("got the intent: " + type);

                if (type.equals("msg_update")) {
                    messageAdapter.notifyDataSetChanged();
                    messageRecycler.scrollToPosition(0);
                }
            }
        };
        registerReceiver(receiver, filter);
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
        } else if (id == R.id.action_remove_contact) {
            database.contactDao().remove(messageAdapter.getContact());
            database.messageDao().remove(messageAdapter.getContact());
            switchContact("");
            recreate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        int gid = item.getGroupId();

        if (gid == R.id.contacts_group) {
            System.out.println(item.getContentDescription());
            switchContact(item.getContentDescription().toString());
        } else if (id == R.id.add_contact) {
            Intent intent = new Intent(this, NewContactActivity.class);
            startActivityForResult(intent, NEW_CONTACT_REQUEST_CODE);
        } else if (id == R.id.share_contact) {
            Intent intent = new Intent(this, ShareContactActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void switchContact(String identity) {
        Contact contact = database.contactDao().getContact(identity);
        if (contact == null)
            return;
        setTitle(contact.getNickname());
        messageAdapter.setContact(identity);

        makeIntent("discover", identity);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("currentContact", identity);
        editor.apply();
    }

    public void addContactItem(String identity, String nickname) {
        navigationView.getMenu().add(R.id.contacts_group, 0, 0, nickname).setContentDescription(identity);
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_CONTACT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String newIdentity = data.getStringExtra(NEW_CONTACT_IDENTITY);
            String newNickname = data.getStringExtra(NEW_CONTACT_NICKNAME);
            if (database.contactDao().countIdentity(newIdentity) > 0)
                return;
            database.contactDao().insertAll(new Contact(newIdentity, newNickname));

            makeIntent("add_contact", newIdentity);
            addContactItem(newIdentity, newNickname);
            makeIntent("discover", newIdentity);

            switchContact(newIdentity);
        } else if (requestCode == SETTINGS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            identity = settings.getString("identity", "");
            makeIntent("set_identity", identity);
        }
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
    public void onResume() {
        super.onResume();
        makeIntent("main_activity", "open");
    }

    @Override
    public void onPause() {
        super.onPause();
        makeIntent("main_activity", "close");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        makeIntent("main_activity", "close");
        unregisterReceiver(receiver);
    }
}
