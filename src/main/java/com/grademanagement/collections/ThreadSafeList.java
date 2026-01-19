package com.grademanagement.collections;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// Generic thread-safe list implementation using ReadWriteLock for better performance
public class ThreadSafeList<T> implements List<T> {
    private final List<T> internalList;      // Backing list
    private final ReentrantReadWriteLock lock; // Read/Write lock for thread safety

    public ThreadSafeList() {
        this.internalList = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock(true); // Fair lock
    }

    public ThreadSafeList(int initialCapacity) {
        this.internalList = new ArrayList<>(initialCapacity);
        this.lock = new ReentrantReadWriteLock(true);
    }

    @Override
    public boolean add(T element) {
        lock.writeLock().lock();      // Acquire write lock for modification
        try {
            return internalList.add(element);
        } finally {
            lock.writeLock().unlock(); // Always release lock in finally block
        }
    }

    @Override
    public void add(int index, T element) {
        lock.writeLock().lock();
        try {
            internalList.add(index, element);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public T get(int index) {
        lock.readLock().lock();       // Acquire read lock for reading
        try {
            if (index < 0 || index >= internalList.size()) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
            }
            return internalList.get(index);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public T set(int index, T element) {
        lock.writeLock().lock();
        try {
            if (index < 0 || index >= internalList.size()) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
            }
            return internalList.set(index, element);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public T remove(int index) {
        lock.writeLock().lock();
        try {
            if (index < 0 || index >= internalList.size()) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
            }
            return internalList.remove(index);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        lock.writeLock().lock();
        try {
            return internalList.remove(o);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return internalList.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return internalList.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean contains(Object o) {
        lock.readLock().lock();
        try {
            return internalList.contains(o);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Iterator<T> iterator() {
        lock.readLock().lock();
        try {
            // Return a copy iterator to prevent ConcurrentModificationException
            return new ArrayList<>(internalList).iterator();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Object[] toArray() {
        lock.readLock().lock();
        try {
            return internalList.toArray();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public <E> E[] toArray(E[] a) {
        lock.readLock().lock();
        try {
            return internalList.toArray(a);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        lock.readLock().lock();
        try {
            return internalList.containsAll(c);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        lock.writeLock().lock();
        try {
            return internalList.addAll(c);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        lock.writeLock().lock();
        try {
            return internalList.addAll(index, c);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        lock.writeLock().lock();
        try {
            return internalList.removeAll(c);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        lock.writeLock().lock();
        try {
            return internalList.retainAll(c);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            internalList.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int indexOf(Object o) {
        lock.readLock().lock();
        try {
            return internalList.indexOf(o);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        lock.readLock().lock();
        try {
            return internalList.lastIndexOf(o);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public ListIterator<T> listIterator() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(internalList).listIterator();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        lock.readLock().lock();
        try {
            return new ArrayList<>(internalList).listIterator(index);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        lock.readLock().lock();
        try {
            return new ArrayList<>(internalList.subList(fromIndex, toIndex));
        } finally {
            lock.readLock().unlock();
        }
    }

    // Batch operation with single lock acquisition
    public boolean addAllBatch(Collection<? extends T> collection) {
        lock.writeLock().lock();
        try {
            return internalList.addAll(collection);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Stream processing with thread safety
    public List<T> filter(Predicate<T> predicate) {
        lock.readLock().lock();
        try {
            return internalList.stream()
                    .filter(predicate)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    // Map operation with thread safety
    public <R> List<R> map(java.util.function.Function<T, R> mapper) {
        lock.readLock().lock();
        try {
            return internalList.stream()
                    .map(mapper)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    // Sort the list
    public void sort(Comparator<? super T> comparator) {
        lock.writeLock().lock();
        try {
            internalList.sort(comparator);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Get lock statistics (for debugging)
    public Map<String, Object> getLockStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("readLockCount", lock.getReadLockCount());
        stats.put("writeLockCount", lock.getWriteHoldCount());
        stats.put("queueLength", lock.getQueueLength());
        stats.put("isFair", lock.isFair());
        return stats;
    }

    // Perform operation with read lock
    public <R> R performWithReadLock(java.util.function.Function<List<T>, R> operation) {
        lock.readLock().lock();
        try {
            return operation.apply(new ArrayList<>(internalList));
        } finally {
            lock.readLock().unlock();
        }
    }

    // Perform operation with write lock
    public void performWithWriteLock(java.util.function.Consumer<List<T>> operation) {
        lock.writeLock().lock();
        try {
            operation.accept(internalList);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
