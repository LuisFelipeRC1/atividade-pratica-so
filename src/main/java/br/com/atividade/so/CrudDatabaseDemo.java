package br.com.atividade.so;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Programa de teste do Exercicio 4.
 */
public class CrudDatabaseDemo {
    public static void main(String[] args) throws InterruptedException {
        DatabaseAccessController database = new DatabaseAccessController();
        for (int i = 1; i <= 20; i++) {
            database.create(i, "Registro " + i);
        }

        List<Thread> threads = new ArrayList<>();
        Random random = new Random();

        for (int i = 1; i <= 25; i++) {
            int id = random.nextInt(20) + 1;
            Thread reader = new Thread(() -> database.read(id), "consulta-" + i);
            threads.add(reader);
        }

        threads.add(new Thread(() -> database.create(21, "Registro 21"), "escrita-create"));
        threads.add(new Thread(() -> database.update(5, "Registro 5 atualizado"), "escrita-update"));
        threads.add(new Thread(() -> database.delete(8), "escrita-delete"));

        for (Thread thread : threads) {
            thread.start();
            Thread.sleep(80);
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("\nTeste finalizado. Quantidade de registros: " + database.size());
    }
}
