package com.csl.macrologandroid.data.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.local.LocalDatabase;
import com.csl.macrologandroid.data.local.daos.FoodDao;
import com.csl.macrologandroid.data.local.daos.PortionDao;
import com.csl.macrologandroid.dtos.FoodDto;
import com.csl.macrologandroid.mappers.FoodMapper;
import com.csl.macrologandroid.mappers.PortionMapper;
import com.csl.macrologandroid.models.Food;
import com.csl.macrologandroid.data.network.FoodClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.Getter;

public class FoodRepository {

    // Local
    private final FoodDao foodDao;
    private final PortionDao portionDao;
    @Getter
    private final MutableLiveData<List<Food>> mFood = new MutableLiveData<>();

    // Network
    private final FoodClient foodClient;
    private final List<Disposable> disposables = new ArrayList<>();
    @Getter
    private final MutableLiveData<Boolean> mSynced = new MutableLiveData<>(false);

    public FoodRepository(final Application application) {
        final var db = LocalDatabase.getDatabase(application.getApplicationContext());
        foodDao = db.foodDao();
        portionDao = db.portionDao();
        foodClient = new FoodClient(application.getApplicationContext());
    }

    public void getFood() {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            final var localFood = foodDao.getAllFood();
            final var models = FoodMapper.mapDatasToModels(localFood);
            mFood.postValue(models);
        });
    }

    public void disposeAll() {
        for (var disposable : disposables) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    public void getNetworkFood() {
        disposables.add(foodClient.getAllFood().observeOn(AndroidSchedulers.mainThread()).subscribe(networkFood ->
                LocalDatabase.databaseWriteExecutor.execute(() -> {
                    insertFoodAndPortions(networkFood);
                    mSynced.postValue(true);
                }), err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))));
    }

    public void saveFood(final Food food) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            final var entity = FoodMapper.mapModelToEntity(food);
            final var foodId = foodDao.insert(entity);
            final var portionEntities = PortionMapper.mapModelsToEntities(food.getPortions(), foodId);
            portionDao.insertAll(portionEntities);
        });
    }

    public void deleteFood(final long id) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            // Ingredients?
            portionDao.deleteForFood(id);
            foodDao.delete(id);
        });
    }

    public void deletePortion(final Long id) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            // Ingredients?
            portionDao.delete(id);
        });
    }

    private void insertFoodAndPortions(final List<FoodDto> foodDtos) {
        final var foodEntities = FoodMapper.mapDtosToEntities(foodDtos);
        foodDao.insertAll(foodEntities);
        final var newFoodEntities = foodDao.getAllFood();
        final var portionEntities = foodDtos.stream()
                .map(foodDto -> {
                    final var food = newFoodEntities.stream().filter(foodData -> foodData.foodEntity.getExternalId().equals(foodDto.getId())).toList().get(0);
                    return PortionMapper.mapDtosToEntities(foodDto.getPortions(), food.foodEntity.getId());
                })
                .flatMap(Collection::stream).toList();
        portionDao.insertAll(portionEntities);
    }

}
