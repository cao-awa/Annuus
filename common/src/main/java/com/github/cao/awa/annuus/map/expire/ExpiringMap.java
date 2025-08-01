package com.github.cao.awa.annuus.map.expire;

import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.github.cao.awa.annuus.map.expire.internal.Assert;
import com.github.cao.awa.annuus.map.expire.internal.NamedThreadFactory;
import org.jetbrains.annotations.NotNull;

/**
 * A thread-safe map that expires entries. Optional features include expiration policies, variable entry expiration,
 * lazy entry loading, and expiration listeners.
 *
 * <p>
 * Entries are tracked by expiration time and expired by a single thread.
 *
 * <p>
 * Expiration listeners are called synchronously as entries are expired and block write operations to the map until they
 * completed. Asynchronous expiration listeners are called on a separate thread pool and do not block map operations.
 *
 * <p>
 * When variable expiration is disabled (default), put/remove operations have a time complexity <i>O(1)</i>. When
 * variable expiration is enabled, put/remove operations have time complexity of <i>O(log n)</i>.
 *
 * <p>
 * Example usages:
 *
 * <pre>
 * {@code
 * Map<String, Integer> map = ExpiringMap.create();
 * Map<String, Integer> map = ExpiringMap.builder().expiration(30, TimeUnit.SECONDS).build();
 * Map<String, Connection> map = ExpiringMap.builder()
 *   .expiration(10, TimeUnit.MINUTES)
 *   .entryLoader(new EntryLoader<String, Connection>() {
 *     public Connection load(String address) {
 *       return new Connection(address);
 *     }
 *   })
 *   .expirationListener(new ExpirationListener<String, Connection>() {
 *     public void expired(String key, Connection connection) {
 *       connection.close();
 *     }
 *   })
 *   .build();
 * }
 * </pre>
 *
 * @param <K> Key type
 * @param <V> Value type
 * @author Jonathan Halterman
 */
public class ExpiringMap<K, V> implements ConcurrentMap<K, V> {
    static volatile ScheduledExecutorService EXPIRER;
    static volatile ThreadPoolExecutor LISTENER_SERVICE;
    static ThreadFactory THREAD_FACTORY;

    List<ExpirationListener<K, V>> expirationListeners;
    List<ExpirationListener<K, V>> asyncExpirationListeners;
    private volatile long expirationNanos;
    private int maxSize;
    private final AtomicReference<ExpirationPolicy> expirationPolicy;
    private final EntryLoader<? super K, ? extends V> entryLoader;
    private final ExpiringEntryLoader<? super K, ? extends V> expiringEntryLoader;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    /**
     * Guarded by "readWriteLock"
     */
    private final EntryMap<K, V> entries;
    private volatile boolean variableExpiration;

    /**
     * Sets the {@link ThreadFactory} that is used to create expiration and listener callback threads for all ExpiringMap
     * instances.
     *
     * @param threadFactory
     * @throws NullPointerException if {@code threadFactory} is null
     */
    public static void setThreadFactory(ThreadFactory threadFactory) {
        THREAD_FACTORY = Assert.notNull(threadFactory, "threadFactory");
    }

    /**
     * Creates a new instance of ExpiringMap.
     *
     * @param builder The map builder
     */
    private ExpiringMap(final Builder<K, V> builder) {
        if (EXPIRER == null) {
            synchronized (ExpiringMap.class) {
                if (EXPIRER == null) {
                    EXPIRER = Executors.newSingleThreadScheduledExecutor(
                            THREAD_FACTORY == null ? new NamedThreadFactory("ExpiringMap-Expirer") : THREAD_FACTORY);
                }
            }
        }

        if (LISTENER_SERVICE == null && builder.asyncExpirationListeners != null)
            initListenerService();

        variableExpiration = builder.variableExpiration;
        entries = variableExpiration ? new EntryTreeHashMap<>() : new EntryLinkedHashMap<K, V>();
        if (builder.expirationListeners != null)
            expirationListeners = new CopyOnWriteArrayList<>(builder.expirationListeners);
        if (builder.asyncExpirationListeners != null)
            asyncExpirationListeners = new CopyOnWriteArrayList<>(builder.asyncExpirationListeners);
        expirationPolicy = new AtomicReference<>(builder.expirationPolicy);
        expirationNanos = TimeUnit.NANOSECONDS.convert(builder.duration, builder.timeUnit);
        maxSize = builder.maxSize;
        entryLoader = builder.entryLoader;
        expiringEntryLoader = builder.expiringEntryLoader;
    }

    /**
     * Builds ExpiringMap instances. Defaults to ExpirationPolicy.CREATED, expiration of 60 TimeUnit.SECONDS and
     * a maxSize of Integer.MAX_VALUE.
     */
    public static final class Builder<K, V> {
        private ExpirationPolicy expirationPolicy = ExpirationPolicy.CREATED;
        private List<ExpirationListener<K, V>> expirationListeners;
        private List<ExpirationListener<K, V>> asyncExpirationListeners;
        private TimeUnit timeUnit = TimeUnit.SECONDS;
        private boolean variableExpiration;
        private long duration = 60;
        private int maxSize = Integer.MAX_VALUE;
        private EntryLoader<K, V> entryLoader;
        private ExpiringEntryLoader<K, V> expiringEntryLoader;

