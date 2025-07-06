package com.csl.macrologandroid.ui.food;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.R;
import com.csl.macrologandroid.data.repositories.FoodRepository;
import com.csl.macrologandroid.models.Food;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;

@Getter
public class FoodViewModel extends AndroidViewModel {

    private final FoodRepository foodRepository;
    private final MutableLiveData<List<Food>> mFood;

    private final List<Food> allFood = new ArrayList<>();
    private List<Food> searchedFood = new ArrayList<>();
    private List<Food> convertedFood = new ArrayList<>();
    private FoodSortHeader currentSortHeader = FoodSortHeader.FOOD;
    private boolean sortDirectionReversed = false;
    private int selectedMeasurementUnit = R.id.grams_radio;

    public FoodViewModel(@NonNull Application application) {
        super(application);
        foodRepository = new FoodRepository(application);

        mFood = foodRepository.getMFood();
    }

    public void loadFood() {
        foodRepository.getFood();
    }

    public void initFoodLists(final List<Food> food) {
        allFood.addAll(food);
        searchedFood.addAll(food);
        convertedFood.addAll(food);
    }

    public void searchFood(final CharSequence chars) {
        searchedFood.clear();
        if (chars == null || chars.toString().isEmpty()) {
            searchedFood = allFood;
        } else {
            for (var food : allFood) {
                if (food.getName().toLowerCase().contains(chars.toString().toLowerCase())) {
                    searchedFood.add(food);
                }
            }
        }
        determineGramsOrPercentage(selectedMeasurementUnit);
    }

    public void determineGramsOrPercentage(final int measurementUnit) {
        selectedMeasurementUnit = measurementUnit;
        if (selectedMeasurementUnit == R.id.grams_radio) {
            convertedFood = searchedFood;
        } else {
            convertedFood = convertGramsToPercentage(searchedFood);
        }
        sortFood(currentSortHeader, false);
    }

    private List<Food> convertGramsToPercentage(final List<Food> foodlist) {
        var result = new ArrayList<Food>();
        for (var food : foodlist) {
            double total = food.getProtein() + food.getFat() + food.getCarbs();
            var foodPercentage = new Food(
                    food.getId(),
                    food.getExternalId(),
                    food.getName(),
                    (food.getProtein() / total * 100),
                    (food.getFat() / total * 100),
                    (food.getCarbs() / total * 100),
                    food.getPortions()
            );
            result.add(foodPercentage);
        }
        return result;
    }

    public void sortFood(final FoodSortHeader sortHeader, boolean flip) {
        if (sortHeader == currentSortHeader && flip) {
            sortDirectionReversed = !sortDirectionReversed;
        }
        currentSortHeader = sortHeader;

        switch (sortHeader) {
            case PROTEIN:
                convertedFood.sort((o1, o2) -> Double.compare(o2.getProtein(), o1.getProtein()));
                break;
            case FAT:
                convertedFood.sort((o1, o2) -> Double.compare(o2.getFat(), o1.getFat()));
                break;
            case CARBS:
                convertedFood.sort((o1, o2) -> Double.compare(o2.getCarbs(), o1.getCarbs()));
                break;
            default:
                convertedFood.sort(Comparator.comparing(Food::getName));
        }

        if (sortDirectionReversed) {
            Collections.reverse(convertedFood);
        }
    }

}
