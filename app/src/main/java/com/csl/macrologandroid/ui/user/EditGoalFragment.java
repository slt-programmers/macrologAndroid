package com.csl.macrologandroid.ui.user;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.csl.macrologandroid.databinding.FragmentEditGoalBinding;

public class EditGoalFragment extends Fragment {

    private FragmentEditGoalBinding binding;
    private EditGoalViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditGoalBinding.inflate(inflater, container, false);
        final var root = binding.getRoot();
        viewModel = new ViewModelProvider(this).get(EditGoalViewModel.class);

//        TODO handle intake flow
//        Intent intent = getIntent();
//        boolean intake = intent.getBooleanExtra("INTAKE", false);
//        if (intake) {
//            backButton.setVisibility(View.INVISIBLE);
//            TextView title = findViewById(R.id.adjust_intake_title);
//            title.setVisibility(View.VISIBLE);
//            userSettings = (UserSettingsResponse) intent.getSerializableExtra("userSettings");
//        }

        binding.backButton.setOnClickListener(v -> requireActivity().finish());
        binding.editProtein.addTextChangedListener(textwatcher);
        binding.editFat.addTextChangedListener(textwatcher);
        binding.editCarbs.addTextChangedListener(textwatcher);
        binding.saveButton.setOnClickListener(v -> saveGoalMacros());
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getMUserSettings().observe(getViewLifecycleOwner(), userSettings -> {
            binding.editProtein.setText(String.valueOf(userSettings.getGoalProtein()));
            binding.editFat.setText(String.valueOf(userSettings.getGoalFat()));
            binding.editCarbs.setText(String.valueOf(userSettings.getGoalCarbs()));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadUserSettings();
    }

    private void saveGoalMacros() {
        binding.errorMessage.setVisibility(View.GONE);
        final var proteinEditable = binding.editProtein.getText();
        final var fatEditable = binding.editFat.getText();
        final var carbsEditable = binding.editCarbs.getText();
        final var optionalError = viewModel.saveGoalMacros(proteinEditable, fatEditable, carbsEditable);
        if (optionalError.isPresent()) {
            binding.errorMessage.setText(optionalError.get());
            binding.errorMessage.setVisibility(View.VISIBLE);
        } else {
            requireActivity().finish();
        }
    }


    private final TextWatcher textwatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            // Not needed
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not needed
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            binding.caloriesResult.setText(String.valueOf(viewModel.calculateCalories(binding.editProtein.getText(), binding.editFat.getText(), binding.editCarbs.getText())));
        }
    };

}