        /**
         * Creates a new Builder object.
         */
        private Builder() {
        }

        /**
         * Builds and returns an expiring map.
         *
         * @param <K1> Key type
         * @param <V1> Value type
         */
        @SuppressWarnings("unchecked")
        public <K1 extends K, V1 extends V> ExpiringMap<K1, V1> build() {
            return new ExpiringMap<K1, V1>((Builder<K1, V1>) this);
        }

        /**
         * Sets the default map entry expiration.
         *
         * @param duration the length of time after an entry is created that it should be removed
         * @param timeUnit the unit that {@code duration} is expressed in
         * @throws NullPointerException if {@code timeUnit} is null
         */
        public Builder<K, V> expiration(long duration, TimeUnit timeUnit) {
            this.duration = duration;
            this.timeUnit = Assert.notNull(timeUnit, "timeUnit");
            return this;
        }

        /**
         * Sets the maximum size of the map. Once this size has been reached, adding an additional entry will expire the
         * first entry in line for expiration based on the expiration policy.
         *
         * @param maxSize The maximum size of the map.
         */
        public Builder<K, V> maxSize(int maxSize) {
            Assert.operation(maxSize > 0, "maxSize");
            this.maxSize = maxSize;
            return this;
        }

        /**
         * Sets the EntryLoader to use when loading entries. Either an EntryLoader or ExpiringEntryLoader may be set, not
         * both.
         *
         * @param loader to set
         * @throws NullPointerException  if {@code loader} is null
         * @throws IllegalStateException if an {@link #expiringEntryLoader(ExpiringEntryLoader) ExpiringEntryLoader} is set
         */
        @SuppressWarnings("unchecked")
        public <K1 extends K, V1 extends V> Builder<K1, V1> entryLoader(EntryLoader<? super K1, ? super V1> loader) {
            assertNoLoaderSet();
            entryLoader = (EntryLoader<K, V>) Assert.notNull(loader, "loader");
            return (Builder<K1, V1>) this;
        }

        /**
         * Sets the ExpiringEntryLoader to use when loading entries and configures {@link #enableExpiration() variable
         * expiration}. Either an EntryLoader or ExpiringEntryLoader may be set, not both.
         *
         * @param loader to set
         * @throws NullPointerException  if {@code loader} is null
         * @throws IllegalStateException if an {@link #entryLoader(EntryLoader) EntryLoader} is set
         */
        @SuppressWarnings("unchecked")
        public <K1 extends K, V1 extends V> Builder<K1, V1> expiringEntryLoader(
                ExpiringEntryLoader<? super K1, ? super V1> loader) {
            assertNoLoaderSet();
            expiringEntryLoader = (ExpiringEntryLoader<K, V>) Assert.notNull(loader, "loader");
            enableExpiration();
            return (Builder<K1, V1>) this;
        }

        /**
         * Configures the expiration listener that will receive notifications upon each map entry's expiration.
         * Notifications are delivered synchronously and block map write operations.
         *
         * @param listener to set
         * @throws NullPointerException if {@code listener} is null
         */
        @SuppressWarnings("unchecked")
        public <K1 extends K, V1 extends V> Builder<K1, V1> expirationListener(
                ExpirationListener<? super K1, ? super V1> listener) {
            Assert.notNull(listener, "listener");
            if (expirationListeners == null)
                expirationListeners = new ArrayList<>();
            expirationListeners.add((ExpirationListener<K, V>) listener);
            return (Builder<K1, V1>) this;
        }

        /**
         * Configures the expiration listeners which will receive notifications upon each map entry's expiration.
         * Notifications are delivered synchronously and block map write operations.
         *
         * @param listeners to set
         * @throws NullPointerException if {@code listener} is null
         */
        @SuppressWarnings("unchecked")
        public <K1 extends K, V1 extends V> Builder<K1, V1> expirationListeners(
                List<ExpirationListener<? super K1, ? super V1>> listeners) {
            Assert.notNull(listeners, "listeners");
            if (expirationListeners == null)
                expirationListeners = new ArrayList<ExpirationListener<K, V>>(listeners.size());
            for (ExpirationListener<? super K1, ? super V1> listener : listeners)
                expirationListeners.add((ExpirationListener<K, V>) listener);
            return (Builder<K1, V1>) this;
        }

        /**
         * Configures the expiration listener which will receive asynchronous notifications upon each map entry's
         * expiration.
         *
         * @param listener to set
         * @throws NullPointerException if {@code listener} is null
         */
        @SuppressWarnings("unchecked")
        public <K1 extends K, V1 extends V> Builder<K1, V1> asyncExpirationListener(
                ExpirationListener<? super K1, ? super V1> listener) {
            Assert.notNull(listener, "listener");
            if (asyncExpirationListeners == null)
                asyncExpirationListeners = new ArrayList<>();
            asyncExpirationListeners.add((ExpirationListener<K, V>) listener);
            return (Builder<K1, V1>) this;
        }

        /**
         * Configures the expiration listeners which will receive asynchronous notifications upon each map entry's
         * expiration.
         *
         * @param listeners to set
         * @throws NullPointerException if {@code listener} is null
         */
        @SuppressWarnings("unchecked")
        public <K1 extends K, V1 extends V> Builder<K1, V1> asyncExpirationListeners(
                List<ExpirationListener<? super K1, ? super V1>> listeners) {
            Assert.notNull(listeners, "listeners");
            if (asyncExpirationListeners == null)
                asyncExpirationListeners = new ArrayList<>(listeners.size());
            for (ExpirationListener<? super K1, ? super V1> listener : listeners)
                asyncExpirationListeners.add((ExpirationListener<K, V>) listener);
            return (Builder<K1, V1>) this;
        }

