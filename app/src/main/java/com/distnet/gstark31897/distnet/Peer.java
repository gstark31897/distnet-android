package com.distnet.gstark31897.distnet;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "peers")
public class Peer {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "uri")
    private String uri;


    public Peer(String uri) {
        this.uri = uri;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getUri()
    {
        return uri;
    }

    public void setUri(String uri)
    {
        this.uri = uri;
    }
}