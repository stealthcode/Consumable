package io.reactivex.flowable;
import java.util.function.Consumer;

import org.reactivestreams.Subscriber;

public interface FlowableOnSubscribe<T> extends Consumer<Subscriber<? super T>>{

}
