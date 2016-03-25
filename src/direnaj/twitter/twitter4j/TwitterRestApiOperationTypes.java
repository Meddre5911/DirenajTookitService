package direnaj.twitter.twitter4j;

public enum TwitterRestApiOperationTypes {

	STATUS_USERTIMELINE("/statuses/user_timeline");

	private final String str;

	private TwitterRestApiOperationTypes(String s) {
		str = s;
	}

	public boolean equalsName(String otherName) {
		return (otherName == null) ? false : str.equals(otherName);
	}

	public String toString() {
		return this.str;
	}

}
