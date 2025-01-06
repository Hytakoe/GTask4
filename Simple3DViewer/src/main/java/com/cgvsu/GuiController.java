package com.cgvsu;

import com.cgvsu.affine_transformations.*;
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
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
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
import javafx.scene.control.TextField;
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
    Pane mainPane;

    @FXML
    private Canvas canvas;

    private Model mesh = null;
    private Model modifiedMesh = null;
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

        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()- mainPane.getWidth()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(50), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            float[][] ZBuffer = new float[(int) width][(int) height];
            for (int x = 0; x < (int)width; x++) {
                for (int y = 0; y < (int)height; y++) {
                    ZBuffer[x][y] = 9999.0F;
                }
            }

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
                            colorPicker.getValue(),
                            ZBuffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        deleteModelButton.setOnAction(this::handleDeleteModelButtonClick);

        canvas.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        anchorPane.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasFiles()) {
                for (File file : dragboard.getFiles()) {
                    if (file.getName().toLowerCase().endsWith(".obj")) {
                        loadModelFromFile(file); // Загружаем модель
                        success = true;
                    } else if (isImageFile(file)) {
                        loadTextureFromFile(file); // Загружаем текстуру
                        success = true;
                    }
                }
                if (!success) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Поддерживаются только файлы .obj и изображения (.png, .jpg, .jpeg)");
                    alert.show();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        // Подсветка области для Drop
        anchorPane.setOnDragEntered(event -> {
            anchorPane.setStyle("-fx-background-color: #e0e0e0;");
        });

        anchorPane.setOnDragExited(event -> {
            anchorPane.setStyle("-fx-background-color: transparent;");
        });
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
    private void onSaveModelMenuItemClick(Model modelToSave) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");

        String projectDir = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(projectDir));

        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path filePath = Path.of(file.getAbsolutePath());
        Alert alert = new Alert(Alert.AlertType.NONE);
        try {
            String modelData = ObjWriter.write(modelToSave);
            Files.write(filePath, modelData.getBytes());

            alert.setAlertType(Alert.AlertType.INFORMATION);
            alert.setContentText("Модель успешно сохранена в файл: " + filePath);
            alert.show();
        } catch (IOException exception) {
            exception.printStackTrace();

            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setContentText("Ошибка при сохранении модели: " + exception.getMessage());
            alert.show();
        }
    }

    @FXML
    private void onSaveOriginalModel(ActionEvent event) {
        if (mesh == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Исходная модель не загружена");
            alert.setContentText("Пожалуйста, загрузите модель перед сохранением.");
            alert.showAndWait();
        } else {
            onSaveModelMenuItemClick(mesh);
        }
    }

    @FXML
    private void onSaveModifiedModel(ActionEvent event) {
        if (modifiedMesh == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Изменённая модель не загружена");
            alert.setContentText("Пожалуйста, создайте или загрузите изменённую модель перед сохранением.");
            alert.showAndWait();
        } else {
            onSaveModelMenuItemClick(modifiedMesh);
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
    public void handleCanvasClick(MouseEvent mouseEvent){
        canvas.requestFocus();
    }
    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION*10));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION*10));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(TRANSLATION*10, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(-TRANSLATION*10, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, TRANSLATION*10, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION*10, 0));
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

    @FXML
    private TextField rx, ry, rz; // Поля для вращения
    @FXML
    private TextField sx, sy, sz; // Поля для масштабирования
    @FXML
    private TextField tx, ty, tz; // Поля для перемещения

    @FXML
    private void applyTransformations() {
        try {
            if (mesh != null) {
                // Вращение
                float angleX = Float.parseFloat(rx.getText());
                float angleY = Float.parseFloat(ry.getText());
                float angleZ = Float.parseFloat(rz.getText());

                CompositeAffine composite = new CompositeAffine();
                if (angleX!=0){
                    composite.add(new Rotate(angleX, Axis.X));
                }
                if (angleY!=0){
                    composite.add(new Rotate(angleY, Axis.Y));
                }
                if (angleZ!=0){
                    composite.add(new Rotate(angleZ, Axis.Z));
                }
                mesh.vertices = composite.execute(mesh.vertices);

                // Масштабирование
                float scaleX = Float.parseFloat(sx.getText());
                float scaleY = Float.parseFloat(sy.getText());
                float scaleZ = Float.parseFloat(sz.getText());

                composite = new CompositeAffine();
                composite.add(new Scale(scaleX,scaleY,scaleZ));
                mesh.vertices = composite.execute(mesh.vertices);

                // Перемещение
                float translateX = Float.parseFloat(tx.getText());
                float translateY = Float.parseFloat(ty.getText());
                float translateZ = Float.parseFloat(tz.getText());

                composite = new CompositeAffine();
                composite.add(new Transfer(translateX,translateY,translateZ));
                mesh.vertices = composite.execute(mesh.vertices);

                // Обновляем отображение
                timeline.play();
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Некорректные данные в одном из полей");
            alert.show();
        }
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
    }

    private void loadModelFromFile(File file) {
        try {
            String fileContent = Files.readString(file.toPath());
            mesh = ObjReader.read(fileContent); // Загружаем модель
            timeline.play(); // Обновляем отображение
        } catch (IOException exception) {
            exception.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Ошибка при загрузке файла: " + exception.getMessage());
            alert.show();
        }
    }

    private void loadTextureFromFile(File file) {
        try {
            texture = ImageIO.read(file); // Загружаем текстуру
            timeline.play(); // Обновляем отображение
        } catch (IOException exception) {
            exception.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Ошибка при загрузке текстуры: " + exception.getMessage());
            alert.show();
        }
    }
}