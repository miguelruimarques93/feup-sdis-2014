package pt.up.fe.sdis.proj1.utils;

public class Pair<T, V> {
    public Pair(T f, V s) { first = f; second = s; }
    public static <T1, V1> Pair<T1, V1> make_pair(T1 f, V1 s) { return new Pair<T1, V1>(f, s); }
    public T first;
    public V second;
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + first.hashCode();
        result = prime * result + second.hashCode();
        return result;
    }
}
