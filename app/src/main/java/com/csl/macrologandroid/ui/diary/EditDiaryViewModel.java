package com.csl.macrologandroid.ui.diary;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.cache.DiaryLogCache;
import com.csl.macrologandroid.cache.DishCache;
import com.csl.macrologandroid.cache.FoodCache;
import com.csl.macrologandroid.data.repositories.FoodRepository;
import com.csl.macrologandroid.dtos.DishResponse;
import com.csl.macrologandroid.dtos.FoodDto;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.mappers.IngredientResponseToLogEntryResponseMapper;
import com.csl.macrologandroid.mappers.LogEntryMapper;
import com.csl.macrologandroid.models.Food;
import com.csl.macrologandroid.models.LogEntry;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.services.DishService;
import com.csl.macrologandroid.services.LogEntryClient;
import com.csl.macrologandroid.services.FoodClient;
import com.csl.macrologandroid.util.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.Getter;
import lombok.Setter;

public class EditDiaryViewModel extends AndroidViewModel {

    private final List<Disposable> disposables = new ArrayList<>();
    private final FoodRepository foodRepository;
//    private final DishService dishService;
//    private final LogEntryClient logEntryClient;

    @Getter
    private final MutableLiveData<List<LogEntry>> mLogEntries;
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
    private List<Food> allFood;

    private Observable<List<Food>> observableFood;
    private List<DishResponse> allDishes;


    public EditDiaryViewModel(final Application app) {
        super(app);
        foodRepository = new FoodRepository(app);
        final var token = app.getApplicationContext().getSharedPreferences("AUTH", Context.MODE_PRIVATE).getString("TOKEN", null);
//        dishService = new DishService(token);
//        logEntryClient = new LogEntryClient(token);
        mLogEntries = new MutableLiveData<>();
        mAutocompleteFilling = new MutableLiveData<>();
        getFood();
        getDishes();
    }

    public void initLogEntries() {
        getLogEntries();
    }

    public void addDishToLogEntries(final String dishName) {
        final var dish = allDishes.stream().filter(d -> dishName.equals(d.getName())).findFirst().orElse(null);
        if (dish != null) {
            final var logEntriesFromIngredients = dish.getIngredients().stream()
                    .map(i -> IngredientResponseToLogEntryResponseMapper.map(i, selectedDate, selectedMeal))
                    .toList();
            final var logEntries = new ArrayList<>(mLogEntries.getValue());
            // TODO
//            logEntries.addAll(logEntriesFromIngredients);
            mLogEntries.setValue(logEntries);
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
//        final var logEntries = DiaryLogCache.getInstance().getFromCache(selectedDate);
//        if (logEntries == null || logEntries.isEmpty()) {
//            disposables.add(logEntryClient.getLogsForDay(selectedDate)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(res -> {
//                        DiaryLogCache.getInstance().addToCache(selectedDate, res);
//                        mLogEntries.setValue(res.stream().filter(logEntry -> logEntry.getMeal() == selectedMeal).toList());
//                    }, err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))));
//        } else {
//            mLogEntries.setValue(logEntries.stream().filter(logEntry -> logEntry.getMeal() == selectedMeal).toList());
//        }
    }

    private void getFood() {
        mAllFood = foodRepository.getMAllFood();
        foodRepository.getAllFood();
        disposables.add(foodRepository.getAllObservableFood().subscribeOn(Schedulers.io()).subscribe((foods -> {
            allFood = foods;
            combineFoodAndDishesSearchList();
        })));
//        disposables.add(foodClient.getAllFood().observeOn(AndroidSchedulers.mainThread()).subscribe(res -> {
//            allFood = res;
//            combineFoodAndDishesSearchList();
//        }, err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))));

    }

    private void getDishes() {
        final var dishes = DishCache.getInstance().getCache();
//        if (dishes == null || dishes.isEmpty()) {
//            disposables.add(dishService.getAllDishes().observeOn(AndroidSchedulers.mainThread()).subscribe(res -> {
//                allDishes = res;
//                combineFoodAndDishesSearchList();
//            }, err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))));
//        }
    }

    private void combineFoodAndDishesSearchList() {
        final var autocompleteList = new ArrayList<String>();
//        if (allFood != null && allDishes != null) {
            for (Food res : allFood) {
                autocompleteList.add(res.getName());
            }
//            for (DishResponse res : allDishes) {
//                autocompleteList.add(res.getName() + " (Dish)");
//            }
//            Collections.sort(autocompleteList);
//        }
        mAutocompleteFilling.setValue(autocompleteList);
    }

}
