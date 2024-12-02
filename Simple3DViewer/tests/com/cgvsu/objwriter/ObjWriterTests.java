package com.cgvsu.objwriter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.objwriter.ObjWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;


class ObjWriterTests extends ObjWriter{
    @Test
    public void testWriteVertices()  {
        ArrayList<Vector3f> vertices = new ArrayList<>(Arrays.asList(
                new Vector3f(1.01f, 1.02f, 1.03f),
                new Vector3f(2.01f, 2.02f, 2.03f)
        ));

        String expectedResult = "v 1.0100 1.0200 1.0300\nv 2.0100 2.0200 2.0300\n";
        String result = ObjWriter.writeVertices(vertices).toString();
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testWriteTextureVertices() {
        ArrayList<Vector2f> textureVertices = new ArrayList<>(Arrays.asList(
                new Vector2f(0.5f, 0.6f),
                new Vector2f(0.7f, 0.8f)
        ));

        String expectedResult = "vt 0.5000 0.6000\nvt 0.7000 0.8000\n";
        String result = ObjWriter.writeTextureVertices(textureVertices).toString();

        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testWriteNormals() {
        ArrayList<Vector3f> normals = new ArrayList<>(Arrays.asList(
                new Vector3f(0.1f, 0.2f, 0.3f),
                new Vector3f(0.4f, 0.5f, 0.6f)
        ));

        String expectedResult = "vn 0.1000 0.2000 0.3000\nvn 0.4000 0.5000 0.6000\n";
        String result = ObjWriter.writeNormals(normals).toString();

        Assertions.assertEquals(expectedResult, result);
    }
    @Test
    public void testWritePolygons() {
        ArrayList<Polygon> polygons = new ArrayList<>();
        Polygon polygon1 = new Polygon();
        polygon1.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon1.setTextureVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon1.setNormalIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygons.add(polygon1);

        Polygon polygon2 = new Polygon();
        polygon2.setVertexIndices(new ArrayList<>(Arrays.asList(3, 4, 5)));
        polygon2.setTextureVertexIndices(new ArrayList<>(Arrays.asList(3, 4, 5)));
        polygon2.setNormalIndices(new ArrayList<>(Arrays.asList(3, 4, 5)));
        polygons.add(polygon2);

        String expectedResult = "f 1/1/1 2/2/2 3/3/3\nf 4/4/4 5/5/5 6/6/6\n";
        String result = ObjWriter.writePolygons(polygons).toString();

        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testWritePolygonsWithoutTextureVertices() {
        ArrayList<Polygon> polygons = new ArrayList<>();
        Polygon polygon1 = new Polygon();
        polygon1.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon1.setNormalIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygons.add(polygon1);

        Polygon polygon2 = new Polygon();
        polygon2.setVertexIndices(new ArrayList<>(Arrays.asList(3, 4, 5)));
        polygon2.setNormalIndices(new ArrayList<>(Arrays.asList(3, 4, 5)));
        polygons.add(polygon2);

        String expectedResult = "f 1//1 2//2 3//3\nf 4//4 5//5 6//6\n";
        String result = ObjWriter.writePolygons(polygons).toString();

        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testWritePolygonsWithoutNormals() {
        ArrayList<Polygon> polygons = new ArrayList<>();
        Polygon polygon1 = new Polygon();
        polygon1.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygon1.setTextureVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygons.add(polygon1);

        Polygon polygon2 = new Polygon();
        polygon2.setVertexIndices(new ArrayList<>(Arrays.asList(3, 4, 5)));
        polygon2.setTextureVertexIndices(new ArrayList<>(Arrays.asList(3, 4, 5)));
        polygons.add(polygon2);

        String expectedResult = "f 1/1 2/2 3/3\nf 4/4 5/5 6/6\n";
        String result = ObjWriter.writePolygons(polygons).toString();

        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testWritePolygonsWithoutTextureVerticesAndNormals() {
        ArrayList<Polygon> polygons = new ArrayList<>();
        Polygon polygon1 = new Polygon();
        polygon1.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygons.add(polygon1);

        Polygon polygon2 = new Polygon();
        polygon2.setVertexIndices(new ArrayList<>(Arrays.asList(3, 4, 5)));
        polygons.add(polygon2);

        String expectedResult = "f 1 2 3\nf 4 5 6\n";
        String result = ObjWriter.writePolygons(polygons).toString();

        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testWritePolygonsWithEmptyLists() {
        ArrayList<Polygon> polygons = new ArrayList<>();
        Polygon polygon1 = new Polygon();
        polygon1.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        polygons.add(polygon1);

        String expectedResult = "f 1 2 3\n";
        String result = ObjWriter.writePolygons(polygons).toString();

        Assertions.assertEquals(expectedResult, result);
    }
    @Test
    public void testEmptyModel(){
        String expectedResult = "";
        String result = ObjWriter.write(new Model());
        Assertions.assertEquals(expectedResult, result);
    }
}

