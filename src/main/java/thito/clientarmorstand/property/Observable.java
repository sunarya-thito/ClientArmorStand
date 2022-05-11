package thito.clientarmorstand.property;

import org.apache.commons.lang.Validate;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Observable<V> {
    private static final Function directFunction = x -> x;
    private V value;
    private final List<Observer<V>> observers = new ArrayList<>();

    public Observable() {
    }

    public Observable(V value) {
        this.value = value;
    }

    public void bindBidirectionally(Observable<V> other) {
        bindBidirectionally(other, directFunction, directFunction);
    }

    public <K> void bindBidirectionally(Observable<K> other, Function<V, K> mapperA, Function<K, V> mapperB) {
        Validate.notNull(other);
        if (isBound()) throw new IllegalStateException("already bound");
        if (getClass() != other.getClass()) throw new IllegalArgumentException("different Observable type");
        set(mapperB.apply(other.get()));
        BiBoundObserver<V, K> observer = new BiBoundObserver<>(this, other, mapperB, mapperA);
        removeListener(observer);
        other.removeListener(observer);
        addListener(observer);
        other.addListener(observer);
    }

    public void unbindBidirectionally(Observable<V> other) {
        Validate.notNull(other);
        BiBoundObserver<V, V> observer = new BiBoundObserver<>(this, other, directFunction, directFunction);
        removeListener(observer);
        other.removeListener(observer);
    }

    public boolean isBound() {
        for (Observer<V> observer : observers) {
            if (observer instanceof BoundObserver) {
                return true;
            }
        }
        return false;
    }

    protected boolean isBiBoundOrBound() {
        for (Observer<V> observer : observers) {
            if (observer instanceof BoundObserver || observer instanceof BiBoundObserver) {
                return true;
            }
        }
        return false;
    }

    public void unbind() {
        for (int i = observers.size() - 1; i >= 0; i--) {
            Observer<V> next = observers.get(i);
            if (next instanceof BoundObserver) {
                observers.remove(i);
                break;
            }
        }
    }

    public void replace(UnaryOperator<V> replacer) {
        set(replacer.apply(get()));
    }

    public void bind(Observable<V> target) {
        bind(target, directFunction);
    }

    public <K> void bind(Observable<K> target, Function<K, V> mapper) {
        Validate.notNull(target);
        if (isBiBoundOrBound()) throw new IllegalStateException("already bound");
        set(mapper.apply(target.get()));
        BoundObserver<V, K> observer = new BoundObserver<>(this, mapper);
        target.removeListener(observer);
        target.addListener(observer);
    }

    public Observable<V> set(V value) {
        if (isBound()) throw new IllegalStateException("value bound");
        V oldValue = this.value;
        this.value = value;
        broadcastChange(oldValue, value);
        return this;
    }

    public V setAndGet(V value) {
        if (isBound()) throw new IllegalStateException("value bound");
        V oldValue = this.value;
        this.value = value;
        broadcastChange(oldValue, value);
        return value;
    }

    public V getAndSet(V value) {
        if (isBound()) throw new IllegalStateException("value bound");
        V oldValue = this.value;
        this.value = value;
        broadcastChange(oldValue, value);
        return oldValue;
    }

    public void addListener(Observer<V> observer) {
        observers.add(observer);
    }

    public void removeListener(Observer<?> observer) {
        observers.remove(observer);
    }

    public void clearListeners() {
        observers.clear();
    }

    public Optional<V> setAndGetOptionally(V value) {
        return Optional.ofNullable(setAndGet(value));
    }

    public Optional<V> getOptionallyAndSet(V value) {
        return Optional.ofNullable(getAndSet(value));
    }

    public Optional<V> getOptionally() {
        return Optional.ofNullable(value);
    }

    public V get() {
        return value;
    }

    private void broadcastChange(V oldValue, V newValue) {
        for (int i = observers.size() - 1; i >= 0; i--) {
            Observer<V> next = observers.get(i);
            if (next.shouldRemove()) {
                observers.remove(i);
                continue;
            }
            try {
                next.onChange(this, oldValue, newValue);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static class BiBoundObserver<K, V> implements Observer {
        private final WeakReference<Observable<K>> a;
        private final WeakReference<Observable<V>> b;

        private final Function<V, K> mapperA;
        private final Function<K, V> mapperB;

        private boolean update;

        public BiBoundObserver(Observable<K> a, Observable<V> b, Function<V, K> mapperA, Function<K, V> mapperB) {
            this.a = new WeakReference<>(a);
            this.b = new WeakReference<>(b);
            this.mapperA = mapperA;
            this.mapperB = mapperB;
        }

        @Override
        public boolean shouldRemove() {
            return a.get() == null || b.get() == null;
        }

        @Override
        public void onChange(Observable observable, Object oldValue, Object newValue) {
            if (update) return;
            update = true;
            if (observable == a.get()) {
                Observable<V> target = b.get();
                if (target == null) {
                    observable.removeListener(this);
                } else {
                    target.set(mapperB.apply((K) newValue));
                }
            } else if (observable == b.get()) {
                Observable<K> target = a.get();
                if (target == null) {
                    observable.removeListener(this);
                } else {
                    target.set(mapperA.apply((V) newValue));
                }
            }
            update = false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BiBoundObserver)) return false;
            BiBoundObserver<?,?> that = (BiBoundObserver<?, ?>) o;
            Observable<K> obsA = a.get();
            Observable<V> obsB = b.get();
            Observable<?> thatA = that.a.get();
            Observable<?> thatB = that.b.get();
            return (Objects.equals(obsA, thatA) && Objects.equals(obsB, thatB)) ||
                    (Objects.equals(obsA, thatB) && Objects.equals(obsB, thatA));
        }

        @Override
        public int hashCode() {
            return Objects.hash(a.get(), b.get());
        }
    }

    private static class BoundObserver<K, V> implements Observer<V> {
        private final WeakReference<Observable<K>> observableWeakReference;
        private final Function<V, K> mapper;

        public BoundObserver(Observable<K> observable, Function<V, K> mapper) {
            observableWeakReference = new WeakReference<>(observable);
            this.mapper = mapper;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BoundObserver)) return false;
            BoundObserver<?, ?> that = (BoundObserver<?, ?>) o;
            Observable<K> val = observableWeakReference.get();
            return Objects.equals(val, that.observableWeakReference.get());
        }

        @Override
        public int hashCode() {
            return Objects.hash(observableWeakReference.get());
        }

        @Override
        public boolean shouldRemove() {
            return observableWeakReference.get() == null;
        }

        @Override
        public void onChange(Observable<V> observable, V oldValue, V newValue) {
            Observable<K> correspondent = observableWeakReference.get();
            if (correspondent == null) {
                observable.removeListener(this);
                return;
            }
            correspondent.set(mapper.apply(newValue));
        }
    }
}
