package utils;

public class AbstractLeaf<T> {
	public QuadLeaf<T> value;

	public AbstractLeaf(QuadLeaf<T> value) {
		this.value = value;
	}
}