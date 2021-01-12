package kjd.reactnative.android;

/**
 * Backport of java.util.function.BiConsumer only available in SDK 24.  This snuck in here when
 * I changed the Min SDK to 24, and then reverted it back to 16 after a request (forgot to
 * clean up the code).
 *
 * @param <T>
 * @param <U>
 */
@FunctionalInterface
public interface BiConsumer<T,U> {
    void accept(T t, U u);
}
