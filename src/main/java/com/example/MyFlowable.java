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
    
    public static <T> MyFlowable<T> just(final T t) {
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
        
    public <R> MyFlowable<R> lift(final Function<Subscriber<? super R>, Subscriber<? super T>> operator) {
        return new MyFlowable<R>(new MyOnSubscribe<R>() {
            @Override
            public void accept(Subscriber<? super R> subscriber) {
                onSubscribe.accept(operator.apply(subscriber));
            }
        });
    }
    
    public <R> MyFlowable<R> map(Function<? super T, ? extends R> f) {
        return lift(new OperatorMap<T, R>(f));
    }
    
    public <R> MyFlowable<R> flatMap(Function<? super T, ? extends Consumable<Subscriber<? super R>>> f) {
        return Flowable.merge(map(f)).extend(new Function<Consumer<Subscriber<? super R>>, MyFlowable<R>>() {

            @Override
            public MyFlowable<R> apply(Consumer<Subscriber<? super R>> t) {
                return MyFlowable.fromFlowable(t);
            }});
    }
    
    public static <T> MyFlowable<T> fromFlowable(final Consumer<Subscriber<? super T>> onSubscribe) {
        return new MyFlowable<T>(new MyOnSubscribe<T>() {
            @Override
            public void accept(Subscriber<? super T> subscriber) {
                onSubscribe.accept(subscriber);
            }});        
    }


}
