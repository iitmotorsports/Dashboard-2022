package com.iit.dashboard2022.ui.widget;

import android.view.View;
import androidx.collection.ArraySet;

public class LiveDataSelector {
    private final ArraySet<LiveDataEntry> entries = new ArraySet<>();
    private SelectionChangedListener selectionChangedListener;

    private void onClick(View view) {
        LiveDataEntry current = (LiveDataEntry) view;
        for (LiveDataEntry entry : entries) {
            if (!current.equals(entry)) {
                entry.setActive(false);
            }
        }
        current.setActive(true);
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(current);
        }
        WidgetUpdater.post();
    }

    public void setSelectionChangedListener(SelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    public void addEntry(LiveDataEntry lde) {
        lde.setOnClickListener(this::onClick);
        entries.add(lde);
    }

    public interface SelectionChangedListener {
        void onSelectionChanged(LiveDataEntry newSelection);
    }
}
