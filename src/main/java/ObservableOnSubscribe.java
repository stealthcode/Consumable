import java.util.function.Consumer;

public interface ObservableOnSubscribe<T> extends Consumer<Observer<? super T>> {

}
