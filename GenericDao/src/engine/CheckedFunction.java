package engine;

@FunctionalInterface
public interface CheckedFunction<T,G, R> {
   R apply(T t,G g) throws Exception;
}
