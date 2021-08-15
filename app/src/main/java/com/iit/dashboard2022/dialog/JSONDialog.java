package com.iit.dashboard2022.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.iit.dashboard2022.ECU.ECU;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.util.PasteAPI;
import com.iit.dashboard2022.util.Toaster;

public class JSONDialog {
    private final AlertDialog dialog;

    public JSONDialog(Activity activity, ECU frontECU) {
        MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(activity);
        View mView = activity.getLayoutInflater().inflate(R.layout.dialog_json_selection, null);

        Button downloadBtn = mView.findViewById(R.id.downloadBtn);
        Button scanBtn = mView.findViewById(R.id.scanBtn);
        Button findBtn = mView.findViewById(R.id.findBtn);
        Button delBtn = mView.findViewById(R.id.delBtn);
        ConstraintLayout jsonDialogMainLayout = mView.findViewById(R.id.jsonDialogMainLayout);

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
        findBtn.setOnClickListener(v -> frontECU.requestJSONFile());
        delBtn.setOnClickListener(v -> frontECU.clear());

    }

    public void showDialog() {
        if (!dialog.isShowing())
            dialog.show();
    }
}
