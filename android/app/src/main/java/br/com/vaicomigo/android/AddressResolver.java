package br.com.vaicomigo.android;

import java.util.List;

/** Call only after contextual consent. An empty result also represents failure. */
interface AddressResolver {
    interface Callback { void completed(List<ResolvedPlace> places); }
    void search(String address, Callback callback);
    void cancel();
}
