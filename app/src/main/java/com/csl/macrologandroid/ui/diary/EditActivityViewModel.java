package com.csl.macrologandroid.ui.diary;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.models.Activity;

import java.util.List;

import lombok.Getter;

@Getter
public class EditActivityViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Activity>> mActivities = new MutableLiveData<>();
    public EditActivityViewModel(@NonNull Application application) {
        super(application);
    }
}
