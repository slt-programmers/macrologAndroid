package com.csl.macrologandroid.ui.diary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.csl.macrologandroid.R;
import com.csl.macrologandroid.databinding.FragmentEditDiaryBinding;
import com.csl.macrologandroid.models.Meal;

import java.util.Date;

public class EditDiaryFragment extends Fragment {

    private FragmentEditDiaryBinding binding;
    private EditDiaryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(EditDiaryViewModel.class);
        binding = FragmentEditDiaryBinding.inflate(inflater, container, false);
        final var root = binding.getRoot();
        procesFragmentArguments();

        final var backButton = root.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> requireActivity().finish());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void procesFragmentArguments() {
        final var arguments = getArguments();
        if (arguments != null) {
            viewModel.setSelectedDate(arguments.getParcelable("DATE", Date.class));
            viewModel.setSelectedMeal(arguments.getParcelable("MEAL", Meal.class));
        }
    }
}
