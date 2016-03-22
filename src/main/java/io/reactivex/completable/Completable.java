package io.reactivex.completable;
import java.util.function.Consumer;
import java.util.function.Function;

import io.reactivex.consumable.Consumable;

public class Completable implements Consumable<CompletableObserver> {

    private final CompletableOnSubscribe onSubscribe;

    public Completable(CompletableOnSubscribe onSubscribe) {
        this.onSubscribe = onSubscribe;
    }
    
    @Override
    public void subscribe(CompletableObserver subscriber) {
    }

    @Override
    public <S2, X extends Consumable<S2>> X extend(Function<Consumer<CompletableObserver>, X> f) {
        return f.apply(onSubscribe);
    }

}
