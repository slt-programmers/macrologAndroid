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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
    private FragmentDiaryBinding binding;
    private View root;
    private TextView diaryDate;
    private ConstraintLayout logEntriesLayout;
    private int goalProtein;
    private int goalFat;
    private int goalCarbs;
    private int goalCalories;

    private SimpleDateFormat simpleDateFormat;

    private final ActivityResultLauncher<Intent> editEntriesForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
//                    logEntryCache.removeFromCache(selectedDate);
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
        binding = FragmentDiaryBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        final var layout = (ViewGroup) inflater.inflate(R.layout.layout_diary_page, container, false);
        logEntriesLayout = root.findViewById(R.id.diary_entries_layout);
        logEntriesLayout.addView(layout);

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
        setupDateSelect();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        diaryViewModel.getMUserSettings().observe(getViewLifecycleOwner(), this::setGoalIntake);
        diaryViewModel.getMLogEntries().observe(getViewLifecycleOwner(), (logEntries) -> {
            updateTotals(logEntries);
            updateLogEntriesLayout(logEntries);
        });
    }

    @Override
    public void onDestroy() {
        diaryViewModel.disposeAll();
        super.onDestroy();
    }

    private void setupDateSelect() {
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        diaryDate = root.findViewById(R.id.diary_date);
        diaryDate.setOnClickListener(v -> showDateDialog());
        setDateText();
    }

    private void setDateText() {
        diaryDate.setText(simpleDateFormat.format(diaryViewModel.getSelectedDate()));
    }

    private void updateLogEntriesLayout(final List<LogEntryResponse> logEntries) {
        final var breakfastLayout = (LinearLayout) logEntriesLayout.findViewById(R.id.breakfast_layout);
        final var lunchLayout = (LinearLayout) logEntriesLayout.findViewById(R.id.lunch_layout);
        final var dinnerLayout = (LinearLayout) logEntriesLayout.findViewById(R.id.dinner_layout);
        final var snacksLayout = (LinearLayout) logEntriesLayout.findViewById(R.id.snacks_layout);
        List<LogEntryResponse> breakfastEntries = new ArrayList<>();
        List<LogEntryResponse> lunchEntries = new ArrayList<>();
        List<LogEntryResponse> dinnerEntries = new ArrayList<>();
        List<LogEntryResponse> snacksEntries = new ArrayList<>();

        for (LogEntryResponse logEntry : logEntries) {
            if (logEntry.getMeal() == Meal.BREAKFAST) {
                breakfastEntries.add(logEntry);
            } else if (logEntry.getMeal() == Meal.LUNCH) {
                lunchEntries.add(logEntry);
            } else if (logEntry.getMeal() == Meal.DINNER) {
                dinnerEntries.add(logEntry);
            } else {
                snacksEntries.add(logEntry);
            }
        }

        fillCard(breakfastLayout, breakfastEntries);
        fillCard(lunchLayout, lunchEntries);
        fillCard(dinnerLayout, dinnerEntries);
        fillCard(snacksLayout, snacksEntries);
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

//    private void startEditMeal(Meal meal) {
//        Intent intent = new Intent(getActivity(), EditEntryActivity.class);
//        List<LogEntryResponse> entries = logEntryCache.getFromCache(selectedDate);
//        List<LogEntryResponse> filteredEntries = new ArrayList<>();
//        for (LogEntryResponse entry : entries) {
//            if (entry.getMeal().equals(meal)) {
//                filteredEntries.add(entry);
//            }
//        }
//        entries = filteredEntries;
//
//        intent.putExtra("DATE", selectedDate);
//        intent.putExtra("MEAL", meal);
//        intent.putExtra("LOGENTRIES", (Serializable) entries);
//        editEntriesForResult.launch(intent);
//    }

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

    private void updateTotals(final List<LogEntryResponse> logEntries) {
        double totalProtein = 0.0;
        double totalFat = 0.0;
        double totalCarbs = 0.0;
        int totalCalories = 0;

        if (logEntries != null) {
            for (LogEntryResponse entry : logEntries) {
                MacrosResponse macros = entry.getMacrosCalculated();
                totalProtein += macros.getProtein();
                totalFat += macros.getFat();
                totalCarbs += macros.getCarbs();
            }
            totalCalories = (int) ((totalProtein * 4) + (totalFat * 9) + (totalCarbs * 4));
        }

        TextView totalProteinView = root.findViewById(R.id.total_protein);
        totalProteinView.setText(String.valueOf(Math.round(totalProtein * 10) / 10f));
        TextView totalFatView = root.findViewById(R.id.total_fat);
        totalFatView.setText(String.valueOf(Math.round(totalFat * 10) / 10f));
        TextView totalCarbsView = root.findViewById(R.id.total_carbs);
        totalCarbsView.setText(String.valueOf(Math.round(totalCarbs * 10) / 10f));

        TextView totalCaloriesView = root.findViewById(R.id.total_calories);
        totalCaloriesView.setText(String.format(Locale.getDefault(), "%d/%d", totalCalories, goalCalories));

        setProgress(totalProtein, totalFat, totalCarbs);
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

    private void setProgress(double protein, double fat, double carbs) {
        final var surplusProtein = (ProgressBar) root.findViewById(R.id.outer_progress_ring_protein);
        final var progressProtein = (ProgressBar) root.findViewById(R.id.progress_ring_protein);
        progressProtein.setProgress((int) Math.round(protein));
        if (protein < goalProtein) {
            surplusProtein.setVisibility(View.INVISIBLE);
        } else {
            surplusProtein.setVisibility(View.VISIBLE);
            surplusProtein.setProgress((int) Math.round(protein - goalProtein));
        }

        final var progressFat = (ProgressBar) root.findViewById(R.id.progress_ring_fat);
        final var surplusFat = (ProgressBar) root.findViewById(R.id.outer_progress_ring_fat);
        progressFat.setProgress((int) Math.round(fat));
        if (fat < goalFat) {
            surplusFat.setVisibility(View.INVISIBLE);
        } else {
            surplusFat.setVisibility(View.VISIBLE);
            surplusFat.setProgress((int) Math.round(fat - goalFat));
        }

        final var progressCarbs = (ProgressBar) root.findViewById(R.id.progress_ring_carbs);
        final var surplusCarbs = (ProgressBar) root.findViewById(R.id.outer_progress_ring_carbs);
        progressCarbs.setProgress((int) Math.round(carbs));
        if (carbs < goalCarbs) {
            surplusCarbs.setVisibility(View.INVISIBLE);
        } else {
            surplusCarbs.setVisibility(View.VISIBLE);
            surplusCarbs.setProgress((int) Math.round(carbs - goalCarbs));
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
