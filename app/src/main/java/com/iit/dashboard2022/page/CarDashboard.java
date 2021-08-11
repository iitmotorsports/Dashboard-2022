package com.iit.dashboard2022.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.TestUI;
import com.iit.dashboard2022.ui.widget.gauge.SpeedGauge;

public class CarDashboard extends Page implements TestUI {
    SpeedGauge sgL, sgR;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.tab_dashboard_layout, container, false);
        sgL = rootView.findViewById(R.id.speedGaugeLeft);
        sgR = rootView.findViewById(R.id.speedGaugeRight);
        return rootView;
    }

    public void setPercentage(float percent) {
        sgL.setPercent(percent);
        sgR.setPercent(percent);
    }

    @NonNull
    @Override
    public String getTitle() {
        return "Dashboard";
    }

    @Override
    public void testUI(float percent) {
        setPercentage(percent);
    }
}
