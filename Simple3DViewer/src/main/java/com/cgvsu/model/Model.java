package com.cgvsu.model;
import com.cgvsu.math.vector.Vector2f;
import com.cgvsu.math.vector.Vector3f;

import java.util.*;

public class Model {

    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();
    // метод для расчета нормалей к полигонам
    public void calculatePolygonNormals() {
        for (Polygon polygon : polygons) {
            Vector3f v0 = vertices.get(polygon.getVertexIndices().get(0));
            Vector3f v1 = vertices.get(polygon.getVertexIndices().get(1));
            Vector3f v2 = vertices.get(polygon.getVertexIndices().get(2));

            polygon.setNormal(new Vector3f(0, 0, 0).normalPolygon(v0, v1, v2));
        }
    }

    // метод для расчета нормалей к вершинам.
    public void calculateVertexNormals() {
        normals.clear(); // на случай если модель уже содержит записи о нормалях

        calculatePolygonNormals(); // сначала надо посчитать нормали к полигонам

        // создаем нормали вершин ("пустые")
        for (int i = 0; i < vertices.size(); i++) {
            normals.add(new Vector3f(0, 0, 0));
        }

        // суммируем нормали полигонов для каждой вершины
        int[] polygonsToVertexCount = new int[vertices.size()];
        for (Polygon polygon : polygons) {
            for (int vertexIndex : polygon.getVertexIndices()) {
                normals.set(vertexIndex, normals.get(vertexIndex).add(polygon.getNormal()));
                polygonsToVertexCount[vertexIndex]++;
            }
        }

        // находим нормали к вершинам и нормализуем их
        for (int i = 0; i < normals.size(); i++) {
            Vector3f normal = normals.get(i);
            if (polygonsToVertexCount[i] > 0) {
                normal = normal.divide(polygonsToVertexCount[i]);
            }
            new Vector3f(0, 0, 0).normalize(normal);
            normals.set(i, normal);
        }
    }
}
