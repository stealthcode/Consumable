public interface SingleObserver<T> {

    void onSuccess(T t);
    
    void onError(Throwable t);
    
    void onSubscribe(Disposable d);
}
