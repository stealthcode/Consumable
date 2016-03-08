public interface CompletableObserver {
    void onComplete();
    void onError(Throwable t);
    void onSubscribe(Disposable d);
}
