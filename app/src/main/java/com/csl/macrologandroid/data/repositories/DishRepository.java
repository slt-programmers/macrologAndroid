package com.csl.macrologandroid.data.repositories;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.local.LocalDatabase;
import com.csl.macrologandroid.data.local.daos.DishDao;
import com.csl.macrologandroid.data.local.daos.IngredientDao;
import com.csl.macrologandroid.data.network.DishClient;
import com.csl.macrologandroid.mappers.DishMapper;
import com.csl.macrologandroid.mappers.FoodMapper;
import com.csl.macrologandroid.mappers.IngredientMapper;
import com.csl.macrologandroid.models.Dish;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.Getter;

public class DishRepository {

    private final DishDao dishDao;
    private final IngredientDao ingredientDao;
    private final DishClient dishClient;
    @Getter
    private final MutableLiveData<List<Dish>> mDishes = new MutableLiveData<>();
    private final List<Disposable> disposables = new ArrayList<>();

    public DishRepository(final Application application) {
        final var db = LocalDatabase.getDatabase(application.getApplicationContext());
        dishDao = db.dishDao();
        ingredientDao = db.ingredientDao();
        final var token = application.getApplicationContext().getSharedPreferences("AUTH", Context.MODE_PRIVATE).getString("TOKEN", null);
        dishClient = new DishClient(token);
    }

    public void getAllDishes() {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            final var localDishes = dishDao.getAllDishes();
            if (localDishes == null || localDishes.isEmpty()) {
                fetchInsertAndSetFood();
            } else {
                final var models = DishMapper.mapDatasToModels(localDishes);
                mDishes.postValue(models);
//                syncWithNetwork(localFood);
            }
        });
    }

    private void fetchInsertAndSetFood() {
        disposables.add(dishClient.getAllDishes().observeOn(AndroidSchedulers.mainThread()).subscribe(networkDishes -> {
            final var dishEntities = DishMapper.mapDtosToEntities(networkDishes);
            final var ingredientEntities = networkDishes.stream().map(dishDto -> IngredientMapper.mapDtosToEntities(dishDto.getIngredients(),
                    dishDto.getId())).flatMap(Collection::stream).toList();
            LocalDatabase.databaseWriteExecutor.execute(() -> {
                ingredientDao.deleteAll();
                dishDao.deleteAll();
                dishDao.insertAll(dishEntities);
                ingredientDao.insertAll(ingredientEntities);
                final var allNewDishData = dishDao.getAllDishes();
                final var dates = DishMapper.mapDatasToModels(allNewDishData);
                mDishes.postValue(dates);
            });
        }, err -> {
            Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()));
        }));
    }

}
