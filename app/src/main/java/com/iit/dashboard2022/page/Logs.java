package com.iit.dashboard2022.page;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.common.collect.Lists;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.logging.Log;
import com.iit.dashboard2022.logging.LogFile;
import com.iit.dashboard2022.logging.ToastLevel;
import com.iit.dashboard2022.ui.widget.ListedFile;
import com.iit.dashboard2022.ui.widget.SideButton;
import com.iit.dashboard2022.ui.widget.console.ConsoleWidget;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
        deleteAllButton.setOnClickListener(v -> Log.toast("Hold to confirm", ToastLevel.INFO));
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

    public void displayFiles(Collection<LogFile> files) {
        if (files.size() == 0) {
            return;
        }

        final HashSet<LogFile> fileHash = new HashSet<>(getCurrentFiles().keySet());
        for (LogFile f : files) {
            worker.post(() -> {
                if (!fileHash.contains(f)) {
                    rootView.post(() -> displayListedFile(ListedFile.getInstance(rootView.getContext(), f)));
                }
            });
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

    @SuppressWarnings("SameReturnValue")
    private boolean onDeleteAllButtonLongClick(View view) { // TODO: Add dialog to confirm again
        Log.toast("Deleting all entries", ToastLevel.WARNING);
        deleteAllEntries();
        return true;
    }

    @NonNull
    private HashMap<LogFile, ListedFile> getCurrentFiles() {
        List<ListedFile> views = getCurrentListedFiles();
        HashMap<LogFile, ListedFile> files = new HashMap<>();
        for (ListedFile view : views) {
            files.put(view.getFile(), view);
        }
        return files;
    }

    @NonNull
    private List<ListedFile> getCurrentListedFiles() {
        List<ListedFile> views = Lists.newArrayList();

        for (int i = 0; i < fileEntries.getChildCount(); i++) {
            View view = fileEntries.getChildAt(i);
            if (view instanceof ListedFile) {
                views.add((ListedFile) view);
            }
        }

        return views;
    }

    private void deleteAllEntries() {
        List<ListedFile> views = getCurrentListedFiles();
        worker.post(() -> {
            int preSize = views.size();
            Lists.newArrayList(views).forEach(view -> {
                LogFile file = view.getFile();
                if (file == null || (Log.getInstance().getActiveLogFile() != null && file.getEpochSeconds() == Log.getInstance().getActiveLogFile().getEpochSeconds())) {
                    return;
                }
                views.remove(view);
                rootView.post(() -> removeEntry(view));
            });
            if (views.size() == preSize) {
                Log.toast("No files deleted", ToastLevel.INFO);
            } else {
                Log.toast("Done deleting", ToastLevel.INFO);
            }
        });
    }

    private void onListedFileAction(@NonNull ListedFile listedFile, @NonNull ListedFile.ListedFileAction action) {
        switch (action) {
            case SHOW:
                // TODO: SHOW
                /*
                if (console == null) {
                    Log.toast("No console attached", ToastLevel.ERROR);
                    return;
                }
                ListedFile.deselectActive();
                worker.post(() -> {
                    console.post(() -> console.clear());

                    LogFile file = listedFile.getFile();
                    if (file == null) {
                        Log.toast("File returned null", ToastLevel.ERROR);
                        return;
                    }

                    String msg = ECULogger.interpretLogFile(file);
                    if (msg.length() == 0) {
                        Log.toast("File returned empty", ToastLevel.WARNING);
                        return;
                    }

                    Spannable[] msgBlocks = ECUColor.colorMsgString(rootView.getContext(), msg);
                    Log.toast("Showing file on console", ToastLevel.INFO);
                    showConsole.run();
                    worker.post(new Runnable() {
                        int c = 0;

                        @Override
                        public void run() {
                            Spannable span = msgBlocks[c++];
                            if (span != null) {
                                console.systemPost("Log", TextUtils.concat(file.getTitle(), " - " + c + "/" + msgBlocks.length + "\n", span));
                            }
                            if (c != msgBlocks.length) {
                                worker.postDelayed(this, 64);
                            }
                        }
                    });
                });

                break;

                 */
                break;
            case UPLOAD:
                Log.toast("Uploading File", ToastLevel.INFO);
                worker.post(() -> Log.getInstance().postToCabinet(listedFile.getFile()));
                break;
            case DELETE:
                Log.toast("Deleting File", ToastLevel.INFO);
                LogFile file = listedFile.getFile();
                if (file != null) {
                    if (file.delete()) {
                        Log.toast("File deleted", ToastLevel.SUCCESS);
                        removeEntry(listedFile);
                    } else {
                        Log.toast("Failed to delete file", ToastLevel.ERROR);
                    }
                    return;
                }
                Log.toast("File returned null", ToastLevel.WARNING);
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
