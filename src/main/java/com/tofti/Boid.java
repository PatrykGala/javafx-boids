package com.tofti;


import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Sphere;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class Boid {
    static final boolean COLOR_SENSITIVE_DEFAULT = false;
    private final UUID uuid = UUID.randomUUID();
    Vector2D location;
    Vector2D velocity;

    double xBound;
    double yBound;

    double aligmentWeight;
    double seperationWeight;
    double cohesionWeight;

    boolean colorSensitive;

    final Sphere sphere;
    final Polygon poly;
    final Color color;

    static final double MAX_VELOCITY = 3;

    static final int LOC_Z = 0;
    static final int RADIUS = 5;
    static final double TRI_SIZE = 8d;


    static final double DEFAULT_WEIGHT = 0.5d;
    static final double MAX_WEIGHT = 3d;


    static final Random RNG = new Random(System.currentTimeMillis());
    static final List<Color> COLORS = io.vavr.collection.List.of(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN).asJava();
    private final Algorithm algorithm;

    Boid(Algorithm algorithm, double locX, double locY, double xBound, double yBound) {
        this.algorithm = algorithm;
        this.location = new Vector2D(locX, locY);
        this.xBound = xBound;
        this.yBound = yBound;
        this.velocity = new Vector2D(RNG.nextDouble() * MAX_VELOCITY - 0.5 * MAX_VELOCITY,
                RNG.nextDouble() * MAX_VELOCITY - 0.5 * MAX_VELOCITY);

        this.setAligmentWeight(DEFAULT_WEIGHT);
        this.setSeperationWeight(DEFAULT_WEIGHT);
        this.setCohesionWeight(DEFAULT_WEIGHT);
        this.setColorSensitive(COLOR_SENSITIVE_DEFAULT);

        this.sphere = new Sphere(RADIUS);
        this.poly = new Polygon();
        poly.getPoints().addAll(0.0, TRI_SIZE, TRI_SIZE, -TRI_SIZE, -TRI_SIZE, -TRI_SIZE);
        poly.setCache(true);
        poly.setCacheHint(CacheHint.SPEED);

        color = COLORS.get(RNG.nextInt(COLORS.size()));
        poly.setFill(color);
    }

    public void setAligmentWeight(double aligmentWeight) {
        this.aligmentWeight = aligmentWeight;
    }

    public void setSeperationWeight(double seperationWeight) {
        this.seperationWeight = seperationWeight;
    }

    public void setCohesionWeight(double cohesionWeight) {
        this.cohesionWeight = cohesionWeight;
    }

    public double getXBound() {
        return xBound;
    }

    public void setXBound(double xBound) {
        this.xBound = xBound;
    }

    public double getYBound() {
        return yBound;
    }

    public void setYBound(double yBound) {
        this.yBound = yBound;
    }

    @Override
    public String toString() {
        return String.format("location=[%s] velocity=[%s]" + System.lineSeparator(), location, velocity);
    }

    void updateAndRender(Set<Boid> all) {
        update(all);
        render();
    }

    void update(Set<Boid> all) {

        final Set<Boid> ts = new HashSet<>(all);
        ts.remove(this);
        velocity = algorithm.update(location, ts);


        velocity = velocity.normalizeTo(MAX_VELOCITY);
        location = location.plus(velocity);
        location = location.wrapAround(xBound, yBound);
    }

    public Vector2D getLocation() {
        return location;
    }


    void render() {
        sphere.setTranslateX(location.getX());
        sphere.setTranslateY(location.getY());
        sphere.setTranslateZ(-LOC_Z);

        poly.setTranslateX(location.getX());
        poly.setTranslateY(location.getY());
        double r = -90 + Math.toDegrees(Math.atan(velocity.getY() / velocity.getX()));
        r = velocity.getX() < 0.0d ? r - 180d : r;
        poly.setRotate(r);
    }

    Node getNode() {
        return poly;
    }

    public void setColorSensitive(Boolean colorSensitive) {
        this.colorSensitive = colorSensitive;
    }


    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return uuid.equals(obj);
    }
}
