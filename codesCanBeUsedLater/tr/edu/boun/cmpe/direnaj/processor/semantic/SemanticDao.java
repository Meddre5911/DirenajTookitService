package semantic;

import org.apache.commons.collections15.Bag;

public interface SemanticDao {
	
	public String freebaseToDbpedia(String freebaseId);
	
	public Bag<String> findRedirects(String dbpediaResource);
	
	public boolean haveDisambiguation(String dbpediaResource);
	
}
