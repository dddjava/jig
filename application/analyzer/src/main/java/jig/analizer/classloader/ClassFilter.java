package jig.analizer.classloader;

public interface ClassFilter {
    boolean test(Class<?> clz);
}
