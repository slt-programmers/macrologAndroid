package com.csl.macrologandroid.ui.food;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.R;
import com.csl.macrologandroid.data.repositories.FoodRepository;
import com.csl.macrologandroid.models.Food;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
public class FoodViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Food>> mFood;

    private List<Food> searchedFood = new ArrayList<>();
    private List<Food> convertedFood = new ArrayList<>();

    @Setter
    private int selectedMeasurementUnit;

    public FoodViewModel(@NonNull Application application) {
        super(application);
        var foodRepository = new FoodRepository(application);

        mFood = foodRepository.getMFood();
        foodRepository.getFood();
    }

    public void searchFood(final CharSequence chars) {
        var allFood = mFood.getValue() != null ? mFood.getValue() : new ArrayList<Food>();
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
    }

    public void determineGramsOrPercentage() {
        if (selectedMeasurementUnit == R.id.grams_radio) {
            convertedFood = searchedFood;
        } else {
            convertedFood = convertGramsToPercentage(searchedFood);
        }
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
                    null
            );
            result.add(foodPercentage);
        }
        return result;
    }

}
