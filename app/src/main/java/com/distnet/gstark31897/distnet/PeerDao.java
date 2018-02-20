package com.distnet.gstark31897.distnet;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PeerDao {
    @Query("SELECT * FROM peers")
    List<Peer> getAll();

    @Query("SELECT COUNT(id) FROM peers")
    int count();

    @Query("SELECT * FROM peers LIMIT 1 OFFSET :i")
    Peer get(int i);

    @Insert
    void insertAll(Peer... peers);

    @Delete
    void delete(Peer... peers);
}