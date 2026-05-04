package br.com.atividade.so;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Exercicio 4 - Controle de acesso a um banco de dados.
 *
 * Regras implementadas:
 * - No maximo 10 consultas simultaneas.
 * - Apenas 1 escrita simultanea.
 * - Escrita so ocorre quando nao ha consultas em andamento.
 */
public class DatabaseAccessController {
    private final Map<Integer, String> records = new HashMap<>();
    private final Semaphore readSlots = new Semaphore(10, true);
    private final ReentrantReadWriteLock databaseLock = new ReentrantReadWriteLock(true);
    private final Random random = new Random();

    public void create(int id, String value) {
        databaseLock.writeLock().lock();
        try {
            log("CREATE iniciado para id=%d", id);
            simulateDatabaseWork();
            records.put(id, value);
            log("CREATE finalizado para id=%d", id);
        } finally {
            databaseLock.writeLock().unlock();
        }
    }

    public Optional<String> read(int id) {
        boolean acquiredSlot = false;
        try {
            readSlots.acquire();
            acquiredSlot = true;
            databaseLock.readLock().lock();
            log("READ iniciado para id=%d | consultas simultaneas=%d", id, 10 - readSlots.availablePermits());
            simulateDatabaseWork();
            Optional<String> result = Optional.ofNullable(records.get(id));
            log("READ finalizado para id=%d | resultado=%s", id, result.orElse("nao encontrado"));
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } finally {
            if (databaseLock.getReadHoldCount() > 0) {
                databaseLock.readLock().unlock();
            }
            if (acquiredSlot) {
                readSlots.release();
            }
        }
    }

    public void update(int id, String newValue) {
        databaseLock.writeLock().lock();
        try {
            log("UPDATE iniciado para id=%d", id);
            simulateDatabaseWork();
            records.put(id, newValue);
            log("UPDATE finalizado para id=%d", id);
        } finally {
            databaseLock.writeLock().unlock();
        }
    }

    public void delete(int id) {
        databaseLock.writeLock().lock();
        try {
            log("DELETE iniciado para id=%d", id);
            simulateDatabaseWork();
            records.remove(id);
            log("DELETE finalizado para id=%d", id);
        } finally {
            databaseLock.writeLock().unlock();
        }
    }

    public int size() {
        databaseLock.readLock().lock();
        try {
            return records.size();
        } finally {
            databaseLock.readLock().unlock();
        }
    }

    private void simulateDatabaseWork() {
        try {
            Thread.sleep(400 + random.nextInt(700));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void log(String template, Object... args) {
        String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        System.out.printf("[%s] [%s] %s%n", now, Thread.currentThread().getName(), String.format(template, args));
    }
}
