package io.reactivex.flowable;
public interface Subscription {
    void cancel();
    void request(long n);
}
