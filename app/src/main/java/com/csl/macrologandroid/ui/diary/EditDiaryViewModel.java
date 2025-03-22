package com.csl.macrologandroid.ui.diary;

import androidx.lifecycle.ViewModel;

import com.csl.macrologandroid.models.Meal;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class EditDiaryViewModel extends ViewModel {

    @Setter
    private Date selectedDate;
    @Setter
    private Meal selectedMeal;

}
