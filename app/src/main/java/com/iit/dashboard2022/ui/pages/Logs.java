package com.iit.dashboard2022.ui.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iit.dashboard2022.R;

public class Logs extends Page {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.tab_logs_layout, container, false);
        return rootView;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "Logs";
    }
}
