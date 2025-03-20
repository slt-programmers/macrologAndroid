//package com.csl.macrologandroid.adapters;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.graphics.Typeface;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.core.content.res.ResourcesCompat;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.viewpager.widget.PagerAdapter;
//
//import com.csl.macrologandroid.R;
//import com.csl.macrologandroid.cache.ActivityCache;
//import com.csl.macrologandroid.cache.DiaryLogCache;
//import com.csl.macrologandroid.dtos.ActivityResponse;
//import com.csl.macrologandroid.dtos.LogEntryResponse;
//import com.csl.macrologandroid.models.Meal;
//import com.csl.macrologandroid.services.ActivityService;
//import com.csl.macrologandroid.services.EntryService;
//import com.csl.macrologandroid.util.DateParser;
//
//import org.jetbrains.annotations.NotNull;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//import java.util.Objects;
//
//import io.reactivex.rxjava3.disposables.Disposable;
//import lombok.Getter;
//import lombok.Setter;
//
//import static android.content.Context.MODE_PRIVATE;
//
//public class DiaryPagerAdaper extends RecyclerView.Adapter<DiaryPagerAdaper.ViewHolder> {
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        final var v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_diary_page, parent, false);
//        return new ViewHolder(v);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return 0;
//    }
//
//    @Getter
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        private final LinearLayout breakfastLayout;
//        private final LinearLayout lunchLayout;
//        private final LinearLayout dinnerLayout;
//        private final LinearLayout snacksLayout;
//
//        public ViewHolder(View view) {
//            super(view);
//            breakfastLayout = view.findViewById(R.id.breakfast_layout);
//            lunchLayout = view.findViewById(R.id.lunch_layout);
//            dinnerLayout = view.findViewById(R.id.dinner_layout);
//            snacksLayout = view.findViewById(R.id.snacks_layout);
//        }
//    }
//
//    @Setter
//    private Date selectedDate;
//    private int currentPosition = -1;
//
//    private static final int LOOP_COUNT = 1000;
//    private static final int START_COUNT = 500;
//
//    @Setter
//    private OnMealClickListener onMealClickListener;
//    @Setter
//    private OnActivityClickListener onActivityClickListener;
//    @Setter
//    private OnActivityLinkClickListener onActivityLinkClickListener;
//    @Setter
//    private OnActivitySyncListener onActivitySyncListener;
//    private OnTotalUpdateListener onTotalUpdateListener;
//
//    public void setOnTotalsUpdateListener(OnTotalUpdateListener listener) {
//        this.onTotalUpdateListener = listener;
//    }
//
////    @Override
////    @NonNull
////    public Object instantiateItem(@NonNull ViewGroup container, int position) {
////        final var inflater = LayoutInflater.from(context);
////        final var layout = (ViewGroup) inflater.inflate(R.layout.layout_diary_page, container, false);
////        final var date = getDateFromPosition(position);
////
////        List<LogEntryResponse> entries = DiaryLogCache.getInstance().getFromCache(date);
////        List<ActivityResponse> activities = ActivityCache.getInstance().getFromCache(date);
////        if (entries == null || activities == null) {
////
////            disposableActs = activityService.getActivitiesForDay(date)
////                    .subscribe(
////                            res -> {
////                                ActivityCache.getInstance().addToCache(date, res);
////                                notifyForTotalsUpdate(date);
////                                fillActivitiesOnPage(res, layout);
////                                container.removeView(layout);
////                                container.addView(layout);
////                            },
////                            err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))
////                    );
////        } else {
////            fillLogEntriesOnPage(entries, layout);
////            fillActivitiesOnPage(activities, layout);
////            notifyForTotalsUpdate(date);
////            container.removeView(layout);
////            container.addView(layout);
////        }
////        return layout;
////    }
//
//    private void notifyForTotalsUpdate(Date date) {
//        if (date.equals(selectedDate)) {
//            onTotalUpdateListener.updateTotals(date);
//        }
//    }
//
////    @Override
////    public void setPrimaryItem(@NotNull ViewGroup container, int position, @NotNull Object object) {
////        super.setPrimaryItem(container, position, object);
////        if (position != currentPosition) {
////            LinearLayout diaryPage = (LinearLayout) object;
////            DiaryPager pager = (DiaryPager) container;
////            currentPosition = position;
////            pager.measureCurrentView(diaryPage);
////        }
////    }
//
//
//    private Date getDateFromPosition(int position) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DATE, (position - START_COUNT));
//        return DateParser.parse(DateParser.format(calendar.getTime()));
//    }
//
//    private void fillLogEntriesOnPage(List<LogEntryResponse> entries, ViewGroup view) {
//        LinearLayout breakfastLayout = view.findViewById(R.id.breakfast_layout);
//        LinearLayout lunchLayout = view.findViewById(R.id.lunch_layout);
//        LinearLayout dinnerLayout = view.findViewById(R.id.dinner_layout);
//        LinearLayout snacksLayout = view.findViewById(R.id.snacks_layout);
//
//        List<LogEntryResponse> breakfastEntries = new ArrayList<>();
//        List<LogEntryResponse> lunchEntries = new ArrayList<>();
//        List<LogEntryResponse> dinnerEntries = new ArrayList<>();
//        List<LogEntryResponse> snacksEntries = new ArrayList<>();
//
//        for (LogEntryResponse entry : entries) {
//            if (entry.getMeal() == Meal.BREAKFAST) {
//                breakfastEntries.add(entry);
//            } else if (entry.getMeal() == Meal.LUNCH) {
//                lunchEntries.add(entry);
//            } else if (entry.getMeal() == Meal.DINNER) {
//                dinnerEntries.add(entry);
//            } else {
//                snacksEntries.add(entry);
//            }
//        }
//
////        fillCard(breakfastLayout, breakfastEntries);
////        fillCard(lunchLayout, lunchEntries);
////        fillCard(dinnerLayout, dinnerEntries);
////        fillCard(snacksLayout, snacksEntries);
//
////        Button editBreakfast = view.findViewById(R.id.edit_breakfast);
////        editBreakfast.setOnClickListener(v -> onMealClickListener.onMealClick(Meal.BREAKFAST));
////        Button editLunch = view.findViewById(R.id.edit_lunch);
////        editLunch.setOnClickListener(v -> onMealClickListener.onMealClick(Meal.LUNCH));
////        Button editDinner = view.findViewById(R.id.edit_dinner);
////        editDinner.setOnClickListener(v -> onMealClickListener.onMealClick(Meal.DINNER));
////        Button editSnacks = view.findViewById(R.id.edit_snacks);
////        editSnacks.setOnClickListener(v -> onMealClickListener.onMealClick(Meal.SNACKS));
//    }
//
////    private void fillCard(LinearLayout layout, List<LogEntryResponse> entries) {
////        if (!entries.isEmpty()) {
////            addEntryCardHeader(layout);
////            for (LogEntryResponse entry : entries) {
////                addEntryToTable(layout, entry);
////            }
////        } else {
//////            TextView hint = new TextView(context);
//////            hint.setText(R.string.eaten);
//////            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//////                    ViewGroup.LayoutParams.WRAP_CONTENT);
//////            lp.setMargins(0, 8, 0, 8);
//////            layout.addView(hint, lp);
////        }
////    }
////
////    private void fillActivitiesOnPage(List<ActivityResponse> activities, ViewGroup view) {
////        LinearLayout activitiesLayout = view.findViewById(R.id.activities_layout);
////        if (!activities.isEmpty()) {
////            addActivityCardHeader(activitiesLayout);
////
////            for (ActivityResponse activity : activities) {
////                LinearLayout row = new LinearLayout(context);
////                row.setOrientation(LinearLayout.HORIZONTAL);
////
////                TextView name = getCustomizedTextView(new TextView(context));
////                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
////                lp.setMargins(0, 8, 32, 8);
////                name.setText(activity.getName());
////                name.setLayoutParams(lp);
////                row.addView(name);
////
////                if ("STRAVA".equals(activity.getSyncedWith())) {
////                    TextView link = getCustomizedTextView(new TextView(context));
////                    link.setText(R.string.strava_view);
////                    link.setTextColor(Color.rgb(252, 76, 2));
////                    link.setOnClickListener(v -> this.onActivityLinkClickListener.onActivityLinkClick(String.valueOf(activity.getSyncedId())));
////                    row.addView(link, lp);
////                }
////
////                TextView kcal = getCustomizedCalorieTextView(activity.getCalories());
////                lp = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
////                lp.setMargins(0, 8, 0, 8);
////                row.addView(kcal, lp);
////
////                activitiesLayout.addView(row);
////            }
////        } else {
////            TextView hint = new TextView(context);
////            hint.setText(R.string.activity_done);
////            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
////                    ViewGroup.LayoutParams.WRAP_CONTENT);
////            lp.setMargins(0, 8, 0, 8);
////            activitiesLayout.addView(hint, lp);
////        }
////
////        Button editActivities = view.findViewById(R.id.edit_activities);
////        editActivities.setOnClickListener(v -> onActivityClickListener.onActivityClick());
////        Button syncActivities = view.findViewById(R.id.sync_activities);
////        syncActivities.setOnClickListener(v -> onActivitySyncListener.onActivitySync());
////    }
////
////    private void addEntryCardHeader(LinearLayout layout) {
////        LinearLayout header = new LinearLayout(context);
////        header.setOrientation(LinearLayout.HORIZONTAL);
////
////        TextView dummy = new TextView(context);
////        dummy.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
////
////        TextView p = new TextView(context);
////        p.setText(R.string.p);
////        p.setTypeface(null, Typeface.BOLD);
////        p.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
////
////        TextView f = new TextView(context);
////        f.setText(R.string.f);
////        f.setTypeface(null, Typeface.BOLD);
////        f.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
////
////        TextView c = new TextView(context);
////        c.setText(R.string.c);
////        c.setTypeface(null, Typeface.BOLD);
////        c.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
////
////        TextView kcal = new TextView(context);
////        kcal.setText(R.string.kcal);
////        kcal.setTypeface(null, Typeface.BOLD);
////        kcal.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
////
////        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
////        lp.setMargins(0, 8, 0, 8);
////        header.addView(dummy);
////        header.addView(p, lp);
////        header.addView(f, lp);
////        header.addView(c, lp);
////        header.addView(kcal, lp);
////
////        layout.addView(header);
////    }
////
////    private void addActivityCardHeader(LinearLayout activitiesLayout) {
////        LinearLayout header = new LinearLayout(context);
////        header.setOrientation(LinearLayout.HORIZONTAL);
////
////        TextView kcal = new TextView(context);
////        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
////                ViewGroup.LayoutParams.WRAP_CONTENT);
////        lp.setMargins(0, 8, 0, 8);
////        kcal.setText(R.string.kcal);
////        kcal.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
////        kcal.setTypeface(null, Typeface.BOLD);
////
////        header.addView(kcal, lp);
////
////        activitiesLayout.addView(header);
////    }
////
////    private void addEntryToTable(LinearLayout layout, LogEntryResponse entry) {
////        LinearLayout row = new LinearLayout(context);
////        row.setOrientation(LinearLayout.HORIZONTAL);
////
////        TextView name = getCustomizedTextView(new TextView(context));
////        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
////                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
////        name.setText(entry.getFood().getName());
////        name.setLayoutParams(lp);
////
////        TextView protein = getCustomizedMacroTextView(entry.getMacrosCalculated().getProtein());
////        TextView fat = getCustomizedMacroTextView(entry.getMacrosCalculated().getFat());
////        TextView carbs = getCustomizedMacroTextView(entry.getMacrosCalculated().getCarbs());
////        TextView kcal = getCustomizedCalorieTextView(entry.getMacrosCalculated().getCalories());
////
////        row.addView(name);
////        row.addView(protein);
////        row.addView(fat);
////        row.addView(carbs);
////        row.addView(kcal);
////
////        layout.addView(row);
////    }
////
////    private TextView getCustomizedCalorieTextView(double text) {
////        TextView view = new TextView(context);
////        view.setText(String.format(Locale.ENGLISH, "%1.0f", text));
////        setTextViewLayout(view);
////        return getCustomizedTextView(view);
////    }
////
////    private TextView getCustomizedMacroTextView(double text) {
////        TextView view = new TextView(context);
////        view.setText(String.format(Locale.ENGLISH, "%.1f", text));
////        setTextViewLayout(view);
////        return getCustomizedTextView(view);
////    }
////
////    private TextView getCustomizedTextView(TextView view) {
////        view.setTextSize(16);
////        Typeface typeface = ResourcesCompat.getFont(context, R.font.assistant_light);
////        view.setTypeface(typeface);
////        return view;
////    }
////
////    private void setTextViewLayout(TextView view) {
////        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
////        lp.setMargins(0, 8, 0, 8);
////        view.setLayoutParams(lp);
////        view.setGravity(Gravity.END);
////    }
//
//    public interface OnTotalUpdateListener {
//        void updateTotals(Date date);
//    }
//
//    public interface OnMealClickListener {
//        void onMealClick(Meal meal);
//    }
//
//    public interface OnActivityClickListener {
//        void onActivityClick();
//    }
//
//    public interface OnActivityLinkClickListener {
//        void onActivityLinkClick(String path);
//    }
//
//    public interface OnActivitySyncListener {
//        void onActivitySync();
//    }
//
//}
