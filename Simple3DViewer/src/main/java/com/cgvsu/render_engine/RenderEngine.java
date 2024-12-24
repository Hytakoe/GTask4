package com.cgvsu.render_engine;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import com.cgvsu.math.vector.Vector2f;
import com.cgvsu.math.vector.Vector3f;
import com.cgvsu.rasterization.Rasterization;
import javafx.scene.canvas.GraphicsContext;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;

import com.cgvsu.model.Model;
import javafx.scene.paint.Color;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {


    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            BufferedImage texture) throws IOException {
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        final int textureWidth = texture.getWidth();
        final int textureHeight = texture.getHeight();
        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);
        Double[][] ZBuffer = new Double[width][height];
        for (int x = 0; x < width-1; x++){
            for (int y = 0; y < height-1; y++){
                ZBuffer[x][y] = 9999.0;
            }
        }
        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();
            final int nTextureCoordinates = mesh.polygons.get(polygonInd).getTextureVertexIndices().size();

            ArrayList<Point2f> resultPoints = new ArrayList<>();
            ArrayList<Point2f> textureResultPoints = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));
                Vector2f textureVertex = mesh.textureVertices.get(mesh.polygons.get(polygonInd).getTextureVertexIndices().get(vertexInPolygonInd));

                javax.vecmath.Vector3f vertexVecmath = new javax.vecmath.Vector3f((float) vertex.getX(), (float) vertex.getY(), (float) vertex.getZ());

                Point2f resultPoint = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath), width, height);
                float[] coordinates = new float[2];
                Point2f texturePoint = new Point2f((float) textureVertex.getX()*textureWidth, (float) textureVertex.getY()*textureHeight);
                resultPoints.add(resultPoint);
                textureResultPoints.add(texturePoint);
            }

            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                graphicsContext.strokeLine(
                        resultPoints.get(vertexInPolygonInd - 1).x,
                        resultPoints.get(vertexInPolygonInd - 1).y,
                        resultPoints.get(vertexInPolygonInd).x,
                        resultPoints.get(vertexInPolygonInd).y);
                if (vertexInPolygonInd >= 2) {
                    Rasterization.drawTriangle(graphicsContext.getPixelWriter(),
                            (int) resultPoints.get(vertexInPolygonInd - 2).x,
                            (int) resultPoints.get(vertexInPolygonInd - 2).y,
                            (int) resultPoints.get(vertexInPolygonInd - 1).x,
                            (int) resultPoints.get(vertexInPolygonInd - 1).y,
                            (int) resultPoints.get(vertexInPolygonInd).x,
                            (int) resultPoints.get(vertexInPolygonInd).y,
                            (int) textureResultPoints.get(vertexInPolygonInd - 2).x,
                            (int) textureResultPoints.get(vertexInPolygonInd - 2).y,
                            (int) textureResultPoints.get(vertexInPolygonInd - 1).x,
                            (int) textureResultPoints.get(vertexInPolygonInd - 1).y,
                            (int) textureResultPoints.get(vertexInPolygonInd).x,
                            (int) textureResultPoints.get(vertexInPolygonInd).y,
                            Color.RED,
                            ZBuffer,
                            texture);
                }
            }

            if (nVerticesInPolygon > 0)
                graphicsContext.strokeLine(
                        resultPoints.get(nVerticesInPolygon - 1).x,
                        resultPoints.get(nVerticesInPolygon - 1).y,
                        resultPoints.get(0).x,
                        resultPoints.get(0).y);
        }
    }
}