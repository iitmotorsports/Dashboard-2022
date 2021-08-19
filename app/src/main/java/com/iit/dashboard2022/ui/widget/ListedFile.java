package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.iit.dashboard2022.R;

import java.io.File;
import java.util.Locale;

public class ListedFile extends FrameLayout {
    private static String fileListFormatKB, fileListFormatMB;
    private static GlobalFileListListener globalFileListListener;

    public enum ListedFilePress {
        SHOW,
        UPLOAD,
        DELETE,
    }

    public interface GlobalFileListListener {
        void fileUpdate(File file, ListedFilePress action);
    }

    File file;
    MaterialTextView fileInfo;
    MaterialButton showButton, uploadButton, deleteButton;

    public ListedFile(@NonNull Context context) {
        this(context, null);
    }

    public ListedFile(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListedFile(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.widget_listed_file, this);

        fileInfo = findViewById(R.id.fileInfo);
        showButton = findViewById(R.id.showButton);
        uploadButton = findViewById(R.id.uploadButton);
        deleteButton = findViewById(R.id.deleteButton);

        if (fileListFormatKB == null || fileListFormatMB == null) {
            fileListFormatKB = context.getString(R.string.listed_file_format_kb);
            fileListFormatMB = context.getString(R.string.listed_file_format_mb);
        }

        showButton.setOnClickListener(this::onShowPressed);
        uploadButton.setOnClickListener(this::onUploadPressed);
        deleteButton.setOnLongClickListener(this::onDeleteLongPressed);
    }

    public static void setGlobalFileListListener(GlobalFileListListener globalFileListListener) {
        ListedFile.globalFileListListener = globalFileListListener;
    }

    public void setFile(File file) {
        try {
            this.file = file;
            long bytes = file.getTotalSpace();
            double kb = bytes / 1000.0;
            if (kb >= 1000) {
                double mb = kb / 1000.0;
                fileInfo.setText(String.format(Locale.US, fileListFormatMB, file.getName(), mb));
            } else {
                fileInfo.setText(String.format(Locale.US, fileListFormatKB, file.getName(), kb));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (fileInfo != null) {
                fileInfo.setText(R.string.listed_file_fail);
            }
        }
    }

    private void notifyListener(ListedFilePress listedFilePress) {
        if (file != null && globalFileListListener != null)
            globalFileListListener.fileUpdate(file, listedFilePress);
    }

    private void onShowPressed(View v) {
        notifyListener(ListedFilePress.SHOW);
    }

    private void onUploadPressed(View v) {
        notifyListener(ListedFilePress.UPLOAD);
    }

    private boolean onDeleteLongPressed(View v) {
        notifyListener(ListedFilePress.DELETE);
        return true;
    }
}
