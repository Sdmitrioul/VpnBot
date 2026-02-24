package com.dskroba.vpn.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class FP {
    public static <F, S, T> Function<F, T> compose(Function<F, S> first, Function<S, T> second) {
        return first.andThen(second);
    }

    public static <T> List<T> prepend(T element, List<T> list) {
        List<T> result = new ArrayList<>();
        result.add(element);
        result.addAll(list);
        return result;
    }

    public static <T> Consumer<T> constant(Class<T> clazz) {
        return (T input) -> {
        };
    }

    private FP() {
    }
}
