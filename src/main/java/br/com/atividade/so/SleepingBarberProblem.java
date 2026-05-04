package br.com.atividade.so;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Exercício 1 - Problema do Barbeiro Dorminhoco.
 *
 * Regras implementadas:
 * - 2 barbeiros.
 * - Até 10 clientes esperando.
 * - Corte demora entre 5 e 15 segundos.
 * - Novo cliente chega entre 4 e 6 segundos.
 */
public class SleepingBarberProblem {
    private static final int NUMBER_OF_BARBERS = 2;
    private static final int WAITING_ROOM_SIZE = 10;
    private static final int SIMULATION_SECONDS = 80;

    private final BlockingQueue<Customer> waitingRoom = new ArrayBlockingQueue<>(WAITING_ROOM_SIZE);
    private final AtomicBoolean shopOpen = new AtomicBoolean(true);
    private final AtomicInteger customerSequence = new AtomicInteger(1);
    private final Random random = new Random();

    public static void main(String[] args) throws InterruptedException {
        new SleepingBarberProblem().start();
    }

    private void start() throws InterruptedException {
        System.out.println("Iniciando barbearia com 2 barbeiros e 10 cadeiras de espera.\n");

        for (int i = 1; i <= NUMBER_OF_BARBERS; i++) {
            Thread barber = new Thread(new Barber(i), "Barbeiro-" + i);
            barber.start();
        }

        Thread customerGenerator = new Thread(this::generateCustomers, "GeradorDeClientes");
        customerGenerator.start();

        TimeUnit.SECONDS.sleep(SIMULATION_SECONDS);
        shopOpen.set(false);
        customerGenerator.interrupt();

        System.out.println("\nEncerrando chegada de novos clientes. Clientes restantes ainda serão atendidos.");
    }

    private void generateCustomers() {
        while (shopOpen.get()) {
            Customer customer = new Customer(customerSequence.getAndIncrement());
            boolean entered = waitingRoom.offer(customer);

            if (entered) {
                log("Cliente %02d entrou na fila. Esperando agora: %d", customer.id(), waitingRoom.size());
            } else {
                log("Cliente %02d foi embora: fila cheia (%d clientes esperando).", customer.id(), WAITING_ROOM_SIZE);
            }

            sleepSeconds(randomBetween(4, 6));
        }
    }

    private class Barber implements Runnable {
        private final int id;

        private Barber(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (shopOpen.get() || !waitingRoom.isEmpty()) {
                try {
                    Customer customer = waitingRoom.poll(1, TimeUnit.SECONDS);
                    if (customer == null) {
                        log("Barbeiro %d está dormindo: nenhum cliente na fila.", id);
                        continue;
                    }

                    int haircutSeconds = randomBetween(5, 15);
                    log("Barbeiro %d iniciou o corte do Cliente %02d. Duração prevista: %ds.", id, customer.id(), haircutSeconds);
                    sleepSeconds(haircutSeconds);
                    log("Barbeiro %d finalizou o corte do Cliente %02d.", id, customer.id());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            log("Barbeiro %d encerrou o expediente.", id);
        }
    }

    private int randomBetween(int minInclusive, int maxInclusive) {
        return random.nextInt(maxInclusive - minInclusive + 1) + minInclusive;
    }

    private void sleepSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void log(String template, Object... args) {
        String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.printf("[%s] %s%n", now, String.format(template, args));
    }

    private record Customer(int id) { }
}
