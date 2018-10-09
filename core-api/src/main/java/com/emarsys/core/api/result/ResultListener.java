package com.emarsys.core.api.result;

import android.support.annotation.NonNull;

public interface ResultListener<T> {
    void onResult(@NonNull T result);
}