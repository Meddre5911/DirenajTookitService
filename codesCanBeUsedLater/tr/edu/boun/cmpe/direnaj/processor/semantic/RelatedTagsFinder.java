package semantic;

import org.apache.commons.collections15.Bag;

public interface RelatedTagsFinder {

	public Bag<String> findCooccuringTags(String query) throws Exception;
	public int maxItems();
	public int id();
}
