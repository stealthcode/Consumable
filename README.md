# Consumable

This is a prototype to show that Operators can be reused for various implementations of an Observable while still interoping with the standard Flowable, Observable, Completable, Single implementations. 

## Motivation

Library authors who expose an observable, completable, single, or flowable object sometimes need to constrain or extend the available operators. For instance, in some streaming apis there is a need to not allow any `filter` type operations or a flatMap that returns an `empty()`. Other cases might include "namespacing" custom operators into an Observable/Flowable custom implementation that adds or removes operators as necessary. Also some operators (`groupBy`) must return a specialized type of an `Observable`/`Flowable` (the `GroupedObservable`) which must extend Observable in order to interop with other Observable sources and thus must include all methods/operators on the `Observable`. This change would allow for operators to be written that constrain or extend the operators available to the user.    

## Summary

Note that in this prototype I have not made any reference to RxJava or any other libraries other than the Java runtime library. Therefore I have re-implemented some features or used functional interfaces. 

Concerns this prototype attempts to address:
 
 1. A library owner should be able to expose a custom Observable or Flowable implementation.
 2. A custom implementation should be able to reuse standard operators.
 3. A custom implementation should interop with other existing Observables.

Specific Challenges that have to be addressed...

 1. What is the minimal interface that a custom Observable must implement? And is this the same interface for an Observable as say a Flowable, Completable, or Single? 
 2. Operators that take nested observables should allow heterogeneous kinds of Observable/Flowables.
 3. Instance method operators that use the standard Operators (i.e. `OperatorScan`) should be able to return an instance of the same original observable type. 

## 1. Standard Minimal Interface

I propose that the goal of an Observable interface simply be to facilitate subscribing to the Observable. I have somewhat arbitrarily chosen the name `Consumable`. 

```java
/**
 *
 * @param <O>
 *            the type of observer that this consumable can accept
 */
public interface Consumable<O> {
    /**
     * @param observer
     */
    void subscribe(O observer);

    /**
     * This is a fluent {@link Consumable#subscribe(Object) subscribe} which must return a
     * {@code Consumable}. The result may be a consumable of any type and any observer type.
     * 
     * @param <O2>
     *            the type of observer of the resultant consumable
     * @param <X>
     *            the consumable returned by {@code f}
     * @param f
     *            a function that receives this consumable's onSubscribe which takes an observer
     *            type {@code O} and returns another consumable
     * @return the consumable returned by {@code f}
     */
    <O2, X extends Consumable<O2>> X extend(Function<Consumer<O>, X> f);
}
```

The intent of this interface is to facilitate getting to the underlying onSubscribe functionality via a designated observer type `O`. The consumable interface facilitates creating various types of observables which may agree on the underlying observer type but have different semantics of operators or assumptions in the subscription chaining. 

### 2. Working with Nested Consumables

Operations that need an `Observable<Observable<T>>` or `Flowable<Flowable<T>>` can be written in such a way that the type of the outer shell (the operator menu that is the `rx.Observable`) should not impede interop. To this end, I propose that the type of `Observer` used in the underlying (potentially custom) observable/flowable be referenced as a first class. Here is an example of the merge API in the Flowable implementation. 

```java
public static <T, I extends Consumable<Subscriber<? super T>>> Flowable<T> merge(Consumable<Subscriber<? super I>> others);

/**
 * FlatMap takes a function from each value of type T emitted by this Flowable to a
 * {@link Consumable} that supports subscriptions by {@link Subscriber} and returns a
 * Flowable that serialized all values from each returned Consumable.
 * 
 * @param f
 *            a function from {@code T} to a {@code Consumable<Subscriber<? super R>>}
 * @return a Flowable that emits transformed all values from all inner flowables.
 */
public <R> Flowable<R> flatMap(Function<? super T, ? extends Consumable<Subscriber<? super R>>> f) {
    return merge(map(f));
}
```

As I am sure you can see, the generic type signature of a heterogeneous merge is made more complicated by referencing the `Subscriber`. However the benefit here can be seen when a custom Flowable implements a generic `flatMap`. 

```java
public <R> MyFlowable<R> flatMap(Function<? super T, ? extends Consumable<Subscriber<R>>> f) {
    return Flowable.merge(map(f)).extend(MyFlowable::fromFlowable);
}
```

This facilitates flat mapping any kind of Flowable (specifically any Consumable that supports an observer of type `Subscriber`).

```java
Observable<Integer> integer = Observable.just(1);
Flowable<Integer> flowableInt = integer.extend(Flowable::fromObservable);
Flowable<Integer> merged = flowableInt.flatMap(i ->
    i % 2 == 0 ? MyFlowable.just(i) : Flowable.just(i));
```

In the above example the `someServiceCall` may return a `Flowable` instance or any other instance which is backed by a `Subscriber` and the user of RxJava doesn't need to know or care. 

Another final note, in this example you may have noticed that I'm using the `.extend(MyFlowable::fromFlowable)` to convert it from a `Flowable` (returned by merge) back into the custom `MyFlowable`. Depending on the custom implementation, it is possible that the same type is used for the `OnSubscribe` and thus the `Flowable` which could be GC'ed as the implementation of extend simply takes the underlying onSubscribe and rewraps it. However when a subscriber adapter has to be built, then there is an extra hop of translating from one onSubscribe into another. 

### 3. Reusing standard operators

I do not forsee any problems using the existing RxJava standard operators as they are defined today as they are operations over Observer types. 

```java
public <R> MyFlowable<R> map(Function<? super T, ? extends R> f) {
    return lift(new OperatorMap<T, R>(f));
}

public <R> MyFlowable<R> lift(Function<Subscriber<? super R>, Subscriber<? super T>> operator) {
    return new MyFlowable<R>(subscriber -> onSubscribe.accept(operator.apply(subscriber)));
}
```

## Cons

The clearest draw back is the impact to the type signatures. The argument to the frequently used `Flowable.flatMap()` method would change from a function returning a `Flowable<T>` to a function that returns a more generic type of `Consumable<Subscriber<T>>`. That is to say, a thing that can be subscribed to with back-pressure semantics of the `Subscriber`. This would require that users understood that a `Flowable` implements a `Consumable<Subsciber>` when they use `flatMap()`.    

## Pros

In RxJava v1.1.2 the only option for producing a streamable source of data that interops with operators like `merge` or `concat` is to extend  `Observable`. With this change the operators would be generic enough to accept any implementation which still functions on the same underlying semantics of subscription. 

This also means that any class that implements `Consumable<Subscriber<T>>` could use a Flowable Operator class because it is a function that adapts a `Subscriber` to chain together operations. So provided that the custom observable implements a protected method to instantiate the class the lift could be abstracted into a static class and all conventional `Subscriber -> Subscriber` operators could be reused.

Additionally when a custom streamable source is implemented, the author could chose exactly which operators to include or to exclude. This is not possible with current classes that extend `Observable`. A custom Observable/Flowable for instance could be implemented with only a subset of operators. This could be useful in preventing operator combinations that would otherwise lead to incorrect or non-performant code. 

Lastly, library authors in Java often times constrain the valid inputs by what types the api accepts. With this a user could implement a streaming source using standard operators and subscription semantics and require their usage in their apis. For instance, if the argument to a method call only takes a custom observable type `AcmeStreamingObservable` and the only way to publicly create such an observable is to use the library to create one, then the library author could rely on functionality built in and potentially state carried with the `AcmeStreamingObservable` with the knowledge that the user is unable to change the state, as any valid program that a user writes would not be able to create a valid instance of the observable (baring a clearly illegal usage of `Objenesis` which would likely result in an unusable class).