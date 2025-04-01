package com.csl.macrologandroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.csl.macrologandroid.dtos.DishDto;
import com.csl.macrologandroid.dtos.FoodDto;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.data.network.DishClient;
import com.csl.macrologandroid.data.network.FoodClient;
import com.csl.macrologandroid.data.network.LogEntryClient;
import com.csl.macrologandroid.ui.diary.EditDiaryFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditEntryActivity extends AppCompatActivity {

    private Date selectedDate;
    private LogEntryClient logEntryClient;
    private FoodClient foodClient;
    private DishClient dishClient;

    private List<FoodDto> allFood;
    private List<DishDto> allDishes;

    private final List<String> autoCompleteList = new ArrayList<>();

    private Meal selectedMeal;

    private AutoCompleteTextView foodTextView;
    private FoodDto selectedFood;

    private Spinner editPortionOrUnitSpinner;
    private TextInputEditText editGramsOrAmount;
    private TextInputLayout editGramsOrAmountLayout;

    private Button addButton;
    private Button addNewFoodButton;

    private LinearLayout logEntryContraintLayout;
    private List<LogEntryResponse> logEntries;
    private Meal meal;
    private Button saveButton;

//    private final ActivityResultLauncher<Intent> addFoodForResult = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if (result.getResultCode() == Activity.RESULT_OK) {
//                    String foodName = (String) Objects.requireNonNull(result.getData()).getSerializableExtra("FOOD_NAME");
//                    setNewlyAddedFood(foodName);
//                }
//            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_diary_host);

        if (savedInstanceState == null) {
            final var fragment = new EditDiaryFragment();
            fragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_content, fragment)
                    .commit();
        }

//
//        if (logEntries.isEmpty()) {
//            saveButton.setVisibility(View.GONE);
//        }

//        addNewFoodButton = findViewById(R.id.add_new_food_button);
//        addNewFoodButton.setOnClickListener(v -> {
//            Intent intent = new Intent(this, AddFoodActivity.class);
//            intent.putExtra("FOOD_NAME", foodTextView.getText().toString());
//            addFoodForResult.launch(intent);
//        });

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


//    private void addDishEntry(String dishName) {
//        String dishFromInput = dishName.substring(0, dishName.length() - 7);
//        DishResponse selectedDish = allDishes
//                .stream()
//                .filter(d -> d.getName().equalsIgnoreCase(dishFromInput))
//                .findFirst()
//                .orElse(null);
//
//        if (selectedDish != null) {
//            for (IngredientResponse ingredient : selectedDish.getIngredients()) {
//                LogEntryResponse entry = new LogEntryResponse();
//                entry.setFood(ingredient.getFood());
//                entry.setPortion(ingredient.getPortion());
//                entry.setMultiplier(ingredient.getMultiplier());
//                entry.setDay(selectedDate);
//                entry.setMeal(selectedMeal);
//
//                logEntries.add(entry);
//                addEntryToLayout(entry);
//            }
//        }
//    }

//
//    private void setNewlyAddedFood(String foodName) {
//        addNewFoodButton.setVisibility(View.GONE);
//        if (foodDisposable != null) {
//            foodDisposable.dispose();
//        }
//        foodDisposable = foodService.getAllFood()
//                .subscribe(res -> {
//                    allFood = res;
//                    fillAutoCompleteList();
//                    setupAutoCompleteTextView();
//                    foodTextView.setText(foodName);
//                    setupPortionUnitSpinner(foodName);
//                    toggleFields(true);
//                }, err -> Log.e(this.getLocalClassName(), Objects.requireNonNull(err.getMessage())));
//    }
//

//    private void saveLogEntries() {
//        List<EntryDto> entries = new ArrayList<>();
//        for (LogEntryResponse entry : logEntries) {
//            EntryDto request = makeLogEntryRequest(entry);
//            entries.add(request);
//        }
//
//        postDisposable = entryService.postEntries(entries, selectedDate, meal)
//                .subscribe(
//                        res -> {
//                            Intent resultIntent = new Intent();
//                            setResult(Activity.RESULT_OK, resultIntent);
//                            finish();
//                        },
//                        err -> {
//                            System.out.println(err.getMessage());
//                            saveButton.setEnabled(true);
//                        });
//
//    }

//    private EntryDto makeLogEntryRequest(LogEntryResponse entry) {
//        int index = logEntries.indexOf(entry);
//        ConstraintLayout logEntryLayout = (ConstraintLayout) logEntryContraintLayout.getChildAt(index);
//        Spinner portionSpinner = (Spinner) logEntryLayout.getChildAt(2);
//        String item = (String) portionSpinner.getSelectedItem();
//
//        double multiplier = 1;
//        TextInputEditText foodAmount = (TextInputEditText) ((TextInputLayout) logEntryLayout.getChildAt(4)).getEditText();
//        if (foodAmount != null && foodAmount.getText() != null) {
//            multiplier = Double.parseDouble(foodAmount.getText().toString());
//        }
//
//        PortionResponse portion = null;
//        if (!item.equals("gram")) {
//            for (PortionResponse prt : entry.getFood().getPortions()) {
//                String trimmedItem = item.substring(0, item.indexOf('(')).trim();
//                if (trimmedItem.equals(prt.getDescription())) {
//                    portion = prt;
//                    break;
//                }
//            }
//        } else {
//            multiplier = multiplier / 100;
//        }
//
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//
//        return new EntryDto(
//                (long) entry.getId(),
//                entry.getFood(),
//                portion,
//                multiplier,
//                format.format(entry.getDay()),
//                entry.getMeal().toString()
//        );
//    }

//    private boolean isDish(String selectedName) {
//        return selectedName != null && selectedName.endsWith(" (Dish)");
//    }




}
