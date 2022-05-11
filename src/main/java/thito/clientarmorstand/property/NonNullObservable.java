package thito.clientarmorstand.property;

import org.apache.commons.lang.Validate;

public class NonNullObservable<V> extends Observable<V> {

    public NonNullObservable(V value) {
        super(value);
        Validate.notNull(value);
    }

    @Override
    public void bind(Observable<V> target) {
        if (!(target instanceof NonNullObservable)) throw new IllegalArgumentException("different Observable type");
        super.bind(target);
    }

    @Override
    public void bindBidirectionally(Observable<V> other) {
        if (!(other instanceof NonNullObservable)) throw new IllegalArgumentException("different Observable type");
        super.bindBidirectionally(other);
    }

    @Override
    public V setAndGet(V value) {
        Validate.notNull(value, "value");
        return super.setAndGet(value);
    }

    @Override
    public V getAndSet(V value) {
        Validate.notNull(value, "value");
        return super.getAndSet(value);
    }
}
