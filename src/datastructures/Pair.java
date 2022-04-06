package datastructures;

public class Pair {
	public int i, k;
	
	public Pair(int i, int k) {
		this.i = i;
		this.k = k;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + i;
		result = prime * result + k;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Pair other = (Pair) obj;
		if (i != other.i)
			return false;
		if (k != other.k)
			return false;
		return true;
	}
}
