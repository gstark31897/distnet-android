package com.distnet.gstark31897.distnet;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "messages")
public class Message {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "contact")
    private String contact;

    @ColumnInfo(name = "outgoing")
    private boolean outgoing;

    @ColumnInfo(name = "pos")
    private int pos;

    @ColumnInfo(name = "body")
    private String body;


    public Message(String contact, boolean outgoing, int pos, String body) {
        this.contact = contact;
        this.outgoing = outgoing;
        this.pos = pos;
        this.body = body;
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

    public int getPos()
    {
        return pos;
    }

    public void setPos(int pos)
    {
        this.pos = pos;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }
}
