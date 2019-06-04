package ObjectConditionExample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.StringJoiner;

public class ObjectConditionMain {

    private static final int N = 10;
    private static final String INPUT_FILE = "./out/matrices";
    private static final String OUTPUT_FILE = "./out/matrices-result.txt";

    public static void main(String[] args) throws IOException {
        ThreadSafeQueue queue = new ThreadSafeQueue();
        File inputFile = new File(INPUT_FILE);
        File outputFile = new File(OUTPUT_FILE);

        MatricesReaderProducer producer = new MatricesReaderProducer(new FileReader(inputFile), queue);
        MatricesMultiplierConsumer consumer = new MatricesMultiplierConsumer(new FileWriter(outputFile), queue);

        producer.start();
        consumer.start();
    }

    public static class MatricesMultiplierConsumer extends Thread {
        private ThreadSafeQueue queue;
        private FileWriter fileWriter;

        public MatricesMultiplierConsumer(FileWriter fileWriter, ThreadSafeQueue threadSafeQueue) {
            this.fileWriter = fileWriter;
            this.queue = threadSafeQueue;
        }

        private static void saveMatricesToFile(FileWriter fileWriter, float[][] m) throws IOException {
            for (int i = 0; i < N; i++) {
                StringJoiner stringJoiner = new StringJoiner(", ");
                for (int j = 0; j < N; j++) {
                    stringJoiner.add(String.format("%.2f", m[i][j]));
                }
                fileWriter.write(stringJoiner.toString());
                fileWriter.write("\n");
            }

            fileWriter.write("\n");
        }

        @Override
        public void run() {
            while (true) {
                MatricesPair pair = queue.remove();

                if (pair == null) {
                    System.out.println("There are no more matrices to consume. Consumer is terminating");
                    break;
                }

                float[][] result = multiplyMatrices(pair.matrix1, pair.matrix2);
                try {
                    saveMatricesToFile(fileWriter, result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private float[][] multiplyMatrices(float[][] m1, float[][] m2) {
            float result[][] = new float[N][N];

            for (int i = 0 ; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    for (int k = 0; k < N; k++) {
                        result[i][j] = m1[i][k] * m2[k][j];
                    }
                }
            }

            return result;
        }
    }

    private static class MatricesReaderProducer extends Thread {
        private Scanner scanner;
        private ThreadSafeQueue queue;

        public MatricesReaderProducer(FileReader reader, ThreadSafeQueue queue) {
            this.scanner = new Scanner(reader);
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true) {
                float[][] matrix1 = readMatrix();
                float[][] matrix2 = readMatrix();

                if (matrix1 == null || matrix2 == null) {
                    queue.terminate();
                    System.out.println("No more matrices to read. Producer Thread is terminating");
                    return;
                }

                MatricesPair matricesPair = new MatricesPair(matrix1, matrix2);
                queue.add(matricesPair);
            }
        }

        private float[][] readMatrix() {
            float[][] matrix = new float[N][N];
            for (int i = 0; i < N; i++) {
                if (!scanner.hasNext()) {
                    return null;
                }
                String[] line = scanner.nextLine().split(",");
                for (int j = 0; j < N; j++) {
                    matrix[i][j] = Float.valueOf(line[j]);
                }
            }
            scanner.nextLine();
            return matrix;
        }
    }

    public static class ThreadSafeQueue {
        private Queue<MatricesPair> queue = new LinkedList<>();
        private boolean isEmpty = true;
        private boolean isTerminate = false;
        private static final int CAPACITY = 5;

        public synchronized void add(MatricesPair pair) {
            while(queue.size() == CAPACITY) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            queue.add(pair);
            isEmpty = false;
            notify();
        }

        public synchronized MatricesPair remove() {
            MatricesPair pair = null;
            while (isEmpty && !isTerminate) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            if (queue.size() == 1) {
                isEmpty = true;
            }

            if (queue.size() == 0 && isTerminate) {
                return null;
            }

            System.out.println("queue size " + queue.size());
            pair = queue.remove();

            if (queue.size() == CAPACITY - 1) {
                notifyAll();
            }
            return pair;
        }

        public synchronized void terminate() {
            isTerminate = true;
            notifyAll();
        }
    }

    public static class MatricesPair {
        public float[][] matrix1;
        public float[][] matrix2;

        public MatricesPair(float[][] matrix1, float[][] matrix2) {
            this.matrix1 = matrix1;
            this.matrix2 = matrix2;
        }
    }
}
