package jig.classloader;

public interface ClassFilter {
    boolean test(Class<?> clz);
}
