package io.reactivex.observable;
import io.reactivex.nonbp.Disposable;

public interface Observer<T> {
    void onNext(T t);
    void onError(Throwable t);
    void onComplete();
    void onSubscribe(Disposable d);
}
