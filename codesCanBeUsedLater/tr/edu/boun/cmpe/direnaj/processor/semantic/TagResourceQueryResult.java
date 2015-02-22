package semantic;

import org.apache.commons.collections15.Bag;

public class TagResourceQueryResult {

	private int id;
	private String name;
	private String searchTxt;
	private Bag<String> tags;
	
	public TagResourceQueryResult(int id, String name, String searchTxt, Bag<String> tags) {
		this.id = id;
		this.name = name;
		this.searchTxt = searchTxt;
		this.tags = tags;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getSearchTxt() {
		return searchTxt;
	}

	public void setSearchTxt(String searchTxt) {
		this.searchTxt = searchTxt;
	}
	
	public Bag<String> getTags() {
		return tags;
	}
	
	public void setTags(Bag<String> tags) {
		this.tags = tags;
	}
}
