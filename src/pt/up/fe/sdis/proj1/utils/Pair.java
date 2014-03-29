package pt.up.fe.sdis.proj1.utils;

import java.io.Serializable;

public class Pair<T, V> implements Serializable {
    private static final long serialVersionUID = 1L;

    public Pair(T f, V s) { first = f; second = s; }
    public static <T1, V1> Pair<T1, V1> make_pair(T1 f, V1 s) { return new Pair<T1, V1>(f, s); }
    public T first;
    public V second;
    
    @Override
    public boolean equals(Object other) {
        if(other != null && (other instanceof Pair<?, ?>)) {
            Pair<?, ?> p = (Pair<?, ?>)other;
            return p.first.equals(first) && p.second.equals(second);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "[" + first.toString() + ", " + second.toString() + "]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + first.hashCode();
        result = prime * result + second.hashCode();
        return result;
    }
}
