package com.example;
import java.util.function.Consumer;
import java.util.function.Function;

import org.reactivestreams.Subscriber;

import io.reactivex.consumable.Consumable;
import io.reactivex.flowable.Flowable;
import io.reactivex.flowable.OperatorMap;

public class MyFlowable<T> implements Consumable<Subscriber<? super T>> {

    private final MyOnSubscribe<T> onSubscribe;

    public static interface MyOnSubscribe<T> extends Consumer<Subscriber<? super T>>{
        
    }
    
    public MyFlowable(MyOnSubscribe<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }
    
    public static <T> MyFlowable<T> just(T t) {
        return new MyFlowable<T>(new MyOnSubscribe<T>(){

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
    public <S2, X extends Consumable<S2>> X extend(Function<Consumer<Subscriber<? super T>>, X> f) {
        return null;
    }
        
    public <R> MyFlowable<R> lift(Function<Subscriber<? super R>, Subscriber<? super T>> operator) {
        return new MyFlowable<R>(subscriber -> onSubscribe.accept(operator.apply(subscriber)));
    }
    
    public <R> MyFlowable<R> map(Function<? super T, ? extends R> f) {
        return lift(new OperatorMap<T, R>(f));
    }
    
    public <R> MyFlowable<R> flatMap(Function<? super T, ? extends Consumable<Subscriber<? super R>>> f) {
        return Flowable.merge(map(f)).extend(MyFlowable::fromFlowable);
    }
    
    public static <T> MyFlowable<T> fromFlowable(Consumer<Subscriber<? super T>> onSubscribe) {
        return new MyFlowable<T>(new MyOnSubscribe<T>() {
            @Override
            public void accept(Subscriber<? super T> subscriber) {
                onSubscribe.accept(subscriber);
            }});        
    }


}
