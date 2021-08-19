package com.iit.dashboard2022.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.anim.AnimSetting;
import com.iit.dashboard2022.util.LogFileIO;
import com.iit.dashboard2022.util.Toaster;

import java.util.ArrayList;
import java.util.List;

public class ListedFile extends FrameLayout {
    private static final List<ListedFile> inactiveEntries = new ArrayList<>();
    private static GlobalFileListListener globalFileListListener;

    private static ValueAnimator animatorShow, animatorHide;
    private static ListedFile lastSelected, animViewShow, animViewHide;

    public enum ListedFileAction {
        SHOW,
        UPLOAD,
        DELETE,
    }

    public interface GlobalFileListListener {
        void onListedFileAction(@NonNull ListedFile listedFile, @NonNull ListedFileAction action);
    }

    public static void setGlobalFileListListener(GlobalFileListListener globalFileListListener) {
        ListedFile.globalFileListListener = globalFileListListener;
    }

    public static ListedFile getInstance(@NonNull Context context, @NonNull LogFileIO.LogFile file) {
        if (inactiveEntries.size() == 0)
            new ListedFile(context);

        ListedFile entry = inactiveEntries.get(0);
        entry.setFile(file);

        if (lastSelected == null) // Nothing has been selected yet, select this first
            entry.select();

        if (entry.getParent() != null) {
            ((ViewGroup) entry.getParent()).removeView(entry);
        }

        return entry;
    }

    public static void deselectActive() {
        if (lastSelected != null)
            lastSelected.deselect();
    }

    private final MaterialButton showButton, uploadButton, deleteButton;
    private final ConstraintLayout listedFileMain;
    private final MaterialTextView fileInfo;
    private LogFileIO.LogFile file;

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

        listedFileMain = findViewById(R.id.listedFileMain);

        listedFileMain.setOnClickListener(this::onViewPressed);

        showButton.setOnClickListener(this::onShowPressed);
        uploadButton.setOnClickListener(this::onUploadPressed);
        deleteButton.setOnClickListener(this::onDeletePressed);
        deleteButton.setOnLongClickListener(this::onDeleteLongPressed);

        recycle();

        if (animatorShow == null) {
            animatorShow = ValueAnimator.ofFloat(1.0f);
            animatorShow.setDuration(AnimSetting.ANIM_DURATION / 2);
            animatorShow.setInterpolator(AnimSetting.ANIM_DEFAULT_INTERPOLATOR);
            animatorShow.addUpdateListener(animation -> {
                float fraction = animation.getAnimatedFraction();
                animViewShow.showButton.setAlpha(fraction);
                animViewShow.uploadButton.setAlpha(fraction);
                animViewShow.deleteButton.setAlpha(fraction);
                animViewShow.listedFileMain.getBackground().setAlpha(64 + (int) (fraction * 191));
            });
            animatorShow.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    animViewShow.showButton.setVisibility(VISIBLE);
                    animViewShow.uploadButton.setVisibility(VISIBLE);
                    animViewShow.deleteButton.setVisibility(VISIBLE);
                }
            });
            animatorHide = ValueAnimator.ofFloat(1.0f);
            animatorHide.setDuration(AnimSetting.ANIM_DURATION / 2);
            animatorHide.setInterpolator(AnimSetting.ANIM_DEFAULT_INTERPOLATOR);
            animatorHide.addUpdateListener(animation -> {
                float fraction = animation.getAnimatedFraction();
                animViewHide.showButton.setAlpha(fraction);
                animViewHide.uploadButton.setAlpha(fraction);
                animViewHide.deleteButton.setAlpha(fraction);
                animViewHide.listedFileMain.getBackground().setAlpha(64 + (int) (fraction * 191));
            });
            animatorHide.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animViewHide.showButton.setVisibility(GONE);
                    animViewHide.uploadButton.setVisibility(GONE);
                    animViewHide.deleteButton.setVisibility(GONE);
                }
            });
        }
    }

    public void destroy() {
        recycle();
        inactiveEntries.remove(this);
    }

    public void recycle() {
        if (lastSelected == this)
            lastSelected = null;
        if (animViewShow == this) {
            animatorShow.cancel();
            animViewShow = null;
        }
        if (animViewHide == this) {
            animViewHide = null;
            animatorHide.cancel();
        }
        showButton.setVisibility(GONE);
        uploadButton.setVisibility(GONE);
        deleteButton.setVisibility(GONE);
        listedFileMain.getBackground().setAlpha(64);
        file = null;
        setVisibility(GONE);
        inactiveEntries.add(this);
    }

    public void deselect() {
        lastSelected = null;
        animateVisibility(false);
    }

    public void select() {
        if (lastSelected == this)
            return;
        if (lastSelected != null)
            lastSelected.deselect();
        lastSelected = this;
        animateVisibility(true);
    }

    public void setFile(LogFileIO.LogFile file) {
        if (!inactiveEntries.contains(this))
            return;
        inactiveEntries.remove(this);
        setVisibility(VISIBLE);
        this.file = file;
        updateInfo();
    }

    public void updateInfo() {
        try {
            fileInfo.setText(file.getTitle());
        } catch (Exception e) {
            e.printStackTrace();
            if (fileInfo != null) {
                fileInfo.setText(R.string.listed_file_fail);
            }
        }
    }

    @Nullable
    public LogFileIO.LogFile getFile() {
        return file;
    }


    private void notifyListener(ListedFileAction listedFileAction) {
        if (file != null && globalFileListListener != null)
            globalFileListListener.onListedFileAction(this, listedFileAction);
    }

    private void onShowPressed(View v) {
        notifyListener(ListedFileAction.SHOW);
    }

    private void onUploadPressed(View v) {
        notifyListener(ListedFileAction.UPLOAD);
    }

    private void onDeletePressed(View v) {
        Toaster.showToast("Hold to confirm", Toaster.INFO);
    }

    private boolean onDeleteLongPressed(View v) {
        if (file.isActiveFile()) {
            Toaster.showToast("Cannot delete active file", Toaster.WARNING);
            return true;
        }
        notifyListener(ListedFileAction.DELETE);
        return true;
    }

    private void onViewPressed(View view) {
        select();
    }

    private void animateVisibility(boolean visible) {
        if (visible) {
            if (animViewShow != null)
                animatorShow.cancel();
            animViewShow = this;
            animatorShow.start();
        } else {
            if (animViewHide != null)
                animatorHide.cancel();
            animViewHide = this;
            animatorHide.reverse();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }
}
