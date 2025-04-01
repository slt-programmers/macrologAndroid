package com.csl.macrologandroid.ui.diary;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.repositories.DishRepository;
import com.csl.macrologandroid.data.repositories.FoodRepository;
import com.csl.macrologandroid.data.repositories.LogEntryRepository;
import com.csl.macrologandroid.dtos.DishDto;
import com.csl.macrologandroid.mappers.IngredientResponseToLogEntryResponseMapper;
import com.csl.macrologandroid.models.Dish;
import com.csl.macrologandroid.models.Food;
import com.csl.macrologandroid.models.LogEntry;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.util.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.Getter;
import lombok.Setter;

public class EditDiaryViewModel extends AndroidViewModel {

    private final List<Disposable> disposables = new ArrayList<>();
    private final FoodRepository foodRepository;
    private final DishRepository dishRepository;
    private final LogEntryRepository logEntryRepository;

    @Getter
    private final MutableLiveData<List<LogEntry>> mLogEntries = new MutableLiveData<>();
    @Getter
    private final MutableLiveData<List<String>> mAutocompleteFilling;
    @Setter
    private Date selectedDate;
    @Setter
    @Getter
    private Meal selectedMeal;
    @Getter
    private Food selectedFood;

    private MutableLiveData<List<Food>> mAllFood;
    private MutableLiveData<List<Dish>> mAllDishes;
    private List<Food> allFood;
    private List<Dish> allDishes;

    public EditDiaryViewModel(final Application app) {
        super(app);
        foodRepository = new FoodRepository(app);
        dishRepository = new DishRepository(app);
        logEntryRepository = new LogEntryRepository(app);
        mAutocompleteFilling = new MutableLiveData<>();
        getFood();
        getDishes();
    }

    @Override
    protected void onCleared() {
        foodRepository.disposeAll();
        for(var disposable : disposables) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        super.onCleared();
    }

    public void initLogEntries() {
        getLogEntries();
    }

    public void addDishToLogEntries(final String dishName) {
        final var dish = allDishes.stream().filter(d -> dishName.equals(d.getName())).findFirst().orElse(null);
        if (dish != null) {
//            final var logEntriesFromIngredients = dish.getIngredients().stream()
//                    .map(i -> IngredientResponseToLogEntryResponseMapper.map(i, selectedDate, selectedMeal))
//                    .toList();
//            final var logEntries = new ArrayList<>(mLogEntries.getValue());
            // TODO
//            logEntries.addAll(logEntriesFromIngredients);
//            mLogEntries.setValue(logEntries);
        }
    }

    public void setSelectedFood(final String foodName) {
        selectedFood = mAllFood.getValue().stream()
                .filter(food -> foodName.trim().equals(food.getName().trim()))
                .findFirst().orElse(null);
    }

    public void disposeAll() {
        for (var disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    public void addSelectedFoodToEntries(final String selectedPortionDescription, final String gramsOrAmount) {
        final var optionalPortion = getSelectedFood().getPortions().stream()
                .filter(p -> selectedPortionDescription.equals(p.getDescription())).findFirst();

        double multiplier = 1.0;
        if (gramsOrAmount != null) {
            multiplier = Double.parseDouble(gramsOrAmount);
            if (optionalPortion.isEmpty()) {
                multiplier = multiplier / 100;
            }
        }

        final var entry = LogEntry.builder()
                .food(getSelectedFood())
                .portion(optionalPortion.orElse(null))
                .multiplier(multiplier)
                .day(DateUtil.format(selectedDate))
                .meal(selectedMeal.name()).build();

        final var logEntries = new ArrayList<>(mLogEntries.getValue());
        logEntries.add(entry);
        mLogEntries.setValue(logEntries);
    }

    public void removeLogEntry(final LogEntry logEntry) {
        final var logEntries = new ArrayList<>(mLogEntries.getValue());
        logEntries.remove(logEntry);
        mLogEntries.setValue(logEntries);
    }

    public void saveLogEntries(final Runnable callback) {
        final var logEntries = mLogEntries.getValue();
//        if (logEntries != null) {
//            final var logEntryDtos = mLogEntries.getValue().stream().map(LogEntryMapper::mapResponseToRequest).toList();
//            disposables.add(logEntryClient.postEntries(logEntryDtos, selectedDate, selectedMeal)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(res -> callback.run(),
//                            err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage())))
//            );
//        }
    }

    private void getLogEntries() {
        final var editLogEntries = logEntryRepository.getMEditLogEntries();
        editLogEntries.observeForever(mLogEntries::setValue);
        logEntryRepository.getLogEntriesForDayAndMeal(selectedDate, selectedMeal);
    }

    private void getFood() {
        mAllFood = foodRepository.getMAllFood();
        mAllFood.observeForever(food -> {
            allFood = food;
            combineFoodAndDishesSearchList();
        });
        foodRepository.getAllFood();
    }

    private void getDishes() {
        mAllDishes = dishRepository.getMDishes();
        mAllDishes.observeForever(dishes -> {
            allDishes = dishes;
            combineFoodAndDishesSearchList();
        });
        dishRepository.getAllDishes();
    }

    private void combineFoodAndDishesSearchList() {
        final var autocompleteList = new ArrayList<String>();
        if (allFood != null && allDishes != null) {
            for (Food res : allFood) {
                autocompleteList.add(res.getName());
            }
            for (Dish res : allDishes) {
                autocompleteList.add(res.getName() + " (Dish)");
            }
            Collections.sort(autocompleteList);
        }
        mAutocompleteFilling.setValue(autocompleteList);
    }

}
