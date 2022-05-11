package thito.clientarmorstand.property;

public interface Observer<V> {
    default boolean shouldRemove() {
        return false;
    }
    void onChange(Observable<V> observable, V oldValue, V newValue);
}