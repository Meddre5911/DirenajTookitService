package testPackage.organizedBehaviors;

import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.twitter.twitter4j.Twitter4jUtil;
import twitter4j.User;

public class PreProcessUserTest {

	public static void main(String[] args) throws Exception {

		// delete object
		// DirenajMongoDriver.getInstance().getOrgBehaviorRequestCollection().remove(new
		// BasicDBObject());
		// DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations().remove(new
		// BasicDBObject());
		//
		//
		// DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData().remove(new
		// BasicDBObject());
		// DirenajMongoDriver.getInstance().getOrgBehaviourTweetsOfRequest().remove(new
		// BasicDBObject());
		//
		// DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets().remove(new
		// BasicDBObject());
		//
		// DirenajMongoDriver.getInstance().getOrgBehaviorPreProcessUsers().remove(new
		// BasicDBObject());
		// DirenajMongoDriver.getInstance().getOrgBehaviourProcessCosSimilarityIDF().remove(new
		// BasicDBObject());
		//
		// DirenajMongoDriver.getInstance().getOrgBehaviourCosSimilarityTF().remove(new
		// BasicDBObject());
		// DirenajMongoDriver.getInstance().getOrgBehaviourTweetsShortInfo().remove(new
		// BasicDBObject());
		// DirenajMongoDriver.getInstance().getOrgBehaviourProcessCosSimilarityTF_IDF().remove(new
		// BasicDBObject());
		// DirenajMongoDriver.getInstance().getOrgBehaviourProcessTweetSimilarity().remove(new
		// BasicDBObject());
		// DirenajMongoDriver.getInstance().getOrgBehaviourRequestedSimilarityCalculations().remove(new
		// BasicDBObject());

		// DirenajMongoDriver.getInstance().getOrgBehaviorPreProcessUsers()
		// .remove(queryObject);
		// insertPreProcessUser2Collection("4345534", createTestUser());
		// insertPreProcessUser2Collection("4345534", createTestUser());

		// BasicDBObject updateQuery = new BasicDBObject();
		// updateQuery.append("$addToSet", new
		// BasicDBObject().append("multiDeneme", "c"));
		//
		//
		// WriteResult updateMulti =
		// DirenajMongoDriver.getInstance().getOrgBehaviorPreProcessUsers().updateMulti(queryObject,updateQuery
		// );
		// updateMulti.getN();

		// DBObject distinctRetweetUserQuery = new
		// BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID,
		// "NBAwards")
		// .append("$where", "this.hashtagEntities.length > 0")
		// .append(MongoCollectionFieldNames.MONGO_TWEET_HASHTAG_ENTITIES_TEXT,
		// new BasicDBObject("$regex", "NBAwards").append("$options", "i"))
		// .append("retweetedStatus.id", new BasicDBObject("$exists", true));
		//
		// int distinctRetweetUserCount =
		// DirenajMongoDriver.getInstance().getOrgBehaviourUserTweets()
		// .distinct("user.id", distinctRetweetUserQuery).size();

		// db.tweets.aggregate(
		// [
		// {$match : {$and:
		// [{"tweet.hashtagEntities.text":{"$regex":"NBAChamps","$options":"i"}},
		// {campaign_id: {$in :["20160622_Deneme2","20160622_Deneme3"]}}]}
		// },
		// {$group : { _id: "$tweet.user.id",
		// grouping: {$addToSet: "$campaign_id"} }
		// },
		// {$match : {$and:[ { "grouping": "20160622_Deneme3"},{"grouping":
		// "20160622_Deneme2"} ]}},
		// { $project : { _id : 1 } }
		// ]
		// )

//		// prepare first match element
//		BasicDBObject hashtagQuery = new BasicDBObject().append("tweet.hashtagEntities.text",
//				new BasicDBObject("$regex", "NBAChamps").append("$options", "i"));
//		BasicDBObject campaignIdQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID,
//				"20160622_Deneme2");
//		BasicDBObject matchQuery4Initial = new BasicDBObject();
//		matchQuery4Initial.append("$and", Arrays.asList(hashtagQuery, campaignIdQuery));
//
//		BasicDBObject comparedHashtagQuery = new BasicDBObject().append("tweet.hashtagEntities.text",
//				new BasicDBObject("$regex", "NBAChamps").append("$options", "i"));
//		BasicDBObject comparedCampaignIdQuery = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID,
//				"20160622_Deneme3");
//		BasicDBObject comparedMatchQuery4Initial = new BasicDBObject();
//		comparedMatchQuery4Initial.append("$and", Arrays.asList(comparedHashtagQuery, comparedCampaignIdQuery));
//
//		DBObject comparedQuery  = new BasicDBObject("$or",Arrays.asList(matchQuery4Initial,comparedMatchQuery4Initial));
//		
//		
//		DBObject initialMatchElement  =(DBObject) new BasicDBObject("$match", comparedQuery);
//		// prepare group element
//		DBObject groupElement = (DBObject) new BasicDBObject("$group", new BasicDBObject("_id", "$tweet.user.id")
//				.append("grouping", new BasicDBObject("$addToSet", "$campaign_id")));
//		// prepare match element after grouping
//		BasicDBObject query4FirstCampaign = new BasicDBObject("grouping", "20160622_Deneme2");
//		BasicDBObject query4SecondCampaign = new BasicDBObject("grouping", "20160622_Deneme3");
//		BasicDBObject matchQuery4Group = new BasicDBObject("$and",
//				Arrays.asList(query4FirstCampaign, query4SecondCampaign));
//		DBObject matchElementAfterGroup = new BasicDBObject("$match", matchQuery4Group);
//		// prepare project element
//		DBObject projectionElement = new BasicDBObject("$project", new BasicDBObject("_id", 1));
//		List<DBObject> wholeAggregationQuery = Arrays.asList(initialMatchElement, groupElement, matchElementAfterGroup,
//				projectionElement);
//
//		AggregationOptions aggregationOptions = AggregationOptions.builder().batchSize(50)
//				.outputMode(AggregationOptions.OutputMode.CURSOR).build();
//
//		Cursor commonUsers = DirenajMongoDriver.getInstance().getTweetsCollection().aggregate(wholeAggregationQuery,
//				aggregationOptions);
//
//		while (commonUsers.hasNext()) {
//			DBObject next = commonUsers.next();
//			
//			System.out.println("Result : " + Long.valueOf(String.valueOf(next.get("_id"))));
//		}
//		
		
		DBObject userRetrievalQuery = new BasicDBObject();
		userRetrievalQuery.put("tweet.user.id", 27375420);
		userRetrievalQuery.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, "20160622_Deneme3");

		BasicDBObject projectionKey = new BasicDBObject("tweet.user",1);
		DBObject queryResult = DirenajMongoDriver.getInstance().getTweetsCollection()
				.findOne(userRetrievalQuery, projectionKey);

		
		JSONObject jsonObject = new JSONObject(queryResult.get("tweet").toString());
		String string = jsonObject.get("user").toString();
		User user = Twitter4jUtil.deserializeTwitter4jUserFromGson(Twitter4jUtil.getGsonObject4Deserialization(),
				string);
		
		System.out.println(user);

	}
}