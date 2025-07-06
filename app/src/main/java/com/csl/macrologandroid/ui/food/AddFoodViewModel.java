package com.csl.macrologandroid.ui.food;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.repositories.FoodRepository;
import com.csl.macrologandroid.models.Food;

import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

public class AddFoodViewModel extends AndroidViewModel {

    private final FoodRepository foodRepository;

    @Getter
    private final MutableLiveData<List<Food>> mFood;
    @Getter
    @Setter
    private Food foodToBeEdited;
    @Setter
    @Getter
    private String newFoodName;

    public AddFoodViewModel(@NonNull final Application app) {
        super(app);
        foodRepository = new FoodRepository(app);
        mFood = foodRepository.getMFood();
        foodRepository.getFood();
    }

    public boolean foodNameExists(final String kandidateName) {
        final var allFood = mFood.getValue();
        assert allFood != null;
        return allFood.stream().map(Food::getName).anyMatch(name -> name.equals(kandidateName));
    }

    public Optional<String> saveFood(final Food foodKandidate) {
        if (foodNameExists(foodKandidate.getName())) {
            return Optional.of("You've already added a product with this name");
        }
        if (foodKandidate.getName().length() < 2) {
            return Optional.of("Food name is too short");
        }
        foodRepository.saveFood(foodKandidate);
        return Optional.empty();
    }

    public void deletePortion(final Long id) {
        foodRepository.deletePortion(id);
    }

    public void deleteFood() {
        foodRepository.deleteFood(foodToBeEdited.getId());
    }
}
