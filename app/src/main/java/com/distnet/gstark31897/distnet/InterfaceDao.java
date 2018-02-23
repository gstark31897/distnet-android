package com.distnet.gstark31897.distnet;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface InterfaceDao {
    @Query("SELECT * FROM interfaces")
    List<Interface> getAll();

    @Query("SELECT COUNT(id) FROM interfaces")
    int count();

    @Query("SELECT * FROM interfaces LIMIT 1 OFFSET :i")
    Interface get(int i);

    @Query("DELETE FROM interfaces WHERE uri == :uri")
    void remove(String uri);

    @Query("DELETE FROM interfaces WHERE permanent == 0")
    void deleteTemporary();

    @Insert
    void insertAll(Interface... interfaces);

    @Delete
    void delete(Interface... interfaces);
}