        /**
         * Configures the map entry expiration policy.
         *
         * @param expirationPolicy
         * @throws NullPointerException if {@code expirationPolicy} is null
         */
        public Builder<K, V> expirationPolicy(ExpirationPolicy expirationPolicy) {
            this.expirationPolicy = Assert.notNull(expirationPolicy, "expirationPolicy");
            return this;
        }

        /**
         * Allows for map entries to have individual expirations and for expirations to be changed.
         */
        public Builder<K, V> enableExpiration() {
            this.variableExpiration = true;
            return this;
        }

        public Builder<K, V> disableExpiration() {
            this.variableExpiration = true;
            return this;
        }

        public Builder<K, V> setExpiration(boolean expiration) {
            this.variableExpiration = expiration;
            return this;
        }

        public Builder<K, V> switchExpiration() {
            this.variableExpiration = !this.variableExpiration;
            return this;
        }

        private void assertNoLoaderSet() {
            Assert.state(entryLoader == null && expiringEntryLoader == null,
                    "Either entryLoader or expiringEntryLoader may be set, not both");
        }
    }

    /**
     * Entry map definition.
     */
    private interface EntryMap<K, V> extends Map<K, ExpiringEntry<K, V>> {
        /**
         * Returns the first entry in the map or null if the map is empty.
         */
        ExpiringEntry<K, V> first();

        /**
         * Reorders the given entry in the map.
         *
         * @param entry to reorder
         */
        void reorder(ExpiringEntry<K, V> entry);

        /**
         * Returns a values iterator.
         */
        Iterator<ExpiringEntry<K, V>> valuesIterator();
    }

    /**
     * Entry LinkedHashMap implementation.
     */
    private static class EntryLinkedHashMap<K, V> extends LinkedHashMap<K, ExpiringEntry<K, V>>
            implements EntryMap<K, V> {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean containsValue(Object value) {
            for (ExpiringEntry<K, V> entry : values()) {
                V v = entry.value;
                if (v == value || (value != null && value.equals(v)))
                    return true;
            }
            return false;
        }

        @Override
        public ExpiringEntry<K, V> first() {
            return isEmpty() ? null : values().iterator().next();
        }

        @Override
        public void reorder(ExpiringEntry<K, V> value) {
            remove(value.key);
            value.resetExpiration();
            put(value.key, value);
        }

        @Override
        public Iterator<ExpiringEntry<K, V>> valuesIterator() {
            return values().iterator();
        }

        abstract class AbstractHashIterator {
            private final Iterator<Map.Entry<K, ExpiringEntry<K, V>>> iterator;
            private ExpiringEntry<K, V> next;

            @SuppressWarnings({"unchecked", "rawtypes"})
            AbstractHashIterator() {
                iterator = (Iterator) Arrays.asList(entrySet().toArray(new Map.Entry[0])).iterator();
            }

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public ExpiringEntry<K, V> getNext() {
                next = iterator.next().getValue();
                return next;
            }

            public void remove() {
                iterator.remove();
            }
        }

        final class KeyIterator extends AbstractHashIterator implements Iterator<K> {
            public K next() {
                return getNext().key;
            }
        }

        final class ValueIterator extends AbstractHashIterator implements Iterator<V> {
            public V next() {
                return getNext().value;
            }
        }

        public final class EntryIterator extends AbstractHashIterator implements Iterator<Map.Entry<K, V>> {
            public Map.Entry<K, V> next() {
                return mapEntryFor(getNext());
            }
        }
    }

    /**
     * Entry TreeHashMap implementation for variable expiration ExpiringMap entries.
     */
    private static class EntryTreeHashMap<K, V> extends HashMap<K, ExpiringEntry<K, V>> implements EntryMap<K, V> {
        private static final long serialVersionUID = 1L;
        SortedSet<ExpiringEntry<K, V>> sortedSet = new ConcurrentSkipListSet<ExpiringEntry<K, V>>();

        @Override
        public void clear() {
            super.clear();
            sortedSet.clear();
        }

        @Override
        public boolean containsValue(Object value) {
            for (ExpiringEntry<K, V> entry : values()) {
                V v = entry.value;
                if (v == value || (value != null && value.equals(v)))
                    return true;
            }
            return false;
        }

        @Override
        public ExpiringEntry<K, V> first() {
            return sortedSet.isEmpty() ? null : sortedSet.first();
        }

        @Override
        public ExpiringEntry<K, V> put(K key, ExpiringEntry<K, V> value) {
            sortedSet.add(value);
            return super.put(key, value);
        }

        @Override
        public ExpiringEntry<K, V> remove(Object key) {
            ExpiringEntry<K, V> entry = super.remove(key);
            if (entry != null)
                sortedSet.remove(entry);
            return entry;
        }

        @Override
        public void reorder(ExpiringEntry<K, V> value) {
            sortedSet.remove(value);
            value.resetExpiration();
            sortedSet.add(value);
        }

