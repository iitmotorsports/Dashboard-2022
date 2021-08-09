package com.iit.dashboard2022.ui.pages;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class PageManager extends FragmentStateAdapter {
    private final Page[] pages;

    public PageManager(@NonNull FragmentManager fragmentManager) {
        super(fragmentManager, new Lifecycle() {
            @Override
            public void addObserver(@NonNull LifecycleObserver observer) {
            }

            @Override
            public void removeObserver(@NonNull LifecycleObserver observer) {
            }

            @NonNull
            @Override
            public State getCurrentState() {
                return State.STARTED;
            }
        });
        ArrayList<Page> pageList = new ArrayList<>();

        /* ADD NEW PAGES HERE */
        pageList.add(new CarDashboard());
        pageList.add(new Logs());
        pageList.add(new LiveData());

        pages = pageList.toArray(new Page[0]);
    }

    @NonNull
    public String getPageTitle(int position) {
        return pages[position].getTitle();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return pages[position];
    }

    @Override
    public int getItemCount() {
        return pages.length;
    }
}
