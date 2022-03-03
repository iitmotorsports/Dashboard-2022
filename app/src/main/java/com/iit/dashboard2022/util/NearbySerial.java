package com.iit.dashboard2022.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.iit.dashboard2022.R;

import java.util.concurrent.atomic.AtomicBoolean;

public class NearbySerial extends SerialCom { // TODO: ditch google API
    private static final String USERNAME = "Nearby Serial Stream";
    private static final String SERVICE_ID = "RAW_DATA_STREAM";

    private final Activity activity;
    private final ConnectionsClient client;
    private final AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build();
    private final DiscoveryOptions discoveryOptions = new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build();
    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
            newConnData(payload.asBytes());
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
        }
    };
    private AlertDialog acceptDialog;
    private TextView authText, connName;
    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo info) {
            pendingEndpointId = endpointId;
            String authTok = info.getAuthenticationDigits();
            String endName = info.getEndpointName();
            activity.runOnUiThread(() -> {
                authText.setText(authTok);
                connName.setText(String.format(" %s", endName));
                showDialog();
            });
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
            switch (result.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    Toaster.showToast("Nearby serial connected", Toaster.Status.SUCCESS);
                    setConnStatus(Attached | Opened);
                    client.stopAdvertising();
                    client.stopDiscovery();
                    return;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    Toaster.showToast("Nearby serial connection rejected", Toaster.Status.ERROR);
                    acceptDialog.dismiss();
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    Toaster.showToast("Nearby serial connection dropped", Toaster.Status.ERROR);
                    break;
                default:
                    Toaster.showToast("Nearby serial stream unknown error", Toaster.Status.ERROR);
            }
            setConnStatus(Detached | Closed);
        }

        @Override
        public void onDisconnected(@NonNull String s) {
            setConnStatus(Detached | Closed);
            Toaster.showToast("Nearby serial disconnected", Toaster.Status.WARNING);
        }

    };
    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            Toaster.showToast("Found Endpoint, wait for connection", Toaster.Status.INFO);
            client.requestConnection(USERNAME, endpointId, connectionLifecycleCallback);
        }

        @Override
        public void onEndpointLost(@NonNull String s) {
            Toaster.showToast("Lost Endpoint", Toaster.Status.INFO);
        }
    };
    private String currentEndpointId, pendingEndpointId = "";

    public NearbySerial(Activity activity) {
        this.activity = activity;

        client = Nearby.getConnectionsClient(activity.getApplicationContext());
        createAcceptDialog();
    }

    public static Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    private void acceptConnection(String endpointId) {
        Toaster.showToast("Connection accepted with " + endpointId, Toaster.Status.SUCCESS);
        currentEndpointId = endpointId;
        client.acceptConnection(endpointId, payloadCallback);
    }

    private void rejectConnection(String endpointId) {
        Toaster.showToast("Connection Rejected!", Toaster.Status.ERROR);
        client.rejectConnection(endpointId);
    }

    private void createAcceptDialog() {
        MaterialAlertDialogBuilder mBuilder = new MaterialAlertDialogBuilder(activity, R.style.Theme_Dashboard2022_Dialog);
        View mView = activity.getLayoutInflater().inflate(R.layout.dialog_nearby_prompt, null);

        Button acceptBtn = mView.findViewById(R.id.acceptBtn);
        Button rejectBtn = mView.findViewById(R.id.rejectBtn);
        authText = mView.findViewById(R.id.authText);
        connName = mView.findViewById(R.id.connName);

        mBuilder.setView(mView);
        acceptDialog = mBuilder.create();

        AtomicBoolean accepted = new AtomicBoolean(false);

        acceptBtn.setOnClickListener(v -> {
            accepted.set(true);
            acceptConnection(pendingEndpointId);
            acceptDialog.dismiss();
        });

        rejectBtn.setOnClickListener(v -> {
            rejectConnection(pendingEndpointId);
            acceptDialog.dismiss();
        });

        acceptDialog.setOnDismissListener(dialog -> {
            if (!accepted.get()) {
                rejectConnection(pendingEndpointId);
            }
            accepted.set(false);
        });

        acceptDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void showDialog() {
        acceptDialog.show();
        HawkUtil.setWindowFlags(acceptDialog.getWindow());
    }

    private void startAdvertising() {
        client.startAdvertising(USERNAME, SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener((Void unused) -> Toaster.showToast("Searching for receiver", Toaster.Status.INFO))
                .addOnFailureListener((Exception e) -> {
                    Toaster.showToast("Failed to start search for a receiver", Toaster.Status.ERROR, Toast.LENGTH_LONG);
                    e.printStackTrace();
                });
    }

    private void startDiscovery() {
        client.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener((Void unused) -> Toaster.showToast("Searching for broadcaster", Toaster.Status.INFO))
                .addOnFailureListener((Exception e) -> {
                    Toaster.showToast("Failed to start search for a broadcaster", Toaster.Status.ERROR, Toast.LENGTH_LONG);
                    e.printStackTrace();
                });
    }

    public boolean getLocationPerms() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
            return false;
        }
        if (!isLocationEnabled(activity)) {
            Toaster.showToast("Location services must be on!", Toaster.Status.ERROR);
            return false;
        }
        return true;
    }

    @Override
    public boolean open() {
        if (isOpen()) {
            return true;
        }
        if (getLocationPerms()) {
            startDiscovery();
            startAdvertising();
        }
        return false;
    }

    @Override
    public void close() {
        if (!isOpen()) {
            return;
        }
        setConnStatus(Detached | Closed);
        client.stopAllEndpoints();
        client.stopAdvertising();
        client.stopDiscovery();
    }

    @Override
    public void write(byte[] buffer) {
        if (isOpen()) {
            client.sendPayload(currentEndpointId, Payload.fromBytes(buffer));
        }
    }
}
