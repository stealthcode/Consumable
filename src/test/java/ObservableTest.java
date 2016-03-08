import static org.junit.Assert.*;

import org.junit.Test;

public class ObservableTest {
    
    @Test
    public void testSingleToObservable() {
        Object value = new Object();
        new Single<Object>(t -> {
                t.onSubscribe(() -> {});
                t.onSuccess(value);
            })
                .extend(Observable::fromSingle)
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
        Object value = new Object();
        Single<Object> single = new Single<Object>(t -> {
            t.onSubscribe(() -> {});
            t.onSuccess(value);
        });
        single.extend(Flowable::fromSingle)
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
        Flowable<Integer> flowableInt = integer.extend(Flowable::fromObservable);
        Flowable<Integer> merged = flowableInt.flatMap(i ->
            i % 2 == 0 ? MyFlowable.just(i) : Flowable.just(i));
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
        Flowable<Integer> flowable = new Observable<Integer>(observer -> {
            observer.onNext(1);
        }).extend(Flowable::fromObservable);
        flowable.flatMap(MyFlowable::just);
    }
   
}
