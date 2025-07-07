package com.csl.macrologandroid.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.csl.macrologandroid.EditGoalActivity;
import com.csl.macrologandroid.ChangePasswordActivity;
import com.csl.macrologandroid.ConnectivityActivity;
import com.csl.macrologandroid.DeleteAccountActivity;
import com.csl.macrologandroid.EditPersonalDetailsActivity;
import com.csl.macrologandroid.R;
import com.csl.macrologandroid.databinding.FragmentUserBinding;
import com.csl.macrologandroid.models.UserSettings;
import com.csl.macrologandroid.util.DateUtil;

import org.jetbrains.annotations.NotNull;

import lombok.Setter;

public class UserFragment extends Fragment {

    private UserViewModel viewModel;
    private FragmentUserBinding binding;

    @Setter
    private OnLogoutPressedListener onLogoutPressedListener;

//    private final ActivityResultLauncher<Intent> deleteAccountForResult = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if (result.getResultCode() == Activity.RESULT_OK) {
//                    onLogoutPressedListener.onLogoutPressed();
//                }
//            });

    @Override
    public View onCreateView(@NotNull final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(inflater, container, false);
        final var root = binding.getRoot();
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        binding.logoutButton.setOnClickListener(v -> onLogoutPressedListener.onLogoutPressed());

        binding.header.setOnClickListener(v -> {
            final var intent = new Intent(getActivity(), EditGoalActivity.class);
            startActivity(intent);
        });

        binding.personal.setOnClickListener(v -> {
            final var intent = new Intent(getActivity(), EditPersonalDetailsActivity.class);
            startActivity(intent);
        });

        binding.connectivityButton.setOnClickListener(v -> {
            final var intent = new Intent(getActivity(), ConnectivityActivity.class);
            startActivity(intent);
        });

        binding.changePassword.setOnClickListener(v -> {
            final var intent = new Intent(getActivity(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        binding.deleteAccount.setOnClickListener(v -> {
            final var intent = new Intent(getActivity(), DeleteAccountActivity.class);
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getMUserSettings().observe(getViewLifecycleOwner(), (this::setUserData));
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadUserSettings();
    }

    private void setUserData(final UserSettings userSettings) {
        binding.userName.setText(userSettings.getName());
        binding.userAge.setText(String.valueOf(DateUtil.birthdayToAge(userSettings.getBirthday())));
        final var gender = userSettings.getGender();
        if (gender != null) {
            var genderStr = gender.toString();
            genderStr = genderStr.charAt(0) + genderStr.substring(1).toLowerCase();
            binding.userGender.setText(genderStr);
        } else {
            binding.userGender.setText(R.string.gender_unknown);
        }

        final var height = userSettings.getHeight() + " cm";
        binding.userHeight.setText(height);

        final var weight = userSettings.getCurrentWeight() + " kg";
        binding.userWeight.setText(weight);

        final var activity = switch (String.valueOf(userSettings.getActivity())) {
            case "1.375" -> "Lightly active";
            case "1.55" -> "Moderately active";
            case "1.725" -> "Very active";
            case "1.9" -> "Extremely active";
            default -> "Sedentary";
        };
        binding.userActivity.setText(activity);

        final var protein = userSettings.getGoalProtein() + "";
        binding.goalProtein.setText(protein);

        final var fat = userSettings.getGoalFat() + "";
        binding.goalFat.setText(fat);

        final var carbs = userSettings.getGoalCarbs() + "";
        binding.goalCarbs.setText(carbs);
    }

    public interface OnLogoutPressedListener {
        void onLogoutPressed();
    }

}
