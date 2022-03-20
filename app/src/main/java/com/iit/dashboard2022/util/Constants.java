package com.iit.dashboard2022.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utility class containing constants for the dashboard
 *
 * @author Noah Husby
 */
public class Constants {
    // PasteAPI
    public static final String PASTE_API = "https://api.paste.ee/v1/pastes";
    public static final String JSON_API_BASE64 = "dVE4NWZCOVVLanRhSnFBazlKVEExaGVVc3J2QURnZVBIejc5RXhKMlo=";
    public static final String LOG_API_BASE64 = "dTBXUXZabUNsdVFkZWJycUlUNjZSRHJoR1paTlVXaXE3U09LTVlPUE8=";

    //JSON
    public static final String JSON_FILE = "ECU_JSON_MAP.json";
    public static final Gson GSON;

    public static final int v1MappingCutoff = 256;

    static {
        GSON = new GsonBuilder().setPrettyPrinting().create();
    }

    public static class Statistics {
        public static final String MC0Voltage = "mc0_dc_v";
        public static final String MC1Voltage = "mc1_dc_v";
        public static final String MC1Current = "mc0_dc_i";
        public static final String MC0Current = "mc1_dc_i";
        public static final String MC1BoardTemp = "mc1_brd_tmp";
        public static final String MC0BoardTemp = "mc0_brd_tmp";
        public static final String MC1MotorTemp = "mc1_mtr_tmp";
        public static final String MC0MotorTemp = "mc0_mtr_tmp";
        public static final String Speedometer = "mtr_spd";
        public static final String PowerGauge = "mc_curr_pwer";
        public static final String BatteryLife = "bms_soc";
        public static final String BMSVolt = "bms_v";
        public static final String BMSAmp = "bms_avg_i";
        public static final String BMSHighTemp = "bms_h_tmp";
        public static final String BMSLowTemp = "bms_l_tmp";
        public static final String BMSDischargeLim = "bms_dis_i_lim";
        public static final String BMSChargeLim = "bms_chr_i_lim";
        public static final String Fault = "fault";
        //public static final String Lag = 18;
        //public static final String Beat = 19;
        //public static final String StartLight = 20;
        public static final String State = "state"; // State is special :)
        public static final String SerialVarResponse = "serial_var_response";

    }

