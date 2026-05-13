package dev.lego.app;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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

    void point(Point2D p) {
        final int s = 20;
        gc.setFill(FOREGROUND_COLOR);
        var sHalf = s / 2.0;
        gc.fillRect(p.x() - sHalf, p.y() - sHalf, s, s);
    }

    void line(Point2D p1, Point2D p2) {
        gc.setLineWidth(LINE_WIDTH);
        gc.setStroke(FOREGROUND_COLOR);
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

    Point3D translate_z(Point3D p, double dz) {
        return new Point3D(p.x(), p.y(), p.z() + dz);
    }

    Point3D rotate_xy(Point3D p, double angle) {
        var cosAngle = Math.cos(angle);
        var sinAngle = Math.sin(angle);
        return new Point3D(
                p.x() * cosAngle - p.z() * sinAngle,
                p.y(),
                p.x() * sinAngle + p.z() * cosAngle
        );
    }

    Point3D[] vs = new Point3D[]{
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

    int[][] fs = new int[][]{
            {0, 1, 2, 3},
            {4, 5, 6, 7},
            {0, 4},
            {1, 5},
            {2, 6},
            {3, 7},
    };

    private final int ROT_SPEED = 1;

    private double dz = 1;
    private double angle = 0;

    private void update(double dt) {
        angle += ROT_SPEED * Math.PI * dt;
    }

    private void render() {
        clear();
        for (int[] f : fs) {
            for (int i = 0; i < f.length; ++i) {
                Point3D a = vs[f[i]];
                Point3D b = vs[f[(i + 1) % f.length]];
                line(
                        screen(project(translate_z(rotate_xy(a, angle), dz))),
                        screen(project(translate_z(rotate_xy(b, angle), dz)))
                );
            }
        }
    }

    @Override
    public void start(Stage stage) {

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        this.gc = canvas.getGraphicsContext2D();

        Scene scene = new Scene(new Group(canvas));

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

    public static void main(String[] args) {
        launch();
    }
}