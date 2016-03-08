import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

public class Flowable<T> implements Consumable<T, Subscriber<? super T>> {
    
    private final FlowableOnSubscribe<T> onSubscribe;

    public Flowable(FlowableOnSubscribe<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }
    
    public static <T> Flowable<T> just(T t) {
        return new Flowable<T>(new FlowableOnSubscribe<T>(){

            @Override
            public void accept(Subscriber<? super T> subscriber) {
                subscriber.onNext(t);
                subscriber.onComplete();
            }});
    }
    
    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        onSubscribe.accept(subscriber);
    }

    @Override
    public <R, S2, X extends Consumable<R, S2>> X extend(Function<Consumer<Subscriber<? super T>>, X> convertion) {
        return convertion.apply(onSubscribe);
    }

    public <R> Flowable<R> lift(Function<Subscriber<? super R>, Subscriber<? super T>> operator) {
        return new Flowable<R>(new FlowableOnSubscribe<R>() {
            
            @Override
            public void accept(Subscriber<? super R> subscriber) {
                onSubscribe.accept(operator.apply(subscriber));
            }
        });
    }
    
    public <R> Flowable<R> map(Function<? super T, ? extends R> f) {
        return lift(new OperatorMap<T, R>(f));
    }

    /**
     * FlatMap takes a function from each value of type T emitted by this Flowable to a
     * {@link Consumable} of type R that supports subscriptions by {@link Subscriber} and returns a
     * Flowable that serialized all values from each returned Consumable.
     * 
     * @param f
     *            a function from {@code T} to a {@code Consumable<R, Subscriber<? super R>>}
     * @return a Flowable that emits transformed all values from all inner flowables.
     */
    public <R> Flowable<R> flatMap(Function<? super T, ? extends Consumable<R, Subscriber<? super R>>> f) {
        return merge(map(f));
    }

    public static <T> Flowable<T> fromObservable(Consumer<? super Observer<T>> onSubscribe) {
        return new Flowable<T>(new FlowableOnSubscribe<T>() {
            @Override
            public void accept(Subscriber<? super T> subscriber) {
                onSubscribe.accept(new Observer<T>() {
                    @Override
                    public void onNext(T t) {
                        // back pressure implementation here
                        subscriber.onNext(t);
                    }
                    
                    @Override
                    public void onComplete() {
                        subscriber.onComplete();
                    }

                    @Override
                    public void onError(Throwable t) {
                        subscriber.onError(t);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        subscriber.onSubscribe(new Subscription(){
                            @Override
                            public void cancel() {
                                d.dispose();
                            }

                            @Override
                            public void request(long n) {
                                // back pressure implementation here
                            }
                        });
                    }
                });
            }});
    }

    public static <T> Flowable<T> fromSingle(Consumer<? super SingleObserver<T>> onSubscribe) {
        return new Flowable<T>(new FlowableOnSubscribe<T>() {
            @Override
            public void accept(Subscriber<? super T> subscriber) {
                onSubscribe.accept(new SingleObserver<T>() {
                    T singleValue;
                    final AtomicLong requested = new AtomicLong(0);
                    
                    @Override
                    public void onSuccess(T t) {
                        if (requested.compareAndSet(0, -1)) {
                            subscriber.onNext(t);
                            subscriber.onComplete();
                        }
                        else {
                            singleValue = t;
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        subscriber.onError(t);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        subscriber.onSubscribe(new Subscription(){
                            @Override
                            public void cancel() {
                                d.dispose();
                            }

                            @Override
                            public void request(long n) {
                                if (!requested.compareAndSet(0, n)) {
                                    subscriber.onNext(singleValue);
                                }
                            }});
                    }});
            }});
    }
    
    /**
     * Subscribes to all inner consumables using a back-pressure aware {@link Subscriber} and returns a Flowable of .
     * 
     * @param others
     * @return
     */
    public static <T, I extends Consumable<T, Subscriber<? super T>>> Flowable<T> merge(Consumable<? super I, Subscriber<? super I>> others) {
        return others.extend(onSubscribe -> new Flowable<T>(new FlowableOnSubscribe<T>(){
            @Override
            public void accept(Subscriber<? super T> subscriber) {
                onSubscribe.accept(new Subscriber<Consumable<T, Subscriber<? super T>>>() {
                    @Override
                    public void onNext(Consumable<T, Subscriber<? super T>> inner) {
                        inner.subscribe(new Subscriber<T>() {
                            @Override
                            public void onNext(T t) {
                                // here be dragons
                            }

                            @Override
                            public void onError(Throwable t) {
                            }

                            @Override
                            public void onComplete() {
                            }

                            @Override
                            public void onSubscribe(Subscription subscription) {
                            }});;
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onSubscribe(Subscription subscription) {
                    }});
            }}));
    }
    
}
