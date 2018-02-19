package com.distnet.gstark31897.distnet;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "messages")
public class Message {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "contact")
    private String contact;

    @ColumnInfo(name = "outgoing")
    private boolean outgoing;

    @ColumnInfo(name = "body")
    private String body;

    @ColumnInfo(name = "time")
    private long time;

    @Ignore
    public Message(String contact, boolean outgoing, String body) {
        this(contact, outgoing, body, System.currentTimeMillis());
    }

    public Message(String contact, boolean outgoing, String body, long time) {
        this.contact = contact;
        this.outgoing = outgoing;
        this.body = body;
        this.time = time;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getContact()
    {
        return contact;
    }

    public void setContact(String contact)
    {
        this.contact = contact;
    }

    public boolean getOutgoing()
    {
        return outgoing;
    }

    public void setOutgoing(boolean outgoing)
    {
        this.outgoing = outgoing;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long time)
    {
        this.time = time;
    }
}
