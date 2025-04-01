package com.csl.macrologandroid.ui.diary;

import static android.content.Context.MODE_PRIVATE;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.csl.macrologandroid.R;
import com.csl.macrologandroid.adapters.AutocompleteAdapter;
import com.csl.macrologandroid.databinding.FragmentEditDiaryBinding;
import com.csl.macrologandroid.models.LogEntry;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.util.KeyboardManager;
import com.csl.macrologandroid.util.SpinnerSetupUtil;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class EditDiaryFragment extends Fragment {

    private ConstraintLayout root;
    private AutoCompleteTextView autocompleteTextView;
    private AutocompleteAdapter autocompleteAdapter;
    private Spinner editPortionOrUnitSpinner;
    private TextInputEditText editGramsOrAmount;
    private TextInputLayout editGramsOrAmountLayout;
    private LinearLayout logEntryLayout;
    private Button saveButton;
    private FragmentEditDiaryBinding binding;
    private EditDiaryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(EditDiaryViewModel.class);
        binding = FragmentEditDiaryBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        procesFragmentArguments();

        final var backButton = root.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> requireActivity().finish());

        ((TextView) root.findViewById(R.id.edit_meal_type)).setText(viewModel.getSelectedMeal().name());
        autocompleteTextView = root.findViewById(R.id.search_input);

        setupAutocomplete();
        final var addButton = root.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> addButtonClicked());
        addButton.setEnabled(false);

        logEntryLayout = root.findViewById(R.id.logentry_layout);
        saveButton = root.findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            saveButton.setEnabled(false);
            viewModel.saveLogEntries(() -> requireActivity().finish());
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getMAutocompleteFilling().observe(getViewLifecycleOwner(), (autocompleteList) -> {
            autocompleteAdapter = new AutocompleteAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, autocompleteList);
            autocompleteTextView.setAdapter(autocompleteAdapter);
        });
        viewModel.getMLogEntries().observe(getViewLifecycleOwner(), this::addEntriesToLayout);
        viewModel.initLogEntries();
    }

    @Override
    public void onDestroy() {
        viewModel.disposeAll();
        super.onDestroy();
    }

    private void procesFragmentArguments() {
        final var arguments = getArguments();
        if (arguments != null) {
            viewModel.setSelectedDate(arguments.getParcelable("DATE", Date.class));
            viewModel.setSelectedMeal(arguments.getParcelable("MEAL", Meal.class));
        }
    }

    private void setupAutocomplete() {
        autocompleteTextView.setThreshold(2);
        autocompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            autocompleteItemClicked(view);
        });
    }

    private void autocompleteItemClicked(final View view) {
        final var foodName = ((AppCompatCheckedTextView) view).getText().toString();
        if (isDish(foodName)) {
            autocompleteTextView.setText("");
            addDishEntry(foodName);
        } else {
            setupPortionUnitSpinner(((AppCompatCheckedTextView) view).getText().toString());
            toggleFields(true);
        }
//            addNewFoodButton.setVisibility(View.INVISIBLE);
    }

    private void addDishEntry(final String dishName) {
        final var dishNameTrimmed = dishName.substring(0, dishName.length() - 7);
        viewModel.addDishToLogEntries(dishNameTrimmed);
    }

    private void setupPortionUnitSpinner(final String foodName) {
        final var spinnerUtil = new SpinnerSetupUtil();
        viewModel.setSelectedFood(foodName);

        editPortionOrUnitSpinner = root.findViewById(R.id.edit_portion_unit);
        editGramsOrAmountLayout = root.findViewById(R.id.edit_grams_amount_layout);
        editGramsOrAmount = root.findViewById(R.id.edit_grams_amount);

        final var prefs = requireActivity().getSharedPreferences("PREF_PORTION", MODE_PRIVATE);
        spinnerUtil.setupPortionUnitSpinner(requireContext(), viewModel.getSelectedFood(), editPortionOrUnitSpinner, editGramsOrAmount, prefs);
    }

    private void toggleFields(final boolean visible) {
        final var addButton = root.findViewById(R.id.add_button);
        if (visible) {
            editPortionOrUnitSpinner.setVisibility(View.VISIBLE);
            editGramsOrAmountLayout.setVisibility(View.VISIBLE);
            editGramsOrAmount.requestFocus();
            addButton.setVisibility(View.VISIBLE);
            addButton.setEnabled(true);
        } else {
            editPortionOrUnitSpinner.setVisibility(View.GONE);
            editGramsOrAmountLayout.setVisibility(View.GONE);
            addButton.setEnabled(false);
        }
    }

    private void addButtonClicked() {
        toggleFields(false);
        KeyboardManager.hideKeyboard(requireActivity());
        autocompleteTextView.setText("");
        final var selectedPortion = (String) editPortionOrUnitSpinner.getSelectedItem();
        final var gramsOrAmount = editGramsOrAmount.getText() != null ? editGramsOrAmount.getText().toString() : null;
        viewModel.addSelectedFoodToEntries(selectedPortion, gramsOrAmount);
    }

    private void addEntriesToLayout(final List<LogEntry> logEntries) {
        logEntryLayout.removeAllViews();
        for (var logEntry : logEntries) {
            addEntryToLayout(logEntry);
        }
    }

    private void addEntryToLayout(final LogEntry logEntry) {
        final var row = (ConstraintLayout) getLayoutInflater().inflate(R.layout.layout_edit_log_entry, null);
        final var foodNameTextView = (TextView) row.findViewById(R.id.food_name);
        foodNameTextView.setText(logEntry.getFood().getName());

        final var trashImageView = row.findViewById(R.id.trash_icon);
        trashImageView.setOnClickListener(v -> viewModel.removeLogEntry(logEntry));

        final var foodAmount = (TextInputEditText) row.findViewById(R.id.food_amount);
        foodAmount.setId(R.id.food_amount);

        if (logEntry.getPortion() == null) {
            foodAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
            foodAmount.setText(String.valueOf(Math.round(logEntry.getMultiplier() * 100)));
        } else {
            foodAmount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            foodAmount.setText(String.valueOf(logEntry.getMultiplier()));
        }

        final var foodPortion = (Spinner) row.findViewById(R.id.portion_spinner);
        setupPortionSpinner(foodPortion, logEntry, foodAmount);

        logEntryLayout.addView(row);
        saveButton.setVisibility(View.VISIBLE);
    }

    private void setupPortionSpinner(final Spinner foodPortion, final LogEntry logEntry,
                                     final TextInputEditText foodAmount) {
        final var portionList = logEntry.getFood().getPortions().stream().map(p -> p.getDescription() + " (" + p.getGrams() + " gr)").collect(Collectors.toList());
        portionList.add("gram");

        final var dataAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, portionList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        foodPortion.setAdapter(dataAdapter);
        final var selectedPortion = logEntry.getPortion();
        if (selectedPortion != null) {
            foodPortion.setSelection(portionList.indexOf(selectedPortion.getDescription() + " (" +
                    selectedPortion.getGrams() + " gr)"));
        } else {
            foodPortion.setSelection(portionList.size() - 1);
        }
        foodAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                final var amountString = s.toString();
                if (isNumeric(amountString)) {
                    final var amount = Double.parseDouble(amountString);
                    if (logEntry.getPortion() != null) {
                        logEntry.setMultiplier(amount);
                    } else {
                        logEntry.setMultiplier(amount / 100);
                    }
                }
            }
        });

        foodPortion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (((AppCompatTextView) view).getText().toString().equals("gram")) {
                    foodAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
                    foodAmount.setText(String.valueOf(Math.round(logEntry.getMultiplier() * 100)));
                    logEntry.setPortion(null);
                } else {
                    foodAmount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    foodAmount.setText(String.valueOf(logEntry.getMultiplier()));
                    logEntry.setPortion(logEntry.getFood().getPortions().get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not needed
            }
        });
    }

    private boolean isDish(final String name) {
        return name != null && name.endsWith(" (Dish)");
    }

    public boolean isNumeric(final String strNum) {
        if (strNum == null || strNum.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
