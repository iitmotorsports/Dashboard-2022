package com.iit.dashboard2022.page;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.anim.AnimSetting;
import com.iit.dashboard2022.ui.anim.TranslationAnim;

public class Pager {
    public static final int LEFT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 3;
    private final Page[] pages;
    private final ViewPager2 viewPager;
    private final PageManager pageManager;
    private final ValueAnimator edgeAnim;
    private final TranslationAnim tabLayoutAnim;
    private final ViewGroup.MarginLayoutParams params;
    private int tl = 0, tt = 0, tr = 0, tb = 0;
    private int l, t, r, b;

    public Pager(FragmentActivity activity) {
        this.viewPager = activity.findViewById(R.id.mainPager);
        TabLayout tabs = activity.findViewById(R.id.tabs);

        pageManager = new PageManager(activity.getSupportFragmentManager());

        tabs.removeAllTabs();
        for (int i = 0; i < pageManager.getItemCount(); i++) {
            tabs.addTab(tabs.newTab());
        }

        pages = pageManager.getPages();

        for (Page page : pages) {
            page.onPageChange(false);
        }

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            int init = 0;

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                pages[position].onPageChange(true);
                pages[init].onPageChange(false);
                init = position;
            }
        });

        viewPager.setKeepScreenOn(true);
        viewPager.setAdapter(pageManager);
        viewPager.setOffscreenPageLimit(pageManager.getItemCount());

        new TabLayoutMediator(tabs, viewPager, (tab, position) -> tab.setText(pageManager.getPageTitle(position))).attach();

        params = (ViewGroup.MarginLayoutParams) viewPager.getLayoutParams();

        tabLayoutAnim = new TranslationAnim(tabs, TranslationAnim.Y_AXIS, TranslationAnim.ANIM_BACKWARD);
        tabLayoutAnim.setOnInitializedListener(() -> {
            params.bottomMargin = (int) tabLayoutAnim.getPositionDelta();
            b = params.bottomMargin;
            viewPager.setLayoutParams(params);
        });

        edgeAnim = ValueAnimator.ofFloat(0, 1);
        edgeAnim.setDuration(AnimSetting.ANIM_DURATION);
        edgeAnim.setInterpolator(AnimSetting.ANIM_DEFAULT_INTERPOLATOR);
        // FIXME: Performance: Instead of resizing the actual View, take a screenshot and resize that instead
        edgeAnim.addUpdateListener(animation -> {
            float f = animation.getAnimatedFraction();
            params.leftMargin += (int) ((l - params.leftMargin) * f);
            params.topMargin += (int) ((t - params.topMargin) * f);
            params.rightMargin += (int) ((r - params.rightMargin) * f);
            params.bottomMargin += (int) ((b - params.bottomMargin) * f);
            viewPager.setLayoutParams(params);
            fakeTouch(); // TODO: Fix unwanted bouncy effect when changing side margins
            if (f == 1) {
                tl = 0;
                tt = 0;
                tr = 0;
                tb = 0;
            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    public void setOnTouchCallback(Runnable onTouchCallback) {
        viewPager.setOnTouchListener((v, event) -> {
            onTouchCallback.run();
            return v.performClick();
        });
    }

    private void fakeTouch() {
        viewPager.beginFakeDrag();
        viewPager.fakeDragBy(0);
        viewPager.endFakeDrag();
    }

    private void startMarginAnim() {
        l = params.leftMargin + tl;
        t = params.topMargin + tt;
        r = params.rightMargin + tr;
        b = params.bottomMargin + tb;
        edgeAnim.start();
    }

    public void pushMargin(int edge, int size) {
        switch (edge) {
            case LEFT:
                tl += size;
                break;
            case TOP:
                tt += size;
                break;
            case RIGHT:
                tr += size;
                break;
            case BOTTOM:
                tb += size;
                break;
            default:
                return;
        }
        startMarginAnim();
    }

    public void setUserInputEnabled(boolean enabled) {
        viewPager.setUserInputEnabled(enabled);
        if (enabled) {
            pushMargin(BOTTOM, (int) -tabLayoutAnim.reverse());
        } else {
            pushMargin(BOTTOM, (int) -tabLayoutAnim.start());
        }
    }

    public Page getPage(@PageManager.PageIndex int page) {
        return pageManager.getPage(page);
    }

}
