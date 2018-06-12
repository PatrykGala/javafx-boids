package com.tofti;

import java.util.Set;

public interface Algorithm {

    Vector2D update(Vector2D current, Set<Boid> all);
}
