import java.util.function.Consumer;
import java.util.function.Function;

public class Single<T> implements Consumable<T, SingleObserver<? super T>> {

    private SingleOnSubscribe<T> onSubscribe;

    public Single(SingleOnSubscribe<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }
    
    @Override
    public void subscribe(SingleObserver<? super T> subscriber) {
        onSubscribe.accept(subscriber);
    }

    @Override
    public <R, S2, X extends Consumable<R, S2>> X extend(Function<Consumer<SingleObserver<? super T>>, X> f) {
        return f.apply(onSubscribe);
    }
    
}
