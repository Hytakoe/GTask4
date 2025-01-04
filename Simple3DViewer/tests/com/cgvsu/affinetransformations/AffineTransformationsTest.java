package com.cgvsu.affinetransformations;

import com.cgvsu.affine_transformations.*;
import com.cgvsu.math.vector.Vector3f;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class AffineTransformationsTest {
    @Test
    public void testScaleVertex01() {
        final ArrayList<Vector3f> input = new ArrayList<>(Arrays.asList(new Vector3f(20, 0, 5), new Vector3f(40, 4, -6), new Vector3f(0, -10, 0)));
        final ArrayList<Vector3f> expectedResult = new ArrayList<>(Arrays.asList(new Vector3f(60, 0, 25), new Vector3f(120, 16, -30), new Vector3f(0, -40, 0)));
        ArrayList<Vector3f> result;

        CompositeAffine composite = new CompositeAffine();
        composite.add(new Scale(3,4,5));
        result = composite.execute(input);
        for (int i = 0; i < expectedResult.size(); i++){
            Assertions.assertTrue(result.get(i).equals(expectedResult.get(i)));
        }
    }
    @Test
    public void testRotateVertexAxisX01() {
        final ArrayList<Vector3f> input = new ArrayList<>(Arrays.asList(new Vector3f(20, 0, 5), new Vector3f(40, 4, -6), new Vector3f(0, -10, 0)));
        final ArrayList<Vector3f> expectedResult = new ArrayList<>(Arrays.asList(new Vector3f(20, 5, 0), new Vector3f(40, -6, -4), new Vector3f(0, 0, 10)));
        ArrayList<Vector3f> result;

        CompositeAffine composite = new CompositeAffine();
        composite.add(new Rotate((float) (Math.PI/2), Axis.X));
        result = composite.execute(input);
        for (int i = 0; i < expectedResult.size(); i++){
            Assertions.assertTrue(result.get(i).equals(expectedResult.get(i)));
        }
    }
    @Test
    public void testRotateVertexAxisY01() {
        final ArrayList<Vector3f> input = new ArrayList<>(Arrays.asList(new Vector3f(20, 0, 5), new Vector3f(40, 4, -6), new Vector3f(0, -10, 0)));
        final ArrayList<Vector3f> expectedResult = new ArrayList<>(Arrays.asList(new Vector3f(5, 0, -20), new Vector3f(-6, 4, -40), new Vector3f(0, -10, 0)));
        ArrayList<Vector3f> result;

        CompositeAffine composite = new CompositeAffine();
        composite.add(new Rotate((float) (Math.PI/2), Axis.Y));
        result = composite.execute(input);
        for (int i = 0; i < expectedResult.size(); i++){
            Assertions.assertTrue(result.get(i).equals(expectedResult.get(i)));
        }
    }
    @Test
    public void testRotateVertexAxisZ01() {
        final ArrayList<Vector3f> input = new ArrayList<>(Arrays.asList(new Vector3f(20, 0, 5), new Vector3f(40, 4, -6), new Vector3f(0, -10, 0)));
        final ArrayList<Vector3f> expectedResult = new ArrayList<>(Arrays.asList(new Vector3f(0, -20, 5), new Vector3f(4, -40, -6), new Vector3f(-10, 0, 0)));
        ArrayList<Vector3f> result;

        CompositeAffine composite = new CompositeAffine();
        composite.add(new Rotate((float) (Math.PI/2), Axis.Z));
        result = composite.execute(input);
        for (int i = 0; i < expectedResult.size(); i++){
            Assertions.assertTrue(result.get(i).equals(expectedResult.get(i)));
        }
    }
    @Test
    public void testTransferVertex01() {
        final ArrayList<Vector3f> input = new ArrayList<>(Arrays.asList(new Vector3f(20, 0, 5), new Vector3f(40, 4, -6), new Vector3f(0, -10, 0)));
        final ArrayList<Vector3f> expectedResult = new ArrayList<>(Arrays.asList(new Vector3f(-20, -10, -15), new Vector3f(0, -6, -26), new Vector3f(-40, -20, -20)));
        ArrayList<Vector3f> result;

        CompositeAffine composite = new CompositeAffine();
        composite.add(new Transfer(-40,-10,-20));
        result = composite.execute(input);
        for (int i = 0; i < expectedResult.size(); i++){
            Assertions.assertTrue(result.get(i).equals(expectedResult.get(i)));
        }
    }
    @Test
    public void testScaleTransfer01() {
        final ArrayList<Vector3f> input = new ArrayList<>(Arrays.asList(new Vector3f(20, 0, 5), new Vector3f(40, 4, -6), new Vector3f(0, -10, 0)));
        final ArrayList<Vector3f> expectedResult = new ArrayList<>(Arrays.asList(new Vector3f(100, 10, 45), new Vector3f(160, 26, -10), new Vector3f(40, -30, 20)));
        ArrayList<Vector3f> result;

        CompositeAffine composite = new CompositeAffine();
        composite.add(new Scale(3,4,5));
        composite.add(new Transfer(40,10,20));
        result = composite.execute(input);
        for (int i = 0; i < expectedResult.size(); i++){
            Assertions.assertTrue(result.get(i).equals(expectedResult.get(i)));
        }
    }
    @Test
    public void testRotateScaleTransferVertexAxisZ01() {
        final ArrayList<Vector3f> input = new ArrayList<>(Arrays.asList(new Vector3f(20, 0, 5), new Vector3f(40, 4, -6), new Vector3f(0, -10, 0)));
        final ArrayList<Vector3f> expectedResult = new ArrayList<>(Arrays.asList(new Vector3f(10, -40, 20), new Vector3f(18, -100, -2), new Vector3f(-10, 20, 10)));
        ArrayList<Vector3f> result;

        CompositeAffine composite = new CompositeAffine();
        composite.add(new Rotate((float) (Math.PI/2), Axis.Z));
        composite.add(new Scale(2,3,2));
        composite.add(new Transfer(10,20,10));
        result = composite.execute(input);
        for (int i = 0; i < expectedResult.size(); i++){
            Assertions.assertTrue(result.get(i).equals(expectedResult.get(i)));
        }
    }
    @Test
    public void testRotateScaleTransferVertexAxisZ02() {
        final ArrayList<Vector3f> input = new ArrayList<>(Arrays.asList(new Vector3f(20, 0, 5), new Vector3f(40, 4, -6), new Vector3f(0, -10, 0)));
        final ArrayList<Vector3f> expectedResult = new ArrayList<>(Arrays.asList(new Vector3f(20, 0, 30), new Vector3f(28, -60, 8), new Vector3f(0, 60, 20)));
        ArrayList<Vector3f> result;
        CompositeAffine composite = new CompositeAffine();
        composite.add(new Rotate((float) (Math.PI/2), Axis.Z));
        composite.add(new Transfer(10,20,10));
        composite.add(new Scale(2,3,2));
        result = composite.execute(input);
        for (int i = 0; i < expectedResult.size(); i++){
            Assertions.assertTrue(result.get(i).equals(expectedResult.get(i)));
        }
    }
    @Test
    public void testRotateScaleTransferVertexAxisZ03() {
        final ArrayList<Vector3f> input = new ArrayList<>(Arrays.asList(new Vector3f(20, 0, 5), new Vector3f(40, 4, -6), new Vector3f(0, -10, 0)));
        final ArrayList<Vector3f> expectedResult = new ArrayList<>(Arrays.asList(new Vector3f(30, 20, 40), new Vector3f(38, -40, 18), new Vector3f(10, 80, 30)));
        ArrayList<Vector3f> result;
        CompositeAffine composite = new CompositeAffine();
        composite.add(new Rotate((float) (Math.PI/2), Axis.Z));
        composite.add(new Transfer(10,20,10));
        composite.add(new Scale(2,3,2));
        composite.add(new Transfer(10,20,10));
        result = composite.execute(input);
        for (int i = 0; i < expectedResult.size(); i++){
            Assertions.assertTrue(result.get(i).equals(expectedResult.get(i)));
        }
    }
}
