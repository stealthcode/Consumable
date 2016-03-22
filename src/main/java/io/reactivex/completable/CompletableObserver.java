package io.reactivex.completable;
import io.reactivex.nonbp.Disposable;

public interface CompletableObserver {
    void onComplete();
    void onError(Throwable t);
    void onSubscribe(Disposable d);
}
