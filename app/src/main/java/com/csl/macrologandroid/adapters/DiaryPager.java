//package com.csl.macrologandroid.adapters;
//
//import android.content.Context;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.viewpager.widget.ViewPager;
//import androidx.viewpager2.widget.ViewPager2;
//
//import android.util.AttributeSet;
//import android.view.View;
//
//public class DiaryPager extends ViewPager {
//
//    private View mCurrentView;
//
//    public DiaryPager(@NonNull Context context) {
//        super(context);
//    }
//
//    public DiaryPager(@NonNull Context context, @Nullable AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    @Override
//    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        if (mCurrentView == null) {
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//            return;
//        }
//        int height = 0;
//        mCurrentView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
//        int h = mCurrentView.getMeasuredHeight();
//        if (h > height) height = h;
//        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
//
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//    }
//
//    public void measureCurrentView(View currentView) {
//        mCurrentView = currentView;
//        requestLayout();
//    }
//
//}
