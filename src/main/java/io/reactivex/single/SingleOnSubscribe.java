package io.reactivex.single;
import java.util.function.Consumer;

public interface SingleOnSubscribe<T> extends Consumer<SingleObserver<? super T>>{

}
