package pl.patrykgala.very.fast.effective.swarm.algorithm;

import com.tofti.Vector2D;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Random;
import java.util.Set;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

class NeuralNetAlgo {
    private static final int seed = 123;
    private static final double learningRate = 0.005;
    private static final int batchSize = 13;
    private static final int nEpochs = 10;

    private static final int numInputs = 6;
    private static final int numOutputs = 3;
    private static final int numHiddenNodes = 20;

    private final MultiLayerNetwork model;


    private static final double VELOCITY = 2;


    private static final Vector2D straight = new Vector2D(0, VELOCITY);
    private static final Vector2D left = new Vector2D(-VELOCITY * cos(PI / 3), VELOCITY * sin(PI / 3));
    private static final Vector2D right = new Vector2D(VELOCITY * cos(PI / 3), VELOCITY * sin(PI / 3));


    private static final Vector2D t1 = new Vector2D(VELOCITY * cos(PI / 4), VELOCITY * sin(PI / 4));
    private static final Vector2D t2 = new Vector2D(VELOCITY * cos(PI / 4), VELOCITY * sin(PI / 4));


    private static Map<Integer, Vector2D> maps = HashMap.of(0, left, 1, right, 2, straight, 3, t1, 4, t2);
    private static final Random random = new Random();
    private static final Logger LOG = LoggerFactory.getLogger(NeuralNetAlgo.class);

    NeuralNetAlgo() {

        RecordReader rr = new CSVRecordReader();
        try {
            final String filenameTrain = new ClassPathResource("Zeszyt1.csv").getFile().getPath();


            rr.initialize(new FileSplit(new File(filenameTrain)));
            final RecordReaderDataSetIterator trainIter = new RecordReaderDataSetIterator(rr, batchSize, 0, 3);


            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                    .seed(seed)
                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                    .updater(new Nesterovs(learningRate))
                    .list()
                    .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                            .weightInit(WeightInit.XAVIER)
                            .activation(Activation.RELU)
                            .build())
                    .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                            .weightInit(WeightInit.XAVIER)
                            .activation(Activation.SOFTMAX)
                            .nIn(numHiddenNodes).nOut(numOutputs).build())
                    .pretrain(false).backprop(true).build();

            model = new MultiLayerNetwork(conf);
            model.init();
            model.setListeners(new ScoreIterationListener(10));

            for (int n = 0; n < nEpochs; n++) {
                model.fit(trainIter);
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    Vector2D update(Set<Integer> inputs) {

        if (!inputs.isEmpty()) {
            final double[] input = new double[6];
            inputs.forEach(i -> input[i] = 1);


            final INDArray output = model.output(Nd4j.create(input), false);

            final DataBuffer data = output.data();

            double[] values = data.asDouble();
            double max = DoubleStream.of(values).max().orElse(0d);
            int index = IntStream.range(0, 3).filter(u -> max > 0.5).filter(i -> values[i] == max).findFirst().orElseGet(() -> random.nextInt() % 5);

            return maps.getOrElse(abs(index), straight);
        } else {
            return maps.get(abs(random.nextInt() % 5)).get();
        }


    }


}
