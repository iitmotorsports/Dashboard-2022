package com.iit.dashboard2022.dialog;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ecu.ECU;
import com.iit.dashboard2022.util.PasteAPI;
import com.iit.dashboard2022.util.Toaster;

public class JSONDialog {
    private final AlertDialog dialog;

    public JSONDialog(Activity activity, ECU frontECU) {
        MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(activity, R.style.Theme_Dashboard2022_Dialog);
        View mView = activity.getLayoutInflater().inflate(R.layout.dialog_json_selection, null);

        Button downloadBtn = mView.findViewById(R.id.downloadBtn);
        Button scanBtn = mView.findViewById(R.id.scanBtn);
        Button findBtn = mView.findViewById(R.id.findBtn);
        Button delBtn = mView.findViewById(R.id.delBtn);
        LinearLayout jsonDialogMainLayout = mView.findViewById(R.id.jsonDialogMainLayout);

        mBuilder.setView(mView);
        dialog = mBuilder.create();

        downloadBtn.setEnabled(PasteAPI.checkInternetConnection(activity));
        downloadBtn.setOnClickListener(v -> {
            PasteAPI.getLastJSONPaste(response -> {
                boolean pasteAPILoad = frontECU.loadJSONString(response);
                Toaster.showToast(pasteAPILoad ? "Loaded JSON from Paste API" : "Failed to load JSON from Paste API", pasteAPILoad ? Toaster.SUCCESS : Toaster.ERROR);
            });
            dialog.dismiss();
        });

//        scanBtn.setOnClickListener(v -> stream.updateQRJson());
        findBtn.setOnClickListener(v -> {
            frontECU.requestJSONFile();
            dialog.dismiss();
        });
        delBtn.setOnClickListener(v -> frontECU.clear());

        scanBtn.setOnClickListener(v -> frontECU.requestJSONFromUSBSerial());
        scanBtn.setText(R.string.USB);

        // TODO: Figure out this whole dialog sizing thing
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void hideUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog.getWindow().getInsetsController().hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    public void showDialog() {
        if (!dialog.isShowing()) {
            hideUI();
            dialog.show();
        }
    }
}
