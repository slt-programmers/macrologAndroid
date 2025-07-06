package com.csl.macrologandroid.ui.food;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.csl.macrologandroid.R;
import com.csl.macrologandroid.databinding.FragmentAddFoodBinding;
import com.csl.macrologandroid.models.Food;
import com.csl.macrologandroid.models.Portion;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddFoodFragment extends Fragment {

    private AddFoodViewModel viewModel;
    private FragmentAddFoodBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AddFoodViewModel.class);
        binding = FragmentAddFoodBinding.inflate(inflater, container, false);
        var root = binding.getRoot();
        procesFragmentArguments();

        binding.backButton.setOnClickListener(v -> requireActivity().finish());
        binding.saveButton.setOnClickListener(v -> saveFood());
        if (viewModel.getFoodToBeEdited() != null && viewModel.getFoodToBeEdited().getExternalId() == null) {
            binding.deleteButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setOnClickListener(v -> {
                viewModel.deleteFood();
                requireActivity().finish();
            });
        }
        binding.plus.setOnClickListener(v -> addPortion(null));

        final var foodToBeEdited = viewModel.getFoodToBeEdited();
        if (foodToBeEdited != null) {
            binding.foodName.setText(foodToBeEdited.getName());
            binding.editProtein.setText(String.valueOf(foodToBeEdited.getProtein()));
            binding.editFat.setText(String.valueOf(foodToBeEdited.getFat()));
            binding.editCarbs.setText(String.valueOf(foodToBeEdited.getCarbs()));
            for (Portion portion : foodToBeEdited.getPortions()) {
                addPortion(portion);
            }
        } else {
            binding.foodName.setText(viewModel.getNewFoodName());
            binding.foodName.requestFocus();
        }
        return root;
    }

    private void procesFragmentArguments() {
        final var arguments = getArguments();
        if (arguments != null) {
            viewModel.setFoodToBeEdited(arguments.getParcelable("FOOD", Food.class));
            viewModel.setNewFoodName(arguments.getParcelable("FOOD_NAME", String.class));
        }
    }

    private void addPortion(final Portion portion) {
        final var newPortionLayout = getLayoutInflater().inflate(R.layout.layout_add_portion, binding.portionsLayout, false);
        TextInputEditText portionDescription = newPortionLayout.findViewById(R.id.portion_description);
        TextInputEditText portionGrams = newPortionLayout.findViewById(R.id.portion_grams);
        final var trashcan = newPortionLayout.findViewById(R.id.trash_icon);

        if (portion != null) {
            portionDescription.setText(portion.getDescription());
            portionGrams.setText(String.valueOf(portion.getGrams()));
            if (portion.getExternalId() != null) {
                trashcan.setVisibility(View.INVISIBLE);
            } else {
                trashcan.setOnClickListener(v -> {
                    binding.portionsLayout.removeView(newPortionLayout);
                    viewModel.deletePortion(portion.getId());
                });
            }
        } else {
            trashcan.setOnClickListener(v -> binding.portionsLayout.removeView(newPortionLayout));
        }

        binding.portionsLayout.addView(newPortionLayout);
    }

    private void saveFood() {
        binding.foodNameLayout.setError("");
        binding.foodNameLayout.setErrorEnabled(false);

        final var nameField = binding.foodName.getText();
        final var proteinField = binding.editProtein.getText();
        final var fatField = binding.editFat.getText();
        final var carbsField = binding.editCarbs.getText();

        final var validPortions = getValidPortionsList();
        if (validPortions != null && foodFieldsValid(nameField, proteinField, fatField, carbsField)) {
            final var newFood = new Food(null,
                    null,
                    Objects.requireNonNull(nameField).toString(),
                    Double.parseDouble(Objects.requireNonNull(proteinField).toString()),
                    Double.parseDouble(Objects.requireNonNull(fatField).toString()),
                    Double.parseDouble(Objects.requireNonNull(carbsField).toString()),
                    validPortions);
            final var optionalError = viewModel.saveFood(newFood);
            if (optionalError.isPresent()) {
                binding.foodNameLayout.setError(optionalError.get());
            } else {
                requireActivity().finish();
            }
        } else {
            binding.foodNameLayout.setError("Not all fields are filled in");
        }
    }

    private List<Portion> getValidPortionsList() {
        final var portions = new ArrayList<Portion>();
        final var numberOfPortions = binding.portionsLayout.getChildCount();
        for (int i = 0; i < numberOfPortions; i++) {
            final var inner = binding.portionsLayout.getChildAt(i);
            TextInputEditText portionDescription = inner.findViewById(R.id.portion_description);
            TextInputEditText portionGrams = inner.findViewById(R.id.portion_grams);
            if (portionDescription.getText() == null || portionDescription.getText().toString().isEmpty()) {
                return null;
            }
            if (portionGrams.getText() == null || portionGrams.getText().toString().isEmpty()) {
                return null;
            }
            final var description = portionDescription.getText().toString().trim();
            final var amountOfGrams = Double.parseDouble(portionGrams.getText().toString());
            final var portion = new Portion(findIdForPortion(i), null, amountOfGrams, description);
            portions.add(portion);
        }
        return portions;
    }

    private boolean foodFieldsValid(final Editable nameField, final Editable proteinField, final Editable fatField,
                                    final Editable carbsField) {
        final var nameValid = nameField != null && !nameField.toString().isEmpty();
        final var proteinValid = proteinField != null && !proteinField.toString().isEmpty();
        final var fatValid = fatField != null && !fatField.toString().isEmpty();
        final var carbsValid = carbsField != null && !carbsField.toString().isEmpty();

        return nameValid && proteinValid && fatValid && carbsValid;
    }

    private Long findIdForPortion(int index) {
        if (viewModel.getFoodToBeEdited() != null) {
            final var portions = viewModel.getFoodToBeEdited().getPortions();
            if (portions != null && !portions.isEmpty()) {
                try {
                    final var portion = portions.get(index);
                    return portion.getId();
                } catch (Exception ex) {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}
