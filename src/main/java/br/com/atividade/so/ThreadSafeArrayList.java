package br.com.atividade.so;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Exercício 2 - ArrayList thread safe.
 *
 * Leituras compartilham o lock de leitura, pois não geram condição de corrida entre si.
 * Inserções e remoções usam lock de escrita, pois modificam a estrutura interna da lista.
 */
public class ThreadSafeArrayList<E> {
    private final List<E> list;
    private final ReadWriteLock lock;

    public ThreadSafeArrayList() {
        this.list = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock(true);
    }

    public void add(E element) {
        lock.writeLock().lock();
        try {
            list.add(element);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void add(int index, E element) {
        lock.writeLock().lock();
        try {
            list.add(index, element);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public E get(int index) {
        lock.readLock().lock();
        try {
            return list.get(index);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean contains(E element) {
        lock.readLock().lock();
        try {
            return list.contains(element);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int indexOf(E element) {
        lock.readLock().lock();
        try {
            return list.indexOf(element);
        } finally {
            lock.readLock().unlock();
        }
    }

    public E removeAt(int index) {
        lock.writeLock().lock();
        try {
            return list.remove(index);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean remove(E element) {
        lock.writeLock().lock();
        try {
            return list.remove(element);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return list.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public List<E> snapshot() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(list);
        } finally {
            lock.readLock().unlock();
        }
    }
}
