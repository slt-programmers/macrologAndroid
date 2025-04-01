package com.csl.macrologandroid.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.csl.macrologandroid.R;
import com.csl.macrologandroid.dtos.DishDto;
import com.csl.macrologandroid.dtos.IngredientDto;
import com.csl.macrologandroid.dtos.PortionDto;

import java.util.List;
import java.util.Locale;

public class DishListAdapter extends RecyclerView.Adapter<DishListAdapter.DishViewHolder> {

    private final List<DishDto> dishList;
    private final Context context;
    private OnEditClickListener onEditClickLister;

    public DishListAdapter(Context context, List<DishDto> dishList) {
        this.dishList = dishList;
        this.context = context;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickLister = listener;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_dish_card, parent, false);
        return new DishViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder viewHolder, int position) {
        DishDto dish = dishList.get(position);
        viewHolder.title.setText(dish.getName());
        viewHolder.edit.setOnClickListener((v) -> onEditClickLister.onEditClick(dish));
        viewHolder.ingredientLayout.removeAllViews();

        List<IngredientDto> ingredients = dish.getIngredients();
        for (IngredientDto ingredient : ingredients) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView foodName = getCustomStringTextView(ingredient.getFood().getName());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(0, 8, 16, 8);
            foodName.setLayoutParams(lp);
            TextView amount;
            TextView portionName;

            if (ingredient.getPortion() != null ) {
                PortionDto usedPortion = null;
                for (PortionDto portion : ingredient.getFood().getPortions()) {
                    if (portion.getId().equals(ingredient.getPortion().getId())) {
                        usedPortion = portion;
                        break;
                    }
                }
                if (usedPortion != null) {
                    amount = getCustomNumberTextView(ingredient.getMultiplier());
                    portionName = getCustomStringTextView(usedPortion.getDescription());
                } else {
                    amount = getCustomNumberTextView(0);
                    portionName = getCustomStringTextView("");
                }
            } else {
                amount = getCustomNumberTextView(Math.round(ingredient.getMultiplier() * 100));
                portionName = getCustomStringTextView("gram");
            }

            row.addView(foodName);
            row.addView(amount);
            row.addView(portionName);

            viewHolder.ingredientLayout.addView(row);
        }
    }

    @Override
    public int getItemCount() {
        if (dishList != null) {
            return dishList.size();
        }
        return 0;
    }

    private TextView getCustomStringTextView(String text) {
        TextView view = new TextView(context);
        view.setText(text);

        // layout
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        view.setLayoutParams(lp);

        // font
        view.setTextSize(16);
        Typeface typeface = ResourcesCompat.getFont(context, R.font.assistant_light);
        view.setTypeface(typeface);
        return view;
    }

    private TextView getCustomNumberTextView(double text) {
        TextView view = new TextView(context);
        if (String.valueOf(text).endsWith(".0")) {
            view.setText(String.format(Locale.ENGLISH, "%.0f", text));
        } else {
            view.setText(String.format(Locale.ENGLISH, "%.1f", text));
        }

        // layout
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 16, 8);
        view.setLayoutParams(lp);
        view.setGravity(Gravity.END);

        // font
        view.setTextSize(16);
        Typeface typeface = ResourcesCompat.getFont(context, R.font.assistant_light);
        view.setTypeface(typeface);
        return view;
    }

    static class DishViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        LinearLayout ingredientLayout;
        Button edit;

        DishViewHolder(View view) {
            super(view);

            title = view.findViewById(R.id.dish_title);
            ingredientLayout = view.findViewById(R.id.dish_ingredient_layout);
            edit = view.findViewById(R.id.dish_edit);
        }
    }

    public interface OnEditClickListener {
        void onEditClick(DishDto dish);
    }
}
