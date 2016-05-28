package utils;
public class Tuple<T, U> {
	public T _1;
	public U _2;

	public Tuple(T arg1, U arg2) {
		super();
		this._1 = arg1;
		this._2 = arg2;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s)", _1, _2);
	}
}