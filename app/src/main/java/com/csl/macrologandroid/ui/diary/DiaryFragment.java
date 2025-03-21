package com.csl.macrologandroid.ui.diary;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.csl.macrologandroid.EditEntryActivity;
import com.csl.macrologandroid.R;
import com.csl.macrologandroid.databinding.FragmentDiaryBinding;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.dtos.MacrosResponse;
import com.csl.macrologandroid.dtos.UserSettingsResponse;
import com.csl.macrologandroid.fragments.DateDialogFragment;
import com.csl.macrologandroid.models.Meal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private int goalProtein;
    private int goalFat;
    private int goalCarbs;
    private int goalCalories;

    private SimpleDateFormat simpleDateFormat;

    private List<LogEntryResponse> logEntries;

    private final ActivityResultLauncher<Intent> editEntriesForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                }
            });

    private final ActivityResultLauncher<Intent> editActivitiesForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
//                    activityCache.removeFromCache(selectedDate);
                }
            });

    public DiaryFragment() {
        // Non arg constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        diaryViewModel = new ViewModelProvider(this).get(DiaryViewModel.class);
        final var binding = FragmentDiaryBinding.inflate(inflater, container, false);
        root = binding.getRoot();

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

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        diaryViewModel.getMUserSettings().observe(getViewLifecycleOwner(), this::setGoalIntake);
        diaryViewModel.getMLogEntries().observe(getViewLifecycleOwner(), (logEntries) -> {
            // TODO remove state
            this.logEntries = logEntries;
            updateTotals();
            updateLogEntries();
        });
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
            diaryViewModel.loadPreviousLogEntries();
            setDateText();
        });
        arrowRight.setOnClickListener(args -> {
            diaryViewModel.loadNextLogEntries();
            setDateText();
        });
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        diaryDate = root.findViewById(R.id.diary_date);
        diaryDate.setOnClickListener(v -> showDateDialog());
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

    private void fillCard(LinearLayout layout, List<LogEntryResponse> entries) {
        layout.removeAllViews();
        if (!entries.isEmpty()) {
            addEntryCardHeader(layout);
            for (LogEntryResponse entry : entries) {
                addEntryToTable(layout, entry);
            }
        } else {
            TextView hint = new TextView(requireContext());
            hint.setText(R.string.eaten);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 8, 0, 8);
            layout.addView(hint, lp);
        }
    }

    private void startEditMeal(Meal meal) {
        Intent intent = new Intent(getActivity(), EditEntryActivity.class);
        final var filteredEntries = new ArrayList<>();
        for (LogEntryResponse entry : logEntries) {
            if (entry.getMeal().equals(meal)) {
                filteredEntries.add(entry);
            }
        }

        intent.putExtra("DATE", diaryViewModel.getSelectedDate());
        intent.putExtra("MEAL", meal);
        // TODO niet meer meergeven
        intent.putExtra("LOGENTRIES", filteredEntries);
        editEntriesForResult.launch(intent);
    }

//    private void startEditActivity() {
//        Intent intent = new Intent(getActivity(), ActivityActivity.class);
//        List<ActivityResponse> activities = activityCache.getFromCache(selectedDate);
//        intent.putExtra("DATE", selectedDate);
//        intent.putExtra("ACTIVITIES", (Serializable) activities);
//        editActivitiesForResult.launch(intent);
//    }

    private void setGoalIntake(final UserSettingsResponse settings) {
        goalProtein = settings.getGoalProtein();
        setMaxProtein(settings.getGoalProtein());
        goalFat = settings.getGoalFat();
        setMaxFat(settings.getGoalFat());
        goalCarbs = settings.getGoalCarbs();
        setMaxCarbs(settings.getGoalCarbs());
        goalCalories = (goalProtein * 4) + (goalFat * 9) + (goalCarbs * 4);
    }

    private void forceSyncActivity() {
        // TODO move to viewmodel
//        disposable = activityService.getActivitiesForDay(selectedDate).subscribe(
//                res -> {
//                    logEntryCache.clearCache();
//                    activityCache.clearCache();
//                },
//                err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))
//        );
    }

    private void openLink(String activityId) {
        Uri intentUri = Uri.parse("https://www.strava.com/activities/")
                .buildUpon()
                .appendPath(activityId)
                .build();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, intentUri);
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

    private void showDateDialog() {
        DateDialogFragment dialog = new DateDialogFragment();
        dialog.setCurrentDate(diaryViewModel.getSelectedDate());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        // TODO
//        dialog.setOnDialogResult(date -> {
//            TextView dateTextView = root.findViewById(R.id.diary_date);
//            dateTextView.setText(simpleDateFormat.format(date));
//            selectedDate = date;
//        });
//        dialog.show(requireActivity().getSupportFragmentManager(), "WeighDialogFragment");
    }

    private void addEntryCardHeader(LinearLayout layout) {
        // TODO component van maken
        final var context = requireContext();
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);

        TextView dummy = new TextView(requireContext());
        dummy.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView p = new TextView(context);
        p.setText(R.string.p);
        p.setTypeface(null, Typeface.BOLD);
        p.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TextView f = new TextView(context);
        f.setText(R.string.f);
        f.setTypeface(null, Typeface.BOLD);
        f.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TextView c = new TextView(context);
        c.setText(R.string.c);
        c.setTypeface(null, Typeface.BOLD);
        c.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TextView kcal = new TextView(context);
        kcal.setText(R.string.kcal);
        kcal.setTypeface(null, Typeface.BOLD);
        kcal.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        header.addView(dummy);
        header.addView(p, lp);
        header.addView(f, lp);
        header.addView(c, lp);
        header.addView(kcal, lp);

        layout.addView(header);
    }

    private void addEntryToTable(LinearLayout layout, LogEntryResponse entry) {
        final var context = requireContext();
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView name = getCustomizedTextView(new TextView(context));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        name.setText(entry.getFood().getName());
        name.setLayoutParams(lp);

        TextView protein = getCustomizedMacroTextView(entry.getMacrosCalculated().getProtein());
        TextView fat = getCustomizedMacroTextView(entry.getMacrosCalculated().getFat());
        TextView carbs = getCustomizedMacroTextView(entry.getMacrosCalculated().getCarbs());
        TextView kcal = getCustomizedCalorieTextView(entry.getMacrosCalculated().getCalories());

        row.addView(name);
        row.addView(protein);
        row.addView(fat);
        row.addView(carbs);
        row.addView(kcal);

        layout.addView(row);
    }

    private TextView getCustomizedTextView(TextView view) {
        final var context = requireContext();
        view.setTextSize(16);
        Typeface typeface = ResourcesCompat.getFont(context, R.font.assistant_light);
        view.setTypeface(typeface);
        return view;
    }

    private TextView getCustomizedCalorieTextView(double text) {
        final var context = requireContext();
        TextView view = new TextView(context);
        view.setText(String.format(Locale.ENGLISH, "%1.0f", text));
        setTextViewLayout(view);
        return getCustomizedTextView(view);
    }

    private TextView getCustomizedMacroTextView(double text) {
        final var context = requireContext();
        TextView view = new TextView(context);
        view.setText(String.format(Locale.ENGLISH, "%.1f", text));
        setTextViewLayout(view);
        return getCustomizedTextView(view);
    }

    private void setTextViewLayout(TextView view) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        view.setLayoutParams(lp);
        view.setGravity(Gravity.END);
    }
}
