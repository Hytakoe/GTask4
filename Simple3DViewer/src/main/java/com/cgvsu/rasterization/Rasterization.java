package com.cgvsu.rasterization;

import com.cgvsu.math.vector.Vector3f;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import javax.vecmath.Point2f;
import java.awt.image.BufferedImage;
import java.util.*;

public class Rasterization {

    public static void drawTriangle(final PixelWriter pixelWriter,
                                    final int x0, final int y0, final float z0,
                                    final int x1, final int y1, final float z1,
                                    final int x2, final int y2, final float z2,
                                    final int tx0, final int ty0,
                                    final int tx1, final int ty1,
                                    final int tx2, final int ty2,
                                    Color color,
                                    Double[][] ZBuffer,
                                    BufferedImage texture,
                                    boolean useTexture,
                                    boolean useLight,
                                    Vector3f n0,
                                    Vector3f n1,
                                    Vector3f n2,
                                    Vector3f ray) {
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
            drawHalfOfTriangle(function1, function2, vertex1, vertex2, d0, d1, d2, texture, ZBuffer, pixelWriter, row, z0, z1, z2, tx0, ty0, tx1, ty1, tx2, ty2, useTexture, useLight, color, n0, n1, n2, ray);
        }

        //Рассчёт функций нижней половины
        if (vertex2.y >= vertex1.y) {
            if (dots.get(2).y - vertex1.y != 0) {
                function2 = (double) (dots.get(2).x - vertex1.x) / (dots.get(2).y - vertex1.y);
            } else function2 = 0;
        } else {
            if (dots.get(2).y - vertex2.y != 0) {
                function1 = (double) (dots.get(2).x - vertex2.x) / (dots.get(2).y - vertex2.y);
            } else function1 = 0;
        }

