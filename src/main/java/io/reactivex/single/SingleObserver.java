package io.reactivex.single;
import io.reactivex.nonbp.Disposable;

public interface SingleObserver<T> {

    void onSuccess(T t);
    
    void onError(Throwable t);
    
    void onSubscribe(Disposable d);
}
