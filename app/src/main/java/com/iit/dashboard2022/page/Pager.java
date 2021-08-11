package com.iit.dashboard2022.page;

import android.annotation.SuppressLint;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.anim.TranslationAnim;

public class Pager {
    private final ViewPager2 viewPager;
    private final TabLayout tabs;
    private final PageManager pageManager;
    private TranslationAnim tabLayoutAnim;

    public Pager(FragmentActivity activity) {
        this.viewPager = activity.findViewById(R.id.mainPager);
        this.tabs = activity.findViewById(R.id.tabs);

        pageManager = new PageManager(activity.getSupportFragmentManager());

        tabs.removeAllTabs();
        for (int i = 0; i < pageManager.getItemCount(); i++) {
            tabs.addTab(tabs.newTab());
        }

        viewPager.setKeepScreenOn(true);
        viewPager.setAdapter(pageManager);
        viewPager.setOffscreenPageLimit(pageManager.getItemCount());

        new TabLayoutMediator(tabs, viewPager, (tab, position) -> tab.setText(pageManager.getPageTitle(position))).attach();

        tabLayoutAnim = new TranslationAnim(tabs, TranslationAnim.Y_AXIS, TranslationAnim.ANIM_BACKWARD);

    }

    @SuppressLint("ClickableViewAccessibility")
    public void setOnTouchCallback(Runnable onTouchCallback) {
        viewPager.setOnTouchListener((v, event) -> {
            v.performClick();
            onTouchCallback.run();
            return true;
        });
    }

    public void setUserInputEnabled(boolean enabled) {
        viewPager.setUserInputEnabled(enabled);
        if (enabled)
            tabLayoutAnim.reverse();
        else
            tabLayoutAnim.start();
    }

    public Page getPage(int index) {
        return pageManager.getPage(index);
    }

}
