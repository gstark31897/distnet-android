package com.distnet.gstark31897.distnet;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "interfaces")
public class Interface {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "uri")
    private String uri;

    @ColumnInfo(name = "permanent")
    private boolean permanent;

    public Interface(String uri, boolean permanent) {
        this.uri = uri;
        this.permanent = permanent;
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

    public boolean getPermanent() {
        return permanent;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }
}