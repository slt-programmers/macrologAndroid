package com.csl.macrologandroid.ui.food;

import android.graphics.Typeface;
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

import com.csl.macrologandroid.R;
import com.csl.macrologandroid.databinding.FragmentFoodBinding;
import com.csl.macrologandroid.models.Food;
import com.csl.macrologandroid.util.KeyboardManager;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.disposables.Disposable;

import static android.view.KeyEvent.KEYCODE_ENTER;

public class FoodFragment extends Fragment {

    private FragmentFoodBinding binding;
    private FoodViewModel foodViewModel;

    private SortHeader currentSortHeader = SortHeader.FOOD;
    private boolean sortDirectionReversed = false;

    private Disposable disposable;

//    private final ActivityResultLauncher<Intent> addFoodForResult = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                KeyboardManager.hideKeyboard(getActivity());
//                if (result.getResultCode() == Activity.RESULT_OK) {
//                    binding.search.setText("");
//                    binding.radioGroup.check(R.id.grams_radio);
//                    currentSortHeader = SortHeader.FOOD;
//                    sortDirectionReversed = false;
//
//                    binding.loader.setVisibility(View.VISIBLE);
//                    binding.foodTableLayout.setVisibility(View.INVISIBLE);
//                    refreshAllFood();
//                }
//            }
//    );

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFoodBinding.inflate(inflater, container, false);
        var root = binding.getRoot();
        foodViewModel = new ViewModelProvider(this).get(FoodViewModel.class);

        // TODO
//        binding.floatingButton.setOnClickListener((v) -> {
//            Intent intent = new Intent(this.getActivity(), AddFoodActivity.class);
//            addFoodForResult.launch(intent);
//        });

        binding.search.addTextChangedListener(watcher);
        binding.search.setOnEditorActionListener(actionListener);
        binding.search.setImeOptions(EditorInfo.IME_ACTION_DONE);

        binding.radioGroup.setOnCheckedChangeListener((v, id) -> {
            foodViewModel.setSelectedMeasurementUnit(id);
            foodViewModel.determineGramsOrPercentage();
            sortTable(currentSortHeader, false);
            fillTable(foodViewModel.getConvertedFood());
        });
        foodViewModel.setSelectedMeasurementUnit(R.id.grams_radio);

        binding.foodHeader.setOnClickListener(v -> {
            sortTable(SortHeader.FOOD, true);
            setSortHeaderColor(binding.foodHeader);
            fillTable(foodViewModel.getConvertedFood());
        });

        binding.proteinHeader.setOnClickListener(v -> {
            sortTable(SortHeader.PROTEIN, true);
            setSortHeaderColor(binding.proteinHeader);
            fillTable(foodViewModel.getConvertedFood());
        });

        binding.fatHeader.setOnClickListener(v -> {
            sortTable(SortHeader.FAT, true);
            setSortHeaderColor(binding.fatHeader);
            fillTable(foodViewModel.getConvertedFood());
        });

        binding.carbsHeader.setOnClickListener(v -> {
            sortTable(SortHeader.CARBS, true);
            setSortHeaderColor(binding.carbsHeader);
            fillTable(foodViewModel.getConvertedFood());
        });

        setSortHeaderColor(binding.foodHeader);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        foodViewModel.getMFood().observe(getViewLifecycleOwner(), this::fillTable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void setSortHeaderColor(TextView header) {
        binding.foodHeader.setTextColor(getResources().getColor(R.color.text, null));
        binding.proteinHeader.setTextColor(getResources().getColor(R.color.text, null));
        binding.fatHeader.setTextColor(getResources().getColor(R.color.text, null));
        binding.carbsHeader.setTextColor(getResources().getColor(R.color.text, null));
        header.setTextColor(getResources().getColor(R.color.darkblue, null));
    }

    private void selectFood(final String foodName) {
//        var intent = new Intent(getContext(), AddFoodActivity.class);
//        Food food = null;
//        for (var response : allFood) {
//            if (response.getName().equals(foodName)) {
//                food = response;
//                break;
//            }
//        }
//
//        intent.putExtra("FOOD_RESPONSE", food);
//        addFoodForResult.launch(intent);
    }

    private void refreshAllFood() {
//        FoodCache.getInstance().clearCache();
//        FoodClient foodClient = new FoodClient(getContext());
//        disposable = foodClient.getAllFood()
//                .subscribe(res ->
//                {
//                    FoodCache.getInstance().addToCache(res);
//                    allFood = res;
//                    searchedFood = allFood;
//                    convertedFood = searchedFood;
//                    fillTable(convertedFood);
//                }, err -> Log.e(this.getClass().getName(), err.toString()));
    }



    private void fillTable(List<Food> selection) {
        binding.foodTableLayout.removeAllViews();
        binding.foodTableLayout.addView(binding.foodTableHeader);

        for (var food : selection) {
            var row = new TableRow(getContext());
            var foodName = getCustomizedTextView(new TextView(getContext()));
            var lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 8.0f);

            foodName.setText(food.getName());
            foodName.setLayoutParams(lp);
            foodName.setClickable(true);
            // TODO
//            foodName.setOnClickListener(v -> selectFood(food.getName()));

            TextView protein = getDecimalNumberTextView(food.getProtein());
            TextView fat = getDecimalNumberTextView(food.getFat());
            TextView carbs = getDecimalNumberTextView(food.getCarbs());

            row.addView(foodName);
            row.addView(protein);
            row.addView(fat);
            row.addView(carbs);
            binding.foodTableLayout.addView(row);
        }

        binding.loader.setVisibility(View.GONE);
        binding.foodTableLayout.setVisibility(View.VISIBLE);
    }

    private void sortTable(SortHeader sortHeader, boolean flip) {
        binding.foodTableLayout.setVisibility(View.INVISIBLE);
        binding.loader.setVisibility(View.VISIBLE);

        if (sortHeader == currentSortHeader && flip) {
            sortDirectionReversed = !sortDirectionReversed;
        }

        switch (sortHeader) {
            case PROTEIN:
                foodViewModel.getConvertedFood().sort((o1, o2) -> Double.compare(o2.getProtein(), o1.getProtein()));
                break;
            case FAT:
                foodViewModel.getConvertedFood().sort((o1, o2) -> Double.compare(o2.getFat(), o1.getFat()));
                break;
            case CARBS:
                foodViewModel.getConvertedFood().sort((o1, o2) -> Double.compare(o2.getCarbs(), o1.getCarbs()));
                break;
            default:
                foodViewModel.getConvertedFood().sort(Comparator.comparing(Food::getName));

        }

        if (sortDirectionReversed) {
            Collections.reverse(foodViewModel.getConvertedFood());
        }

        currentSortHeader = sortHeader;
    }

    private TextView getDecimalNumberTextView(double text) {
        TextView view = new TextView(getContext());
        view.setText(String.format(Locale.ENGLISH, "%.1f", text));
        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.assistant_light);
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
        TableRow.LayoutParams lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
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
            foodViewModel.determineGramsOrPercentage();
            sortTable(currentSortHeader, false);
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

    enum SortHeader {
        FOOD, PROTEIN, FAT, CARBS
    }

}