        @Override
        public Iterator<ExpiringEntry<K, V>> valuesIterator() {
            return new ExpiringEntryIterator();
        }

        abstract class AbstractHashIterator {
            private final Iterator<ExpiringEntry<K, V>> iterator = sortedSet.iterator();
            protected ExpiringEntry<K, V> next;

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public ExpiringEntry<K, V> getNext() {
                next = iterator.next();
                return next;
            }

            public void remove() {
                EntryTreeHashMap.super.remove(next.key);
                iterator.remove();
            }
        }

        final class ExpiringEntryIterator extends AbstractHashIterator implements Iterator<ExpiringEntry<K, V>> {
            public final ExpiringEntry<K, V> next() {
                return getNext();
            }
        }

        final class KeyIterator extends AbstractHashIterator implements Iterator<K> {
            public final K next() {
                return getNext().key;
            }
        }

        final class ValueIterator extends AbstractHashIterator implements Iterator<V> {
            public final V next() {
                return getNext().value;
            }
        }

        final class EntryIterator extends AbstractHashIterator implements Iterator<Map.Entry<K, V>> {
            public final Map.Entry<K, V> next() {
                return mapEntryFor(getNext());
            }
        }
    }

    /**
     * Expiring map entry implementation.
     */
    static class ExpiringEntry<K, V> implements Comparable<ExpiringEntry<K, V>> {
        private volatile long expirationNanos;
        /**
         * Epoch time at which the entry is expected to expire
         */
        private volatile long expectedExpiration;
        final AtomicReference<ExpirationPolicy> expirationPolicy;
        final K key;
        /**
         * Guarded by "this"
         */
        volatile Future<?> entryFuture;
        /**
         * Guarded by "this"
         */
        V value;
        /**
         * Guarded by "this"
         */
        volatile boolean scheduled;

        /**
         * Creates a new ExpiringEntry object.
         *
         * @param key              for the entry
         * @param value            for the entry
         * @param expirationPolicy for the entry
         * @param expirationNanos  for the entry
         */
        ExpiringEntry(K key, V value, AtomicReference<ExpirationPolicy> expirationPolicy, long expirationNanos) {
            this.key = key;
            this.value = value;
            this.expirationPolicy = expirationPolicy;
            this.expirationNanos = expirationNanos;
            this.expectedExpiration = 0L;
            resetExpiration();
        }

        @Override
        public int compareTo(ExpiringEntry<K, V> other) {
            if (key.equals(other.key))
                return 0;
            return expectedExpiration < other.expectedExpiration ? -1 : 1;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass()) {
                return false;
            }
            ExpiringEntry<?, ?> other = (ExpiringEntry<?, ?>) obj;
            if (!key.equals(other.key))
                return false;
            if (value == null) {
                return other.value == null;
            } else {
                return value.equals(other.value);
            }
        }

        @Override
        public String toString() {
            return value.toString();
        }

        /**
         * Marks the entry as canceled.
         *
         * @return true if the entry was scheduled
         */
        synchronized boolean cancel() {
            boolean result = scheduled;
            if (entryFuture != null)
                entryFuture.cancel(false);

            entryFuture = null;
            scheduled = false;
            return result;
        }

        /**
         * Gets the entry value.
         */
        synchronized V getValue() {
            return value;
        }

        /**
         * Resets the entry's expected expiration.
         */
        void resetExpiration() {
            this.expectedExpiration = this.expirationNanos + System.nanoTime();
        }

        /**
         * Marks the entry as scheduled.
         */
        synchronized void schedule(Future<?> entryFuture) {
            this.entryFuture = entryFuture;
            scheduled = true;
        }

