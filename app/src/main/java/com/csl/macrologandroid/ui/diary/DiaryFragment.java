package com.csl.macrologandroid.ui.diary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.csl.macrologandroid.ActivityActivity;
import com.csl.macrologandroid.EditDiaryActivity;
import com.csl.macrologandroid.R;
import com.csl.macrologandroid.databinding.FragmentDiaryBinding;
import com.csl.macrologandroid.models.Activity;
import com.csl.macrologandroid.models.LogEntry;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.models.UserSettings;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DiaryFragment extends Fragment {

    private DiaryViewModel diaryViewModel;
    private FragmentDiaryBinding binding;
    private int goalProtein;
    private int goalFat;
    private int goalCarbs;
    private int goalCalories;

    private SimpleDateFormat simpleDateFormat;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDiaryBinding.inflate(inflater, container, false);
        diaryViewModel = new ViewModelProvider(this).get(DiaryViewModel.class);
        createDateSelectView();

        binding.diaryEntriesLayout.breakfastCard.mealName.setText(R.string.breakfast);
        binding.diaryEntriesLayout.lunchCard.mealName.setText(R.string.lunch);
        binding.diaryEntriesLayout.dinnerCard.mealName.setText(R.string.dinner);
        binding.diaryEntriesLayout.snacksCard.mealName.setText(R.string.snacks);

        binding.diaryEntriesLayout.breakfastCard.edit.setOnClickListener((args) -> startEditMeal(Meal.BREAKFAST));
        binding.diaryEntriesLayout.lunchCard.edit.setOnClickListener((args) -> startEditMeal(Meal.LUNCH));
        binding.diaryEntriesLayout.dinnerCard.edit.setOnClickListener((args) -> startEditMeal(Meal.DINNER));
        binding.diaryEntriesLayout.snacksCard.edit.setOnClickListener((args) -> startEditMeal(Meal.SNACKS));

        binding.diaryEntriesLayout.syncActivities.setOnClickListener(v -> diaryViewModel.syncActivities());
        binding.diaryEntriesLayout.editActivities.setOnClickListener(v -> startEditActivity());
        return binding.getRoot();
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
    public void onResume() {
        super.onResume();
        diaryViewModel.loadCurrentDate();
    }

    private void createDateSelectView() {
        binding.arrowLeft.setOnClickListener(args -> {
            diaryViewModel.loadPreviousDate();
            setDateText();
        });
        binding.arrowRight.setOnClickListener(args -> {
            diaryViewModel.loadNextDate();
            setDateText();
        });
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        setDateText();
    }

    private void setDateText() {
       binding.diaryDate.setText(simpleDateFormat.format(diaryViewModel.getSelectedDate()));
    }

    private void updateLogEntries() {
        fillCard(binding.diaryEntriesLayout.breakfastCard.entriesLayout, diaryViewModel.getBreakfastEntries());
        fillCard(binding.diaryEntriesLayout.lunchCard.entriesLayout, diaryViewModel.getLunchEntries());
        fillCard(binding.diaryEntriesLayout.dinnerCard.entriesLayout, diaryViewModel.getDinnerEntries());
        fillCard(binding.diaryEntriesLayout.snacksCard.entriesLayout, diaryViewModel.getSnacksEntries());
    }

    private void updateActivities(final List<Activity> activities) {
        binding.diaryEntriesLayout.activitiesLayout.removeAllViews();
        if (!activities.isEmpty()) {
            getLayoutInflater().inflate(R.layout.layout_activity_card_header, binding.diaryEntriesLayout.activitiesLayout);
            for (var activity : activities) {
                addActivityToTable(binding.diaryEntriesLayout.activitiesLayout, activity);
            }
        } else {
            final var hint = new TextView(requireContext());
            hint.setText(R.string.activity_done);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 8, 0, 8);
            binding.diaryEntriesLayout.activitiesLayout.addView(hint, lp);
        }
    }

    private void fillCard(final LinearLayout cardEntriesLayout, final List<LogEntry> entries) {
        cardEntriesLayout.removeAllViews();
        if (!entries.isEmpty()) {
            getLayoutInflater().inflate(R.layout.layout_entry_card_header, cardEntriesLayout);
            for (var entry : entries) {
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
        final var intent = new Intent(getActivity(), EditDiaryActivity.class);
        intent.putExtra("DATE", diaryViewModel.getSelectedDate());
        intent.putExtra("MEAL", meal);
        startActivity(intent);
    }

    private void startEditActivity() {
        final var intent = new Intent(getActivity(), ActivityActivity.class);
        intent.putExtra("DATE", diaryViewModel.getSelectedDate());
        startActivity(intent);
    }

    private void setGoalIntake(final UserSettings settings) {
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
        binding.totalProtein.setText(String.valueOf(Math.round(diaryViewModel.getTotalProtein() * 10) / 10f));
        binding.totalFat.setText(String.valueOf(Math.round(diaryViewModel.getTotalFat() * 10) / 10f));
        binding.totalCarbs.setText(String.valueOf(Math.round(diaryViewModel.getTotalCarbs() * 10) / 10f));
        binding.totalCalories.setText(String.format(Locale.getDefault(), "%d/%d", diaryViewModel.getTotalCalories(), goalCalories));
        setProgress();
    }

    private void setMaxProtein(final int goalProtein) {
        binding.progressRingProtein.setMax(goalProtein);
        binding.outerProgressRingProtein.setMax(goalProtein);
    }

    private void setMaxFat(final int goalFat) {
        binding.progressRingFat.setMax(goalFat);
        binding.outerProgressRingFat.setMax(goalFat);
    }

    private void setMaxCarbs(final int goalCarbs) {
        binding.progressRingCarbs.setMax(goalCarbs);
        binding.outerProgressRingCarbs.setMax(goalCarbs);
    }

    private void setProgress() {
        setProgressMacro(diaryViewModel.getTotalProtein(), goalProtein, binding.progressRingProtein, binding.outerProgressRingProtein);
        setProgressMacro(diaryViewModel.getTotalFat(), goalFat, binding.progressRingFat, binding.outerProgressRingFat);
        setProgressMacro(diaryViewModel.getTotalCarbs(), goalCarbs, binding.progressRingCarbs, binding.outerProgressRingCarbs);
    }

    private void setProgressMacro(final double macro, final double goal, final ProgressBar progressBar, final ProgressBar surplusBar) {
        progressBar.setProgress((int) Math.round(macro));
        if (macro < goal) {
            surplusBar.setVisibility(View.INVISIBLE);
        } else {
            surplusBar.setVisibility(View.VISIBLE);
            surplusBar.setProgress((int) Math.round(macro - goal));
        }
    }

    private void addEntryToTable(final LinearLayout layout, final LogEntry entry) {
        final var row = getLayoutInflater().inflate(R.layout.layout_entry_card_row, layout, false);
        ((TextView) row.findViewById(R.id.food_name)).setText(entry.getFood().getName());
        ((TextView) row.findViewById(R.id.food_protein)).setText(String.format(Locale.ENGLISH, "%.1f", entry.getMacros().getProtein()));
        ((TextView) row.findViewById(R.id.food_fat)).setText(String.format(Locale.ENGLISH, "%.1f", entry.getMacros().getFat()));
        ((TextView) row.findViewById(R.id.food_carbs)).setText(String.format(Locale.ENGLISH, "%.1f", entry.getMacros().getCarbs()));
        ((TextView) row.findViewById(R.id.food_kcal)).setText(String.format(Locale.ENGLISH, "%1.0f", entry.getMacros().getCalories()));
        layout.addView(row);
    }

    private void addActivityToTable(final LinearLayout layout, final Activity activity) {
        final var row = getLayoutInflater().inflate(R.layout.layout_activity_card_row, layout, false);
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
