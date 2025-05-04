import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class MatrixOrderSum {
    static class OrderTask extends RecursiveTask<int[]> {
        private final int[][] matrix;
        private final int startRow, endRow;

        public OrderTask(int[][] matrix, int startRow, int endRow) {
            this.matrix = matrix;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        protected int[] compute() {
            int maxOrder = 0;
            int sumAtMaxOrder = 0;
            int n = matrix.length;

            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < n; j++) {
                    int order = 0;

                    // Головна діагональ
                    for (int k = -Math.min(i, j); i + k < n && j + k < n && i + k >= 0 && j + k >= 0; k++) {
                        if (matrix[i + k][j + k] != 0) order++;
                    }

                    // Побічна діагональ
                    for (int k = -Math.min(i, n - 1 - j); i + k < n && j - k >= 0 && i + k >= 0 && j - k < n; k++) {
                        if (matrix[i + k][j - k] != 0) order++;
                    }

                    if (matrix[i][j] != 0) order--;

                    if (order > maxOrder) {
                        maxOrder = order;
                        sumAtMaxOrder = matrix[i][j];
                    } else if (order == maxOrder) {
                        sumAtMaxOrder += matrix[i][j];
                    }
                }
            }

            return new int[]{maxOrder, sumAtMaxOrder};
        }
    }

    public static int parallelMaxOrderSum(int[][] matrix) throws InterruptedException, ExecutionException {
        int n = matrix.length;
        ForkJoinPool pool = new ForkJoinPool();
        List<OrderTask> tasks = new ArrayList<>();

        int chunkSize = Math.max(1, n / Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < n; i += chunkSize) {
            int end = Math.min(i + chunkSize, n);
            tasks.add(new OrderTask(matrix, i, end));
        }

        List<Future<int[]>> results = new ArrayList<>();
        for (OrderTask task : tasks) {
            results.add(pool.submit(task));
        }

        int globalMaxOrder = 0;
        int globalSum = 0;
        for (Future<int[]> result : results) {
            int[] res = result.get();
            if (res[0] > globalMaxOrder) {
                globalMaxOrder = res[0];
                globalSum = res[1];
            } else if (res[0] == globalMaxOrder) {
                globalSum += res[1];
            }
        }

        return globalSum;
    }

    public static void main(String[] args) throws Exception {
        int[][] matrix = generateRandomMatrix(1000);

        long startTime = System.nanoTime();

        int sum = parallelMaxOrderSum(matrix);

        long endTime = System.nanoTime();

        long durationNs = endTime - startTime;
        double durationMs = durationNs / 1_000_000.0;

        System.out.println("Сума елементів найбільшого порядку: " + sum);
        System.out.printf("Час виконання: %.3f мс%n", durationMs);
    }

    // Допоміжний метод для генерації випадкової матриці
    public static int[][] generateRandomMatrix(int size) {
        Random rand = new Random();
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                matrix[i][j] = rand.nextInt(10); // Випадкові значення від 0 до 9
        System.out.println("Сгенерована матриця A " + size + "×" + size + "\n");
        return matrix;
    }
}
