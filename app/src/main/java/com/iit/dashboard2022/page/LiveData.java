package com.iit.dashboard2022.page;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ecu.ECU;
import com.iit.dashboard2022.ecu.ECUStat;
import com.iit.dashboard2022.logging.Log;
import com.iit.dashboard2022.ui.UITester;
import com.iit.dashboard2022.ui.widget.LiveDataEntry;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class LiveData extends Page implements UITester.TestUI {

    private LinearLayout liveDataEntries1, liveDataEntries2;
    private ViewGroup rootView;

    private boolean enabled = true;

    private final Map<ECUStat, LiveDataEntry> entries = new ConcurrentHashMap<>();
    private boolean alt;

    private ECU ecu;

    public void setEcu(ECU ecu) {
        this.ecu = ecu;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.tab_live_data_layout, container, false);
        liveDataEntries1 = rootView.findViewById(R.id.liveDataEntries1);
        liveDataEntries2 = rootView.findViewById(R.id.liveDataEntries2);

        UITester.addTest(this);
        return rootView;
    }

    @UiThread
    public CompletableFuture<LiveDataEntry> post(ECUStat stat) {
        CompletableFuture<LiveDataEntry> future = new CompletableFuture<>();
        AtomicBoolean temp = new AtomicBoolean(true);
        while (temp.get()) {
            if (getActivity() == null) {
                continue;
            }
            getActivity().runOnUiThread(() -> {
                LiveDataEntry entry = entries.get(stat);
                String title = stat.getPrettyName() == null ? stat.getIdentifier() : stat.getPrettyName();
                if (entry == null) {
                    entry = new LiveDataEntry(title, rootView.getContext());
                    entries.put(stat, entry);
                    if (alt) {
                        liveDataEntries2.addView(entry);
                    } else {
                        liveDataEntries1.addView(entry);
                    }
                    alt = !alt;
                }
                temp.set(false);
                future.complete(entry);
            });
        }
        return future;
    }

    public void reset() {
        entries.values().forEach(LiveDataEntry::clear);
    }

    @NonNull
    @Override
    public String getTitle() {
        return "Live Data";
    }

    @Override
    public void onPageChange(boolean enter) {
        enabled = enter;
        if (enter) {
            for (ECUStat stat : ecu.getMessageHandler().getStatistics().values()) {
                if (entries.containsKey(stat)) {
                    continue;
                }
                new Handler(Looper.myLooper()).post(() -> {
                    try {
                        LiveDataEntry entry = post(stat).get();
                        stat.addMessageListener(c -> entry.setValue(c.get()));
                    } catch (ExecutionException | InterruptedException e) {
                        Log.getLogger().error("Error while updating statistic", e);
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        UITester.removeTest(this);
        super.onDestroy();
    }

    @Override
    public void testUI(float percent) {
        if (enabled) {
            if (percent == 0) {
                reset();
            }
        }
    }
}
