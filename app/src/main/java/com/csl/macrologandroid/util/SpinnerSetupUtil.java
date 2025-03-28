package com.csl.macrologandroid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.widget.AppCompatTextView;

import com.csl.macrologandroid.dtos.FoodDto;
import com.csl.macrologandroid.dtos.PortionDto;
import com.csl.macrologandroid.models.Food;
import com.csl.macrologandroid.models.Portion;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SpinnerSetupUtil {

    public Food getFoodFromList(String foodname, List<Food> allFood) {
        for (Food food : allFood) {
            if (food.getName().trim().equals(foodname.trim())) {
                return food;
            }
        }
        return null;
    }

    private List<String> getPortionList(Food selectedFood) {
        List<String> list = new ArrayList<>();
        for (Portion portion : selectedFood.getPortions()) {
            String desc = portion.getDescription();
            if (desc != null && !desc.isEmpty()) {
                list.add(desc);
            }
        }
        list.add("gram");
        return list;
    }

    public void setupPortionUnitSpinner(Context context, Food selectedFood, Spinner editPortionOrUnitSpinner, TextInputEditText editGramsOrAmount, SharedPreferences prefs) {
        List<String> list = getPortionList(selectedFood);

        String prefPortion = prefs.getString(selectedFood.getName().toUpperCase(), null);
        if (prefPortion != null) {
            prefPortionFirst(prefPortion, list);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editPortionOrUnitSpinner.setAdapter(dataAdapter);
        editPortionOrUnitSpinner.setSelection(0);
        editPortionOrUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String portion = ((AppCompatTextView) view).getText().toString();
                if (portion.equals("gram")) {
                    editGramsOrAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editGramsOrAmount.setText(String.valueOf(100));
                    editGramsOrAmount.setSelection(3);
                } else {
                    editGramsOrAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    editGramsOrAmount.setText(String.valueOf(1));
                    editGramsOrAmount.setSelection(1);
                }
                prefs.edit().putString(selectedFood.getName().toUpperCase(), portion).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not needed
            }
        });

        editGramsOrAmount.setVisibility(View.VISIBLE);
    }

    private void prefPortionFirst(String prefPortion, List<String> portionList) {
        portionList.remove(prefPortion);
        portionList.add(0, prefPortion);
    }

}
