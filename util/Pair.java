package utils;


public class Pair<T> {
	public final T left;
	public final T right;
		
	public Pair(T left, T right)
	{
		this.left = left;
		this.right = right;
	}

	public T getLeft() {
		return left;
	}

	public T getRight() {
		return right;
	}
	
	public String toString()
	{
		return left + ":" + right;
	}
}
