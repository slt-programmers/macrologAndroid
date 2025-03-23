package com.csl.macrologandroid.ui.diary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.csl.macrologandroid.ActivityActivity;
import com.csl.macrologandroid.EditEntryActivity;
import com.csl.macrologandroid.R;
import com.csl.macrologandroid.databinding.FragmentDiaryBinding;
import com.csl.macrologandroid.dtos.ActivityResponse;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.dtos.UserSettingsResponse;
import com.csl.macrologandroid.models.Meal;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DiaryFragment extends Fragment {

    private DiaryViewModel diaryViewModel;
    private View root;
    private TextView diaryDate;
    private LinearLayout breakfastLayout;
    private LinearLayout lunchLayout;
    private LinearLayout dinnerLayout;
    private LinearLayout snacksLayout;
    private LinearLayout activitiesLayout;
    private int goalProtein;
    private int goalFat;
    private int goalCarbs;
    private int goalCalories;

    private SimpleDateFormat simpleDateFormat;

    public DiaryFragment() {
        // Non arg constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final var binding = FragmentDiaryBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        diaryViewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(DiaryViewModel.initializer)).get(DiaryViewModel.class);

        createDateSelectView();

        final var logEntriesLayout = root.findViewById(R.id.diary_entries_layout);
        final var breakfastCard = logEntriesLayout.findViewById(R.id.breakfast_card);
        final var lunchCard = logEntriesLayout.findViewById(R.id.lunch_card);
        final var dinnerCard = logEntriesLayout.findViewById(R.id.dinner_card);
        final var snacksCard = logEntriesLayout.findViewById(R.id.snacks_card);
        ((TextView) breakfastCard.findViewById(R.id.meal_name)).setText(R.string.breakfast);
        ((TextView) lunchCard.findViewById(R.id.meal_name)).setText(R.string.lunch);
        ((TextView) dinnerCard.findViewById(R.id.meal_name)).setText(R.string.dinner);
        ((TextView) snacksCard.findViewById(R.id.meal_name)).setText(R.string.snacks);
        breakfastCard.findViewById(R.id.edit).setOnClickListener((args) -> startEditMeal(Meal.BREAKFAST));
        lunchCard.findViewById(R.id.edit).setOnClickListener((args) -> startEditMeal(Meal.LUNCH));
        dinnerCard.findViewById(R.id.edit).setOnClickListener((args) -> startEditMeal(Meal.DINNER));
        snacksCard.findViewById(R.id.edit).setOnClickListener((args) -> startEditMeal(Meal.SNACKS));
        breakfastLayout = breakfastCard.findViewById(R.id.entries_layout);
        lunchLayout = lunchCard.findViewById(R.id.entries_layout);
        dinnerLayout = dinnerCard.findViewById(R.id.entries_layout);
        snacksLayout = snacksCard.findViewById(R.id.entries_layout);
        activitiesLayout = logEntriesLayout.findViewById(R.id.activities_layout);
        logEntriesLayout.findViewById(R.id.sync_activities).setOnClickListener(v ->
                diaryViewModel.syncActivities());
//        logEntriesLayout.findViewById(R.id.edit_activity).setOnClickListener(v ->
//                startEditActivity());
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        diaryViewModel.getMUserSettings().observe(getViewLifecycleOwner(), this::setGoalIntake);
        diaryViewModel.getMLogEntries().observe(getViewLifecycleOwner(), (logEntries) -> {
            updateTotals();
            updateLogEntries();
        });
        diaryViewModel.getMActivities().observe(getViewLifecycleOwner(), this::updateActivities);
    }

    @Override
    public void onDestroy() {
        diaryViewModel.disposeAll();
        super.onDestroy();
    }

    private void createDateSelectView() {
        final var arrowLeft = (ImageView) root.findViewById(R.id.arrow_left);
        final var arrowRight = (ImageView) root.findViewById(R.id.arrow_right);
        arrowLeft.setOnClickListener(args -> {
            diaryViewModel.loadPreviousDate();
            setDateText();
        });
        arrowRight.setOnClickListener(args -> {
            diaryViewModel.loadNextDate();
            setDateText();
        });
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        diaryDate = root.findViewById(R.id.diary_date);
        setDateText();
    }

    private void setDateText() {
        diaryDate.setText(simpleDateFormat.format(diaryViewModel.getSelectedDate()));
    }

    private void updateLogEntries() {
        fillCard(breakfastLayout, diaryViewModel.getBreakfastEntries());
        fillCard(lunchLayout, diaryViewModel.getLunchEntries());
        fillCard(dinnerLayout, diaryViewModel.getDinnerEntries());
        fillCard(snacksLayout, diaryViewModel.getSnacksEntries());
    }

    private void updateActivities(final List<ActivityResponse> activities) {
        activitiesLayout.removeAllViews();
        if (!activities.isEmpty()) {
            addActivityCardHeader(activitiesLayout);
            for (var activity : activities) {
                addActivityToTable(activitiesLayout, activity);
            }
        } else {
            final var hint = new TextView(requireContext());
            hint.setText(R.string.activity_done);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 8, 0, 8);
            activitiesLayout.addView(hint, lp);
        }
    }

    private void fillCard(final LinearLayout cardEntriesLayout, final List<LogEntryResponse> entries) {
        cardEntriesLayout.removeAllViews();
        if (!entries.isEmpty()) {
            addEntryCardHeader(cardEntriesLayout);
            for (LogEntryResponse entry : entries) {
                addEntryToTable(cardEntriesLayout, entry);
            }
        } else {
            final var hint = new TextView(requireContext());
            hint.setText(R.string.eaten);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 8, 0, 8);
            cardEntriesLayout.addView(hint, lp);
        }
    }

    private void startEditMeal(Meal meal) {
        final var intent = new Intent(getActivity(), EditEntryActivity.class);
        intent.putExtra("DATE", diaryViewModel.getSelectedDate());
        intent.putExtra("MEAL", meal);
        startActivity(intent);
    }

    private void startEditActivity() {
        final var intent = new Intent(getActivity(), ActivityActivity.class);
        intent.putExtra("DATE", diaryViewModel.getSelectedDate());
        startActivity(intent);
    }

    private void setGoalIntake(final UserSettingsResponse settings) {
        goalProtein = settings.getGoalProtein();
        setMaxProtein(settings.getGoalProtein());
        goalFat = settings.getGoalFat();
        setMaxFat(settings.getGoalFat());
        goalCarbs = settings.getGoalCarbs();
        setMaxCarbs(settings.getGoalCarbs());
        goalCalories = (goalProtein * 4) + (goalFat * 9) + (goalCarbs * 4);
    }

    private void openLink(final String activityId) {
        final var intentUri = Uri.parse("https://www.strava.com/activities/")
                .buildUpon()
                .appendPath(activityId)
                .build();
        final var browserIntent = new Intent(Intent.ACTION_VIEW, intentUri);
        startActivity(browserIntent);
    }

    private void updateTotals() {
        final var totalProteinView = (TextView) root.findViewById(R.id.total_protein);
        totalProteinView.setText(String.valueOf(Math.round(diaryViewModel.getTotalProtein() * 10) / 10f));
        final var totalFatView = (TextView) root.findViewById(R.id.total_fat);
        totalFatView.setText(String.valueOf(Math.round(diaryViewModel.getTotalFat() * 10) / 10f));
        final var totalCarbsView = (TextView) root.findViewById(R.id.total_carbs);
        totalCarbsView.setText(String.valueOf(Math.round(diaryViewModel.getTotalCarbs() * 10) / 10f));
        final var totalCaloriesView = (TextView) root.findViewById(R.id.total_calories);
        totalCaloriesView.setText(String.format(Locale.getDefault(), "%d/%d", diaryViewModel.getTotalCalories(), goalCalories));

        setProgress();
    }

    private void setMaxProtein(final int goalProtein) {
        final var progressProtein = (ProgressBar) root.findViewById(R.id.progress_ring_protein);
        final var surplusProtein = (ProgressBar) root.findViewById(R.id.outer_progress_ring_protein);
        progressProtein.setMax(goalProtein);
        surplusProtein.setMax(goalProtein);
    }

    private void setMaxFat(final int goalFat) {
        final var progressFat = (ProgressBar) root.findViewById(R.id.progress_ring_fat);
        final var surplusFat = (ProgressBar) root.findViewById(R.id.outer_progress_ring_fat);
        progressFat.setMax(goalFat);
        surplusFat.setMax(goalFat);
    }

    private void setMaxCarbs(final int goalCarbs) {
        final var progressCarbs = (ProgressBar) root.findViewById(R.id.progress_ring_carbs);
        final var surplusCarbs = (ProgressBar) root.findViewById(R.id.outer_progress_ring_carbs);
        progressCarbs.setMax(goalCarbs);
        surplusCarbs.setMax(goalCarbs);
    }

    private void setProgress() {
        setProgressMacro(diaryViewModel.getTotalProtein(), goalProtein, R.id.progress_ring_protein, R.id.outer_progress_ring_protein);
        setProgressMacro(diaryViewModel.getTotalFat(), goalFat, R.id.progress_ring_fat, R.id.outer_progress_ring_fat);
        setProgressMacro(diaryViewModel.getTotalCarbs(), goalCarbs, R.id.progress_ring_carbs, R.id.outer_progress_ring_carbs);
    }

    private void setProgressMacro(final double macro, final double goal, final int ringId, final int outerRingId) {
        final var progressCarbs = (ProgressBar) root.findViewById(ringId);
        final var surplusCarbs = (ProgressBar) root.findViewById(outerRingId);
        progressCarbs.setProgress((int) Math.round(macro));
        if (macro < goal) {
            surplusCarbs.setVisibility(View.INVISIBLE);
        } else {
            surplusCarbs.setVisibility(View.VISIBLE);
            surplusCarbs.setProgress((int) Math.round(macro - goal));
        }
    }

    private void addEntryCardHeader(final LinearLayout layout) {
        final var header = getLayoutInflater().inflate(R.layout.layout_entry_card_header, null);
        layout.addView(header);
    }

    private void addActivityCardHeader(final LinearLayout layout) {
        final var header = getLayoutInflater().inflate(R.layout.layout_activity_card_header, null);
        layout.addView(header);
    }

    private void addEntryToTable(final LinearLayout layout, final LogEntryResponse entry) {
        final var row = getLayoutInflater().inflate(R.layout.layout_entry_card_row, null);
        ((TextView) row.findViewById(R.id.food_name)).setText(entry.getFood().getName());
        ((TextView) row.findViewById(R.id.food_protein)).setText(String.format(Locale.ENGLISH, "%.1f", entry.getMacrosCalculated().getProtein()));
        ((TextView) row.findViewById(R.id.food_fat)).setText(String.format(Locale.ENGLISH, "%.1f", entry.getMacrosCalculated().getFat()));
        ((TextView) row.findViewById(R.id.food_carbs)).setText(String.format(Locale.ENGLISH, "%.1f", entry.getMacrosCalculated().getCarbs()));
        ((TextView) row.findViewById(R.id.food_kcal)).setText(String.format(Locale.ENGLISH, "%1.0f", entry.getMacrosCalculated().getCalories()));
        layout.addView(row);
    }

    private void addActivityToTable(final LinearLayout layout, final ActivityResponse activity) {
        final var row = getLayoutInflater().inflate(R.layout.layout_activity_card_row, null);
        ((TextView) row.findViewById(R.id.activity_name)).setText(activity.getName());
        final var link = row.findViewById(R.id.activity_link);
        if (activity.getSyncedId() != null) {
            link.setOnClickListener((v) -> openLink(String.valueOf(activity.getSyncedId())));
        } else {
            link.setVisibility(View.INVISIBLE);
        }
        ((TextView) row.findViewById(R.id.activity_kcal)).setText(String.valueOf(activity.getCalories()));
        layout.addView(row);
    }

}
