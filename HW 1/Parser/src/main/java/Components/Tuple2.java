package Components;

/**
 * Tuple2 is a convenience generic object to store a pair
 * of objects. Comparisons rely solely on the first object in the pair.
 */
public class Tuple2<X, Y> {
    public X first;
    public Y second;

    public Tuple2(X first, Y second) {
        this.first = first;
        this.second = second;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple2<?, ?> tuple = (Tuple2<?, ?>) o;
        return first.equals(tuple.first);
    }

    @Override
    public int hashCode() {
        int result = 31 * first.hashCode();
        return result;
    }
}
