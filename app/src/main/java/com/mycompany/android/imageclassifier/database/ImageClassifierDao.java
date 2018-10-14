package com.mycompany.android.imageclassifier.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by delaroy on 9/4/18.
 */

@Dao
public interface ImageClassifierDao {

    @Query("SELECT * FROM imageentry")
    LiveData<List<ImageEntry>> loadAllClassifier();

    @Insert
    void insertClassifier(ImageEntry imageEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateClassifier(ImageEntry imageEntry);

    @Delete
    void deleteClassifier(ImageEntry imageEntry);

    @Query("SELECT * FROM imageentry WHERE id = :id")
    LiveData<ImageEntry> loadClassifierById(int id);
}
