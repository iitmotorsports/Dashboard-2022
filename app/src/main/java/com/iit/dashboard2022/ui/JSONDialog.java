package com.iit.dashboard2022.ui;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ecu.ECU;
import com.iit.dashboard2022.ecu.ECUMessageHandler;
import com.iit.dashboard2022.util.HawkUtil;

public class JSONDialog {
    private final AlertDialog dialog;

    public JSONDialog(Activity activity, ECU frontECU) {
        MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(activity, R.style.Theme_Dashboard2022_Dialog);
        View mView = activity.getLayoutInflater().inflate(R.layout.dialog_json_selection, null);

        Button downloadBtn = mView.findViewById(R.id.downloadBtn);
        Button scanBtn = mView.findViewById(R.id.scanBtn);
        Button findBtn = mView.findViewById(R.id.findBtn);
        Button delBtn = mView.findViewById(R.id.delBtn);

        mBuilder.setView(mView);
        dialog = mBuilder.create();

        downloadBtn.setEnabled(HawkUtil.checkInternetConnection(activity));
        downloadBtn.setOnClickListener(v -> dialog.dismiss());

        findBtn.setOnClickListener(v -> {
            frontECU.getMessageHandler().load(ECUMessageHandler.MapHandler.SELECTOR);
            dialog.dismiss();
        });
        delBtn.setOnClickListener(v -> frontECU.getMessageHandler().clear());
        scanBtn.setOnClickListener(v -> frontECU.getMessageHandler().load(ECUMessageHandler.MapHandler.ECU));
        scanBtn.setText(R.string.USB);

        // TODO: Figure out this whole dialog sizing thing
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public void showDialog() {
        if (!dialog.isShowing()) {
            HawkUtil.setWindowFlags(dialog.getWindow());
            dialog.show();
        }
    }
}
