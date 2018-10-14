package com.mycompany.android.imageclassifier.ViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.mycompany.android.imageclassifier.database.AppDatabase;
import com.mycompany.android.imageclassifier.database.ImageEntry;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<ImageEntry>> imageClassifier;

    public MainViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        imageClassifier = database.imageClassifierDao().loadAllClassifier();
    }

    public LiveData<List<ImageEntry>> getImageClassifier() {
        return imageClassifier;
    }
}
