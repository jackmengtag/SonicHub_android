package com.sonichub.app.data;

public interface RepoCallback<T> {
    void onSuccess(T data);

    void onError(String message);
}
