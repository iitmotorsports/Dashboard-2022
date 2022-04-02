package com.iit.dashboard2022.page;

public enum CommanderValue {
    TORQUE_VECTORING("Torque vectoring aggression", 0, 0, 0, 8);

    final String name;
    final int ID;
    final float initial;
    final float min;
    final float max;

    CommanderValue(String name, int ID, float initial, float min, float max) {
        this.name = name;
        this.ID = ID;
        this.initial = initial;
        this.min = min;
        this.max = max;
    }
}
