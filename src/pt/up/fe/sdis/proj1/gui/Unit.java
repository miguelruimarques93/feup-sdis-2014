package pt.up.fe.sdis.proj1.gui;

public enum Unit {
    KB, MB, GB, TB;
    public static long getMultiplier(Unit u) {
        switch (u) {
        case KB:
            return 1000L;
        case MB:
            return 1000000L;
        case GB:
            return 1000000000L;
        case TB:
            return 1000000000000L;
        default:
            throw new IllegalArgumentException();
        }
    }
    
    public static Unit getAppropiateUnit(long value) {
        Unit[] values = values();
        for (int i = 1; i < values.length; ++i) {
            if (value < getMultiplier(values[i])) return values[i-1];
        }
        return TB;
    }
}
