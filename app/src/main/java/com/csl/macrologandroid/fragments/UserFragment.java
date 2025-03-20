package com.csl.macrologandroid.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.csl.macrologandroid.AboutActivity;
import com.csl.macrologandroid.AdjustIntakeActivity;
import com.csl.macrologandroid.ChangePasswordActivity;
import com.csl.macrologandroid.ConnectivityActivity;
import com.csl.macrologandroid.DeleteAccountActivity;
import com.csl.macrologandroid.EditPersonalDetailsActivity;
import com.csl.macrologandroid.R;
import com.csl.macrologandroid.WeightChartActivity;
import com.csl.macrologandroid.cache.DiaryLogCache;
import com.csl.macrologandroid.cache.FoodCache;
import com.csl.macrologandroid.cache.UserSettingsCache;
import com.csl.macrologandroid.dtos.UserSettingsResponse;
import com.csl.macrologandroid.models.Gender;
import com.csl.macrologandroid.services.UserService;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.disposables.Disposable;
import lombok.Setter;

import static android.content.Context.MODE_PRIVATE;

import java.util.Objects;

public class UserFragment extends Fragment {

    private View view;
    private UserSettingsResponse userSettings;

    private Disposable settingsDisposable;
    @Setter
    private OnLogoutPressedListener onLogoutPressedListener;

    private final ActivityResultLauncher<Intent> editDetailsForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    fetchUserSettings();
                }
            });

    private final ActivityResultLauncher<Intent> deleteAccountForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    onLogoutPressedListener.onLogoutPressed();
                }
            });


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user, container, false);

        UserSettingsCache cache = UserSettingsCache.getInstance();
        userSettings = cache.getCache();
        if (userSettings == null) {
            fetchUserSettings();
        } else {
            setUserData();
        }

        Button logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            UserSettingsCache.getInstance().clearCache();
            FoodCache.getInstance().clearCache();
            DiaryLogCache.getInstance().clearCache();
            onLogoutPressedListener.onLogoutPressed();
        });

        LinearLayout personal = view.findViewById(R.id.personal);
        personal.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditPersonalDetailsActivity.class);
            editDetailsForResult.launch(intent);
        });

        ImageView header = view.findViewById(R.id.header);
        header.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AdjustIntakeActivity.class);
            editDetailsForResult.launch(intent);
        });

        Button weightButton = view.findViewById(R.id.weight_button);
        weightButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), WeightChartActivity.class);
            editDetailsForResult.launch(intent);
        });

        Button connectivityButton = view.findViewById(R.id.connectivity_button);
        connectivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ConnectivityActivity.class);
            startActivity(intent);
        });

        Button changePassword = view.findViewById(R.id.change_password);
        changePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        Button aboutButton = view.findViewById(R.id.about_button);
        aboutButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AboutActivity.class);
            startActivity(intent);
        });

        Button deleteButton = view.findViewById(R.id.delete_account);
        deleteButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DeleteAccountActivity.class);
            deleteAccountForResult.launch(intent);
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (settingsDisposable != null) {
            settingsDisposable.dispose();
        }
    }

    private void fetchUserSettings() {
        UserService userService = new UserService(getToken());
        settingsDisposable = userService.getUserSettings()
                .subscribe(
                        res -> {
                            UserSettingsCache.getInstance().updateCache(res);
                            this.userSettings = res;
                            setUserData();
                        },
                        err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))
                );
    }

    private void setUserData() {
        TextView userName = view.findViewById(R.id.user_name);
        userName.setText(userSettings.getName());

        TextView userAge = view.findViewById(R.id.user_age);
        userAge.setText(String.valueOf(userSettings.getAge()));

        TextView userGender = view.findViewById(R.id.user_gender);
        Gender gender = userSettings.getGender();
        if (gender != null) {
            String genderStr = gender.toString();
            genderStr = genderStr.charAt(0) + genderStr.substring(1).toLowerCase();
            userGender.setText(genderStr);
        } else {
            userGender.setText(R.string.gender_unknown);
        }

        TextView userHeight = view.findViewById(R.id.user_height);
        String height = userSettings.getHeight() + " cm";
        userHeight.setText(height);

        TextView userWeight = view.findViewById(R.id.user_weight);
        String weight = userSettings.getWeight() + " kg";
        userWeight.setText(weight);

        TextView userActivity = view.findViewById(R.id.user_activity);
        String activity = switch (String.valueOf(userSettings.getActivity())) {
            case "1.375" -> "Lightly active";
            case "1.55" -> "Moderately active";
            case "1.725" -> "Very active";
            case "1.9" -> "Extremely active";
            default -> "Sedentary";
        };
        userActivity.setText(activity);

        TextView userProtein = view.findViewById(R.id.goal_protein);
        String protein = userSettings.getGoalProtein() + "";
        userProtein.setText(protein);

        TextView userFat = view.findViewById(R.id.goal_fat);
        String fat = userSettings.getGoalFat() + "";
        userFat.setText(fat);

        TextView userCarbs = view.findViewById(R.id.goal_carbs);
        String carbs = userSettings.getGoalCarbs() + "";
        userCarbs.setText(carbs);
    }

    public interface OnLogoutPressedListener {
        void onLogoutPressed();
    }

    private String getToken() {
        return this.requireContext().getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }
}
