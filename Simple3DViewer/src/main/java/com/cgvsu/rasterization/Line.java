package com.cgvsu.rasterization;

import javafx.scene.paint.Color;

public class Line {
    private final int x0, y0, x1, y1;
    private final Color color0, color1;

    public Line(int x0, int y0, int x1, int y1, Color color0, Color color1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.color0 = color0;
        this.color1 = color1;
    }

    public int getX0() { return x0; }
    public int getY0() { return y0; }
    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public Color getColor0() { return color0; }
    public Color getColor1() { return color1; }
}
