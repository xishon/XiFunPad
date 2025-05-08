package com.app.zee.xifunpad.Enum;

public enum Brush {

    Pencil("Pencil"),
    Pen("Pen"),
    Calligraphy("Calligraphy"),
    AirBrush("Air Brush"),
    Marker("Marker"),
    HardEraser("Hard Eraser"),
    SoftEraser("Soft Eraser");

    private String friendlyName;

    private Brush(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    @Override
    public String toString() {
        return friendlyName;
    }

}
