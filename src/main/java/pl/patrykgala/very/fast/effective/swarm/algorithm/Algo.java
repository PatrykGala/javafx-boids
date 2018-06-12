package pl.patrykgala.very.fast.effective.swarm.algorithm;

import com.tofti.Algorithm;
import com.tofti.Boid;
import com.tofti.Vector2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Algo implements Algorithm {

    private static final double RANGE = 30;
    private static final double S = 5.0d;


    private final NeuralNetAlgo alg = new NeuralNetAlgo();


    private static final Logger LOG = LoggerFactory.getLogger(Algo.class);

    //index, x,y
    io.vavr.collection.List<Sensor> sensors = io.vavr.collection.List.of(
            new Sensor(0, -2d * S, 2d * S),
            new Sensor(1, -1d * S, 0d * S),
            new Sensor(2, -2d * S, 1d * S),
            new Sensor(3, 2d * S, 2d * S),
            new Sensor(4, 1d * S, 1d * S),
            new Sensor(5, 2d * S, 0d * S)
    );


    private Optional<Integer> isActive(Sensor sensor, Vector2D current, Vector2D other) {
        final Vector2D sensorV = new Vector2D(current.getX() + sensor.x, current.getY() + sensor.y);

        final double distanceFrom = sensorV.getDistanceFrom(other);

        if (distanceFrom < RANGE) {
            return Optional.of(sensor.id);
        }
        return Optional.empty();


    }

    @Override
    public Vector2D update(Vector2D current, Set<Boid> all) {
        Set<Integer> actives = new HashSet<>();

        for (Boid boid : all) {
            if (boid.getLocation() != current) {
                for (Sensor sensor : sensors) {
                    isActive(sensor, current, boid.getLocation()).ifPresent(actives::add);
                }
            }
        }
        return alg.update(actives);
    }


    final class Sensor {
        final int id;
        final double x;
        final double y;

        Sensor(int id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }
}
