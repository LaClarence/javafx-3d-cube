package dev.lego.app;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.function.DoubleConsumer;

public class Main extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private static final int LINE_WIDTH = 3;

    private static final Color BACKGROUND_COLOR = Color.web("#101010");
    private static final Color FOREGROUND_COLOR = Color.web("#50FF50");

    private record Point2D(double x, double y) {}
    private record Point3D(double x, double y, double z) {}

    private GraphicsContext gc;

    void clear() {
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, WIDTH, HEIGHT);
    }

    void line(Point2D p1, Point2D p2) {
        gc.beginPath();
        gc.moveTo(p1.x(), p1.y());
        gc.lineTo(p2.x(), p2.y());
        gc.stroke();
    }

    Point2D screen(Point2D p) {
        return new Point2D(
                ((p.x() + 1) / 2) * WIDTH,
                (1 - ((p.y() + 1) / 2)) * HEIGHT
        );
    }

    Point2D project(Point3D p) {
        return new Point2D(p.x() / p.z(), p.y() / p.z());
    }

    Point3D translateZ(Point3D p, double dz) {
        return new Point3D(p.x(), p.y(), p.z() + dz);
    }

    Point3D rotateXY(Point3D p, double angle) {
        var cosAngle = Math.cos(angle);
        var sinAngle = Math.sin(angle);
        return new Point3D(
                p.x() * cosAngle - p.z() * sinAngle,
                p.y(),
                p.x() * sinAngle + p.z() * cosAngle
        );
    }

    private static final Point3D[] vs = new Point3D[]{
            // Back face of the cube
            new Point3D(0.25, 0.25, 0.25),
            new Point3D(-0.25, 0.25, 0.25),
            new Point3D(-0.25, -0.25, 0.25),
            new Point3D(0.25, -0.25, 0.25),
            // Front face of the cube
            new Point3D(0.25, 0.25, -0.25),
            new Point3D(-0.25, 0.25, -0.25),
            new Point3D(-0.25, -0.25, -0.25),
            new Point3D(0.25, -0.25, -0.25)
    };

    private static final int[][] fs = new int[][]{
            {0, 1, 2, 3},
            {4, 5, 6, 7},
            {0, 4},
            {1, 5},
            {2, 6},
            {3, 7},
    };

    private static final double ROT_SPEED = 1;
    private static final double MAX_ROT_SPEED = 4;

    // The rotated cube reaches z = -0.25 * sqrt(2) ≈ -0.35: dz must stay above it, otherwise z <= 0 in project()
    private static final double DZ = 1;
    private static final double MIN_DZ = 0.5;
    private static final double MAX_DZ = 5;

    private double dz = DZ;
    private double angle = 0;
    private double rotationSpeed = ROT_SPEED;

    private void update(double dt, double rotationSpeed) {
        angle += rotationSpeed * Math.PI * dt;
    }

    private void update(double dt) {
        update(dt, rotationSpeed);
    }

    private void render() {
        clear();
        gc.setLineWidth(LINE_WIDTH);
        gc.setStroke(FOREGROUND_COLOR);
        for (int[] f : fs) {
            for (int i = 0; i < f.length; ++i) {
                Point3D a = vs[f[i]];
                Point3D b = vs[f[(i + 1) % f.length]];
                line(
                        screen(project(translateZ(rotateXY(a, angle), dz))),
                        screen(project(translateZ(rotateXY(b, angle), dz)))
                );
            }
        }
    }

    private static HBox labeledSlider(String name, double min, double max, double value, DoubleConsumer onChange) {
        Slider slider = new Slider(min, max, value);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1);

        Label label = new Label(sliderText(name, value));
        label.setTextFill(FOREGROUND_COLOR);
        label.setMinWidth(110);

        slider.valueProperty().addListener((_, _, newValue) -> {
            onChange.accept(newValue.doubleValue());
            label.setText(sliderText(name, newValue.doubleValue()));
        });

        HBox row = new HBox(10, label, slider);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static String sliderText(String name, double value) {
        return String.format("%s: %.1f", name, value);
    }

    @Override
    public void start(Stage stage) {

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        this.gc = canvas.getGraphicsContext2D();

        VBox controls = new VBox(5,
                labeledSlider("Vitesse", 0, MAX_ROT_SPEED, rotationSpeed, value -> rotationSpeed = value),
                labeledSlider("Profondeur", MIN_DZ, MAX_DZ, dz, value -> dz = value)
        );
        controls.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane root = new StackPane(canvas, controls);
        StackPane.setAlignment(controls, Pos.TOP_LEFT);
        StackPane.setMargin(controls, new Insets(10));

        Scene scene = new Scene(root);

        stage.setTitle("One Formula That Demystifies 3D Graphics");
        stage.setScene(scene);
        stage.show();

        canvas.requestFocus();

        new AnimationTimer() {

            long lastTime = System.nanoTime();

            @Override
            public void handle(long now) {
                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                update(deltaTime);
                render();
            }

        }.start();
    }

    static void main() {
        launch();
    }
}