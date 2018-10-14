package com.mycompany.android.imageclassifier.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.mycompany.android.imageclassifier.database.AppDatabase;
import com.mycompany.android.imageclassifier.database.ImageEntry;

public class AddClassifierViewModel extends ViewModel {

    private LiveData<ImageEntry> imageClassifier;

    public AddClassifierViewModel(AppDatabase database, int classifierId) {
        imageClassifier = database.imageClassifierDao().loadClassifierById(classifierId);
    }

    public LiveData<ImageEntry> getImageClassifier() {
        return imageClassifier;
    }
}
