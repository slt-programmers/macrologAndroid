package com.csl.macrologandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.csl.macrologandroid.adapters.AutocompleteAdapter;
import com.csl.macrologandroid.dtos.DishResponse;
import com.csl.macrologandroid.dtos.FoodResponse;
import com.csl.macrologandroid.dtos.IngredientResponse;
import com.csl.macrologandroid.dtos.EntryDto;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.dtos.PortionResponse;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.services.DishService;
import com.csl.macrologandroid.services.FoodService;
import com.csl.macrologandroid.services.EntryService;
import com.csl.macrologandroid.util.KeyboardManager;
import com.csl.macrologandroid.util.SpinnerSetupUtil;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.rxjava3.disposables.Disposable;

public class EditEntryActivity extends AppCompatActivity {

    private Date selectedDate;
    private EntryService entryService;
    private FoodService foodService;
    private DishService dishService;

    private List<FoodResponse> allFood;
    private List<DishResponse> allDishes;

    private final List<String> autoCompleteList = new ArrayList<>();

    private Meal selectedMeal;

    private AutoCompleteTextView foodTextView;
    private FoodResponse selectedFood;

    private Spinner editPortionOrUnitSpinner;
    private TextInputEditText editGramsOrAmount;
    private TextInputLayout editGramsOrAmountLayout;

    private Button addButton;
    private Button addNewFoodButton;

    private LinearLayout logEntryContraintLayout;
    private List<LogEntryResponse> logEntries;
    private Meal meal;
    private Button saveButton;
    private Disposable postDisposable;
    private Disposable foodDisposable;
    private Disposable dishDisposable;

