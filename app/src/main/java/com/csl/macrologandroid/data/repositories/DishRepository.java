package com.csl.macrologandroid.data.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.local.LocalDatabase;
import com.csl.macrologandroid.data.local.daos.DishDao;
import com.csl.macrologandroid.data.local.daos.IngredientDao;
import com.csl.macrologandroid.data.network.DishClient;
import com.csl.macrologandroid.mappers.DishMapper;
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

    // Local
    private final DishDao dishDao;
    private final IngredientDao ingredientDao;
    @Getter
    private final MutableLiveData<List<Dish>> mDishes = new MutableLiveData<>();

    // Network
    private final DishClient dishClient;
    private final List<Disposable> disposables = new ArrayList<>();
    @Getter
    private final MutableLiveData<Boolean> mSynced = new MutableLiveData<>(false);

    public DishRepository(final Application application) {
        final var db = LocalDatabase.getDatabase(application.getApplicationContext());
        dishDao = db.dishDao();
        ingredientDao = db.ingredientDao();
        dishClient = new DishClient(application.getApplicationContext());
    }

    public void getAllDishes() {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            final var localDishes = dishDao.getAllDishes();
            final var models = DishMapper.mapDatasToModels(localDishes);
            mDishes.postValue(models);
        });
    }

    public void getNetworkDishes() {
        disposables.add(dishClient.getAllDishes().observeOn(AndroidSchedulers.mainThread()).subscribe(networkDishes -> {
            LocalDatabase.databaseWriteExecutor.execute(() -> {
                final var dishEntities = DishMapper.mapDtosToEntities(networkDishes);
                dishDao.insertAll(dishEntities);
                final var newDishes = dishDao.getAllDishes();
                final var ingredientEntities = networkDishes.stream()
                        .map(dishDto -> {
                            final var dishData = newDishes.stream().filter(data -> data.dishEntity.getExternalId().equals(dishDto.getId())).toList().get(0);
                            return IngredientMapper.mapDtosToEntities(dishDto.getIngredients(), dishData.dishEntity.getId());
                        })
                        .flatMap(Collection::stream).toList();
                ingredientDao.insertAll(ingredientEntities);
                mSynced.postValue(true);
            });
        }, err -> {
            Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()));
        }));
    }

    public void disposeAll() {
        for (var disposable : disposables) {
            if (!disposable.isDisposed()) disposable.dispose();
        }
    }
}
