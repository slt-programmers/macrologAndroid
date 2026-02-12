package com.csl.macrologandroid.data.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.local.LocalDatabase;
import com.csl.macrologandroid.data.local.daos.DishDao;
import com.csl.macrologandroid.data.local.daos.FoodDao;
import com.csl.macrologandroid.data.local.daos.IngredientDao;
import com.csl.macrologandroid.data.local.entities.FoodEntity;
import com.csl.macrologandroid.data.models.FoodData;
import com.csl.macrologandroid.data.network.DishClient;
import com.csl.macrologandroid.dtos.IngredientDto;
import com.csl.macrologandroid.mappers.DishMapper;
import com.csl.macrologandroid.mappers.IngredientMapper;
import com.csl.macrologandroid.models.Dish;

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
    private final FoodDao foodDao;

    @Getter
    private final MutableLiveData<List<Dish>> mDishes = new MutableLiveData<>();

    // Network
    private final DishClient dishClient;
    @Getter
    private final MutableLiveData<Boolean> mSynced = new MutableLiveData<>(false);

    public DishRepository(final Application application) {
        final var db = LocalDatabase.getDatabase(application.getApplicationContext());
        dishDao = db.dishDao();
        ingredientDao = db.ingredientDao();
        foodDao = db.foodDao();
        dishClient = new DishClient(application.getApplicationContext());
    }

    public void getAllDishes() {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            final var localDishes = dishDao.getAllDishes();
            final var models = DishMapper.mapDatasToModels(localDishes);
            mDishes.postValue(models);
        });
    }

    public Disposable getNetworkDishes() {
        return dishClient.getAllDishes().observeOn(AndroidSchedulers.mainThread()).subscribe(networkDishes -> LocalDatabase.databaseWriteExecutor.execute(() -> {
            final var dishEntities = DishMapper.mapDtosToEntities(networkDishes);
            dishDao.insertAll(dishEntities);
            final var newDishes = dishDao.getAllDishes();
            final var ingredientEntities = networkDishes.stream()
                    .map(dishDto -> {
                        final var dishData = newDishes.stream().filter(data -> data.dishEntity.getExternalId().equals(dishDto.getId())).toList().get(0);
                        return dishDto.getIngredients().stream().map(ingredientDto -> {
                            final var foodData = getFoodDataForIngredient(ingredientDto);
                            final var portionId = getPortionIdForIngredient(ingredientDto, foodData);
                            return IngredientMapper.mapDtoToEntity(ingredientDto,
                                    dishData.dishEntity.getId(),
                                    foodData.foodEntity.getId(),
                                    portionId
                                    );
                        }).toList();
                    })
                    .flatMap(Collection::stream).toList();
            ingredientDao.insertAll(ingredientEntities);
            mSynced.postValue(true);
        }), err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage())));
    }

    private FoodData getFoodDataForIngredient(final IngredientDto ingredientDto) {
        return foodDao.getByExternalIds(List.of(ingredientDto.getFood().getId())).get(0);
    }
    private Long getPortionIdForIngredient(final IngredientDto ingredientDto, final FoodData foodData) {
       return ingredientDto.getPortion() != null ?
               foodData.portionEntities.stream().filter(
                       portionEntity -> ingredientDto.getPortion().getId().equals(portionEntity.getExternalId()))
                       .toList().get(0).getId() : null;
    }

}
