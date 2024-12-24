package com.cgvsu.rasterization;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import javax.vecmath.Point2f;
import java.awt.image.BufferedImage;
import java.util.*;

public class Rasterization {

    public static void drawTriangle(final PixelWriter pixelWriter,
                                    final int x0, final int y0,
                                    final int x1, final int y1,
                                    final int x2, final int y2,
                                    final int tx0, final int ty0,
                                    final int tx1, final int ty1,
                                    final int tx2, final int ty2,
                                    Color color,
                                    Double[][] ZBuffer,
                                    BufferedImage texture) {
        Point2f d0 = new Point2f(x0, y0);
        Point2f d1 = new Point2f(x1, y1);
        Point2f d2 = new Point2f(x2, y2);
        List<Point2f> dots = Arrays.asList(d0, d1, d2);
        Point2fYThenXComparator compYX = new Point2fYThenXComparator();
        dots.sort(compYX);


        double function1;
        double function2;
        Point2f vertex1 = dots.get(1);
        Point2f vertex2 = dots.get(2);

        //Рассчёт функций верхней половины
        if (dots.get(0).y - vertex2.y != 0) {
            function1 = ((double) (dots.get(0).x - vertex2.x) / (dots.get(0).y - vertex2.y));
        } else {
            function1 = 0;
        }
        if (dots.get(0).y - vertex1.y != 0) {
            function2 = (double) (dots.get(0).x - vertex1.x) / (dots.get(0).y - vertex1.y);
        } else {
            function2 = 0;
        }
        //Верхняя половина
        for (int row = (int) dots.get(0).y; row < dots.get(1).y; row++) {
            int fullFunctionLeft = (int) (function1 * row + vertex2.x - function1 * vertex2.y);
            int fullFunctionRight = (int) (function2 * row + vertex1.x - function2 * vertex1.y);
            for (int col = Math.min(fullFunctionRight, fullFunctionLeft); col <= Math.max(fullFunctionRight, fullFunctionLeft); col++) {
                double z = calculateBarycentricCoordinate(col, row, d0, d1, d2);
                //if (ZBuffer[col][row] > z) {
                Color color1 = calcColor(col, row, d0, d1, d2);
                double[] coordinates = calculateBarycentricCoordinates(col, row, d0, d1, d2);
                int x = (int) (coordinates[0]*tx0 + coordinates[1]*tx1 + coordinates[2]*tx2);
                int y = (int) (coordinates[0]*ty0 + coordinates[1]*ty1 + coordinates[2]*ty2);
                if (x != 0) {
                    int clr = texture.getRGB(tx0, ty0);
                    double red = (double) ((clr & 0x00ff0000) >> 16) / 255;
                    double green = (double) ((clr & 0x0000ff00) >> 8) / 255;
                    double blue = (double) (clr & 0x000000ff) / 255;
                    pixelWriter.setColor(col, row, new Color(red, green, blue, 1));
                }

                //ZBuffer[col][row] = z;
                //}
            }
        }

        //Рассчёт функций нижней половины
        if (vertex2.y >= vertex1.y) {
            function2 = (double) (dots.get(2).x - vertex1.x) / (dots.get(2).y - vertex1.y);
        } else {
            function1 = (double) (dots.get(2).x - vertex2.x) / (dots.get(2).y - vertex2.y);
        }

        //Нижняя половина
        for (int row = (int) dots.get(1).y; row < dots.get(2).y; row++) {
            int fullFunctionLeft = (int) (function1 * row + vertex2.x - function1 * vertex2.y);
            int fullFunctionRight = (int) (function2 * row + vertex1.x - function2 * vertex1.y);
            for (int col = Math.min(fullFunctionRight, fullFunctionLeft); col <= Math.max(fullFunctionRight, fullFunctionLeft); col++) {
                double z = calculateBarycentricCoordinate(col, row, d0, d1, d2);
                //if (ZBuffer[col][row] > z) {
                Color color1 = calcColor(col, row, d0, d1, d2);
                double[] coordinates = calculateBarycentricCoordinates(col, row, d0, d1, d2);
                int x = (int) (coordinates[0]*tx0 + coordinates[1]*tx1 + coordinates[2]*tx2);
                int y = (int) (coordinates[0]*ty0 + coordinates[1]*ty1 + coordinates[2]*ty2);
                if (x != 0) {
                    int clr = texture.getRGB(x, y);
                    double red = (double) ((clr & 0x00ff0000) >> 16) / 255;
                    double green = (double) ((clr & 0x0000ff00) >> 8) / 255;
                    double blue = (double) (clr & 0x000000ff) / 255;
                    pixelWriter.setColor(col, row, new Color(red, green, blue, 1));
                    //ZBuffer[col][row] = z;
                }

                //}
            }
        }
    }

    private static double[] calculateBarycentricCoordinates(int x, int y, Point2f d0, Point2f d1, Point2f d2) {
        double denominator = (double) (d1.y - d2.y) * (double) (d0.x - d2.x) + (double) (d2.x - d1.x) * (double) (d0.y - d2.y);
        double alpha = (double) ((d1.y - d2.y) * (x - d2.x) + (d2.x - d1.x) * (y - d2.y)) / denominator;
        double beta = (double) ((d2.y - d0.y) * (x - d2.x) + (d0.x - d2.x) * (y - d2.y)) / denominator;
        double gamma = 1 - alpha - beta;
        return new double[]{alpha, beta, gamma};
    }

    private static double calculateBarycentricCoordinate(int x, int y, Point2f d0, Point2f d1, Point2f d2) {
        return (double) (d1.y - d2.y) * (double) (d0.x - d2.x) + (double) (d2.x - d1.x) * (double) (d0.y - d2.y);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static Color calcColor(int col, int row, Point2f d0, Point2f d1, Point2f d2) {
        double[] ABG = calculateBarycentricCoordinates(col, row, d0, d1, d2);
        return new Color(clamp(ABG[0]), clamp(ABG[1]), clamp(ABG[2]), 1);
    }
}
