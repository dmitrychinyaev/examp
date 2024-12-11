package com.example;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;


public class Main {
    static final int SIZE = 11;

    public static void main(String[] args) {
        int[][] matrix = generateMatrix(SIZE);

        long start = System.currentTimeMillis();
        int determinant = determinant(matrix);

        long end = System.currentTimeMillis();
        System.out.println("Однопоточное выполнение: " + determinant + ", время: " + (end - start) + "ms");

        start = System.currentTimeMillis();
        determinant = parallelDeterminant(matrix);
        end = System.currentTimeMillis();
        System.out.println("Многопоточное выполнение: " + determinant + ", время: " + (end - start) + "ms");
    }

    static int[][] generateMatrix(int size) {
        int[][] matrix = new int[size][size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = random.nextInt(100);
            }
        }
        return matrix;
    }

    // static int[][] generateMatrixWithSameRows(int size) {
    //     int[][] matrix = new int[size][size];
    //     Random random = new Random();
    //     int[] row = new int[size];
    //     for (int i = 0; i < size; i++) {
    //         row[i] = random.nextInt(100); // 
    //     }
    //     for (int i = 0; i < size; i++) {
    //         matrix[i] = row.clone();
    //     }
    //     return matrix;
    // }

    static int determinant(int[][] matrix) {
        int n = matrix.length;
        if (n == 1) {
            return matrix[0][0];
        } else if (n == 2) {
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        }

        int det = 0;
        for (int i = 0; i < n; i++) {
            int[][] minor = new int[n - 1][n - 1];
            for (int j = 1; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    if (k < i) {
                        minor[j - 1][k] = matrix[j][k];
                    } else if (k > i) {
                        minor[j - 1][k - 1] = matrix[j][k];
                    }
                }
            }
            det += (i % 2 == 0 ? 1 : -1) * matrix[0][i] * determinant(minor);
        }
        return det;
    }

    static int parallelDeterminant(int[][] matrix) {
        ForkJoinPool pool = new ForkJoinPool();
        return pool.invoke(new DeterminantTask(matrix));
    }

    static class DeterminantTask extends RecursiveTask<Integer> {
        private final int[][] matrix;

        DeterminantTask(int[][] matrix) {
            this.matrix = matrix;
        }

        @Override
        protected Integer compute() {
            int n = matrix.length;
            if (n <= 2) {
                return determinant(matrix);
            }

            List<DeterminantTask> tasks = new ArrayList<>();
            int det = 0;
            for (int i = 0; i < n; i++) {
                int[][] minor = new int[n - 1][n - 1];
                for (int j = 1; j < n; j++) {
                    for (int k = 0; k < n; k++) {
                        if (k < i) {
                            minor[j - 1][k] = matrix[j][k];
                        } else if (k > i) {
                            minor[j - 1][k - 1] = matrix[j][k];
                        }
                    }
                }
                DeterminantTask task = new DeterminantTask(minor);
                tasks.add(task);
                task.fork();
            }

            for (int i = 0; i < n; i++) {
                det += (i % 2 == 0 ? 1 : -1) * matrix[0][i] * tasks.get(i).join();
            }
            return det;
        }
    }
}