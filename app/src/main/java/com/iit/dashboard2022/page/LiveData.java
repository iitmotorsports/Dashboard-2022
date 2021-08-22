package com.iit.dashboard2022.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.UITester;
import com.iit.dashboard2022.ui.widget.LiveDataEntry;

import java.util.Arrays;

public class LiveData extends Page implements UITester.TestUI {

    private LinearLayout liveDataEntries;
    private ViewGroup rootView;

    private LiveDataEntry[] entries;
    private String[] values;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.tab_live_data_layout, container, false);
        liveDataEntries = rootView.findViewById(R.id.liveDataEntries);

        UITester.addTest(this);
        return rootView;
    }

    public String[] setMessageTitles(@NonNull String[] titles) {
        this.values = null;
        this.entries = null;

        liveDataEntries.removeAllViews();
        String[] values = new String[titles.length];
        LiveDataEntry[] entries = new LiveDataEntry[titles.length];

        int i = 0;
        for (String title : titles) {
            entries[i] = new LiveDataEntry(title, rootView.getContext());
            liveDataEntries.addView(entries[i++]);
        }

        this.entries = entries;
        this.values = values;

        resetValues();

        return values;
    }

    public void resetValues() {
        if (values != null) {
            Arrays.fill(values, "0");
            updateValues();
        }
    }

    public void updateValue(int index) {
        if (values != null)
            rootView.post(() -> entries[index].setValue(values[index]));
    }

    public void updateValues() {
        if (values != null)
            rootView.post(() -> {
                for (int i = 0; i < values.length; i++) {
                    entries[i].setValue(values[i]);
                }
            });
    }

    @NonNull
    @Override
    public String getTitle() {
        return "Live Data";
    }

    @Override
    public void onDestroy() {
        UITester.removeTest(this);
        super.onDestroy();
    }

    @Override
    public void testUI(float percent) {
        if (values != null)
            if (percent == 0)
                resetValues();
            else
                for (int i = 0; i < values.length; i++) {
                    if (UITester.Rnd.nextInt(100) < 50 * percent) {
                        entries[i].setValue(Float.toString(((int) (percent * 100)) / 100f));
                    }
                }
    }
}
