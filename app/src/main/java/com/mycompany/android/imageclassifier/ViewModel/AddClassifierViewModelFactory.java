package com.mycompany.android.imageclassifier.ViewModel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.mycompany.android.imageclassifier.database.AppDatabase;

public class AddClassifierViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final int mClassifierId;

    public AddClassifierViewModelFactory(AppDatabase database, int classifierId) {
        mDb = database;
        mClassifierId = classifierId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AddClassifierViewModel(mDb, mClassifierId);
    }
}
