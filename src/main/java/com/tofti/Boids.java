package com.tofti;


import com.sun.javafx.perf.PerformanceTracker;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import pl.patrykgala.very.fast.effective.swarm.algorithm.Algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class Boids extends Application {

    public static final Algo ALGO = new Algo();

    public static void main(String[] args) {
        Application.launch(args);
    }

    private static int DEFAULT_WINDOW_WIDTH = 1024;
    private static int DEFAULT_WINDOW_HEIGHT = 768;
    private static boolean USE_DEPTH_BUFFER = true;
    private static double DEBUG_MARKER_SIZE = 5;

    static Rectangle addDebugMarkers(double x, double y, double xSize, double ySize) {
        Rectangle tl = new Rectangle(x, y, xSize, ySize);
        tl.setFill(Color.RED);
        return tl;
    }

    static List<Rectangle> reinitDebugMarkersInCorner(List<Rectangle> nodes, double xBound, double yBound) {
        nodes.clear();
        nodes.add(addDebugMarkers(0, 0, DEBUG_MARKER_SIZE, DEBUG_MARKER_SIZE));
        nodes.add(addDebugMarkers(xBound - DEBUG_MARKER_SIZE, 0, DEBUG_MARKER_SIZE, DEBUG_MARKER_SIZE));
        nodes.add(addDebugMarkers(xBound - DEBUG_MARKER_SIZE, yBound - DEBUG_MARKER_SIZE, DEBUG_MARKER_SIZE, DEBUG_MARKER_SIZE));
        nodes.add(addDebugMarkers(0, yBound - DEBUG_MARKER_SIZE, DEBUG_MARKER_SIZE, DEBUG_MARKER_SIZE));
        return nodes;
    }

    List<Boid> initRandomBoids(final int n) {
        List<Boid> boids = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            boids.add(new Boid(ALGO, rand.nextDouble() * DEFAULT_WINDOW_WIDTH,
                    rand.nextDouble() * DEFAULT_WINDOW_HEIGHT,
                    DEFAULT_WINDOW_WIDTH,
                    DEFAULT_WINDOW_HEIGHT));
        }
        return boids;
    }


    void reinit(Set<Boid> boids, Group root, List<Slider> sliders) {
        final List<Node> nodes = boids.stream().map(Boid::getNode).collect(Collectors.toList());
        root.getChildren().removeAll(nodes);
        boids.clear();
        boids.addAll(initRandomBoids(1));
        root.getChildren().addAll(nodes);
        sliders.forEach(s -> s.valueProperty().set(Boid.DEFAULT_WEIGHT));
    }

    @Override
    public void start(Stage stage) {
        PerspectiveCamera camera = new PerspectiveCamera(false);

        Group root = new Group();
        Scene scene = new Scene(root, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT, USE_DEPTH_BUFFER);
        final ContextMenu contextMenu = new ContextMenu();
        final Set<Boid> boids = new HashSet<>();

        Slider alignmentSlider = new Slider();
        List<CustomMenuItem> alignmentControls = buildLabelAndSlider(alignmentSlider, "Alignment: %.3f", 0.1,
                Boid.MAX_WEIGHT, (ov, old_val, new_val) -> boids.forEach(b -> b.setAligmentWeight(new_val.doubleValue())));

        Slider cohesionSlider = new Slider();
        List<CustomMenuItem> cohesionControls = buildLabelAndSlider(cohesionSlider, "Cohesion: %.3f", 0.1, Boid.MAX_WEIGHT,
                (ov, old_val, new_val) -> boids.forEach(b -> b.setCohesionWeight(new_val.doubleValue())));

        Slider seperationSlider = new Slider();
        List<CustomMenuItem> seperationControls = buildLabelAndSlider(seperationSlider, "Seperation: %.3f", 0.1, Boid.MAX_WEIGHT,
                (ov, old_val, new_val) -> boids.forEach(b -> b.setSeperationWeight(new_val.doubleValue())));

        contextMenu.getItems().addAll(alignmentControls);
        contextMenu.getItems().addAll(cohesionControls);
        contextMenu.getItems().addAll(seperationControls);

        final List<Slider> allSliders = Arrays.asList(alignmentSlider, cohesionSlider, seperationSlider);

        ToggleButton colorSensitive = new ToggleButton();
        colorSensitive.setSelected(Boid.COLOR_SENSITIVE_DEFAULT);
        colorSensitive.setText("Color Sensitive");
        colorSensitive.selectedProperty().addListener((observable, oldValue, newValue) -> boids.forEach(b -> b.setColorSensitive(newValue)));
        contextMenu.getItems().add(new CustomMenuItem(colorSensitive));

        Button reset = new Button();
        reset.setText("Reset");
        reset.setOnAction(e -> reinit(boids, root, allSliders));
        contextMenu.getItems().add(new CustomMenuItem(reset));

        final Label fpsLabel = new Label();
        contextMenu.getItems().add(new CustomMenuItem(fpsLabel));

        reinit(boids, root, allSliders);

        scene.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(stage, e.getScreenX(), e.getScreenY());
            }
            if (e.getButton() == MouseButton.PRIMARY) {
                Boid newBoid = new Boid(ALGO, e.getSceneX(), e.getSceneY(), scene.getWidth(), scene.getHeight());
                boids.add(newBoid);
                root.getChildren().add(newBoid.getNode());
            }
        });

        final List<Rectangle> debugNodes = new ArrayList<>();
        reinitDebugMarkersInCorner(debugNodes, scene.getWidth(), scene.getHeight());
        root.getChildren().addAll(debugNodes);

        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            boids.forEach(b -> b.setYBound(newVal.doubleValue()));
            root.getChildren().removeAll(debugNodes);
            reinitDebugMarkersInCorner(debugNodes, scene.getWidth(), scene.getHeight());
            root.getChildren().addAll(debugNodes);

        });

        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            boids.forEach(b -> b.setXBound(newVal.doubleValue()));
            root.getChildren().removeAll(debugNodes);
            reinitDebugMarkersInCorner(debugNodes, scene.getWidth(), scene.getHeight());
            root.getChildren().addAll(debugNodes);
        });

        // Add the Scene to the Stage
        scene.setCamera(camera);
        scene.setFill(Color.BLACK);
        stage.setScene(scene);
        stage.setTitle("JavaFX Boids");

        AnimationTimer t = new AnimationTimer() {
            @Override
            public void handle(long now) {
                boids.forEach(b -> b.updateAndRender(boids));
                fpsLabel.setText(String.format("FPS: %.3f", PerformanceTracker.getSceneTracker(scene).getAverageFPS()));
            }
        };
        t.start();
        stage.show();
    }

    static List<CustomMenuItem> buildLabelAndSlider(Slider slider, final String labelText, double blockIncrement, double max, ChangeListener<Number> changeListener) {
        slider.setBlockIncrement(blockIncrement);
        slider.setMax(max);
        Label label = new Label();
        label.setText(String.format(labelText, slider.valueProperty().get()));

        CustomMenuItem sliderLabelMenuItem = new CustomMenuItem(label);
        CustomMenuItem sliderMenuItem = new CustomMenuItem(slider);

        slider.valueProperty().addListener(changeListener);
        slider.valueProperty().addListener((ov, old_val, new_val) -> {
            label.setText(String.format(labelText, new_val));
        });
        return Arrays.asList(sliderLabelMenuItem, sliderMenuItem);
    }
}