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
import java.util.HashMap;

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

    public void displayListedFile(@NonNull ListedFile listedFile) {
        fileEntries.addView(listedFile, 1);
    }

    public void displayFiles(LogFileIO.LogFile[] files) {
        if (files.length == 0)
            return;
        worker.post(new Runnable() {
            int c = 0;
            final HashMap<LogFileIO.LogFile, ListedFile> currentFiles = getCurrentFiles();

            @Override
            public void run() {
                LogFileIO.LogFile file = files[c++];
                if (currentFiles.containsKey(file)) {
                    ListedFile toUpdate = currentFiles.get(file);
                    if (toUpdate != null)
                        toUpdate.updateInfo();
                    currentFiles.remove(file);
                } else if (file != null) {
                    rootView.post(() -> displayListedFile(ListedFile.getInstance(rootView.getContext(), file)));
                }
                if (c == files.length) {
                    for (LogFileIO.LogFile f : currentFiles.keySet()) {
                        ListedFile toRemove = currentFiles.get(f);
                        if (toRemove != null)
                            rootView.post(() -> removeEntry(toRemove));
                    }
                } else {
                    worker.postDelayed(this, 100);
                }
            }
        });
    }

    public void updateAll() {
        for (int i = 0; i < fileEntries.getChildCount(); i++) {
            View view = fileEntries.getChildAt(i);
            if (view instanceof ListedFile) {
                ((ListedFile) view).updateInfo();
            }
        }
    }

    @SuppressWarnings("SameReturnValue")
    private boolean onDeleteAllButtonLongClick(View view) { // TODO: Add dialog to confirm again
        Toaster.showToast("Deleting all entries", Toaster.WARNING);
        deleteAllEntries();
        return true;
    }

    @NonNull
    private HashMap<LogFileIO.LogFile, ListedFile> getCurrentFiles() {
        ArrayList<ListedFile> views = getCurrentListedFiles();
        HashMap<LogFileIO.LogFile, ListedFile> files = new HashMap<>();
        for (ListedFile view : views) {
            files.put(view.getFile(), view);
        }
        return files;
    }

    @NonNull
    private ArrayList<ListedFile> getCurrentListedFiles() {
        ArrayList<ListedFile> views = new ArrayList<>();

        for (int i = 0; i < fileEntries.getChildCount(); i++) {
            View view = fileEntries.getChildAt(i);
            if (view instanceof ListedFile) {
                views.add((ListedFile) view);
            }
        }

        return views;
    }

    private void deleteAllEntries() {
        ArrayList<ListedFile> views = getCurrentListedFiles();

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
                worker.postDelayed(this, 100);
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

                    Spannable[] msgBlocks = ECUColor.colorMsgString(rootView.getContext(), msg);
                    Toaster.showToast("Showing file on console", Toaster.INFO);
                    showConsole.run();
                    worker.post(new Runnable() {
                        int c = 0;

                        @Override
                        public void run() {
                            Spannable span = msgBlocks[c++];
                            if (span != null)
                                console.systemPost("Log", TextUtils.concat(file.getTitle(), " - " + c + "/" + msgBlocks.length + "\n", span));
                            if (c != msgBlocks.length)
                                worker.postDelayed(this, 64);
                        }
                    });
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
