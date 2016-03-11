import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @param <O>
 *            the type of observer that this consumable can accept
 */
public interface Consumable<O> {
    /**
     * @param subscriber
     */
    void subscribe(O subscriber);

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
     *            type {@code S} and returns another consumable
     * @return the consumable returned by {@code f}
     */
    <O2, X extends Consumable<O2>> X extend(Function<Consumer<O>, X> f);
}
