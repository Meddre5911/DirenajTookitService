package direnaj.functionalities.organizedBehaviour;

import java.util.Date;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import direnaj.driver.MongoCollectionFieldNames;
import direnaj.util.DateTimeUtils;
import direnaj.util.TextUtils;

public class CosineSimilarityRequestData {

	private String requestId;
	private BasicDBObject query4OrgBehaviourTweetsOfRequestCollection;
	private BasicDBObject requestIdObject;
	private boolean hashtagSpecificRequest;
	private Date lowerTime;
	private Date upperTime;
	private ResumeBreakPoint resumeBreakPoint;

	public CosineSimilarityRequestData(String requestId, String originalRequestId) {
		this.requestId = requestId;
		query4OrgBehaviourTweetsOfRequestCollection = new BasicDBObject();
		query4OrgBehaviourTweetsOfRequestCollection.append(MongoCollectionFieldNames.MONGO_REQUEST_ID,
				originalRequestId);
		setRequestIdObject(new BasicDBObject());
		getRequestIdObject().append(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId);
	}

	public CosineSimilarityRequestData(String requestId, String originalRequestId, boolean isHashtagSpecificRequest,
			Date lowerTime, Date upperTime) {
		this.requestId = requestId;
		this.setHashtagSpecificRequest(isHashtagSpecificRequest);
		this.setLowerTime(lowerTime);
		this.setUpperTime(upperTime);
		// create requestId Object
		setRequestIdObject(new BasicDBObject());
		getRequestIdObject().append(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId);
		// create query object for OrgBehaviourTweetsOfRequestCollection
		query4OrgBehaviourTweetsOfRequestCollection = new BasicDBObject();
		query4OrgBehaviourTweetsOfRequestCollection.append(MongoCollectionFieldNames.MONGO_REQUEST_ID,
				originalRequestId);
		if (lowerTime != null && upperTime != null) {
			query4OrgBehaviourTweetsOfRequestCollection.append(MongoCollectionFieldNames.MONGO_TWEET_CREATION_DATE,
					new BasicDBObject("$gt", DateTimeUtils.getRataDieFormat4Date(lowerTime)).append("$lt",
							DateTimeUtils.getRataDieFormat4Date(upperTime)));
		}
		if (isHashtagSpecificRequest) {
			query4OrgBehaviourTweetsOfRequestCollection.append(MongoCollectionFieldNames.MONGO_IS_HASHTAG_TWEET, true);
		}
	}

	public CosineSimilarityRequestData(String originalRequestId, DBObject dbObject) throws Exception {
		this.requestId = (String) dbObject.get("requestId");
		this.hashtagSpecificRequest = (boolean) dbObject.get("isHashtagRequest");
		this.lowerTime = DateTimeUtils.getTwitterDate(TextUtils.getNotNullValue(dbObject.get("lowerTimeInterval")));
		this.upperTime = DateTimeUtils.getTwitterDate(TextUtils.getNotNullValue(dbObject.get("upperTimeInterval")));
		Object retrievedResumeBreakPoint = dbObject.get(MongoCollectionFieldNames.MONGO_RESUME_BREAKPOINT);
		if (retrievedResumeBreakPoint != null && !TextUtils.isEmpty(retrievedResumeBreakPoint.toString())) {
			this.setResumeBreakPoint(ResumeBreakPoint.valueOf(retrievedResumeBreakPoint.toString()));
		}
		// prepare queries
		query4OrgBehaviourTweetsOfRequestCollection = new BasicDBObject();
		query4OrgBehaviourTweetsOfRequestCollection.append(MongoCollectionFieldNames.MONGO_REQUEST_ID,
				originalRequestId);
		setRequestIdObject(new BasicDBObject());
		getRequestIdObject().append(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId);
		if (lowerTime != null && upperTime != null) {
			query4OrgBehaviourTweetsOfRequestCollection.append(MongoCollectionFieldNames.MONGO_TWEET_CREATION_DATE,
					new BasicDBObject("$gt", DateTimeUtils.getRataDieFormat4Date(lowerTime)).append("$lt",
							DateTimeUtils.getRataDieFormat4Date(upperTime)));
		}
		if (hashtagSpecificRequest) {
			query4OrgBehaviourTweetsOfRequestCollection.append(MongoCollectionFieldNames.MONGO_IS_HASHTAG_TWEET, true);
		}
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

	public boolean isHashtagSpecificRequest() {
		return hashtagSpecificRequest;
	}

	public void setHashtagSpecificRequest(boolean hashtagSpecificRequest) {
		this.hashtagSpecificRequest = hashtagSpecificRequest;
	}

	public Date getLowerTime() {
		return lowerTime;
	}

	public void setLowerTime(Date lowerTime) {
		this.lowerTime = lowerTime;
	}

	public Date getUpperTime() {
		return upperTime;
	}

	public void setUpperTime(Date upperTime) {
		this.upperTime = upperTime;
	}

	public ResumeBreakPoint getResumeBreakPoint() {
		return resumeBreakPoint;
	}

	public void setResumeBreakPoint(ResumeBreakPoint resumeBreakPoint) {
		this.resumeBreakPoint = resumeBreakPoint;
	}

	@Override
	public String toString() {
		return "RequestId : " + requestId + "\n Query For Request : "
				+ query4OrgBehaviourTweetsOfRequestCollection.toString();
	}

}
