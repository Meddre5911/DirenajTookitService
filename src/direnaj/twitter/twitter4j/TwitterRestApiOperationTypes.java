package direnaj.twitter.twitter4j;

public enum TwitterRestApiOperationTypes {

	STATUS_USERTIMELINE("statuses", "/statuses/user_timeline"), //
	SEARCH_TWEETS("search", "/search/tweets");

	private final String restApiService;
	private final String resource;

	private TwitterRestApiOperationTypes(String resource, String restApiService) {
		this.resource = resource;
		this.restApiService = restApiService;
	}

	public boolean equalsName(String otherName) {
		return (otherName == null) ? false : restApiService.equals(otherName);
	}

	public String toString() {
		return this.restApiService;
	}

	public String getRestApiService() {
		return this.restApiService;
	}

	public String getResource() {
		return resource;
	}

}
