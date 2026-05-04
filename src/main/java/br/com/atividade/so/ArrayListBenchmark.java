package br.com.atividade.so;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class ArrayListBenchmark {
    private static final int[] SIZES = {1000, 10000, 50000};
    private static final int OPS = 5000;
    private static final int THREADS = 16;
    private static final int OPS_PER_THREAD = 2000;

    public static void main(String[] args) throws Exception {
        System.out.println("EXERCICIO 3 - BENCHMARK DE LISTAS\n");
        singleThread();
        multiThread();
    }

    private static void singleThread() {
        System.out.println("=== 1 THREAD: ArrayList x ThreadSafeArrayList ===");
        for (int size : SIZES) {
            ArrayList<Integer> a1 = arrayList(size);
            ThreadSafeArrayList<Integer> s1 = safeList(size);
            Result ai = measure("ArrayList", size, "insercao", OPS, () -> a1.add(ThreadLocalRandom.current().nextInt()));
            Result si = measure("ThreadSafeArrayList", size, "insercao", OPS, () -> s1.add(ThreadLocalRandom.current().nextInt()));

            ArrayList<Integer> a2 = arrayList(size);
            ThreadSafeArrayList<Integer> s2 = safeList(size);
            Result ab = measure("ArrayList", size, "busca", OPS, () -> a2.contains(ThreadLocalRandom.current().nextInt(size)));
            Result sb = measure("ThreadSafeArrayList", size, "busca", OPS, () -> s2.contains(ThreadLocalRandom.current().nextInt(size)));

            ArrayList<Integer> a3 = arrayList(size + OPS);
            ThreadSafeArrayList<Integer> s3 = safeList(size + OPS);
            Result ar = measure("ArrayList", size, "remocao", OPS, () -> a3.remove(a3.size() - 1));
            Result sr = measure("ThreadSafeArrayList", size, "remocao", OPS, () -> s3.removeAt(s3.size() - 1));
            print(ai, si, ab, sb, ar, sr);
            System.out.println();
        }
    }

    private static void multiThread() throws Exception {
        System.out.println("=== 16 THREADS: ThreadSafeArrayList x Vector ===");
        for (int size : SIZES) {
            ThreadSafeArrayList<Integer> s1 = safeList(size);
            Vector<Integer> v1 = vector(size);
            Result si = concurrent("ThreadSafeArrayList", size, "insercao", () -> s1.add(ThreadLocalRandom.current().nextInt()));
            Result vi = concurrent("Vector", size, "insercao", () -> v1.add(ThreadLocalRandom.current().nextInt()));

            ThreadSafeArrayList<Integer> s2 = safeList(size);
            Vector<Integer> v2 = vector(size);
            Result sb = concurrent("ThreadSafeArrayList", size, "busca", () -> s2.contains(ThreadLocalRandom.current().nextInt(size)));
            Result vb = concurrent("Vector", size, "busca", () -> v2.contains(ThreadLocalRandom.current().nextInt(size)));

            ThreadSafeArrayList<Integer> s3 = safeList(size + THREADS * OPS_PER_THREAD);
            Vector<Integer> v3 = vector(size + THREADS * OPS_PER_THREAD);
            Result sr = concurrent("ThreadSafeArrayList", size, "remocao", () -> { if (!s3.isEmpty()) s3.removeAt(s3.size() - 1); });
            Result vr = concurrent("Vector", size, "remocao", () -> { synchronized (v3) { if (!v3.isEmpty()) v3.remove(v3.size() - 1); } });
            print(si, vi, sb, vb, sr, vr);
            System.out.println();
        }
    }

    private static Result measure(String list, int size, String op, int operations, Runnable action) {
        long start = System.nanoTime();
        for (int i = 0; i < operations; i++) action.run();
        return Result.of(list, size, op, operations, System.nanoTime() - start);
    }

    private static Result concurrent(String list, int size, String op, Runnable action) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);
        for (int i = 0; i < THREADS; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    for (int j = 0; j < OPS_PER_THREAD; j++) action.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }
        long begin = System.nanoTime();
        start.countDown();
        done.await();
        return Result.of(list, size, op, THREADS * OPS_PER_THREAD, System.nanoTime() - begin);
    }

    private static ArrayList<Integer> arrayList(int size) {
        ArrayList<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) list.add(i);
        return list;
    }

    private static ThreadSafeArrayList<Integer> safeList(int size) {
        ThreadSafeArrayList<Integer> list = new ThreadSafeArrayList<>();
        for (int i = 0; i < size; i++) list.add(i);
        return list;
    }

    private static Vector<Integer> vector(int size) {
        Vector<Integer> list = new Vector<>(size);
        for (int i = 0; i < size; i++) list.add(i);
        return list;
    }

    private static void print(Result... results) {
        System.out.printf("%-22s %-10s %-10s %-12s %-14s %-14s%n", "Lista", "Tamanho", "Operacao", "Operacoes", "Tempo(ms)", "Ops/s");
        for (Result r : results) {
            System.out.printf("%-22s %-10d %-10s %-12d %-14.3f %-14.2f%n", r.list, r.size, r.op, r.ops, r.ms, r.opsPerSecond);
        }
    }

    private record Result(String list, int size, String op, int ops, double ms, double opsPerSecond) {
        static Result of(String list, int size, String op, int ops, long nanos) {
            double ms = nanos / 1_000_000.0;
            double seconds = nanos / 1_000_000_000.0;
            return new Result(list, size, op, ops, ms, ops / seconds);
        }
    }
}
