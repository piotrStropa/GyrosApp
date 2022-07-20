package com.stropa.gyros;

public class GyrosDTO {
    private final float x;
    private final float y;
    private final float z;

    public GyrosDTO(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public GyrosDTO(float[] data) {
        this.x = data[0];
        this.y = data[1];
        this.z = data[2];
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    @Override
    public String toString() {
        return "X:" + x + " Y: " + y + " Z: " + z;
    }
}
