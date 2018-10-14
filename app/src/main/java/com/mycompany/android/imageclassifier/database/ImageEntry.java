package com.mycompany.android.imageclassifier.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.sql.Blob;
import java.util.Date;

/**
 * Created by delaroy on 9/4/18.
 */

@Entity(tableName = "imageentry")
public class ImageEntry {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "predclass")
    private String predclass;

    @ColumnInfo(name = "melanoma")
    private String melanoma;

    @ColumnInfo(name = "nevus")
    private String nevus;

    @ColumnInfo(name = "seborrheic")
    private String seborrheic;

    @ColumnInfo(name = "image")
    private String image;

    @Ignore
    public ImageEntry(String predclass, String melanoma, String nevus, String seborrheic, String image) {
        this.predclass = predclass;
        this.melanoma = melanoma;
        this.nevus = nevus;
        this.seborrheic = seborrheic;
        this.image = image;
    }

    public ImageEntry(int id, String predclass, String melanoma, String nevus, String seborrheic, String image) {
        this.id = id;
        this.predclass = predclass;
        this.melanoma = melanoma;
        this.nevus = nevus;
        this.seborrheic = seborrheic;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPredclass() {
        return predclass;
    }

    public void setPredclass(String predclass) {
        this.predclass = predclass;
    }

    public String getMelanoma() {
        return melanoma;
    }

    public void setMelanoma(String melanoma) {
        this.melanoma = melanoma;
    }

    public String getNevus() {
        return nevus;
    }

    public void setNevus(String nevus) {
        this.nevus = nevus;
    }

    public void setSeborrheic(String seborrheic){ this.seborrheic = seborrheic; }

    public String getSeborrheic() { return seborrheic; }

    public void setImage(String image) { this.image = image; }

    public String getImage() { return image; }

}
