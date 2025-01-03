package com.cgvsu;

import com.cgvsu.model.NormalsCalculation;
import com.cgvsu.model.Triangulator;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.RenderEngine;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import com.cgvsu.math.vector.Vector3f;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;

public class GuiController {
    private final BooleanProperty drawLines = new SimpleBooleanProperty(false);
    private final BooleanProperty drawTexture = new SimpleBooleanProperty(false);
    private final BooleanProperty useLight = new SimpleBooleanProperty(false);
    final private float TRANSLATION = 0.5F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    private Model mesh = null;
    private BufferedImage texture;

    private List<Camera> cameras = List.of(
            new Camera(
                    new Vector3f(0, 0, 100),
                    new Vector3f(0, 0, 0),
                    1.0F, 1, 0.01F, 100),
            new Camera(
                    new Vector3f(0, 0, 10),
                    new Vector3f(0, 0, 0),
                    1.0F, 1, 0.01F, 100),

            new Camera(
                    new Vector3f(10, 10, 10),
                    new Vector3f(0, 0, 0),
                    1.0F, 1, 0.01F, 100)
    );
    private Camera camera = new Camera(
            new Vector3f(0, 0, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;
    //Это через текстовые поля в интерфейсе
    public void addCamera(Camera camera){
        this.cameras.add(camera);
    }
    //Это тоже чтобы можно было выбрать в интерфейсе
    public void setCamera(int camera){
        this.camera = cameras.get(camera);
    }
    @FXML
    private void initialize() {
        drawLines.bind(drawLinesCheckBox.selectedProperty());
        drawTexture.bind(drawTextureCheckBox.selectedProperty());
        useLight.bind(useLightCheckBox.selectedProperty());

        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            if (mesh != null) {
                Triangulator.triangulateModel(mesh);
                NormalsCalculation.calculateVertexNormals(mesh);
                try {
                    RenderEngine.render(canvas.getGraphicsContext2D(),
                            camera,
                            mesh,
                            (int) width,
                            (int) height,
                            texture,
                            drawLines.get(),
                            drawTexture.get(),
                            useLight.get(),
                            colorPicker.getValue());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        deleteModelButton.setOnAction(this::handleDeleteModelButtonClick);
        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        String projectDir = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(projectDir));

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            mesh = ObjReader.read(fileContent);
            // todo: обработка ошибок
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    private void onSaveModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");

        // Устанавливаем начальную директорию как папку проекта
        String projectDir = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(projectDir));

        // Открываем диалог сохранения файла
        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return; // Если пользователь закрыл диалог без выбора файла
        }

        // Получаем путь к выбранному файлу
        Path filePath = Path.of(file.getAbsolutePath());
        Alert alert = new Alert(Alert.AlertType.NONE);
        try {
            String modelData = ObjWriter.write(mesh);
            Files.write(filePath, modelData.getBytes()); // Записываем данные в файл
            alert.setAlertType(Alert.AlertType.INFORMATION);
            alert.setContentText("Модель успешно сохранена в файл: " + filePath);
            alert.show();
        } catch (IOException exception) {
            // Обработка ошибок записи файла
            exception.printStackTrace();
        }
    }



    @FXML
    private void onLoadTextureMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Texture");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );

        String projectDir = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(projectDir));

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.NONE);
        try {
            texture = ImageIO.read(file);
            alert.setAlertType(Alert.AlertType.INFORMATION);
            alert.setContentText("Текстура успешно загружена");
            alert.show();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }

    @FXML
    private CheckBox drawLinesCheckBox;

    @FXML
    private CheckBox drawTextureCheckBox;

    @FXML
    private CheckBox useLightCheckBox;

    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Button deleteModelButton;
    @FXML
    private void handleDeleteModelButtonClick(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        mesh = null; // Очищаем модель
        texture = null; // Очищаем текстуру
        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setContentText("Модель и текстура удалены.");
        alert.show();
    }
}