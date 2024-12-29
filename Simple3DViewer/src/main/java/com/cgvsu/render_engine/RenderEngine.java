package com.cgvsu.render_engine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.chrono.IsoChronology;
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
            BufferedImage texture,
            boolean drawLines,
            boolean drawTexture,
            boolean useLight,
            Color color) throws IOException {
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        final Vector3f l = new Vector3f(1, 0, 0);
        int textureWidth = 0;
        int textureHeight = 0;
        if (texture != null) {
            textureWidth = texture.getWidth();
            textureHeight = texture.getHeight();
        }
        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);
        Float[][] ZBuffer = new Float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                ZBuffer[x][y] = 9999.0F;
            }
        }
        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();

            ArrayList<Vector3f> normals = new ArrayList<>();
            ArrayList<Point2f> resultPoints = new ArrayList<>();
            ArrayList<Point2f> textureResultPoints = new ArrayList<>();
            ArrayList<Float> zCoordinates = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));
                normals.add(mesh.normals.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd)));
                Vector2f textureVertex = mesh.textureVertices.get(mesh.polygons.get(polygonInd).getTextureVertexIndices().get(vertexInPolygonInd));

                javax.vecmath.Vector3f vertexVecmath = new javax.vecmath.Vector3f((float) vertex.getX(), (float) vertex.getY(), (float) vertex.getZ());

                Point2f resultPoint = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath), width, height);
                Point2f texturePoint = new Point2f(textureWidth-(float) textureVertex.getX() * textureWidth, (float) ((float) textureHeight-(textureVertex.getY() * textureHeight)));
                vertexVecmath = multiplyMatrix4ByVector3(viewMatrix, vertexVecmath);
                zCoordinates.add(vertexVecmath.z);
                resultPoints.add(resultPoint);
                textureResultPoints.add(texturePoint);
            }

            draw(nVerticesInPolygon, graphicsContext, resultPoints, zCoordinates, textureResultPoints, ZBuffer, texture, drawLines, drawTexture, useLight, normals, l, color, width, height);
        }
    }

    private static void draw(int nVerticesInPolygon, GraphicsContext graphicsContext
            , ArrayList<Point2f> resultPoints, ArrayList<Float> zCoordinates,
                             ArrayList<Point2f> textureResultPoints, Float[][] ZBuffer,
                             BufferedImage texture,
                             boolean drawLines,
                             boolean drawTexture,
                             boolean useLight,
                             ArrayList<Vector3f> normals,
                             Vector3f ray,
                             Color color,
                             int width,
                             int height) {
        for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
            if (drawLines) {
                Rasterization.drawLine(graphicsContext.getPixelWriter(),
                        (int) resultPoints.get(vertexInPolygonInd - 1).x,
                        (int) resultPoints.get(vertexInPolygonInd - 1).y,
                        zCoordinates.get(vertexInPolygonInd - 1),
                        (int) resultPoints.get(vertexInPolygonInd).x,
                        (int) resultPoints.get(vertexInPolygonInd).y,
                        zCoordinates.get(vertexInPolygonInd),
                        ZBuffer,
                        width,
                        height);
            }
            if (vertexInPolygonInd == 2) {

                Rasterization.drawTriangle(graphicsContext.getPixelWriter(),
                        (int) resultPoints.get(0).x,
                        (int) resultPoints.get(0).y,
                        zCoordinates.get(0),
                        (int) resultPoints.get(1).x,
                        (int) resultPoints.get(1).y,
                        zCoordinates.get(1),
                        (int) resultPoints.get(2).x,
                        (int) resultPoints.get(2).y,
                        zCoordinates.get(2),
                        (int) textureResultPoints.get(0).x,
                        (int) textureResultPoints.get(0).y,
                        (int) textureResultPoints.get(1).x,
                        (int) textureResultPoints.get(1).y,
                        (int) textureResultPoints.get(2).x,
                        (int) textureResultPoints.get(2).y,
                        color,
                        ZBuffer,
                        texture,
                        drawTexture,
                        useLight,
                        normals.get(0),
                        normals.get(1),
                        normals.get(2),
                        ray,
                        width,
                        height);
            }
        }
    }
}