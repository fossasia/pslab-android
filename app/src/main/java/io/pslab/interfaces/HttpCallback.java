package io.pslab.interfaces;

public interface HttpCallback<T> {
    void success(T t1);
    void error(Exception e);
}
