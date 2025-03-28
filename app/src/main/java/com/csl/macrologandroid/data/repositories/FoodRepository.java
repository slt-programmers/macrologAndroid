package com.csl.macrologandroid.data.repositories;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.local.LocalDatabase;
import com.csl.macrologandroid.data.local.daos.FoodDao;
import com.csl.macrologandroid.data.local.daos.PortionDao;
import com.csl.macrologandroid.data.models.FoodData;
import com.csl.macrologandroid.dtos.FoodDto;
import com.csl.macrologandroid.mappers.FoodMapper;
import com.csl.macrologandroid.mappers.PortionMapper;
import com.csl.macrologandroid.models.Food;
import com.csl.macrologandroid.services.FoodClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;


public class FoodRepository {

    private final FoodDao foodDao;
    private final PortionDao portionDao;
    private final FoodClient foodClient;
    private final MutableLiveData<List<Food>> mFood = new MutableLiveData<>();
    private final List<Disposable> disposables = new ArrayList<>();

    public FoodRepository(final Application application) {
        final var db = LocalDatabase.getDatabase(application.getApplicationContext());
        foodDao = db.foodDao();
        portionDao = db.portionDao();
        final var token = application.getApplicationContext().getSharedPreferences("AUTH", Context.MODE_PRIVATE).getString("TOKEN", null);
        foodClient = new FoodClient(token);
    }

    public MutableLiveData<List<Food>> getMAllFood() {
        return mFood;
    }

    public void getAllFood() {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            final var localFood = foodDao.getAllFood();
            if (localFood == null || localFood.isEmpty()) {
                fetchInsertAndSetFood();
            } else {
                final var models = FoodMapper.mapDatasToModels(localFood);
                mFood.postValue(models);
//                syncWithNetwork(localFood);
            }
        });
    }

    public Observable<List<Food>> getAllObservableFood() {
        return Observable.fromCallable(() -> {
            final var localFood = foodDao.getAllFood();
            return FoodMapper.mapDatasToModels(localFood);
        }).flatMap(foodlist -> {
            // TODO test
            if (foodlist.isEmpty()) {
                return foodClient.getAllFood().map((networkFood -> {
                    final var allNewFoodData = cleanDatabaseAndReturnNewFoodData(networkFood);
                    return FoodMapper.mapDatasToModels(allNewFoodData);
                }));
            } else {
                return Observable.just(foodlist);
            }
        }).observeOn(AndroidSchedulers.mainThread());
    }

    private List<FoodData> cleanDatabaseAndReturnNewFoodData(final List<FoodDto> networkFood) {
        final var foodEntities = FoodMapper.mapDtosToEntities(networkFood);
        final var portionEntities = networkFood.stream().map(foodDto -> PortionMapper.mapDtosToEntities(foodDto.getPortions(), foodDto.getId())).flatMap(Collection::stream).toList();
        portionDao.deleteAll();
        foodDao.deleteAll();
        foodDao.insertAll(foodEntities);
        portionDao.insertAll(portionEntities);
        return foodDao.getAllFood();
    }

    private void fetchInsertAndSetFood() {
        disposables.add(foodClient.getAllFood().observeOn(AndroidSchedulers.mainThread()).subscribe(networkFood -> {
            final var foodEntities = FoodMapper.mapDtosToEntities(networkFood);
            final var portionEntities = networkFood.stream().map(foodDto -> PortionMapper.mapDtosToEntities(foodDto.getPortions(), foodDto.getId())).flatMap(Collection::stream).toList();
            LocalDatabase.databaseWriteExecutor.execute(() -> {
                portionDao.deleteAll();
                foodDao.deleteAll();
                foodDao.insertAll(foodEntities);
                portionDao.insertAll(portionEntities);
                final var allNewFoodData = foodDao.getAllFood();
                final var dates = FoodMapper.mapDatasToModels(allNewFoodData);
                mFood.postValue(dates);
            });
        }, err -> {
            Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()));
        }));
    }

    private void syncWithNetwork(final List<FoodData> food) {
        final var newFood = food.stream().filter(f -> f.foodEntity.getExternalId() == null).toList();
        newFood.forEach(f -> {
            disposables.add(foodClient.postFood(FoodMapper.mapDataToDto(f)).observeOn(AndroidSchedulers.mainThread()).subscribe((res) -> {
            }, err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))));
        });
        // TODO changed food
    }
}
