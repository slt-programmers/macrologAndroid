package com.csl.macrologandroid.ui.diary;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.csl.macrologandroid.R;
import com.csl.macrologandroid.databinding.FragmentEditActivityBinding;
import com.csl.macrologandroid.models.Activity;
import com.csl.macrologandroid.util.KeyboardManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class EditActivityFragment extends Fragment {

    private FragmentEditActivityBinding binding;
    private EditActivityViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditActivityBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(EditActivityViewModel.class);
        final var root = binding.getRoot();

        binding.editName.addTextChangedListener(textWatcher);
        binding.editCalories.addTextChangedListener(textWatcher);
        binding.backButton.setOnClickListener(v -> requireActivity().finish());
        binding.saveButton.setOnClickListener(v -> {
            binding.saveButton.setEnabled(false);
            saveActivities();
        });
        binding.addButton.setOnClickListener(v -> {
            KeyboardManager.hideKeyboard(requireActivity());
            addActivity();
        });
        binding.addButton.setEnabled(false);
        return root;
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getMActivities().observe(getViewLifecycleOwner(), this::fillActivityLayout);
    }

    private void fillActivityLayout(final List<Activity> activities) {
        for (Activity act : activities) {
            addActivityToLayout(act);
        }
    }

    private void addActivity() {
//        Activity act = new Activity();
//        act.setDay(selectedDate);
//        act.setName(Objects.requireNonNull(editName.getText()).toString().trim());
//        act.setCalories(Integer.parseInt(Objects.requireNonNull(editCalories.getText()).toString().trim()));
//
//        addActivityToLayout(act);
//
//        binding.editName.setText("");
//        binding.editCalories.setText("");
//        binding.saveButton.setVisibility(View.VISIBLE);
    }

    private void addActivityToLayout(final Activity act) {
        @SuppressLint("InflateParams")
        ConstraintLayout activityConstraintLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.layout_edit_activity, null);

        TextInputEditText activityName = activityConstraintLayout.findViewById(R.id.activity_name);
        activityName.setText(act.getName());

        ImageView trashImageView = activityConstraintLayout.findViewById(R.id.trash_icon);
        trashImageView.setOnClickListener(v -> removeActivity(act));

        TextInputEditText caloriesAmount = activityConstraintLayout.findViewById(R.id.calories_amount);
        caloriesAmount.setText(String.valueOf(act.getCalories()));

        binding.activityLayout.addView(activityConstraintLayout);
    }

    private void removeActivity(final Activity act) {
//        int index = activities.indexOf(act);
//        ConstraintLayout activityConstraintLayout = (ConstraintLayout) binding.activityLayout.getChildAt(index);
//        binding.activityLayout.removeView((activityConstraintLayout));
//        activities.remove(act);
    }

    private void saveActivities() {
//        List<ActivityRequest> activityRequests = new ArrayList<>();
//
//        for (ActivityResponse act : activities) {
//            ActivityRequest request = makeActivityRequest(act);
//            activityRequests.add(request);
//        }
//
//        postDisposable = activityService.postActivitiesForDay(activityRequests, selectedDate)
//                .subscribe(res -> {
//                    Intent resultIntent = new Intent();
//                    setResult(Activity.RESULT_OK, resultIntent);
//                    finish();
//                });

    }

//    private ActivityRequest makeActivityRequest(final Activity act) {
//        int index = activities.indexOf(act);
//        ConstraintLayout editActivity = (ConstraintLayout) activityLayout.getChildAt(index);
//        TextInputLayout activityNameLayout = (TextInputLayout) editActivity.getChildAt(0);
//        String name = Objects.requireNonNull(activityNameLayout.getEditText()).getText().toString();
//
//        TextInputLayout calorieLayout = (TextInputLayout) editActivity.getChildAt(4);
//        int calories = Integer.parseInt(Objects.requireNonNull(calorieLayout.getEditText()).getText().toString());
//
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//
//        return new ActivityRequest(
//                act.getId(),
//                format.format(act.getDay()),
//                name,
//                calories,
//                act.getSyncedWith(),
//                act.getSyncedId()
//        );
//    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not needed
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            binding.addButton.setEnabled(!(s == null || s.toString().isEmpty()));
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Not needed
        }
    };
}
