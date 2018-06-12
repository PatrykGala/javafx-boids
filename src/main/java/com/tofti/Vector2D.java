package com.tofti;

import java.util.Collection;
import java.util.Objects;

public final class Vector2D {
    private final double x;
    private final double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vector2D plus(Vector2D arg) {
        return new Vector2D(x + arg.getX(), y + arg.getY());
    }

    public Vector2D minus(Vector2D arg) {
        return new Vector2D(x - arg.getX(), y - arg.getY());
    }

    public Vector2D wrapAround(double xLimit, double yLimit) {
        double wx = x > xLimit ? x - xLimit : x;
        wx = wx < 0 ? xLimit - wx : wx;

        double wy = y > yLimit ? y - yLimit : y;
        wy = wy < 0 ? yLimit - wy : wy;

        return new Vector2D(wx, wy);
    }

    public static Vector2D getArithmeticMean(Collection<? extends Vector2D> input) {
        double sumX = 0d;
        double sumY = 0d;
        for (Vector2D i : input) {
            sumX += i.getX();
            sumY += i.getY();
        }
        int size = input.size();
        return new Vector2D(sumX / (double) size, sumY / (double) size);
    }

    public double getDistanceFrom(Vector2D other) {
        return getDistanceBetween(this, other);
    }

    public Vector2D normalize() {
        return normalizeTo(1d);
    }

    public Vector2D normalizeTo(double to) {
        double magnitude = getMagnitude();
        if (0d == magnitude) {
            return new Vector2D(0d, 0d);
        }
        double xi = x / magnitude * to;
        double yi = y / magnitude * to;
        return new Vector2D(xi, yi);
    }

    public double getMagnitude() {
        double m = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        return m;
    }

    public static double getDistanceBetween(Vector2D a, Vector2D b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }


    @Override
    public String toString() {
        return "Vector2D{" + "x=" + x + ", y=" + y + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2D vector2D = (Vector2D) o;
        return Double.compare(vector2D.x, x) == 0 &&
                Double.compare(vector2D.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
