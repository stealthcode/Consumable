package io.reactivex;
import static org.junit.Assert.assertEquals;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.example.MyFlowable;

import io.reactivex.consumable.Consumable;
import io.reactivex.flowable.Flowable;
import io.reactivex.nonbp.Disposable;
import io.reactivex.observable.Observable;
import io.reactivex.observable.ObservableOnSubscribe;
import io.reactivex.observable.Observer;
import io.reactivex.single.Single;
import io.reactivex.single.SingleObserver;
import io.reactivex.single.SingleOnSubscribe;

public class ObservableTest {
    
    @Test
    public void testSingleToObservable() {
        final Object value = new Object();
        new Single<Object>(new SingleOnSubscribe<Object>() {
            @Override
            public void accept(SingleObserver<? super Object> t) {
                    t.onSubscribe(new Disposable() {
                        @Override
                        public void dispose() {}
                    });
                    t.onSuccess(value);
                }
            })
                .extend(new Function<Consumer<SingleObserver<? super Object>>, Observable<Object>>() {
                    @Override
                    public Observable<Object> apply(Consumer<SingleObserver<? super Object>> t) {
                        return Observable.fromSingle(t);
                    }
                })
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onNext(Object t) {
                        assertEquals(value, t);
                    }

                    @Override
                    public void onError(Throwable t) {
                        throw new IllegalStateException("onerror");
                    }

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                    }});
    }

    @Test
    public void testSingleToFlowable() {
        final Object value = new Object();
        Single<Object> single = new Single<Object>(new SingleOnSubscribe<Object>() {
            @Override
            public void accept(SingleObserver<? super Object> t) {
                t.onSubscribe(new Disposable() {
                    @Override
                    public void dispose() {}
                });
                t.onSuccess(value);
            }
        });
        single.extend(new Function<Consumer<SingleObserver<? super Object>>, Flowable<Object>>() {
            @Override
            public Flowable<Object> apply(Consumer<SingleObserver<? super Object>> t) {
                return Flowable.fromSingle(t);
            }
        })
        .subscribe(new Subscriber<Object>() {
            @Override
            public void onNext(Object t) {
                assertEquals(value, t);
            }
            
            @Override
            public void onError(Throwable t) {
                throw new IllegalStateException("onerror");
            }
            
            @Override
            public void onComplete() {
            }
            
            @Override
            public void onSubscribe(Subscription subscription) {
            }});
    }

    @Test
    public void testFlatMap() {
        Observable<Integer> integer = Observable.just(1);
        Flowable<Integer> flowableInt = integer.extend(new Function<Consumer<Observer<? super Integer>>, Flowable<Integer>>() {
            @Override
            public Flowable<Integer> apply(Consumer<Observer<? super Integer>> t) {
                return Flowable.fromObservable(t);
            }
        });
        Flowable<Integer> merged = flowableInt.flatMap(new Function<Integer, Consumable<Subscriber<? super Integer>>>() {
            @Override
            public Consumable<Subscriber<? super Integer>> apply(Integer i) {
                return i % 2 == 0 ? MyFlowable.just(i) : Flowable.just(i);
            }
        });
        merged.subscribe(new Subscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
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
    }
   
    @Test
    public void testMerge() {
        Flowable<Integer> flowable = new Observable<Integer>(new ObservableOnSubscribe<Integer>() {
            @Override
            public void accept(Observer<? super Integer> observer) {
                observer.onNext(1);
            }
        }).extend(new Function<Consumer<Observer<? super Integer>>, Flowable<Integer>>() {
            @Override
            public Flowable<Integer> apply(Consumer<Observer<? super Integer>> t) {
                return Flowable.fromObservable(t);
            }
        });
        flowable.flatMap(new Function<Integer, MyFlowable<Integer>>() {
            @Override
            public MyFlowable<Integer> apply(Integer t) {
                return MyFlowable.just(t);
            }
        });
    }
   
}
