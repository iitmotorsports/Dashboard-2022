package com.iit.dashboard2022.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ecu.ECU;
import com.iit.dashboard2022.ecu.Metric;
import com.iit.dashboard2022.logging.Log;
import com.iit.dashboard2022.logging.ToastLevel;
import com.iit.dashboard2022.ui.widget.LiveDataEntry;
import com.iit.dashboard2022.ui.widget.LiveDataSelector;
import com.iit.dashboard2022.util.ByteSplit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Commander extends Page {

    private final LiveDataSelector selector = new LiveDataSelector();

    private SwitchMaterial toggle;
    private Slider slider;
    private EditText valueEdit;
    private TextView maxValueText, minValueText, valueActiveText, IDTextView;
    private LinearLayout valueListLayout;
    private LiveDataEntry currentSelection;

    private byte ID = -1;
    private float currentValue = 0f;
    private float setMax = 1;
    private float setMin = 0;
    private ECU frontECU;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.tab_commander_layout, container, false);
        slider = rootView.findViewById(R.id.ValueSlider);
        valueEdit = rootView.findViewById(R.id.ValueEdit);
        toggle = rootView.findViewById(R.id.OnOffSwitch);
        maxValueText = rootView.findViewById(R.id.maxValueText);
        minValueText = rootView.findViewById(R.id.minValueText);
        valueActiveText = rootView.findViewById(R.id.valueActiveText);
        IDTextView = rootView.findViewById(R.id.IDTextView);
        MaterialButton submitBtn = rootView.findViewById(R.id.submitBtn);
        valueListLayout = rootView.findViewById(R.id.valueListLayout);

        selector.setSelectionChangedListener(newSelection -> {
            currentSelection = newSelection;
            double[] values = newSelection.getValues();
            newSetup(newSelection.getTitle(), (byte) values[1], (float) values[0], (float) values[2], (float) values[3]);
        });

        submitBtn.setOnClickListener(view -> {
            if (frontECU == null || !frontECU.isOpen()) {
                Log.toast("Unable to submit, not connected to device", ToastLevel.ERROR);
                return;
            }
            valueEdit.clearFocus();
            Log.toast("Submitting Value", ToastLevel.INFO);
            frontECU.issueCommand(ECU.Command.SET_SERIAL_VAR);

            ByteBuffer bb = ByteBuffer.allocate(8);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.putFloat(currentValue);
            frontECU.write(ByteSplit.joinArray(new byte[]{ ID }, bb.array()));
        });

        slider.addOnChangeListener((slider1, value, fromUser) -> {
            currentValue = value;
            updateValues(slider);
        });

        toggle.setOnCheckedChangeListener((compoundButton, b) -> {
            currentValue = b ? setMax : setMin;
            updateValues(toggle);
        });

        valueEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        valueEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                valueEdit.clearFocus();
            }
            return false;
        });

        valueEdit.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                try {
                    currentValue = Float.parseFloat(valueEdit.getText().toString());
                    currentValue = Math.max(Math.min(currentValue, setMax), setMin);
                    updateValues(valueEdit);
                } catch (NumberFormatException e) {
                    currentValue = 0;
                    updateValues(valueEdit);
                }
            }
        });

        newSetup("No selected value", ID, currentValue, setMin, setMax);

        Arrays.stream(CommanderValue.values()).forEach(v -> addEntry(v.getName(), (byte) v.getId(), v.getInitial(), v.getMin(), v.getMax()));
        return rootView;
    }

    public void addEntry(String name, byte ID, float initial, float min, float max) {
        LiveDataEntry lde = new LiveDataEntry(name, getContext());
        lde.setEnableValue(false);
        lde.setRawValue(initial);
        lde.setRawStats(ID, min, max);
        selector.addEntry(lde);
        valueListLayout.addView(lde);
    }

    public void setECU(ECU frontECU) {
        this.frontECU = frontECU;
        Metric.SERIAL_VAR_RESPONSE.addMessageListener(val -> Log.toast("Value received (truncated): " + val, ToastLevel.SUCCESS), Metric.UpdateMethod.ON_RECEIVE);
    }

    @UiThread
    public void updateValues(@Nullable View activeView) {
        if (!toggle.equals(activeView)) {
            if (currentValue == setMax) {
                toggle.setChecked(true);
            } else if (currentValue == setMin) {
                toggle.setChecked(false);
            }
        }
        if (!slider.equals(activeView)) {
            slider.setValue(currentValue);
        }
        if (!valueEdit.equals(activeView)) {
            valueEdit.setText(String.valueOf(currentValue));
        }
        if (currentSelection != null) {
            currentSelection.setRawValue(currentValue);
        }
    }

    public void newSetup(String name, byte ID, float initial, float min, float max) {
        this.ID = ID;
        currentValue = initial;
        setMax = max;
        setMin = min;
        slider.post(() -> {
            valueActiveText.setText(name);
            slider.setValueTo(max);
            slider.setValueFrom(min);
            maxValueText.setText(String.valueOf(max));
            minValueText.setText(String.valueOf(min));
            IDTextView.setText(String.valueOf(ID));
            updateValues(null);
        });
    }

    @NonNull
    @Override
    public String getTitle() {
        return "Commander";
    }
}
