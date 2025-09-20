package ru.mirea.newrav1k.accountservice.utils;

import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class PageableKeyGenerator implements org.springframework.cache.interceptor.KeyGenerator {

    @NonNull
    @Override
    public Object generate(@NonNull Object target,
                           @NonNull Method method,
                           @NonNull Object... params) {
        return Arrays.stream(params)
                .map(param -> {
                    if (param instanceof Pageable p) {
                        return String.format("%d-%d-%s",
                                p.getPageNumber(), p.getPageSize(), p.getSort());
                    }
                    return param;
                })
                .map(String::valueOf)
                .collect(Collectors.joining(":"));
    }

}