        //Нижняя половина
        for (int row = (int) dots.get(1).y; row < dots.get(2).y; row++) {
            drawHalfOfTriangle(function1, function2, vertex1, vertex2, d0, d1, d2, texture, ZBuffer, pixelWriter, row, z0, z1, z2, tx0, ty0, tx1, ty1, tx2, ty2, useTexture, useLight, color, n0, n1, n2, ray);
        }
    }

    private static void drawHalfOfTriangle(double function1, double function2,
                                           Point2f vertex1, Point2f vertex2,
                                           Point2f d0, Point2f d1, Point2f d2,
                                           BufferedImage texture,
                                           Double[][] ZBuffer,
                                           PixelWriter pixelWriter,
                                           int row,
                                           float z0,
                                           float z1,
                                           float z2,
                                           int tx0, int ty0,
                                           int tx1, int ty1,
                                           int tx2, int ty2,
                                           boolean useTexture,
                                           boolean useLight,
                                           Color color,
                                           Vector3f n0,
                                           Vector3f n1,
                                           Vector3f n2,
                                           Vector3f ray) {

        int fullFunctionLeft = (int) (function1 * row + vertex2.x - function1 * vertex2.y);
        int fullFunctionRight = (int) (function2 * row + vertex1.x - function2 * vertex1.y);
        for (int col = Math.min(fullFunctionRight, fullFunctionLeft); col <= Math.max(fullFunctionRight, fullFunctionLeft); col++) {
            double[] coordinates = calculateBarycentricCoordinates(col, row, d0, d1, d2);
            double z = coordinates[0] * z0 + coordinates[1] * z1 + coordinates[2] * z2;
            if (ZBuffer[col][row] > z) {
                Color finalColor;
                if (useTexture) {
                    int x = coordinatesFix((int) (coordinates[0] * tx0 + coordinates[1] * tx1 + coordinates[2] * tx2), texture.getWidth());
                    int y = coordinatesFix((int) (coordinates[0] * ty0 + coordinates[1] * ty1 + coordinates[2] * ty2), texture.getHeight());
                    int clr = texture.getRGB(x, y);
                    double red = (double) ((clr & 0x00ff0000) >> 16) / 255;
                    double green = (double) ((clr & 0x0000ff00) >> 8) / 255;
                    double blue = (double) (clr & 0x000000ff) / 255;
                    finalColor = new Color(red, green, blue, 1);
                } else {
                    finalColor = color;
                }
                if (useLight) {
                    Vector3f currentN = new Vector3f(coordinates[0] * n0.getX() + coordinates[1] * n1.getX() + coordinates[2] * n2.getX(),
                            coordinates[0] * n0.getY() + coordinates[1] * n1.getY() + coordinates[2] * n2.getY(),
                            coordinates[0] * n0.getZ() + coordinates[1] * n1.getZ() + coordinates[2] * n2.getZ());
                    double l = -(currentN.getX()*ray.getX() + currentN.getY()*ray.getY() + currentN.getZ()*ray.getZ());// currentN.vectorMultiply(ray);
                    double k = 0.7;
                    if (l > 0){
                        if (l>1){
                            l = 1;
                        }
                        finalColor = new Color(finalColor.getRed()*(1-k) + finalColor.getRed()*k*l, finalColor.getGreen()*(1-k) + finalColor.getGreen()*k*l, finalColor.getBlue()*(1-k) + finalColor.getBlue()*k*l, 1);
                    } else {
                        finalColor = new Color(finalColor.getRed()*(1-k), finalColor.getGreen()*(1-k), finalColor.getBlue()*(1-k), 1);
                    }
                }
                pixelWriter.setColor(col, row, finalColor);
                ZBuffer[col][row] = z;
            }
        }
    }

    private static double[] calculateBarycentricCoordinates(int x, int y, Point2f d0, Point2f d1, Point2f d2) {
        double denominator = (double) (d1.y - d2.y) * (double) (d0.x - d2.x) + (double) (d2.x - d1.x) * (double) (d0.y - d2.y);
        double alpha;
        double beta;
        double gamma;
        if (Double.isNaN(denominator)) {
            alpha = ((d1.y - d2.y) * (x - d2.x) + (d2.x - d1.x) * (y - d2.y));
            beta = ((d2.y - d0.y) * (x - d2.x) + (d0.x - d2.x) * (y - d2.y));
        } else {
            alpha = (double) ((d1.y - d2.y) * (x - d2.x) + (d2.x - d1.x) * (y - d2.y)) / denominator;
            beta = (double) ((d2.y - d0.y) * (x - d2.x) + (d0.x - d2.x) * (y - d2.y)) / denominator;
        }
        gamma = 1 - alpha - beta;
        return new double[]{clamp(alpha), clamp(beta), clamp(gamma)};
    }

    private static double clamp(double value) {
        if (!Double.isNaN(value)) {
            return Math.max(0.0, Math.min(1.0, value));
        }
        return 0;
    }

    private static int coordinatesFix(int value, int max) {
        if (value < 0) {
            value = 0;
        }
        if (value >= max) {
            value = max - 1;
        }
        return value;
    }

    public static void drawLine(PixelWriter pixelWriter, int x0, int y0, float z0, int x1, int y1, float z1, Double[][] ZBuffer) {
        int x = x0;
        int y = y0;
        int deltax = Math.abs(x1 - x0);
        int deltay = Math.abs(y1 - y0);
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;
        int error = deltax - deltay;
        while (true) {
            double alpha = Math.pow((Math.pow(x - x0, 2) + Math.pow(y - y0, 2)), 0.5);
            double beta = Math.pow((Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2)), 0.5);
            double denominator = 1 / (alpha + beta);
            alpha *= denominator;
            beta *= denominator;
            double z = beta * z0 + alpha * z1;
            if (ZBuffer[x0][y0] > z) {
                pixelWriter.setColor(x0, y0, Color.BLACK);
                ZBuffer[x0][y0] = z;
            }

            if (x0 == x1 && y0 == y1) break;

            int error2 = error * 2;

            if (error2 > -deltay) {
                error -= deltay;
                x0 += sx;
            }
            if (error2 < deltax) {
                error += deltax;
                y0 += sy;
            }
        }
    }
}
