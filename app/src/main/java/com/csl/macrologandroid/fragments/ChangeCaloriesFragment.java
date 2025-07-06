package com.csl.macrologandroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.csl.macrologandroid.R;
import com.csl.macrologandroid.dtos.UserSettingsResponse;
import com.csl.macrologandroid.models.ChangeGoalMacros;
import com.csl.macrologandroid.models.Gender;

import org.jetbrains.annotations.NotNull;

public class ChangeCaloriesFragment extends Fragment implements ChangeGoalMacros {

    private TextView caloriesView;
    private TextView carbsView;

    private UserSettingsResponse userSettings;

    private double goalCalories;
    private double goalProtein;
    private double goalFat;
    private double goalCarbs;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.layout_change_calories, container, false);

        if (getArguments() != null) {
            userSettings = (UserSettingsResponse) getArguments().getSerializable("userSettings");
        }
        goalCalories = calculateCalories();
        goalProtein = userSettings.getCurrentWeight() * 1.8;
        goalFat = userSettings.getCurrentWeight() * 0.8;
        goalCarbs = calculateCarbs();

        caloriesView = view.findViewById(R.id.calories_output);
        caloriesView.setText(String.valueOf(Math.round(goalCalories)));

        TextView proteinView = view.findViewById(R.id.protein_output);
        proteinView.setText(String.valueOf(Math.round(goalProtein)));

        TextView fatView = view.findViewById(R.id.fat_output);
        fatView.setText(String.valueOf(Math.round(goalFat)));

        carbsView = view.findViewById(R.id.carbs_output);
        carbsView.setText(String.valueOf(Math.round(goalCarbs)));

        SeekBar slider = view.findViewById(R.id.slider);
        slider.setMax(4000 - 1200);
        slider.setProgress((int) goalCalories - 1200);

        slider.setOnSeekBarChangeListener(seekBarChangeListener);

        return view;
    }

    private double calculateCarbs() {
        return (goalCalories - (goalProtein * 4) - (goalFat * 9)) / 4;
    }

    private double calculateCalories() {
        double bmr;
        if (userSettings.getGender() == Gender.MALE) {
            bmr = (10 *userSettings.getCurrentWeight()) + (6.25 * userSettings.getHeight()) - (5 * userSettings.getAge()) + 5;
        } else {
            bmr = (10 * userSettings.getCurrentWeight()) + (6.25 * userSettings.getHeight()) - (5 * userSettings.getAge()) - 161;
        }
        return bmr * userSettings.getActivity();
    }

    private final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            goalCalories = (double) progress + 1200;
            caloriesView.setText(String.valueOf(Math.round(goalCalories)));
            goalCarbs = calculateCarbs();
            carbsView.setText(String.valueOf(Math.round(goalCarbs)));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Not needed
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // Not needed
        }
    };

    @Override
    public Bundle getGoalMacros() {
        Bundle bundle = new Bundle();
        bundle.putInt("goalProtein", Integer.parseInt(String.valueOf(Math.round(goalProtein))));
        bundle.putInt("goalFat", Integer.parseInt(String.valueOf(Math.round(goalFat))));
        bundle.putInt("goalCarbs", Integer.parseInt(String.valueOf(Math.round(goalCarbs))));
        return bundle;
    }

}
