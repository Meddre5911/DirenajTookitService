package semantic;


public class Tag implements Comparable<Tag>{	
	
	private String tag;
	private int weight;
	
	public Tag(String tag, int weight) {
		this.tag = tag;
		this.weight = weight;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return new StringBuffer(tag).append(": ").append(weight).toString();
	}
	
	@Override
	public int compareTo(Tag o) {
		return o.getWeight() - weight;
	}
	
}
