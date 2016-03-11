import java.util.function.Consumer;
import java.util.function.Function;

public class Observable<T> implements Consumable<Observer<? super T>> {

    private final ObservableOnSubscribe<T> onSubscribe;
    
    public Observable(ObservableOnSubscribe<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }
    
    public static <T> Observable<T> just(T t) {
        return new Observable<T>(new ObservableOnSubscribe<T>(){

            @Override
            public void accept(Observer<? super T> subscriber) {
                subscriber.onNext(t);
                subscriber.onComplete();
            }});
    }
    
    public <R> Observable<R> lift(Function<Observer<? super R>, Observer<? super T>> operator) {
        return new Observable<R>(new ObservableOnSubscribe<R>() {
            @Override
            public void accept(Observer<? super R> observer) {
                onSubscribe.accept(operator.apply(observer));
            }});
    }

    @Override
    public <S2, X extends Consumable<S2>> X extend(Function<Consumer<Observer<? super T>>, X> f) {
        return f.apply(onSubscribe);
    }
    
    @Override
    public void subscribe(Observer<? super T> subscriber) {
        onSubscribe.accept(subscriber);
    }
    
    public static <T> Observable<T> fromSingle(Consumer<SingleObserver<? super T>> onSubscribe) {
        return new Observable<T>(new ObservableOnSubscribe<T>() {
            @Override
            public void accept(Observer<? super T> observer) {
                onSubscribe.accept(new SingleObserver<T>() {
                    @Override
                    public void onSuccess(T t) {
                        observer.onNext(t);
                        observer.onComplete();
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        observer.onSubscribe(d);
                    }});
            }});
    }
    
    public static <T> Observable<T> fromCompletable(Consumer<CompletableObserver> onSubscribe) {
        return new Observable<T>(new ObservableOnSubscribe<T>() {
            @Override
            public void accept(Observer<? super T> observer) {
                onSubscribe.accept(new CompletableObserver() {
                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        observer.onSubscribe(d);
                    }});
            }});
    }
    
    public static <T> Observable<T> fromFlowable(Consumer<Subscriber<T>> onSubscribe) {
        return new Observable<T>(new ObservableOnSubscribe<T>() {
            @Override
            public void accept(Observer<? super T> observer) {
                onSubscribe.accept(new Subscriber<T>() {
                    @Override
                    public void onNext(T t) {
                        observer.onNext(t);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }

                    @Override
                    public void onSubscribe(Subscription subscription) {
                        observer.onSubscribe(subscription::cancel);
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }});
            }});        
    }

}
