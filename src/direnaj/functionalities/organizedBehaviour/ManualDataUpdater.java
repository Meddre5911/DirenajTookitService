package direnaj.functionalities.organizedBehaviour;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.util.DateTimeUtils;
import direnaj.util.TextUtils;

public class ManualDataUpdater {

	public static void main(String[] args) {
		List<String> requestIds = new ArrayList<>();
		requestIds.add("20160818162207875bc5c3883-d82c-433d-b9d1-081852f1c256");
		requestIds.add("2016081815503787469ef0f05-14cd-4a7b-bc3c-da7f6eb065ce");
		requestIds.add("201608181346192049ea0d5d6-4991-4215-bd2e-2f6e823b54f4");
		requestIds.add("20160818112832806a5ddd8a-cfcc-41f1-b81b-f0b5f9f08a42");
		requestIds.add("201608180313223052e236de6-3e55-4284-a0c1-855b5c103367");
		updateProcessInputData(requestIds);
		updateCosSimilarityRequestData(requestIds);

	}
	
	public static void updateData() {
		List<String> requestIds = new ArrayList<>();
		requestIds.add("20160818162207875bc5c3883-d82c-433d-b9d1-081852f1c256");
		requestIds.add("2016081815503787469ef0f05-14cd-4a7b-bc3c-da7f6eb065ce");
		requestIds.add("201608181346192049ea0d5d6-4991-4215-bd2e-2f6e823b54f4");
		requestIds.add("20160818112832806a5ddd8a-cfcc-41f1-b81b-f0b5f9f08a42");
		requestIds.add("201608180313223052e236de6-3e55-4284-a0c1-855b5c103367");
		updateProcessInputData(requestIds);
		updateCosSimilarityRequestData(requestIds);

	}

	private static void updateCosSimilarityRequestData(List<String> requestIds) {
		DBCursor cursor = null;
		try {

			BasicDBObject query = new BasicDBObject("originalRequestId", new BasicDBObject("$in", requestIds));

			DBCollection orgBehaviourRequestedSimilarityCalculations = DirenajMongoDriver.getInstance()
					.getOrgBehaviourRequestedSimilarityCalculations();
			cursor = orgBehaviourRequestedSimilarityCalculations.find(query);
			while (cursor.hasNext()) {
				DBObject processInputData = cursor.next();
				String requestId = (String) processInputData.get(MongoCollectionFieldNames.MONGO_REQUEST_ID);
				String originalRequestId = (String) processInputData.get("originalRequestId");
				String twitterDateStr = (String) processInputData.get("lowerTimeInterval");
				if (!TextUtils.isEmpty(twitterDateStr)) {
					double rataDieFormat4Date = DateTimeUtils
							.getRataDieFormat4Date(DateTimeUtils.getTwitterDate(twitterDateStr));
					DBObject updateFindQuery = new BasicDBObject("originalRequestId", originalRequestId)
							.append(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId);
					updateRequestInMongoByColumnName(orgBehaviourRequestedSimilarityCalculations, updateFindQuery,
							MongoCollectionFieldNames.MONGO_COS_SIM_REQ_RATA_DIE_LOWER_TIME, rataDieFormat4Date);
				}
			}
			System.out.println("Update For orgBehaviourRequestedSimilarityCalculations Collection is completed.");
		} catch (Exception e) {
			System.out.println("Update For orgBehaviourRequestedSimilarityCalculations Collection received Error.");
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

	}

	private static void updateProcessInputData(List<String> requestIds) {
		DBCursor cursor = null;
		try {
			BasicDBObject query = new BasicDBObject("requestId", new BasicDBObject("$in", requestIds));

			DBCollection orgBehaviourProcessInputData = DirenajMongoDriver.getInstance()
					.getOrgBehaviourProcessInputData();
			cursor = orgBehaviourProcessInputData.find(query);
			while (cursor.hasNext()) {
				DBObject processInputData = cursor.next();
				String requestId = (String) processInputData.get(MongoCollectionFieldNames.MONGO_REQUEST_ID);
				String userId = (String) processInputData.get(MongoCollectionFieldNames.MONGO_USER_ID);
				String twitterDateStr = (String) processInputData
						.get(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE);
				double rataDieFormat4Date = DateTimeUtils
						.getRataDieFormat4Date(DateTimeUtils.getTwitterDate(twitterDateStr));
				DBObject updateFindQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId)
						.append(MongoCollectionFieldNames.MONGO_USER_ID, userId);
				updateRequestInMongoByColumnName(orgBehaviourProcessInputData, updateFindQuery,
						MongoCollectionFieldNames.MONGO_USER_CREATION_DATE_IN_RATA_DIE, rataDieFormat4Date);
			}
			System.out.println("Update For orgBehaviourProcessInputData Collection is completed.");
		} catch (Exception e) {
			System.out.println("Update For orgBehaviourProcessInputData Collection received Error.");
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public static void updateRequestInMongoByColumnName(DBCollection dbCollection, DBObject findQuery,
			String columnName, Object updateValue) {
		BasicDBObject updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append(columnName, updateValue));
		dbCollection.update(findQuery, updateQuery, true, false);
	}

}
