package com.iit.dashboard2022.page;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ecu.ECUColor;
import com.iit.dashboard2022.ecu.ECULogger;
import com.iit.dashboard2022.ui.widget.ListedFile;
import com.iit.dashboard2022.ui.widget.SideButton;
import com.iit.dashboard2022.ui.widget.console.ConsoleWidget;
import com.iit.dashboard2022.util.LogFileIO;
import com.iit.dashboard2022.util.PasteAPI;
import com.iit.dashboard2022.util.Toaster;

import java.util.ArrayList;

public class Logs extends Page {
    private final static HandlerThread workerThread = new HandlerThread("Logging Thread");
    private static Handler worker;

    private ViewGroup rootView;
    private Runnable showConsole;
    private ConsoleWidget console;
    private LinearLayout fileEntries;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.tab_logs_layout, container, false);

        SideButton deleteAllButton = rootView.findViewById(R.id.deleteAllButton);
        SideButton updateAllButton = rootView.findViewById(R.id.updateAllButton);
        fileEntries = rootView.findViewById(R.id.fileEntries);

        ListedFile.setGlobalFileListListener(this::onListedFileAction);
        deleteAllButton.setOnClickListener(v -> Toaster.showToast("Hold to confirm", Toaster.INFO));
        deleteAllButton.setOnLongClickListener(this::onDeleteAllButtonLongClick);
        updateAllButton.setOnClickListener(v -> updateAll());

        if (worker == null) {
            workerThread.start();
            worker = new Handler(workerThread.getLooper());
        }

        return rootView;
    }

    public void attachConsole(ConsoleWidget console, Runnable showConsole) {
        this.console = console;
        this.showConsole = showConsole;
    }

    public void displayFile(@NonNull LogFileIO.LogFile file) {
        fileEntries.addView(ListedFile.getInstance(rootView.getContext(), file));
    }

    public void displayFiles(LogFileIO.LogFile[] files) {
        for (LogFileIO.LogFile file : files) {
            if (file != null)
                displayFile(file);
        }
    }

    public void updateAll() {
        for (int i = 0; i < fileEntries.getChildCount(); i++) {
            View view = fileEntries.getChildAt(i);
            if (view instanceof ListedFile) {
                ((ListedFile) view).updateInfo();
            }
        }
    }

    private boolean onDeleteAllButtonLongClick(View view) { // TODO: Add dialog to confirm again
        Toaster.showToast("Deleting all entries", Toaster.WARNING);
        deleteAllEntries();
        return true;
    }

    private void deleteAllEntries() {
        ArrayList<ListedFile> views = new ArrayList<>();

        for (int i = 0; i < fileEntries.getChildCount(); i++) {
            View view = fileEntries.getChildAt(i);
            if (view instanceof ListedFile) {
                views.add((ListedFile) view);
            }
        }

        worker.post(new Runnable() {
            @Override
            public void run() {
                ListedFile view = views.get(0);
                views.remove(view);
                LogFileIO.LogFile file = view.getFile();
                if (file != null && file.delete())
                    rootView.post(() -> removeEntry(view));
                if (views.size() == 0) {
                    Toaster.showToast("Done deleting", Toaster.INFO);
                    return;
                }
                worker.postDelayed(this, 50);
            }
        });
    }

    private void onListedFileAction(@NonNull ListedFile listedFile, @NonNull ListedFile.ListedFileAction action) {
        switch (action) {
            case SHOW:
                if (console == null) {
                    Toaster.showToast("No console attached", Toaster.ERROR);
                    return;
                }
                ListedFile.deselectActive();
                worker.post(() -> {
                    console.clear();

                    LogFileIO.LogFile file = listedFile.getFile();
                    if (file == null) {
                        Toaster.showToast("File returned null", Toaster.ERROR);
                        return;
                    }

                    String msg = ECULogger.interpretLogFile(file);
                    if (msg.length() == 0) {
                        Toaster.showToast("File returned empty", Toaster.WARNING);
                        return;
                    }

                    Spannable str = ECUColor.colorMsgString(rootView.getContext(), msg);
                    console.systemPost("Log", TextUtils.concat(file.getTitle(), "\n", str));
                    Toaster.showToast("Showing file on console", Toaster.INFO);
                    showConsole.run();
                });
                break;
            case UPLOAD:
                Toaster.showToast("Uploading File", Toaster.INFO);
                worker.post(() -> PasteAPI.uploadPaste(ECULogger.stringifyLogFile(listedFile.getFile())));
                break;
            case DELETE:
                Toaster.showToast("Deleting File", Toaster.INFO);
                LogFileIO.LogFile file = listedFile.getFile();
                if (file != null) {
                    if (file.delete()) {
                        Toaster.showToast("File deleted", Toaster.SUCCESS);
                        removeEntry(listedFile);
                    } else {
                        Toaster.showToast("Failed to delete file", Toaster.ERROR);
                    }
                    return;
                }
                Toaster.showToast("File returned null", Toaster.WARNING);
                removeEntry(listedFile);
                break;
        }
    }

    private void removeEntry(@NonNull ListedFile view) {
        fileEntries.removeView(view);
        view.recycle();
    }

    @NonNull
    @Override
    public String getTitle() {
        return "Logs";
    }

}
