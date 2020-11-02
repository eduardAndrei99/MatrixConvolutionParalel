import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Main {

    public static int getLine(int index, int nrColumns) {
        return index / nrColumns;
    }

    public static int getColumn(int index, int nrColumns) {
        return index % nrColumns;
    }

    public static void main(String[] args) throws IOException {

        final int N, M, n, m;
        final int p = 2;

        CyclicBarrier cyclicBarrier = new CyclicBarrier(p);

        Path filePath = Paths.get("data.txt");
        Scanner scanner = new Scanner(filePath);
        N = scanner.nextInt();
        M = scanner.nextInt();
        double[][] F = new double[N][M];
        double[][] V = new double[N][M];

        for(int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                F[i][j] = scanner.nextDouble();
                V[i][j] = 0;
            }
        }

        n = scanner.nextInt();
        m = scanner.nextInt();
        double[][] W = new double[n][m];

        for(int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                W[i][j] = scanner.nextDouble();
            }
        }

        int chunkSize = (N * M) / p;
        int rest = (N * M) % p;
        int start = 0;
        int end = chunkSize;

        MyThread[] threads = new MyThread[p];

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < p; i++) {

            end = start + chunkSize;
            //start = end;
            //end = end + chunkSize;
            if (rest > 0) {
                end += 1;
                rest--;
            }

            MyThread myThread = new MyThread(cyclicBarrier, F, N, M, W, n, m , start, end);
            threads[i] = myThread;
            threads[i].start();
            //System.out.println(start + " " + end);
            start = end;
        }

        for (int i = 0; i < p; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(System.currentTimeMillis() - startTime);

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                System.out.print(F[i][j] + " ");
            }
            System.out.println();
        }

    }

    public static class MyThread extends Thread {

        private CyclicBarrier cyclicBarrier;
        private int start;
        private int end;
        private int M, N, n, m;
        private double F[][], W[][];

        public MyThread(CyclicBarrier cyclicBarrier, double F[][], int N, int M, double W[][],
                        int n, int m, int start, int end) {
            this.cyclicBarrier = cyclicBarrier;
            this.start = start;
            this.end = end;
            this.N = N;
            this.M = M;
            this.n = n;
            this.m = m;
            this.F = F;
            this.W = W;
        }

        @Override
        public void run() {
            double[] resPartial = new double[end - start + 1];
            //System.out.println("Start = " + start + " end = " + end);
            for (int index = start; index < end; index++) {
                int kCenterX = m / 2;
                int kCenterY = n / 2;
                int i, j;
                i = getLine(index, M);
                j = getColumn(index, M);

                //filtrarea elementului i j
                for (int k = 0; k < n; ++k)     // kernel rows
                {
                    for (int l = 0; l < m; ++l) // kernel columns
                    {

                        // index of input signal, used for checking boundary
                        int ii = i + (k - kCenterY);
                        int jj = j + (l - kCenterX);

                        // ignore input samples which are out of bound
                        if (ii >= 0 && ii < N && jj >= 0 && jj < M)
                            //V[i][j] += F[ii][jj] * W[k][l];
                            resPartial[index - start] += F[ii][jj] * W[k][l];
                    }
                }
            }
            try {
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                // ...
            } catch (BrokenBarrierException e) {
                // ...
            }
            for (int index = start; index < end; index++) {
                int i, j;
                i = getLine(index, M);
                j = getColumn(index, M);
                F[i][j] = resPartial[index - start];
            }
        }
    }
}
