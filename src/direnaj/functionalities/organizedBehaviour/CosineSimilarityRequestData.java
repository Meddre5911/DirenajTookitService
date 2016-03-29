package direnaj.functionalities.organizedBehaviour;

import com.mongodb.BasicDBObject;

import direnaj.driver.MongoCollectionFieldNames;

public class CosineSimilarityRequestData {

	private String requestId;
	private BasicDBObject query4OrgBehaviourTweetsOfRequestCollection;
	private BasicDBObject requestIdObject;

	public CosineSimilarityRequestData(String requestId, String originalRequestId) {
		this.requestId = requestId;
		query4OrgBehaviourTweetsOfRequestCollection = new BasicDBObject();
		query4OrgBehaviourTweetsOfRequestCollection.append(MongoCollectionFieldNames.MONGO_REQUEST_ID,
				originalRequestId);
		setRequestIdObject(new BasicDBObject());
		getRequestIdObject().append(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId);
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public BasicDBObject getQuery4OrgBehaviourTweetsOfRequestCollection() {
		return query4OrgBehaviourTweetsOfRequestCollection;
	}

	public void setQuery4OrgBehaviourTweetsOfRequestCollection(
			BasicDBObject query4OrgBehaviourTweetsOfRequestCollection) {
		this.query4OrgBehaviourTweetsOfRequestCollection = query4OrgBehaviourTweetsOfRequestCollection;
	}

	public BasicDBObject getRequestIdObject() {
		return requestIdObject;
	}

	public void setRequestIdObject(BasicDBObject requestIdObject) {
		this.requestIdObject = requestIdObject;
	}

}
