import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    public static int getLine(int index, int nrColumns) {
        return index / nrColumns;
    }

    public static int getColumn(int index, int nrColumns) {
        return index % nrColumns;
    }

    public static void main(String[] args) throws IOException {

        final int N, M, n, m;
        final int p = 16;

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

            MyThread myThread = new MyThread(F, N, M, W, n, m , V, start, end);
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

        /*
        long startTime = System.currentTimeMillis();
        for (int index = 0; index < N * M; index++) {
            int i = getLine(index, M);
            int j = getColumn(index, M);
            filter(i, j, F, N, M, W, n, m, V);
        }
        System.out.println("time : " + (System.currentTimeMillis() - startTime));
         */



    }

    public static class MyThread extends Thread {

        private int start;
        private int end;
        private int M, N, n, m;
        private double F[][], W[][], V[][];

        public MyThread(double F[][], int N, int M, double W[][],
                        int n, int m, double[][]V, int start, int end) {
            this.start = start;
            this.end = end;
            this.N = N;
            this.M = M;
            this.n = n;
            this.m = m;
            this.F = F;
            this.W = W;
            this.V = V;
        }

        @Override
        public void run() {
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
                            V[i][j] += F[ii][jj] * W[k][l];
                    }
                }
            }
        }
    }
}
