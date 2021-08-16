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
import com.iit.dashboard2022.ui.widget.Indicators;
import com.iit.dashboard2022.ui.widget.StartLight;
import com.iit.dashboard2022.ui.widget.gauge.LinearGauge;
import com.iit.dashboard2022.ui.widget.gauge.SpeedGauge;

public class CarDashboard extends Page implements UITester.TestUI {
    private StartLight dashStartLight;
    private SpeedGauge sgL, sgR;
    private LinearGauge batteryGauge, powerGauge;
    private TextView speedometer;
    private Indicators indicators;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.tab_dashboard_layout, container, false);
        sgL = rootView.findViewById(R.id.speedGaugeLeft);
        sgR = rootView.findViewById(R.id.speedGaugeRight);
        batteryGauge = rootView.findViewById(R.id.batteryGauge);
        powerGauge = rootView.findViewById(R.id.powerGauge);
        speedometer = rootView.findViewById(R.id.speedometer);
        indicators = rootView.findViewById(R.id.indicators);
        dashStartLight = rootView.findViewById(R.id.dashStartLight);
        UITester.addTest(this);
        return rootView;
    }

    @Override
    public void onDestroy() {
        UITester.removeTest(this);
        super.onDestroy();
    }

    public void setSpeedPercentage(float percent) {
        sgL.setPercent(percent);
        sgR.setPercent(percent);
    }

    public void setBatteryPercentage(float percent){
        batteryGauge.setPercent(percent);
    }

    public void setPowerPercentage(float percent){
        powerGauge.setPercent(percent);
    }

    public void setSpeedValue(long mph) {
        speedometer.setText(String.valueOf(mph));
    }

    public void setIndicator(Indicators.Indicator indicator, boolean enabled) {
        indicators.setIndicator(indicator, enabled);
    }

    public void setLagTime(long ms) {
        indicators.setLagTime(ms);
    }

    public void setStartLight(boolean isOn) {
        dashStartLight.setLight(isOn);
    }

    public void setState(CharSequence state) {
        dashStartLight.setState(state);
    }

    @NonNull
    @Override
    public String getTitle() {
        return "Dashboard";
    }

    @Override
    public void testUI(float percent) {
        setSpeedPercentage(percent);
        setBatteryPercentage(percent);
        setPowerPercentage(percent);
        setSpeedValue((int) (300 * percent));
    }
}
