package testPackage.organizedBehaviors;

import java.util.Date;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.DirenajMongoDriverUtil;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.util.DateTimeUtils;
import direnaj.util.TextUtils;

public class UTCDateConverter {

	public void convertUTC() {
		try {
			DirenajMongoDriver.getInstance().getOrgBehaviourRequestMeanVarianceCalculations()
					.remove(new BasicDBObject());

			// convert date in processInputData
			DBCollection orgBehaviourProcessInputData = DirenajMongoDriver.getInstance()
					.getOrgBehaviourProcessInputData();
			DBCursor cursor = orgBehaviourProcessInputData.find();

			while (cursor.hasNext()) {
				DBObject next = cursor.next();

				String requestId = (String) next.get(MongoCollectionFieldNames.MONGO_REQUEST_ID);
				String userId = (String) next.get(MongoCollectionFieldNames.MONGO_USER_ID);

				String localDateString = (String) next.get(MongoCollectionFieldNames.MONGO_USER_CREATION_DATE);
				Date localTwitterDate = DateTimeUtils.getTwitterDate(localDateString);
				String utcDateTimeStringInGenericFormat = DateTimeUtils
						.getUTCDateTimeStringInGenericFormat(localTwitterDate);

				DBObject findQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId);
				findQuery.put(MongoCollectionFieldNames.MONGO_USER_ID, userId);

				DirenajMongoDriverUtil.updateRequestInMongoByColumnName(orgBehaviourProcessInputData, findQuery,
						MongoCollectionFieldNames.MONGO_USER_CREATION_DATE, utcDateTimeStringInGenericFormat, "$set");
			}

			Logger.getLogger(getClass()).debug("orgBehaviourProcessInputData -  convertUTC completed");

			// convert date in similarity request
			DBCollection orgBehaviourRequestedSimilarityCalculations = DirenajMongoDriver.getInstance()
					.getOrgBehaviourRequestedSimilarityCalculations();
			DBCursor cursor2 = orgBehaviourRequestedSimilarityCalculations.find();

			while (cursor2.hasNext()) {
				DBObject next = cursor2.next();

				String requestId = (String) next.get(MongoCollectionFieldNames.MONGO_REQUEST_ID);

				String lowerTimeInterval = (String) next.get("lowerTimeInterval");
				String upperTimeInterval = (String) next.get("upperTimeInterval");

				if (!TextUtils.isEmpty(lowerTimeInterval)) {
					Date localTwitterDate = DateTimeUtils.getTwitterDate(lowerTimeInterval);
					String utcDateTimeStringInGenericFormat = DateTimeUtils
							.getUTCDateTimeStringInGenericFormat(localTwitterDate);

					DBObject findQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId);

					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(orgBehaviourRequestedSimilarityCalculations,
							findQuery, "lowerTimeInterval", utcDateTimeStringInGenericFormat, "$set");

				}
				if (!TextUtils.isEmpty(upperTimeInterval)) {
					Date localTwitterDate = DateTimeUtils.getTwitterDate(upperTimeInterval);
					String utcDateTimeStringInGenericFormat = DateTimeUtils
							.getUTCDateTimeStringInGenericFormat(localTwitterDate);

					DBObject findQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_REQUEST_ID, requestId);

					DirenajMongoDriverUtil.updateRequestInMongoByColumnName(orgBehaviourRequestedSimilarityCalculations,
							findQuery, "upperTimeInterval", utcDateTimeStringInGenericFormat, "$set");

				}

			}
			Logger.getLogger(getClass()).debug("orgBehaviourRequestedSimilarityCalculations -  convertUTC completed");
		} catch (Exception e) {
			Logger.getLogger(getClass()).error("Exception taken", e);
		}
	}

	public static void main(String[] args) throws Exception {
		new UTCDateConverter().convertUTC();
	}

}
