package com.csl.macrologandroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AutocompleteAdapter extends ArrayAdapter<String> {

    private List<String> allItems;
    private List<String> dataList;

    public AutocompleteAdapter(@NonNull Context context, int resource, @NonNull final List<String> objects) {
        super(context, resource, objects);
        dataList = objects;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public @Nullable
    String getItem(int position) {
        return dataList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        CheckedTextView strName = (CheckedTextView) convertView;
        strName.setText(getItem(position));
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                checkAllItemsNull();

                List<String> values = getFilteredList(constraint);
                results.values = values;
                results.count = values.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values instanceof ArrayList) {
                    dataList = getTypedList(results.values);
                } else {
                    dataList = null;
                }

                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };

    }

    private void checkAllItemsNull() {
        if (allItems == null) {
            allItems = new ArrayList<>(dataList);
        }
    }

    private List<String> getFilteredList(CharSequence constraint) {
        if (constraint == null || constraint.length() == 0) {
            return allItems;
        } else {
            List<String> filteredList = new ArrayList<>();
            for (String item : allItems) {
                if (item.toLowerCase().contains(constraint.toString().toLowerCase())) {
                    filteredList.add(item);
                }
            }
            return filteredList;
        }
    }

    private List<String> getTypedList(Object values) {
        List untyped = (ArrayList) values;
        List<String> typed = new ArrayList<>();
        for (Object item : untyped) {
            if (item instanceof String) {
                typed.add((String) item);
            }
        }
        return typed;
    }

}
