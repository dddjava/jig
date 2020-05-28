package org.dddjava.jig.presentation.view.handler;

public class HandlerMethodArgumentResolver {
    private final Object[] args;

    public HandlerMethodArgumentResolver(Object... args) {
        this.args = args;
    }

    public <T> T resolve(Class<T> clz) {
        for (Object arg : args) {
            if ((clz.isInstance(arg))) {
                return clz.cast(arg);
            }
        }
        throw new IllegalArgumentException(clz.toString());
    }
}
