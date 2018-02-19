package com.distnet.gstark31897.distnet;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM messages")
    List<Message> getAll();

    @Query("SELECT COUNT(id) FROM messages WHERE contact = :contact")
    int countMessages(String contact);

    @Query("SELECT * FROM messages WHERE contact = :contact ORDER BY time DESC LIMIT 1 OFFSET :i")
    Message getMessage(String contact, int i);

    @Insert
    void insertAll(Message... messages);

    @Delete
    void delete(Message message);
}