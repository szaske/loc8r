package com.loc8r.seattle.interfaces;

/**
 * Interface for queries on MongoDB
 */

public interface QueryListener<T>
{
    void onSuccess(T result);
    void onError(Exception e);
}
