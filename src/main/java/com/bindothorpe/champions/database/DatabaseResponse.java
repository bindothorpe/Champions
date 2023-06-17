package com.bindothorpe.champions.database;

public interface DatabaseResponse <T> {
    void onResult(T result);
}
