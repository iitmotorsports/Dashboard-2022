package com.iit.dashboard2022.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.UITester;

public class Statistics extends Page implements UITester.TestUI {

    private ViewGroup rootView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.tab_statistics_layout, container, false);

        return rootView;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "Statistics";
    }

    @Override
    public void testUI(float percent) {

    }
}
