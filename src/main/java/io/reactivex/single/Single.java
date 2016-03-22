package io.reactivex.single;
import java.util.function.Consumer;
import java.util.function.Function;

import io.reactivex.consumable.Consumable;

public class Single<T> implements Consumable<SingleObserver<? super T>> {

    private SingleOnSubscribe<T> onSubscribe;

    public Single(SingleOnSubscribe<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }
    
    @Override
    public void subscribe(SingleObserver<? super T> subscriber) {
        onSubscribe.accept(subscriber);
    }

    @Override
    public <S2, X extends Consumable<S2>> X extend(Function<Consumer<SingleObserver<? super T>>, X> f) {
        return f.apply(onSubscribe);
    }
    
}
