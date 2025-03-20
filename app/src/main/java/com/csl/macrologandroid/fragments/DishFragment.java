package com.csl.macrologandroid.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.csl.macrologandroid.DishActivity;
import com.csl.macrologandroid.R;
import com.csl.macrologandroid.adapters.DishListAdapter;
import com.csl.macrologandroid.cache.DishCache;
import com.csl.macrologandroid.dtos.DishResponse;
import com.csl.macrologandroid.services.DishService;
import com.csl.macrologandroid.util.KeyboardManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static android.view.KeyEvent.KEYCODE_ENTER;

import io.reactivex.rxjava3.disposables.Disposable;

public class DishFragment extends Fragment {

    private Disposable dishDisposable;
    private List<DishResponse> allDishes = new ArrayList<>();
    private final List<DishResponse> searchedDishes = new ArrayList<>();

    private DishListAdapter dishAdapter;

    private final ActivityResultLauncher<Intent> dishForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    DishCache.getInstance().clearCache();
                    DishService dishService = new DishService(getToken());
                    dishDisposable = dishService.getAllDishes()
                            .subscribe(res ->
                            {
                                DishCache.getInstance().addToCache(res);
                                allDishes = res;
                                searchedDishes.clear();
                                searchedDishes.addAll(allDishes);
                                dishAdapter.notifyDataSetChanged();
                            }, err -> Log.e(this.getClass().getName(), err.toString()));

//                    super.onActivityResult(requestCode, resultCode, data);
                }
            });

    public DishFragment() {
        // Non arg constructor
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dish, container, false);

        TextInputEditText search = view.findViewById(R.id.search);
        search.addTextChangedListener(watcher);
        search.setOnEditorActionListener(actionListener);
        search.setImeOptions(EditorInfo.IME_ACTION_DONE);

        RecyclerView dishRecyclerView = view.findViewById(R.id.list_view);
        dishRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchedDishes.addAll(allDishes);
        dishAdapter = new DishListAdapter(this.getContext(), searchedDishes);
        dishAdapter.setOnEditClickListener(this::onEditClick);
        dishRecyclerView.setAdapter(dishAdapter);

        FloatingActionButton fab = view.findViewById(R.id.floating_button);
        fab.setOnClickListener((v) -> {
            Intent intent = new Intent(this.getActivity(), DishActivity.class);
            dishForResult.launch(intent);
        });

        DishService dishService = new DishService(getToken());
        dishDisposable = dishService.getAllDishes().subscribe(
                res ->
                {
                    DishCache.getInstance().addToCache(res);
                    allDishes = res;
                    searchedDishes.clear();
                    searchedDishes.addAll(allDishes);
                    dishAdapter.notifyDataSetChanged();
                },
                err -> Log.e(this.getClass().getName(), err.toString())
        );

        return view;
    }



    @Override
    public void onDestroyView() {
        if (dishDisposable != null) {
            dishDisposable.dispose();
        }

        super.onDestroyView();
    }

    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not needed
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            searchedDishes.clear();
            if (s == null || s.toString().isEmpty()) {
                searchedDishes.addAll(allDishes);
            } else {
                for (DishResponse dish : allDishes) {
                    if (dish.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                        searchedDishes.add(dish);
                    }
                }
            }
            dishAdapter.notifyDataSetChanged();
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Not needed
        }
    };

    private void onEditClick(DishResponse dish) {
        Intent intent = new Intent(this.getActivity(), DishActivity.class);
        intent.putExtra("DISH", dish);
        dishForResult.launch(intent);
    }

    private final TextView.OnEditorActionListener actionListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KEYCODE_ENTER) {
            KeyboardManager.hideKeyboard(this.getActivity());
            v.clearFocus();
            return true;
        }
        return false;
    };

    private String getToken() {
        return this.requireContext().getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }


}