    private final ActivityResultLauncher<Intent> addFoodForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    String foodName = (String) Objects.requireNonNull(result.getData()).getSerializableExtra("FOOD_NAME");
                    setNewlyAddedFood(foodName);
                }
            });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (foodDisposable != null) {
            foodDisposable.dispose();
        }
        if (postDisposable != null) {
            postDisposable.dispose();
        }
        if (dishDisposable != null) {
            dishDisposable.dispose();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_log_entry);

        selectedDate = (Date) getIntent().getSerializableExtra("DATE");

        entryService = new EntryService(getToken());
        foodService = new FoodService(getToken());
        dishService = new DishService(getToken());

        getFoodAndDishes();

        logEntries = new ArrayList<>();
        List entries = (List) getIntent().getSerializableExtra("LOGENTRIES");
        if (entries != null) {
            for (Object entry : entries) {
                if (entry instanceof LogEntryResponse) {
                    logEntries.add((LogEntryResponse) entry);
                }
            }

            if (logEntries.isEmpty()) {
                meal = (Meal) getIntent().getSerializableExtra("MEAL");
            } else {
                meal = logEntries.get(0).getMeal();
            }
        }

        setupMealSpinner();
        setupAutoCompleteTextView();

        editPortionOrUnitSpinner = findViewById(R.id.edit_portion_unit);
        editPortionOrUnitSpinner.setVisibility(View.GONE);
        editGramsOrAmount = findViewById(R.id.edit_grams_amount);
        editGramsOrAmountLayout = findViewById(R.id.edit_grams_amount_layout);
        editGramsOrAmountLayout.setVisibility(View.GONE);

        logEntryContraintLayout = findViewById(R.id.logentry_layout);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            saveButton.setEnabled(false);
            saveLogEntries();
        });

        if (logEntries.isEmpty()) {
            saveButton.setVisibility(View.GONE);
        }

        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            toggleFields(false);
            KeyboardManager.hideKeyboard(this);
            foodTextView.setText("");
            addLogEntry();
        });
        addButton.setEnabled(false);

        addNewFoodButton = findViewById(R.id.add_new_food_button);
        addNewFoodButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddFoodActivity.class);
            intent.putExtra("FOOD_NAME", foodTextView.getText().toString());
            addFoodForResult.launch(intent);
        });

        fillLogEntryLayout();
    }

    @Override
    public void onPause() {
        super.onPause();
        Session.resetTimestamp();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Session.getInstance().isExpired()) {
            Intent intent = new Intent(EditEntryActivity.this, RoutingActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);
        }
    }

    private void getFoodAndDishes() {
        allFood = null;
        allDishes = null;
        foodDisposable = foodService.getAllFood().subscribe(res -> {
            allFood = res;
            combineFoodAndDishesSearchList();
        }, err -> Log.e(this.getLocalClassName(), Objects.requireNonNull(err.getMessage())));

        dishDisposable = dishService.getAllDishes().subscribe(res -> {
            allDishes = res;
            combineFoodAndDishesSearchList();
        }, err -> Log.e(this.getLocalClassName(), Objects.requireNonNull(err.getMessage())));
    }

    private void combineFoodAndDishesSearchList() {
        autoCompleteList.clear();
        if (allFood != null && allDishes != null) {
            fillAutoCompleteList();
            setupAutoCompleteTextView();
        }
    }

    private void fillAutoCompleteList() {
        autoCompleteList.clear();
        for (FoodResponse res : allFood) {
            autoCompleteList.add(res.getName());
        }
        for (DishResponse res : allDishes) {
            autoCompleteList.add(res.getName() + " (Dish)");
        }
        Collections.sort(autoCompleteList);
    }

    private void fillLogEntryLayout() {
        for (LogEntryResponse entry : logEntries) {
            addEntryToLayout(entry);
        }
    }

    private void toggleFields(boolean visible) {
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

    private void addDishEntry(String dishName) {
        String dishFromInput = dishName.substring(0, dishName.length() - 7);
        DishResponse selectedDish = allDishes
                .stream()
                .filter(d -> d.getName().equalsIgnoreCase(dishFromInput))
                .findFirst()
                .orElse(null);

        if (selectedDish != null) {
            for (IngredientResponse ingredient : selectedDish.getIngredients()) {
                LogEntryResponse entry = new LogEntryResponse();
                entry.setFood(ingredient.getFood());
                entry.setPortion(ingredient.getPortion());
                entry.setMultiplier(ingredient.getMultiplier());
                entry.setDay(selectedDate);
                entry.setMeal(selectedMeal);

                logEntries.add(entry);
                addEntryToLayout(entry);
            }
        }
    }

    private void addLogEntry() {
        PortionResponse selectedPortion = null;
        for (PortionResponse portion : selectedFood.getPortions()) {
            String portionDescription = (String) editPortionOrUnitSpinner.getSelectedItem();
            if (portionDescription.equals(portion.getDescription())) {
                selectedPortion = portion;
                break;
            }
        }

        double multiplier = 1.0;
        if (editGramsOrAmount.getText() != null) {
            multiplier = Double.parseDouble(editGramsOrAmount.getText().toString());
            if (selectedPortion == null) {
                multiplier = multiplier / 100;
            }
        }

        LogEntryResponse entry = new LogEntryResponse();
        entry.setFood(selectedFood);
        entry.setPortion(selectedPortion);
        entry.setMultiplier(multiplier);
        entry.setDay(selectedDate);
        entry.setMeal(selectedMeal);

        logEntries.add(entry);
        addEntryToLayout(entry);
    }

    private void addEntryToLayout(LogEntryResponse entry) {
        @SuppressLint("InflateParams") ConstraintLayout logEntry = (ConstraintLayout) getLayoutInflater().inflate(R.layout.layout_edit_log_entry, null);

        TextView foodNameTextView = logEntry.findViewById(R.id.food_name);
        foodNameTextView.setText(entry.getFood().getName());

        ImageView trashImageView = logEntry.findViewById(R.id.trash_icon);
        trashImageView.setOnClickListener(v -> removeEntry(entry));

        TextInputEditText foodAmount = logEntry.findViewById(R.id.food_amount);
        foodAmount.setId(R.id.food_amount);

        if (entry.getPortion() == null) {
            foodAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
            foodAmount.setText(String.valueOf(Math.round(entry.getMultiplier() * 100)));
        } else {
            foodAmount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            foodAmount.setText(String.valueOf(entry.getMultiplier()));
        }

        Spinner foodPortion = logEntry.findViewById(R.id.portion_spinner);
        setupPortionSpinner(foodPortion, entry, foodAmount);

        logEntryContraintLayout.addView(logEntry);
        saveButton.setVisibility(View.VISIBLE);
    }

    private void setNewlyAddedFood(String foodName) {
        addNewFoodButton.setVisibility(View.GONE);
        if (foodDisposable != null) {
            foodDisposable.dispose();
        }
        foodDisposable = foodService.getAllFood()
                .subscribe(res -> {
                    allFood = res;
                    fillAutoCompleteList();
                    setupAutoCompleteTextView();
                    foodTextView.setText(foodName);
                    setupPortionUnitSpinner(foodName);
                    toggleFields(true);
                }, err -> Log.e(this.getLocalClassName(), Objects.requireNonNull(err.getMessage())));
    }

    private void removeEntry(LogEntryResponse entry) {
        int index = logEntries.indexOf(entry);
        ConstraintLayout logEntryLayout = (ConstraintLayout) logEntryContraintLayout.getChildAt(index);
        logEntryContraintLayout.removeView((logEntryLayout));
        logEntries.remove(entry);
    }

    private void saveLogEntries() {
        List<EntryDto> entries = new ArrayList<>();
        for (LogEntryResponse entry : logEntries) {
            EntryDto request = makeLogEntryRequest(entry);
            entries.add(request);
        }

        postDisposable = entryService.postEntries(entries, selectedDate, meal)
                .subscribe(
                        res -> {
                            Intent resultIntent = new Intent();
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        },
                        err -> {
                            System.out.println(err.getMessage());
                            saveButton.setEnabled(true);
                        });

    }

    private EntryDto makeLogEntryRequest(LogEntryResponse entry) {
        int index = logEntries.indexOf(entry);
        ConstraintLayout logEntryLayout = (ConstraintLayout) logEntryContraintLayout.getChildAt(index);
        Spinner portionSpinner = (Spinner) logEntryLayout.getChildAt(2);
        String item = (String) portionSpinner.getSelectedItem();

        double multiplier = 1;
        TextInputEditText foodAmount = (TextInputEditText) ((TextInputLayout) logEntryLayout.getChildAt(4)).getEditText();
        if (foodAmount != null && foodAmount.getText() != null) {
            multiplier = Double.parseDouble(foodAmount.getText().toString());
        }

        PortionResponse portion = null;
        if (!item.equals("gram")) {
            for (PortionResponse prt : entry.getFood().getPortions()) {
                String trimmedItem = item.substring(0, item.indexOf('(')).trim();
                if (trimmedItem.equals(prt.getDescription())) {
                    portion = prt;
                    break;
                }
            }
        } else {
            multiplier = multiplier / 100;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        return new EntryDto(
                (long) entry.getId(),
                entry.getFood(),
                portion,
                multiplier,
                format.format(entry.getDay()),
                entry.getMeal().toString()
        );
    }

    private boolean isDish(String selectedName) {
        return selectedName != null && selectedName.endsWith(" (Dish)");
    }

    private void setupAutoCompleteTextView() {
        foodTextView = findViewById(R.id.edit_food_textview);
        ArrayAdapter<String> autocompleteAdapter = new AutocompleteAdapter(this, android.R.layout.simple_spinner_dropdown_item, autoCompleteList);
        foodTextView.setAdapter(autocompleteAdapter);
        foodTextView.setThreshold(2);
        foodTextView.setOnItemClickListener((parent, view, position, id) -> {
            String foodName = ((AppCompatCheckedTextView) view).getText().toString();
            if (isDish(foodName)) {
                foodTextView.setText("");
                addDishEntry(foodName);
            } else {
                setupPortionUnitSpinner(((AppCompatCheckedTextView) view).getText().toString());
                toggleFields(true);
            }
            addNewFoodButton.setVisibility(View.INVISIBLE);
        });

        autocompleteAdapter.registerDataSetObserver(
                new DataSetObserver() {
                    @Override
                    public void onInvalidated() {
                        super.onInvalidated();
                        String text = foodTextView.getText().toString();
                        if (text.length() > 2 && !autoCompleteList.contains(text)) {
                            toggleFields(false);
                            addButton.setVisibility(View.INVISIBLE);
                            addNewFoodButton.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );

        foodTextView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT
                    && foodTextView.isPopupShowing()
                    && autocompleteAdapter.getCount() != 0) {
                String selectedOption = autocompleteAdapter.getItem(0);
                if (selectedOption != null) {
                    if (isDish(selectedOption)) {
                        foodTextView.setText("");
                        addDishEntry(selectedOption);
                    } else {
                        foodTextView.setText(selectedOption);
                        foodTextView.dismissDropDown();
                        setupPortionUnitSpinner(selectedOption);
                        toggleFields(true);
                    }
                    addNewFoodButton.setVisibility(View.INVISIBLE);
                }
                return true;
            }
            return false;
        });
    }

    private void setupMealSpinner() {
        Spinner mealtypeSpinner = findViewById(R.id.edit_meal_type);

        List<String> list = new ArrayList<>();
        list.add("Breakfast");
        list.add("Lunch");
        list.add("Dinner");
        list.add("Snacks");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealtypeSpinner.setAdapter(dataAdapter);

        selectedMeal = meal;
        String capitalizedString = meal.toString().substring(0, 1).toUpperCase() + meal.toString().substring(1).toLowerCase();
        mealtypeSpinner.setSelection(list.indexOf(capitalizedString));
        mealtypeSpinner.setEnabled(false);
        mealtypeSpinner.setClickable(false);
    }

    private void setupPortionSpinner(Spinner foodPortion, LogEntryResponse entry, TextInputEditText foodAmount) {
        List<String> list = new ArrayList<>();
        List<PortionResponse> allPortions = entry.getFood().getPortions();
        for (PortionResponse portion : allPortions) {
            String portionText = portion.getDescription() + " (" + portion.getGrams() + " gr)";
            list.add(portionText);
        }
        list.add("gram");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        foodPortion.setAdapter(dataAdapter);
        PortionResponse selectedPortion = entry.getPortion();
        if (selectedPortion != null) {
            foodPortion.setSelection(list.indexOf(selectedPortion.getDescription() + " (" +
                    selectedPortion.getGrams() + " gr)"));
        } else {
            foodPortion.setSelection(list.size() - 1);
        }

        foodPortion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (((AppCompatTextView) view).getText().toString().equals("gram")) {
                    foodAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
                    foodAmount.setText(String.valueOf(Math.round(entry.getMultiplier() * 100)));
                } else {
                    foodAmount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    foodAmount.setText(String.valueOf(entry.getMultiplier()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not needed
            }
        });
    }

    private void setupPortionUnitSpinner(String foodName) {
        SpinnerSetupUtil spinnerUtil = new SpinnerSetupUtil();
        selectedFood = spinnerUtil.getFoodFromList(foodName, allFood);
        if (selectedFood == null) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences("PREF_PORTION", MODE_PRIVATE);
        spinnerUtil.setupPortionUnitSpinner(this, selectedFood, editPortionOrUnitSpinner, editGramsOrAmount, prefs);
    }

    private String getToken() {
        return getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }

}
