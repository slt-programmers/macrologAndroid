package com.csl.macrologandroid.ui.food;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.csl.macrologandroid.AddFoodActivity;
import com.csl.macrologandroid.R;
import com.csl.macrologandroid.databinding.FragmentFoodBinding;
import com.csl.macrologandroid.models.Food;
import com.csl.macrologandroid.util.KeyboardManager;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import static android.view.KeyEvent.KEYCODE_ENTER;

public class FoodFragment extends Fragment {

    private FragmentFoodBinding binding;
    private FoodViewModel foodViewModel;


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFoodBinding.inflate(inflater, container, false);
        final var root = binding.getRoot();
        foodViewModel = new ViewModelProvider(this).get(FoodViewModel.class);

        binding.floatingButton.setOnClickListener((v) -> {
            final var intent = new Intent(this.getActivity(), AddFoodActivity.class);
            startActivity(intent);
        });

        binding.search.addTextChangedListener(watcher);
        binding.search.setOnEditorActionListener(actionListener);
        binding.search.setImeOptions(EditorInfo.IME_ACTION_DONE);

        binding.radioGroup.setOnCheckedChangeListener((v, id) -> {
            foodViewModel.determineGramsOrPercentage(id);
            fillTable(foodViewModel.getConvertedFood());
        });

        binding.foodHeader.setOnClickListener(v -> {
            foodViewModel.sortFood(FoodSortHeader.FOOD, true);
            setSortHeaderColor(binding.foodHeader);
            fillTable(foodViewModel.getConvertedFood());
        });

        binding.proteinHeader.setOnClickListener(v -> {
            foodViewModel.sortFood(FoodSortHeader.PROTEIN, true);
            setSortHeaderColor(binding.proteinHeader);
            fillTable(foodViewModel.getConvertedFood());
        });

        binding.fatHeader.setOnClickListener(v -> {
            foodViewModel.sortFood(FoodSortHeader.FAT, true);
            setSortHeaderColor(binding.fatHeader);
            fillTable(foodViewModel.getConvertedFood());
        });

        binding.carbsHeader.setOnClickListener(v -> {
            foodViewModel.sortFood(FoodSortHeader.CARBS, true);
            setSortHeaderColor(binding.carbsHeader);
            fillTable(foodViewModel.getConvertedFood());
        });
        setSortHeaderColor(binding.foodHeader);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        foodViewModel.getMFood().observe(getViewLifecycleOwner(), food -> {
            foodViewModel.initFoodLists(food);
            fillTable(food);
        });
    }

    private void setSortHeaderColor(final TextView header) {
        binding.foodHeader.setTextColor(getResources().getColor(R.color.text, null));
        binding.proteinHeader.setTextColor(getResources().getColor(R.color.text, null));
        binding.fatHeader.setTextColor(getResources().getColor(R.color.text, null));
        binding.carbsHeader.setTextColor(getResources().getColor(R.color.text, null));
        header.setTextColor(getResources().getColor(R.color.darkblue, null));
    }

    private void selectFood(final Food food) {
        final var intent = new Intent(getContext(), AddFoodActivity.class);
        intent.putExtra("FOOD", food);
        startActivity(intent);
    }

    private void fillTable(final List<Food> selection) {
        binding.foodTableLayout.removeAllViews();
        binding.foodTableLayout.addView(binding.foodTableHeader);

        for (var food : selection) {
            final var row = new TableRow(getContext());
            final var foodName = getCustomizedTextView(new TextView(getContext()));
            final var lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 8.0f);

            foodName.setText(food.getName());
            foodName.setLayoutParams(lp);
            foodName.setClickable(true);
            foodName.setOnClickListener(v -> selectFood(food));

            final var protein = getDecimalNumberTextView(food.getProtein());
            final var fat = getDecimalNumberTextView(food.getFat());
            final var carbs = getDecimalNumberTextView(food.getCarbs());

            row.addView(foodName);
            row.addView(protein);
            row.addView(fat);
            row.addView(carbs);
            binding.foodTableLayout.addView(row);
        }
    }



    private TextView getDecimalNumberTextView(double text) {
        final var view = new TextView(getContext());
        view.setText(String.format(Locale.ENGLISH, "%.1f", text));
        final var typeface = ResourcesCompat.getFont(requireContext(), R.font.assistant_light);
        view.setTypeface(typeface);
        setTextViewLayout(view);
        return getCustomizedTextView(view);
    }

    private TextView getCustomizedTextView(TextView view) {
        view.setTextSize(16);
        view.setPadding(0, 0, 0, 16);
        return view;
    }

    private void setTextViewLayout(TextView view) {
        final var lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f);
        view.setLayoutParams(lp);
        view.setGravity(Gravity.END);
    }

    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence chars, int start, int before, int count) {
            foodViewModel.searchFood(chars);
            fillTable(foodViewModel.getConvertedFood());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private final TextView.OnEditorActionListener actionListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KEYCODE_ENTER) {
            KeyboardManager.hideKeyboard(this.getActivity());
            v.clearFocus();
            return true;
        }
        return false;
    };

}
