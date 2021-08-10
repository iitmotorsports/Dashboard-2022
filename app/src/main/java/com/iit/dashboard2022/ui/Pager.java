package com.iit.dashboard2022.ui;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.pages.PageManager;

public class Pager {
    public final ViewPager2 mainPager;
    public final TabLayout tabs;
    public final PageManager pageManager;

    public Pager(FragmentActivity activity) {
        this.mainPager = activity.findViewById(R.id.mainPager);
        this.tabs = activity.findViewById(R.id.tabs);

        pageManager = new PageManager(activity.getSupportFragmentManager());

        tabs.removeAllTabs();
        for (int i = 0; i < pageManager.getItemCount(); i++) {
            tabs.addTab(tabs.newTab());
        }

        mainPager.setKeepScreenOn(true);
        mainPager.setAdapter(pageManager);
        mainPager.setOffscreenPageLimit(pageManager.getItemCount());

        new TabLayoutMediator(tabs, mainPager, (tab, position) -> tab.setText(pageManager.getPageTitle(position))).attach();

    }

}
