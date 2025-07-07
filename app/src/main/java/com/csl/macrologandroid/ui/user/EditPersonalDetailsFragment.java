package com.csl.macrologandroid.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.csl.macrologandroid.R;
import com.csl.macrologandroid.databinding.FragmentEditPersonalDetailsBinding;
import com.csl.macrologandroid.models.Gender;
import com.csl.macrologandroid.models.UserSettings;

import java.util.ArrayList;


public class EditPersonalDetailsFragment extends Fragment {

    private FragmentEditPersonalDetailsBinding binding;
    private View root;
    private EditPersonalDetailsViewModel viewModel;
    // TODO determine intake flow
    private boolean intake = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditPersonalDetailsBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        viewModel = new ViewModelProvider(this).get(EditPersonalDetailsViewModel.class);

        // TODO handle intake flow
        if (intake) {
            binding.radioGroupGender.check(R.id.check_male);
            binding.backButton.setVisibility(View.GONE);
            binding.intakeTitle.setVisibility(View.VISIBLE);
            binding.saveButton.setEnabled(false);
        }
        setupSpinner();

        binding.saveButton.setOnClickListener(v -> saveSettings());
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getMUserSettings().observe(getViewLifecycleOwner(), this::fillPersonalDetails);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadUserSettings();
    }

    private void fillPersonalDetails(final UserSettings userSettings) {
        binding.editName.setText(userSettings.getName());
        binding.editBirthday.setText(userSettings.getBirthday());
        if (Gender.FEMALE.equals(userSettings.getGender())) {
            binding.radioGroupGender.check(R.id.check_female);
        } else {
            binding.radioGroupGender.check(R.id.check_male);
        }
        binding.editHeight.setText(String.valueOf(userSettings.getHeight()));
        binding.editWeight.setText(String.valueOf(userSettings.getCurrentWeight()));

        switch (String.valueOf(userSettings.getActivity())) {
            case EditPersonalDetailsViewModel.DEFAULT_ACTIVITY:
                binding.editActivity.setSelection(1);
                break;
            case "1.55":
                binding.editActivity.setSelection(2);
                break;
            case "1.725":
                binding.editActivity.setSelection(3);
                break;
            case "1.9":
                binding.editActivity.setSelection(4);
                break;
            default:
                binding.editActivity.setSelection(0);
        }
    }

    private void setupSpinner() {
        final var list = new ArrayList<String>();
        list.add("Sedentary");
        list.add("Lightly active");
        list.add("Moderately active");
        list.add("Very active");
        list.add("Extremely active");

        final var dataAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.editActivity.setAdapter(dataAdapter);
    }

    private void saveSettings() {
        final var nameEditable = binding.editName.getText();
        final var birthdayEditable = binding.editBirthday.getText();
        final var gender = ((RadioButton) root.findViewById(binding.radioGroupGender.getCheckedRadioButtonId())).getText().toString();
        final var heightEditable = binding.editHeight.getText();
        final var weightEditable = binding.editWeight.getText();
        final var activity = (String) binding.editActivity.getSelectedItem();

        final var optionalError =  viewModel.saveUserSettings(nameEditable, birthdayEditable,
                gender, heightEditable, weightEditable, activity);
        if (optionalError.isPresent()) {
            binding.errorMessage.setText(optionalError.get());
            binding.errorMessage.setVisibility(View.VISIBLE);
        } else {
            requireActivity().finish();
        }
    }

}