    // ECU
    // TODO: Replace FAULTS
    public static final String[] FAULTS = {
            "[ LOG ] MC0 Fault: Hardware Gate/Desaturation Fault",
            "[ LOG ] MC0 Fault: HW over-current Fault",
            "[ LOG ] MC0 Fault: Accelerator shorted",
            "[ LOG ] MC0 Fault: Accelerator Open",
            "[ LOG ] MC0 Fault: Current sensor Low",
            "[ LOG ] MC0 Fault: Current sensor High",
            "[ LOG ] MC0 Fault: Module Temperature Low",
            "[ LOG ] MC0 Fault: Module Temperature High",
            "[ LOG ] MC0 Fault: Control PCB Temperature Low",
            "[ LOG ] MC0 Fault: Control PCB Temperature High",
            "[ LOG ] MC0 Fault: Gate Dive PCB Temperature Low",
            "[ LOG ] MC0 Fault: Gate Dive PCB Temperature High",
            "[ LOG ] MC0 Fault: 5V Sense Voltage Low",
            "[ LOG ] MC0 Fault: 5V Sense Voltage High",
            "[ LOG ] MC0 Fault: 12V Sense Voltage Low",
            "[ LOG ] MC0 Fault: 12V Sense Voltage High",
            "[ LOG ] MC0 Fault: 2.5V Sense Voltage Low",
            "[ LOG ] MC0 Fault: 2.5V Sense Voltage High",
            "[ LOG ] MC0 Fault: 1.5V Sense Voltage Low",
            "[ LOG ] MC0 Fault: 2.5V Sense Voltage High",
            "[ LOG ] MC0 Fault: DC Bus Voltage High",
            "[ LOG ] MC0 Fault: DC Bus Voltage Low",
            "[ LOG ] MC0 Fault: Precharge Timeout",
            "[ LOG ] MC0 Fault: Precharge Voltage Failure",
            "[ LOG ] MC0 Fault: EEPROM Checksum Invalid",
            "[ LOG ] MC0 Fault: EEPROM Data Out of Range",
            "[ LOG ] MC0 Fault: EEPROM Update Required",
            "[ LOG ] MC0 Fault: Reserved",
            "[ LOG ] MC0 Fault: Reserved",
            "[ LOG ] MC0 Fault: Reserved",
            "[ LOG ] MC0 Fault: Brake Shorted",
            "[ LOG ] MC0 Fault: Brake Open",
            "[ LOG ] MC0 Fault: Motor Over-speed Fault",
            "[ LOG ] MC0 Fault: Over-current Fault",
            "[ LOG ] MC0 Fault: Over-voltage Fault",
            "[ LOG ] MC0 Fault: Inverter Over-temperature Fault",
            "[ LOG ] MC0 Fault: Accelerator Input Shorted Fault",
            "[ LOG ] MC0 Fault: Accelerator Input Open Fault",
            "[ LOG ] MC0 Fault: Direction Command Fault",
            "[ LOG ] MC0 Fault: Inverter Response Time-out Fault",
            "[ LOG ] MC0 Fault: Hardware Gate/Desaturation Fault_2",
            "[ LOG ] MC0 Fault: Hardware Over-current Fault_2",
            "[ LOG ] MC0 Fault: Under-voltage Fault",
            "[ LOG ] MC0 Fault: CAN Command Message Lost Fault",
            "[ LOG ] MC0 Fault: Motor Over-temperature Fault",
            "[ LOG ] MC0 Fault: Reserved",
            "[ LOG ] MC0 Fault: Reserved",
            "[ LOG ] MC0 Fault: Reserved",
            "[ LOG ] MC0 Fault: Brake Input Shorted Fault",
            "[ LOG ] MC0 Fault: Brake Input Open Fault",
            "[ LOG ] MC0 Fault: Module A Over-temperature Fault7",
            "[ LOG ] MC0 Fault: Module B Over-temperature Fault7",
            "[ LOG ] MC0 Fault: Module C Over-temperature Fault7",
            "[ LOG ] MC0 Fault: PCB Over-temperature Fault7",
            "[ LOG ] MC0 Fault: Gate Drive Board 1 Over-temperature Fault",
            "[ LOG ] MC0 Fault: Gate Drive Board 2 Over-temperature Fault7",
            "[ LOG ] MC0 Fault: Gate Drive Board 3 Over-temperature Fault7",
            "[ LOG ] MC0 Fault: Current Sensor Fault",
            "[ LOG ] MC0 Fault: Reserved",
            "[ LOG ] MC0 Fault: Reserved",
            "[ LOG ] MC0 Fault: Reserved",
            "[ LOG ] MC0 Fault: Reserved",
            "[ LOG ] MC0 Fault: Resolver Not Connected",
            "[ LOG ] MC0 Fault: Inverter Discharge Active",
            "[ LOG ] MC1 Fault: Hardware Gate/Desaturation Fault",
            "[ LOG ] MC1 Fault: HW over-current Fault",
            "[ LOG ] MC1 Fault: Accelerator shorted",
            "[ LOG ] MC1 Fault: Accelerator Open",
            "[ LOG ] MC1 Fault: Current sensor Low",
            "[ LOG ] MC1 Fault: Current sensor High",
            "[ LOG ] MC1 Fault: Module Temperature Low",
            "[ LOG ] MC1 Fault: Module Temperature High",
            "[ LOG ] MC1 Fault: Control PCB Temperature Low",
            "[ LOG ] MC1 Fault: Control PCB Temperature High",
            "[ LOG ] MC1 Fault: Gate Dive PCB Temperature Low",
            "[ LOG ] MC1 Fault: Gate Dive PCB Temperature High",
            "[ LOG ] MC1 Fault: 5V Sense Voltage Low",
            "[ LOG ] MC1 Fault: 5V Sense Voltage High",
            "[ LOG ] MC1 Fault: 12V Sense Voltage Low",
            "[ LOG ] MC1 Fault: 12V Sense Voltage High",
            "[ LOG ] MC1 Fault: 2.5V Sense Voltage Low",
            "[ LOG ] MC1 Fault: 2.5V Sense Voltage High",
            "[ LOG ] MC1 Fault: 1.5V Sense Voltage Low",
            "[ LOG ] MC1 Fault: 2.5V Sense Voltage High",
            "[ LOG ] MC1 Fault: DC Bus Voltage High",
            "[ LOG ] MC1 Fault: DC Bus Voltage Low",
            "[ LOG ] MC1 Fault: Precharge Timeout",
            "[ LOG ] MC1 Fault: Precharge Voltage Failure",
            "[ LOG ] MC1 Fault: EEPROM Checksum Invalid",
            "[ LOG ] MC1 Fault: EEPROM Data Out of Range",
            "[ LOG ] MC1 Fault: EEPROM Update Required",
            "[ LOG ] MC1 Fault: Reserved",
            "[ LOG ] MC1 Fault: Reserved",
            "[ LOG ] MC1 Fault: Reserved",
            "[ LOG ] MC1 Fault: Brake Shorted",
            "[ LOG ] MC1 Fault: Brake Open",
            "[ LOG ] MC1 Fault: Motor Over-speed Fault",
            "[ LOG ] MC1 Fault: Over-current Fault",
            "[ LOG ] MC1 Fault: Over-voltage Fault",
            "[ LOG ] MC1 Fault: Inverter Over-temperature Fault",
            "[ LOG ] MC1 Fault: Accelerator Input Shorted Fault",
            "[ LOG ] MC1 Fault: Accelerator Input Open Fault",
            "[ LOG ] MC1 Fault: Direction Command Fault",
            "[ LOG ] MC1 Fault: Inverter Response Time-out Fault",
            "[ LOG ] MC1 Fault: Hardware Gate/Desaturation Fault_2",
            "[ LOG ] MC1 Fault: Hardware Over-current Fault_2",
            "[ LOG ] MC1 Fault: Under-voltage Fault",
            "[ LOG ] MC1 Fault: CAN Command Message Lost Fault",
            "[ LOG ] MC1 Fault: Motor Over-temperature Fault",
            "[ LOG ] MC1 Fault: Reserved",
            "[ LOG ] MC1 Fault: Reserved",
            "[ LOG ] MC1 Fault: Reserved",
            "[ LOG ] MC1 Fault: Brake Input Shorted Fault",
            "[ LOG ] MC1 Fault: Brake Input Open Fault",
            "[ LOG ] MC1 Fault: Module A Over-temperature Fault7",
            "[ LOG ] MC1 Fault: Module B Over-temperature Fault7",
            "[ LOG ] MC1 Fault: Module C Over-temperature Fault7",
            "[ LOG ] MC1 Fault: PCB Over-temperature Fault7",
            "[ LOG ] MC1 Fault: Gate Drive Board 1 Over-temperature Fault",
            "[ LOG ] MC1 Fault: Gate Drive Board 2 Over-temperature Fault7",
            "[ LOG ] MC1 Fault: Gate Drive Board 3 Over-temperature Fault7",
            "[ LOG ] MC1 Fault: Current Sensor Fault",
            "[ LOG ] MC1 Fault: Reserved",
            "[ LOG ] MC1 Fault: Reserved",
            "[ LOG ] MC1 Fault: Reserved",
            "[ LOG ] MC1 Fault: Reserved",
            "[ LOG ] MC1 Fault: Resolver Not Connected",
            "[ LOG ] MC1 Fault: Inverter Discharge Active",
    };
}
