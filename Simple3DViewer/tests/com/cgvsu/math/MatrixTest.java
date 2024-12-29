package com.cgvsu.math;

import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.math.vector.Vector3f;
import com.cgvsu.render_engine.GraphicConveyor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class MatrixTest {
    @Test
    public void testMulMatrix() {
        float[] array = new float[]{2,	3,	4,	4,
                9,	8,	9,	0,
                3,	7,	8,	1,
                8,	4,	9,	2};
        Matrix4f matrix = new Matrix4f(array);

        float[] array2 = new float[]{7,	8,	0,	3,
                1,	2,	3,	4,
                6,	8,	9,	8,
                6,	8,	9,	2};
        Matrix4f matrix2 = new Matrix4f(array2);

        float[] resArr = new float[]{65,	86,	81,	58,
                125,	160,	105	,131,
                82,	110,	102,	103,
                126,	160,	111,	116};
        Matrix4f matrixRes = new Matrix4f(resArr);

        matrix=matrix.mulMatrix(matrix2);

        matrix.printMatrix();
        System.out.println("---");
        matrixRes.printMatrix();
        Assertions.assertTrue(matrixRes.equals(matrix));
    }

    @Test
    public void testMatrixMulVectorDivW(){
        float[] arr = new float[]{
                2,	3,	4,	4,
                9,	8,	9,	3,
                3,	7,	8,	1,
                8,	4,	9,	2
        };
        Matrix4f matrix = new Matrix4f(arr);
        Vector3f vector = new Vector3f(7,1,6);

        Vector3f vectorRaw = GraphicConveyor.multiplyMatrix4ByVector3(matrix,vector);
        Vector3f vectorMy = matrix.mulVectorDivW(vector);

        System.out.println(vectorRaw);
        System.out.println("---");
        System.out.println(vectorMy);

        Assertions.assertTrue(vectorRaw.equals(vectorMy));
        // false
    }

    @Test
    public void testMatrixMake(){
        float[] arr = new float[]{
                2,	3,	4,	4,
                9,	8,	9,	3,
                3,	7,	8,	1,
                8,	4,	9,	2
        };
        Matrix4f matrix = new Matrix4f(arr);
        Matrix4f newMatrix = new Matrix4f(matrix);

        matrix.printMatrix();
        System.out.println("---");
        newMatrix.printMatrix();

        System.out.println("---------");

        matrix.setCell(0,0,1000);
        matrix.setCell(1,1,1000);
        matrix.setCell(2,2,1000);
        matrix.setCell(3,3,1000);
        matrix.printMatrix();
        System.out.println("---");
        newMatrix.printMatrix();

        Assertions.assertFalse(newMatrix.equals(matrix));
    }
}
