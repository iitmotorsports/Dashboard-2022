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

    private LinearLayout liveDataEntries1, liveDataEntries2;
    private ViewGroup rootView;

    private LiveDataEntry[] entries;
    private long[] values;
    private boolean enabled = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.tab_live_data_layout, container, false);
        liveDataEntries1 = rootView.findViewById(R.id.liveDataEntries1);
        liveDataEntries2 = rootView.findViewById(R.id.liveDataEntries2);

        UITester.addTest(this);
        return rootView;
    }

    public long[] setMessageTitles(@NonNull String[] titles) {
        this.values = null;
        this.entries = null;

        liveDataEntries1.removeAllViews();
        liveDataEntries2.removeAllViews();
        long[] values = new long[titles.length];
        LiveDataEntry[] entries = new LiveDataEntry[titles.length];

        boolean alt = true;
        int i = 0;
        for (String title : titles) {
            entries[i] = new LiveDataEntry(title, rootView.getContext());
            if (alt) {
                liveDataEntries1.addView(entries[i++]);
            } else {
                liveDataEntries2.addView(entries[i++]);
            }
            alt = !alt;
        }

        this.entries = entries;
        this.values = values;

        reset();
        for (i = 0; i < values.length; i++) {
            entries[i].setValue(values[i]);
        }

        return values;
    }

    public void reset() {
        if (values != null) {
            Arrays.fill(values, 0);
        }
        if (entries != null) {
            for (LiveDataEntry lde : entries) {
                lde.clear();
            }
        }
    }

    public void updateValue(int index) {
        if (enabled && values != null) {
            entries[index].setValue(values[index]);
        }
    }

    public void updateValues() {
        if (enabled && values != null) {
            for (int i = 0; i < values.length; i++) {
                entries[i].setValue(values[i]);
            }
        }
    }

    @NonNull
    @Override
    public String getTitle() {
        return "Live Data";
    }

    @Override
    public void onPageChange(boolean enter) {
        enabled = enter;
    }

    @Override
    public void onDestroy() {
        UITester.removeTest(this);
        super.onDestroy();
    }

    @Override
    public void testUI(float percent) {
        if (enabled && values != null) {
            if (percent == 0) {
                reset();
            } else {
                for (int i = 0; i < values.length; i++) {
                    if (UITester.Rnd.nextInt(100) < 50 * percent) {
                        entries[i].setValue((long) (percent * 100));
                    }
                }
            }
        }
    }
}