        /**
         * Sets the entry value.
         */
        synchronized void setValue(V value) {
            this.value = value;
        }
    }

    /**
     * Creates an ExpiringMap builder.
     *
     * @return New ExpiringMap builder
     */
    public static Builder<Object, Object> builder() {
        return new Builder<>();
    }

    /**
     * Creates a new instance of ExpiringMap with ExpirationPolicy.CREATED and an expiration of 60 seconds.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ExpiringMap<K, V> create() {
        return new ExpiringMap<>((Builder<K, V>) ExpiringMap.builder());
    }

    /**
     * Creates a new instance of ExpiringMap with ExpirationPolicy.CREATED and an expiration of 60 seconds.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ExpiringMap<K, V> createFrom(Class<K> typeKey, Class<V> typeValue) {
        return new ExpiringMap<>((Builder<K, V>) ExpiringMap.builder());
    }

    /**
     * Adds an expiration listener.
     *
     * @param listener to add
     * @throws NullPointerException if {@code listener} is null
     */
    public synchronized ExpiringMap<K, V> whenExpiration(ExpirationListener<K, V> listener) {
        Assert.notNull(listener, "listener");
        if (this.expirationListeners == null) {
            this.expirationListeners = new CopyOnWriteArrayList<>();
        }
        this.expirationListeners.add(listener);

        return this;
    }

    /**
     * Adds an asynchronous expiration listener.
     *
     * @param listener to add
     * @throws NullPointerException if {@code listener} is null
     */
    public synchronized ExpiringMap<K, V> whenExpirationAsync(ExpirationListener<K, V> listener) {
        Assert.notNull(listener, "listener");
        if (this.asyncExpirationListeners == null) {
            this.asyncExpirationListeners = new CopyOnWriteArrayList<>();
        }
        this.asyncExpirationListeners.add(listener);
        // If asyncListener was not added on Builder, LISTENER_SERVICE was not initialized and remain null
        if (LISTENER_SERVICE == null) {
            initListenerService();
        }

        return this;
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            for (ExpiringEntry<K, V> entry : entries.values())
                entry.cancel();
            entries.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        readLock.lock();
        try {
            return entries.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        readLock.lock();
        try {
            return entries.containsValue(value);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns a copy of the map's entries, which can be iterated over safely by multiple threads.
     *
     * @return Copied set of map entries.
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<Map.Entry<K, V>>() {
            @Override
            public void clear() {
                ExpiringMap.this.clear();
            }

            @Override
            public boolean contains(Object entry) {
                if (!(entry instanceof Map.Entry))
                    return false;
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) entry;
                return containsKey(e.getKey());
            }

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                readLock.lock();
                try {
                    return (entries instanceof EntryLinkedHashMap) ? ((EntryLinkedHashMap<K, V>) entries).new EntryIterator()
                            : ((EntryTreeHashMap<K, V>) entries).new EntryIterator();
                } finally {
                    readLock.unlock();
                }
            }

            @Override
            public boolean remove(Object entry) {
                if (entry instanceof Map.Entry) {
                    Map.Entry<?, ?> e = (Map.Entry<?, ?>) entry;
                    return ExpiringMap.this.remove(e.getKey()) != null;
                }
                return false;
            }

            @Override
            public int size() {
                return ExpiringMap.this.size();
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        readLock.lock();
        try {
            return entries.equals(obj);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        ExpiringEntry<K, V> entry = getEntry(key);

        if (entry == null) {
            return load((K) key);
        } else if (ExpirationPolicy.ACCESSED.equals(entry.expirationPolicy.get()))
            resetEntry(entry, false);

        return entry.getValue();
    }

    private V load(K key) {
        if (entryLoader == null && expiringEntryLoader == null)
            return null;

        writeLock.lock();
        try {
            // Double check for entry
            ExpiringEntry<K, V> entry = getEntry(key);
            if (entry != null)
                return entry.getValue();

            if (entryLoader != null) {
                V value = entryLoader.load(key);
                put(key, value);
                return value;
            } else {
                ExpiringValue<? extends V> expiringValue = expiringEntryLoader.load(key);
                if (expiringValue == null) {
                    put(key, null);
                    return null;
                } else {
                    long duration = expiringValue.getTimeUnit() == null ? expirationNanos : expiringValue.getDuration();
                    TimeUnit timeUnit = expiringValue.getTimeUnit() == null ? TimeUnit.NANOSECONDS : expiringValue.getTimeUnit();
                    put(key, expiringValue.getValue(), expiringValue.getExpirationPolicy() == null ? expirationPolicy.get()
                            : expiringValue.getExpirationPolicy(), duration, timeUnit);
                    return expiringValue.getValue();
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns the map's default expiration duration in milliseconds.
     *
     * @return The expiration duration (milliseconds)
     */
    public long getExpiration() {
        return TimeUnit.NANOSECONDS.toMillis(expirationNanos);
    }

    /**
     * Gets the expiration duration in milliseconds for the entry corresponding to the given key.
     *
     * @param key
     * @return The expiration duration in milliseconds
     * @throws NullPointerException   if {@code key} is null
     * @throws NoSuchElementException If no entry exists for the given key
     */
    public long getExpiration(K key) {
        Assert.notNull(key, "key");
        ExpiringEntry<K, V> entry = getEntry(key);
        Assert.element(entry, key);
        return TimeUnit.NANOSECONDS.toMillis(entry.expirationNanos);
    }

    /**
     * Gets the ExpirationPolicy for the entry corresponding to the given {@code key}.
     *
     * @param key
     * @return The ExpirationPolicy for the {@code key}
     * @throws NullPointerException   if {@code key} is null
     * @throws NoSuchElementException If no entry exists for the given key
     */
    public ExpirationPolicy getExpirationPolicy(K key) {
        Assert.notNull(key, "key");
        ExpiringEntry<K, V> entry = getEntry(key);
        Assert.element(entry, key);
        return entry.expirationPolicy.get();
    }

    /**
     * Gets the expected expiration, in milliseconds from the current time, for the entry corresponding to the given
     * {@code key}.
     *
     * @param key
     * @return The expiration duration in milliseconds
     * @throws NullPointerException   if {@code key} is null
     * @throws NoSuchElementException If no entry exists for the given key
     */
    public long getExpectedExpiration(K key) {
        Assert.notNull(key, "key");
        ExpiringEntry<K, V> entry = getEntry(key);
        Assert.element(entry, key);
        return TimeUnit.NANOSECONDS.toMillis(entry.expectedExpiration - System.nanoTime());
    }

    /**
     * Gets the maximum size of the map. Once this size has been reached, adding an additional entry will expire the
     * first entry in line for expiration based on the expiration policy.
     *
     * @return The maximum size of the map.
     */
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public int hashCode() {
        readLock.lock();
        try {
            return entries.hashCode();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        try {
            return entries.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns a copy of the map's keys, which can be iterated over safely by multiple threads.
     *
     * @return Copied set of map keys.
     */
    @Override
    public Set<K> keySet() {
        return new AbstractSet<>() {
            @Override
            public void clear() {
                ExpiringMap.this.clear();
            }

            @Override
            public boolean contains(Object key) {
                return containsKey(key);
            }

            @Override
            public Iterator<K> iterator() {
                readLock.lock();
                try {
                    return (entries instanceof EntryLinkedHashMap) ? ((EntryLinkedHashMap<K, V>) entries).new KeyIterator()
                            : ((EntryTreeHashMap<K, V>) entries).new KeyIterator();
                } finally {
                    readLock.unlock();
                }
            }

            @Override
            public boolean remove(Object value) {
                return ExpiringMap.this.remove(value) != null;
            }

            @Override
            public int size() {
                return ExpiringMap.this.size();
            }
        };
    }

    /**
     * Puts {@code value} in the map for {@code key}. Resets the entry's expiration unless an entry already exists for the
     * same {@code key} and {@code value}.
     *
     * @param key   to put value for
     * @param value to put for key
     * @return the old value
     * @throws NullPointerException if {@code key} is null
     */
    @Override
    public V put(K key, V value) {
        Assert.notNull(key, "key");
        return putInternal(key, value, expirationPolicy.get(), expirationNanos);
    }

    /**
     * @see #put(Object, Object, ExpirationPolicy, long, TimeUnit)
     */
    public V put(K key, V value, ExpirationPolicy expirationPolicy) {
        return put(key, value, expirationPolicy, expirationNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * @see #put(Object, Object, ExpirationPolicy, long, TimeUnit)
     */
    public V put(K key, V value, long duration, TimeUnit timeUnit) {
        return put(key, value, expirationPolicy.get(), duration, timeUnit);
    }

    /**
     * Puts {@code value} in the map for {@code key}. Resets the entry's expiration unless an entry already exists for the
     * same {@code key} and {@code value}. Requires that variable expiration be enabled.
     *
     * @param key      Key to put value for
     * @param value    Value to put for key
     * @param duration the length of time after an entry is created that it should be removed
     * @param timeUnit the unit that {@code duration} is expressed in
     * @return the old value
     * @throws UnsupportedOperationException If variable expiration is not enabled
     * @throws NullPointerException          if {@code key}, {@code expirationPolicy} or {@code timeUnit} are null
     */
    public V put(K key, V value, ExpirationPolicy expirationPolicy, long duration, TimeUnit timeUnit) {
        Assert.notNull(key, "key");
        Assert.notNull(expirationPolicy, "expirationPolicy");
        Assert.notNull(timeUnit, "timeUnit");
        Assert.operation(variableExpiration, "Variable expiration is not enabled");
        return putInternal(key, value, expirationPolicy, TimeUnit.NANOSECONDS.convert(duration, timeUnit));
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        Assert.notNull(map, "map");
        long expiration = expirationNanos;
        ExpirationPolicy expirationPolicy = this.expirationPolicy.get();
        writeLock.lock();
        try {
            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet())
                putInternal(entry.getKey(), entry.getValue(), expirationPolicy, expiration);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        Assert.notNull(key, "key");
        writeLock.lock();
        try {
            if (!entries.containsKey(key))
                return putInternal(key, value, expirationPolicy.get(), expirationNanos);
            else
                return entries.get(key).getValue();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        Assert.notNull(key, "key");
        writeLock.lock();
        try {
            ExpiringEntry<K, V> entry = entries.remove(key);
            if (entry == null)
                return null;
            if (entry.cancel())
                scheduleEntry(entries.first());
            return entry.getValue();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        Assert.notNull(key, "key");
        writeLock.lock();
        try {
            ExpiringEntry<K, V> entry = entries.get(key);
            if (entry != null && entry.getValue().equals(value)) {
                entries.remove(key);
                if (entry.cancel())
                    scheduleEntry(entries.first());
                return true;
            } else
                return false;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V replace(@NotNull K key, @NotNull V value) {
        Assert.notNull(key, "key");
        writeLock.lock();
        try {
            if (entries.containsKey(key)) {
                return putInternal(key, value, expirationPolicy.get(), expirationNanos);
            } else
                return null;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
        Assert.notNull(key, "key");
        writeLock.lock();
        try {
            ExpiringEntry<K, V> entry = entries.get(key);
            if (entry != null && entry.getValue().equals(oldValue)) {
                putInternal(key, newValue, expirationPolicy.get(), expirationNanos);
                return true;
            } else
                return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes an expiration listener.
     *
     * @param listener
     * @throws NullPointerException if {@code listener} is null
     */
    public ExpiringMap<K, V> removeExpirationListener(ExpirationListener<K, V> listener) {
        Assert.notNull(listener, "listener");
        for (int i = 0; i < this.expirationListeners.size(); i++) {
            if (this.expirationListeners.get(i).equals(listener)) {
                this.expirationListeners.remove(i);
                return this;
            }
        }

        return this;
    }

    /**
     * Removes an asynchronous expiration listener.
     *
     * @param listener
     * @throws NullPointerException if {@code listener} is null
     */
    public ExpiringMap<K, V> removeAsyncExpirationListener(ExpirationListener<K, V> listener) {
        Assert.notNull(listener, "listener");
        for (int i = 0; i < this.asyncExpirationListeners.size(); i++) {
            if (this.asyncExpirationListeners.get(i).equals(listener)) {
                this.asyncExpirationListeners.remove(i);
                return this;
            }
        }

        return this;
    }

    /**
     * Resets expiration for the entry corresponding to {@code key}.
     *
     * @param key to reset expiration for
     * @throws NullPointerException if {@code key} is null
     */
    public ExpiringMap<K, V> resetExpiration(K key) {
        Assert.notNull(key, "key");
        ExpiringEntry<K, V> entry = getEntry(key);
        if (entry != null) {
            resetEntry(entry, false);
        }
        return this;
    }

    /**
     * Sets the expiration duration for the entry corresponding to the given key. Supported only if variable expiration is
     * enabled.
     *
     * @param key      Key to set expiration for
     * @param duration the length of time after an entry is created that it should be removed
     * @param timeUnit the unit that {@code duration} is expressed in
     * @throws NullPointerException          if {@code key} or {@code timeUnit} are null
     * @throws UnsupportedOperationException If variable expiration is not enabled
     */
    public ExpiringMap<K, V> setExpiration(K key, long duration, TimeUnit timeUnit) {
        Assert.notNull(key, "key");
        Assert.notNull(timeUnit, "timeUnit");
        Assert.operation(this.variableExpiration, "Variable expiration is not enabled");
        this.writeLock.lock();
        try {
            ExpiringEntry<K, V> entry = this.entries.get(key);
            if (entry != null) {
                entry.expirationNanos = TimeUnit.NANOSECONDS.convert(duration, timeUnit);
                resetEntry(entry, true);
            }
        } finally {
            this.writeLock.unlock();
        }
        return this;
    }

    /**
     * Updates the default map entry expiration. Supported only if variable expiration is enabled.
     *
     * @param duration the length of time after an entry is created that it should be removed
     * @param timeUnit the unit that {@code duration} is expressed in
     * @throws NullPointerException          {@code timeUnit} is null
     * @throws UnsupportedOperationException If variable expiration is not enabled
     */
    public ExpiringMap<K, V> setExpiration(long duration, TimeUnit timeUnit) {
        Assert.notNull(timeUnit, "timeUnit");
        Assert.operation(this.variableExpiration, "Variable expiration is not enabled");
        this.expirationNanos = TimeUnit.NANOSECONDS.convert(duration, timeUnit);
        return this;
    }

    /**
     * Sets the global expiration policy for the map. Individual expiration policies may override the global policy.
     *
     * @param expirationPolicy
     * @throws NullPointerException {@code expirationPolicy} is null
     */
    public ExpiringMap<K, V> setExpirationPolicy(ExpirationPolicy expirationPolicy) {
        Assert.notNull(expirationPolicy, "expirationPolicy");
        this.expirationPolicy.set(expirationPolicy);
        return this;
    }

    /**
     * Sets the expiration policy for the entry corresponding to the given key.
     *
     * @param key              to set policy for
     * @param expirationPolicy to set
     * @throws NullPointerException          if {@code key} or {@code expirationPolicy} are null
     * @throws UnsupportedOperationException If variable expiration is not enabled
     */
    public ExpiringMap<K, V> setExpirationPolicy(K key, ExpirationPolicy expirationPolicy) {
        Assert.notNull(key, "key");
        Assert.notNull(expirationPolicy, "expirationPolicy");
        Assert.operation(variableExpiration, "Variable expiration is not enabled");
        ExpiringEntry<K, V> entry = getEntry(key);
        if (entry != null) {
            entry.expirationPolicy.set(expirationPolicy);
        }
        return this;
    }

    public ExpiringMap<K, V> enableExpiration() {
        this.variableExpiration = true;
        return this;
    }

    public ExpiringMap<K, V> disableExpiration() {
        this.variableExpiration = true;
        return this;
    }

    public ExpiringMap<K, V> setExpiration(boolean expiration) {
        this.variableExpiration = expiration;
        return this;
    }

    public ExpiringMap<K, V> switchExpiration() {
        this.variableExpiration = !this.variableExpiration;
        return this;
    }

    /**
     * Sets the maximum size of the map. Once this size has been reached, adding an additional entry will expire the
     * first entry in line for expiration based on the expiration policy.
     *
     * @param maxSize The maximum size of the map.
     */
    public ExpiringMap<K, V> setMaxSize(int maxSize) {
        Assert.operation(maxSize > 0, "maxSize");
        this.maxSize = maxSize;
        return this;
    }

    @Override
    public int size() {
        readLock.lock();
        try {
            return entries.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String toString() {
        readLock.lock();
        try {
            return entries.toString();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns a copy of the map's values, which can be iterated over safely by multiple threads.
     *
     * @return Copied set of map values.
     */
    @Override
    public Collection<V> values() {
        return new AbstractCollection<V>() {
            @Override
            public void clear() {
                ExpiringMap.this.clear();
            }

            @Override
            public boolean contains(Object value) {
                return containsValue(value);
            }

            @Override
            public Iterator<V> iterator() {
                readLock.lock();
                try {
                    return (entries instanceof EntryLinkedHashMap) ? ((EntryLinkedHashMap<K, V>) entries).new ValueIterator()
                            : ((EntryTreeHashMap<K, V>) entries).new ValueIterator();
                } finally {
                    readLock.unlock();
                }
            }

            @Override
            public int size() {
                return ExpiringMap.this.size();
            }
        };
    }

    /**
     * Notifies expiration listeners that the given entry expired. Must not be called from within a locked context.
     *
     * @param entry Entry to expire
     */
    void notifyListeners(final ExpiringEntry<K, V> entry) {
        if (asyncExpirationListeners != null)
            for (final ExpirationListener<K, V> listener : asyncExpirationListeners) {
                LISTENER_SERVICE.execute(() -> {
                    try {
                        listener.onExpired(entry.key, entry.getValue());
                    } catch (Exception ignoreUserExceptions) {
                    }
                });
            }

        if (expirationListeners != null)
            for (final ExpirationListener<K, V> listener : expirationListeners) {
                try {
                    listener.onExpired(entry.key, entry.getValue());
                } catch (Exception ignoreUserExceptions) {
                }
            }
    }

    /**
     * Returns the internal ExpiringEntry for the {@code key}, obtaining a read lock.
     */
    ExpiringEntry<K, V> getEntry(Object key) {
        readLock.lock();
        try {
            return entries.get(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Puts the given key/value in storage, scheduling the new entry for expiration if needed. If a previous value existed
     * for the given key, it is first cancelled and the entries reordered to reflect the new expiration.
     */
    V putInternal(K key, V value, ExpirationPolicy expirationPolicy, long expirationNanos) {
        writeLock.lock();
        try {
            ExpiringEntry<K, V> entry = entries.get(key);
            V oldValue = null;

            if (entry == null) {
                entry = new ExpiringEntry<>(key, value,
                        variableExpiration ? new AtomicReference<>(expirationPolicy) : this.expirationPolicy,
                        variableExpiration ? expirationNanos : this.expirationNanos);
                if (entries.size() >= maxSize) {
                    ExpiringEntry<K, V> expiredEntry = entries.first();
                    entries.remove(expiredEntry.key);
                    notifyListeners(expiredEntry);
                }
                entries.put(key, entry);
                if (entries.size() == 1 || entries.first().equals(entry)) {
                    scheduleEntry(entry);
                }
            } else {
                oldValue = entry.getValue();
                if (!ExpirationPolicy.ACCESSED.equals(expirationPolicy)
                        && ((oldValue == null && value == null) || (oldValue != null && oldValue.equals(value))))
                    return value;

                entry.setValue(value);
                resetEntry(entry, false);
            }

            return oldValue;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Resets the given entry's schedule canceling any existing scheduled expiration and reordering the entry in the
     * internal map. Schedules the next entry in the map if the given {@code entry} was scheduled or if
     * {@code scheduleNext} is true.
     *
     * @param entry              to reset
     * @param scheduleFirstEntry whether the first entry should be automatically scheduled
     */
    void resetEntry(ExpiringEntry<K, V> entry, boolean scheduleFirstEntry) {
        writeLock.lock();
        try {
            boolean scheduled = entry.cancel();
            entries.reorder(entry);

            if (scheduled || scheduleFirstEntry)
                scheduleEntry(entries.first());
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Schedules an entry for expiration. Guards against concurrent schedule/schedule, cancel/schedule and schedule/cancel
     * calls.
     *
     * @param entry Entry to schedule
     */
    void scheduleEntry(final ExpiringEntry<K, V> entry) {
        if (entry == null || entry.scheduled) {
            return;
        }

        Runnable runnable;
        synchronized (entry) {
            if (entry.scheduled)
                return;

            final WeakReference<ExpiringEntry<K, V>> entryReference = new WeakReference<>(entry);
            runnable = () -> {
                ExpiringEntry<K, V> entry1 = entryReference.get();

                writeLock.lock();
                try {
                    if (entry1 != null && entry1.scheduled) {
                        entries.remove(entry1.key);
                        notifyListeners(entry1);
                    }

                    try {
                        // Expires entries and schedules the next entry
                        Iterator<ExpiringEntry<K, V>> iterator = entries.valuesIterator();
                        boolean schedulePending = true;

                        while (iterator.hasNext() && schedulePending) {
                            ExpiringEntry<K, V> nextEntry = iterator.next();
                            if (nextEntry.expectedExpiration <= System.nanoTime()) {
                                iterator.remove();
                                notifyListeners(nextEntry);
                            } else {
                                scheduleEntry(nextEntry);
                                schedulePending = false;
                            }
                        }
                    } catch (NoSuchElementException ignored) {
                    }
                } finally {
                    writeLock.unlock();
                }
            };

            Future<?> entryFuture = EXPIRER.schedule(runnable, entry.expectedExpiration - System.nanoTime(), TimeUnit.NANOSECONDS);
            entry.schedule(entryFuture);
        }
    }

    private static <K, V> Map.Entry<K, V> mapEntryFor(final ExpiringEntry<K, V> entry) {
        return new Map.Entry<K, V>() {
            @Override
            public K getKey() {
                return entry.key;
            }

            @Override
            public V getValue() {
                return entry.value;
            }

            @Override
            public V setValue(V value) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private void initListenerService() {
        synchronized (ExpiringMap.class) {
            if (LISTENER_SERVICE == null) {
                LISTENER_SERVICE = (ThreadPoolExecutor) Executors.newCachedThreadPool(
                        THREAD_FACTORY == null ? new NamedThreadFactory("ExpiringMap-Listener-%s") : THREAD_FACTORY);
            }
        }
    }
}
