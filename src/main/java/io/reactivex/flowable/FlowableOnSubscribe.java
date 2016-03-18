package io.reactivex.flowable;
import java.util.function.Consumer;

public interface FlowableOnSubscribe<T> extends Consumer<Subscriber<? super T>>{

}
