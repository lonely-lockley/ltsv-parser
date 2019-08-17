package com.github.lolo.ltsv;

import java.io.IOException;

@FunctionalInterface
public interface CheckedBiFunction<T, U, R> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     * @throws IOException if something goes wrong with underlying input stream
     */
    R apply(T t, U u) throws IOException;

}
