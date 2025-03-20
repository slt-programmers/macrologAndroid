package com.csl.macrologandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.csl.macrologandroid.cache.UserSettingsCache;
import com.csl.macrologandroid.dtos.WeightRequest;
import com.csl.macrologandroid.fragments.WeighDialogFragment;
import com.csl.macrologandroid.services.WeightService;
import com.csl.macrologandroid.util.DateParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.disposables.Disposable;

public class WeightChartActivity extends AppCompatActivity {

    private Disposable disposable;
    private List<WeightRequest> weightRequests = new ArrayList<>();
    private double currentWeight;
    private TextView currentWeightTextView;
    private TableLayout weightTable;
    private TableRow weightTableHeader;
    private boolean hasBeenEdited;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_weight_chart);

        currentWeightTextView = findViewById(R.id.current_weight_number);
        weightTable = findViewById(R.id.weight_table);
        weightTableHeader = findViewById(R.id.weight_table_header);

        loadMeasurements();

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            if (hasBeenEdited) {
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
            }
            finish();
        });

        Button weighButton = findViewById(R.id.weigh_entry);
        weighButton.setOnClickListener(v -> showWeighDialog());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void loadMeasurements() {
        WeightService weightService = new WeightService(getToken());
        disposable = weightService.getAllMeasurements()
                .subscribe(res -> {
                    weightRequests = res;
                    sortWeightRequestsByDate();
                    currentWeight = getCurrentWeight();
                    currentWeightTextView.setText(String.valueOf(currentWeight));
                    fillTable();
                }, err -> Log.e(this.getLocalClassName(), Objects.requireNonNull(err.getMessage())));
    }

    private void sortWeightRequestsByDate() {
        weightRequests.sort((o1, o2) -> {
            if (DateParser.parse(o1.getDay()).before(DateParser.parse(o2.getDay()))) {
                return 1;
            } else if (DateParser.parse(o1.getDay()).after(DateParser.parse(o2.getDay()))) {
                return -1;
            } else {
                return 0;
            }
        });
    }

    private void fillTable() {
        weightTable.removeAllViews();
        weightTable.addView(weightTableHeader);
        for (WeightRequest weightRequest : weightRequests) {
            TableRow row = new TableRow(getApplicationContext());
            TableRow.LayoutParams params = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(params);
            row.setGravity(Gravity.FILL_HORIZONTAL);
            row.setPadding(0, 0, 0, (8 * getApplicationContext().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));

            TextView date = new TextView(getApplicationContext());
            date.setTextAppearance(R.style.AppTheme);
            date.setText(weightRequest.getDay());
            date.setTextColor(getResources().getColor(R.color.white, null));
            date.setTextSize(20);
            date.setLayoutParams(new TableRow.LayoutParams(-2, -2, 1.0f));

            row.addView(date);

            TextView weight = new TextView(getApplicationContext());
            weight.setTextAppearance(R.style.AppTheme);
            weight.setText(String.valueOf(weightRequest.getWeight()));
            weight.setTextSize(20);
            weight.setTextColor(getResources().getColor(R.color.white, null));
            weight.setLayoutParams(new TableRow.LayoutParams(-2, -2, 1.0f));

            row.addView(weight);

            weightTable.addView(row);
        }
    }

    private double getCurrentWeight() {
        List<WeightRequest> weightList = weightRequests;
        weightList.sort((o1, o2) -> {
            if (DateParser.parse(o1.getDay()).before(DateParser.parse(o2.getDay()))) {
                return 1;
            } else if (DateParser.parse(o1.getDay()).after(DateParser.parse(o2.getDay()))) {
                return -1;
            } else {
                return 0;
            }
        });
        WeightRequest latest = weightList.get(0);
        if (latest != null) {
            return latest.getWeight();
        } else {
            return 0.0;
        }
    }

    private void showWeighDialog() {
        WeighDialogFragment dialog = new WeighDialogFragment();
        dialog.setCurrentWeight(currentWeight);
        dialog.setOnDialogResult(new WeighDialogFragment.OnDialogResult() {
            @Override
            public void finish(WeightRequest weightRequest) {
                WeightService weightService = new WeightService(getToken());
                disposable = weightService.postMeasurement(weightRequest)
                        .subscribe(
                                res -> {
                                    UserSettingsCache.getInstance().clearCache();
                                    hasBeenEdited = true;
                                    loadMeasurements();
                                },
                                err -> Log.e(this.getClass().toString(), Objects.requireNonNull(err.getMessage()))
                        );
            }
        });
        dialog.show(getSupportFragmentManager(), "WeighDialogFragment");
    }

    private String getToken() {
        return getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }
}
