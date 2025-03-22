package com.csl.macrologandroid.ui.diary;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.csl.macrologandroid.cache.DiaryLogCache;
import com.csl.macrologandroid.cache.DishCache;
import com.csl.macrologandroid.cache.FoodCache;
import com.csl.macrologandroid.dtos.DishResponse;
import com.csl.macrologandroid.dtos.EntryDto;
import com.csl.macrologandroid.dtos.FoodResponse;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.dtos.PortionResponse;
import com.csl.macrologandroid.mappers.LogEntryResponseToEntryDtoMapper;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.services.DishService;
import com.csl.macrologandroid.services.EntryService;
import com.csl.macrologandroid.services.FoodService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.Getter;
import lombok.Setter;

public class EditDiaryViewModel extends ViewModel {

    private final List<Disposable> disposables = new ArrayList<>();
    private final FoodService foodService;
    private final DishService dishService;
    private final EntryService entryService;

    @Getter
    private final MutableLiveData<List<LogEntryResponse>> mLogEntries;
    @Getter
    private final MutableLiveData<List<String>> mAutocompleteFilling;
    @Setter
    private Date selectedDate;
    @Setter
    @Getter
    private Meal selectedMeal;
    @Getter
    private FoodResponse selectedFood;

    private List<FoodResponse> allFood;
    private List<DishResponse> allDishes;

    public EditDiaryViewModel() {
        // TODO refactor
        final var token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2Vycy9Uek1Vb2NNRjRwIiwiZXhwIjoxNzQzMjUwNjg1LCJuYW1lIjoiQ2FybWVuU2Nob2x0ZSIsInVzZXJJZCI6Mn0.9J1kJ6f9e2B-9mpth38PZc6IuPqAs2ylWy-jykmAS5w";
        foodService = new FoodService(token);
        dishService = new DishService(token);
        entryService = new EntryService(token);
        mLogEntries = new MutableLiveData<>();
        mAutocompleteFilling = new MutableLiveData<>();
        getFood();
        getDishes();
    }

    public void initLogEntries() {
        getLogEntries();
    }

    public void setSelectedFood(final String foodName) {
        selectedFood = allFood.stream()
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

        final var entry = new LogEntryResponse();
        entry.setFood(getSelectedFood());
        entry.setPortion(optionalPortion.orElse(null));
        entry.setMultiplier(multiplier);
        entry.setDay(selectedDate);
        entry.setMeal(selectedMeal);

        final var logEntries =new ArrayList<>(mLogEntries.getValue());
        logEntries.add(entry);
        mLogEntries.setValue(logEntries);
    }

    public void removeLogEntry(final LogEntryResponse logEntry) {
        final var logEntries = new ArrayList<>(mLogEntries.getValue());
        logEntries.remove(logEntry);
        mLogEntries.setValue(logEntries);
    }

    public void saveLogEntries(final Runnable callback) {
        final var logEntries = mLogEntries.getValue();
        if (logEntries != null) {
            final var logEntryDtos = mLogEntries.getValue().stream().map(LogEntryResponseToEntryDtoMapper::map).toList();
            disposables.add(entryService.postEntries(logEntryDtos, selectedDate, selectedMeal)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(res -> {
                        callback.run();
                    }, err -> {
                        Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()));
                    })
            );
        }
    }

    private void getLogEntries() {
        final var logEntries = DiaryLogCache.getInstance().getFromCache(selectedDate);
        if (logEntries == null || logEntries.isEmpty()) {
            disposables.add(entryService.getLogsForDay(selectedDate)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(res -> {
                        DiaryLogCache.getInstance().addToCache(selectedDate, res);
                        mLogEntries.setValue(res.stream().filter(logEntry -> logEntry.getMeal() == selectedMeal).toList());
                    }, err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))));
        } else {
            mLogEntries.setValue(logEntries.stream().filter(logEntry -> logEntry.getMeal() == selectedMeal).toList());
        }
    }

    private void getFood() {
        final var food = FoodCache.getInstance().getCache();
        if (food == null || food.isEmpty()) {
            disposables.add(foodService.getAllFood().observeOn(AndroidSchedulers.mainThread()).subscribe(res -> {
                allFood = res;
                combineFoodAndDishesSearchList();
            }, err -> {
                Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()));

                //TODO verwijder

                allDishes = List.of();
                allFood = List.of(new FoodResponse(1L, "Appel", 1, 2, 3, List.of(
                        new PortionResponse(1L, 160, "stuk")
                )));
                combineFoodAndDishesSearchList();

                //
            }));
        }
    }

    private void getDishes() {
        final var dishes = DishCache.getInstance().getCache();
        if (dishes == null || dishes.isEmpty()) {
            disposables.add(dishService.getAllDishes().observeOn(AndroidSchedulers.mainThread()).subscribe(res -> {
                allDishes = res;
                combineFoodAndDishesSearchList();
            }, err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))));
        }
    }

    private void combineFoodAndDishesSearchList() {
        final var autocompleteList = new ArrayList<String>();
        if (allFood != null && allDishes != null) {
            for (FoodResponse res : allFood) {
                autocompleteList.add(res.getName());
            }
            for (DishResponse res : allDishes) {
                autocompleteList.add(res.getName() + " (Dish)");
            }
            Collections.sort(autocompleteList);
        }
        mAutocompleteFilling.setValue(autocompleteList);
    }

}
