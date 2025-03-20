package com.csl.macrologandroid.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.textfield.TextInputLayout;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.csl.macrologandroid.R;
import com.csl.macrologandroid.util.DateParser;

import java.util.Date;
import java.util.Objects;

public class DateDialogFragment extends DialogFragment {

    private OnDialogResult onDialogResult;
    private Date currentDate;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = LayoutInflater.from(getContext());

        @SuppressLint("InflateParams")
        ConstraintLayout dialogView = (ConstraintLayout) inflater.inflate(R.layout.dialog_date, null);

        TextInputLayout dateInputLayout = (TextInputLayout) dialogView.getChildAt(0);
        Objects.requireNonNull(dateInputLayout.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (DateParser.parse(s.toString()) == null) {
                    dateInputLayout.setErrorEnabled(true);
                    dateInputLayout.setError("Invalid format");
                } else {
                    dateInputLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
        Objects.requireNonNull(dateInputLayout.getEditText()).setText(DateParser.format(currentDate));

        TextView customTitle = new TextView(getContext());
        customTitle.setText(getResources().getString(R.string.choose_date));
        customTitle.setTextSize(20);
        customTitle.setPadding(48, 48, 0 ,0);
        customTitle.setTextColor(getResources().getColor(R.color.darkblue, null));
        customTitle.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.assistant_light), Typeface.BOLD);

        builder.setCustomTitle(customTitle)
                .setView(dialogView)
                .setPositiveButton(R.string.done, (dialog, id) -> {
                    Date newDate = DateParser.parse(dateInputLayout.getEditText().getText().toString());
                    if (newDate != null) {
                        onDialogResult.finish(newDate);
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> Objects.requireNonNull(getDialog()).cancel());

        return builder.create();
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public void setOnDialogResult(OnDialogResult onDialogResult) {
        this.onDialogResult = onDialogResult;
    }

    public interface OnDialogResult {
        void finish(Date date);
    }
}
