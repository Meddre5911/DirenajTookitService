package semantic;

import java.util.List;

import org.apache.commons.collections15.Bag;

public class DbpediaResource {

	/**
	 * example: <http://dbpedia.org/resource/Java>
	 */
	private String resource;
	private Bag<String> redirects;
	private List<String> categories;

	private boolean hasDisambiguation;
	
	public boolean hasDisambiguation() {
		return hasDisambiguation;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public void setHasDisambiguation(boolean hasDisambiguation) {
		this.hasDisambiguation = hasDisambiguation;
	}

	public DbpediaResource(String resource) {
		this.resource = resource;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public void setRedirects(Bag<String> redirects) {
		this.redirects = redirects;
	}
	
	public Bag<String> getRedirects() {
		return redirects;
	}
	
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		
		sb.append("DbpediaResource: resource=").append(resource);
		
		return sb.toString();
	}
}
