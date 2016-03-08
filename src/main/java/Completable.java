import java.util.function.Consumer;
import java.util.function.Function;

public class Completable implements Consumable<Void, CompletableObserver> {

    private final CompletableOnSubscribe onSubscribe;

    public Completable(CompletableOnSubscribe onSubscribe) {
        this.onSubscribe = onSubscribe;
    }
    
    @Override
    public void subscribe(CompletableObserver subscriber) {
    }

    @Override
    public <R, S2, X extends Consumable<R, S2>> X extend(Function<Consumer<CompletableObserver>, X> f) {
        return f.apply(onSubscribe);
    }

}
