package com.distnet.gstark31897.distnet;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface ContactDao {
    @Query("SELECT * FROM contacts")
    List<Contact> getAll();

    @Query("SELECT COUNT(id) FROM contacts")
    int count();

    @Query("SELECT COUNT(id) FROM contacts where identity = :identity")
    int countIdentity(String identity);

    @Query("SELECT * FROM contacts WHERE identity = :identity")
    Contact getContact(String identity);

    @Query("DELETE FROM contacts WHERE identity = :identity")
    void remove(String identity);

    @Insert
    void insertAll(Contact... contacts);

    @Delete
    void delete(Contact... contacts);
}