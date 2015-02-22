package semantic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Query {

	public final static int STATUS_WAITING = 1;
	public final static int STATUS_PROCESSING = 2;
	
	public final static int STATUS_FINISHED = 0;
	
	public final static int STATUS_DIRTY = -1;
	
	private int id;
	private Date date;
	private String query;
	private int status;
	
	private FreeBaseTopic freeBaseTopic;

	private DbpediaResource dbpediaResource;
	
	private List<TagResourceQueryResult> taggingResults = new ArrayList<TagResourceQueryResult>();
	
	public List<TagResourceQueryResult> getTaggingResults() {
		return taggingResults;
	}

	public void addNewTaggingResult(TagResourceQueryResult taggingResult) {
		this.taggingResults.add(taggingResult);
	}

	public Query(int id, Date date, String query, String freebaseId, String freebaseType, int status) {
		
		this.id = id;
		this.date = date;
		this.query = query;
		
		freeBaseTopic = new FreeBaseTopic(freebaseId, freebaseType);

		this.status = status;
	}

	public Query(int id) {
		this.id = id;
	}

	public FreeBaseTopic getFreeBaseTopic() {
		return freeBaseTopic;
	}

	public void setFreeBaseTopic(FreeBaseTopic freeBaseTopic) {
		this.freeBaseTopic = freeBaseTopic;
	}

	public DbpediaResource getDbpediaResource() {
		return dbpediaResource;
	}

	public void setDbpediaResource(DbpediaResource dbpediaResource) {
		this.dbpediaResource = dbpediaResource;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
}