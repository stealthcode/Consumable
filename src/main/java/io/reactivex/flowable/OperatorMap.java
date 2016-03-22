package io.reactivex.flowable;
import java.util.function.Function;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class OperatorMap<T, R> implements Function<Subscriber<? super R>, Subscriber<? super T>> {

        private final Function<? super T, ? extends R> f;

        public OperatorMap(Function<? super T, ? extends R> f) {
            this.f = f;
        }
        
        @Override
        public Subscriber<? super T> apply(Subscriber<? super R> subscriber) {
            return new Subscriber<T>(){

                @Override
                public void onNext(T t) {
                    subscriber.onNext(f.apply(t));
                }

                @Override
                public void onError(Throwable t) {
                    subscriber.onError(t);
                }

                @Override
                public void onComplete() {
                    subscriber.onComplete();
                }

                @Override
                public void onSubscribe(Subscription subscription) {
                    subscriber.onSubscribe(subscription);
                }};
        }
        
    }
