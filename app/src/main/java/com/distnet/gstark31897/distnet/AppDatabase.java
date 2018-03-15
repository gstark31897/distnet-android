package com.distnet.gstark31897.distnet;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Contact.class, Interface.class, Peer.class, Message.class}, version = 8)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ContactDao contactDao();
    public abstract InterfaceDao interfaceDao();
    public abstract PeerDao peerDao();
    public abstract MessageDao messageDao();
}