package direnaj.functionalities.organizedBehaviour;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.driver.DirenajDriverVersion2;
import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.DirenajMongoDriverUtil;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.twitter.twitter4j.Twitter4jUtil;
import direnaj.twitter.twitter4j.external.DrenajCampaignStatus;
import direnaj.util.ListUtils;
import direnaj.util.NumberUtils;
import direnaj.util.TextUtils;
import twitter4j.Status;

public class CampaignCreator implements Runnable {

	private String campaignId;
	private String campaignDefinition;
	private String requestedHashtags;
	private String minDateStr;
	private String maxDateStr;
	private Gson gsonDeserializer;

	public CampaignCreator(String campaignId, String campaignDefinition, String requestedHashtags, String minDateStr,
			String maxDateStr) {
		super();
		this.campaignId = campaignId;
		this.campaignDefinition = campaignDefinition;
		this.requestedHashtags = requestedHashtags;
		this.minDateStr = minDateStr;
		this.maxDateStr = maxDateStr;
		gsonDeserializer = Twitter4jUtil.getGsonObject4Deserialization();
		// insert campaign record
		Twitter4jUtil.insertCampaignRecord(campaignId,
				campaignDefinition + " & Date Between : " + maxDateStr + " & " + minDateStr, requestedHashtags);
	}

	public void createCampaign() {
		try {
			Twitter4jUtil.createCampaign4PastTweets(campaignId, campaignDefinition, requestedHashtags, maxDateStr,
					minDateStr);
			calculateCampaignFeatures();
			updateCampaign();
		} catch (Exception e) {
			Logger.getLogger(CampaignCreator.class).error("createCampaign General Error.", e);
		}
	}

	private void updateCampaign() {
		Logger.getLogger(OrganizationDetector.class).debug("updateCampaign for campaignId : " + campaignId);
		DBCollection campaignsCollection = DirenajMongoDriver.getInstance().getCampaignsCollection();
		BasicDBObject findQuery = new BasicDBObject();
		findQuery.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId);
		BasicDBObject updateQuery = new BasicDBObject();
		updateQuery.append("$set", new BasicDBObject().append("tweetCollectionEnded", true));
		campaignsCollection.update(findQuery, updateQuery, true, false);
	}

	public void calculateCampaignFeatures() {
		Logger.getLogger(CampaignCreator.class).debug("calculateCampaignFeatures for campaignId : " + campaignId);
		// start analysis
		BasicDBObject query = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId);
		double totalTweetCount = DirenajMongoDriver.getInstance().getTweetsCollection().count(query);
		BasicDBObject query4RetweetCount = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId)
				.append("tweet.retweetedStatus", new BasicDBObject("$exists", true));
		double retweetedTweetCount = DirenajMongoDriver.getInstance().getTweetsCollection().count(query4RetweetCount);
		BasicDBObject query4ReplyCount = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId)
				.append("$or",
						ListUtils.getListOfObjects(
								new BasicDBObject().append("tweet.inReplyToStatusId", new BasicDBObject("$gt", 0)),
								new BasicDBObject().append("tweet.inReplyToUserId", new BasicDBObject("$gt", 0))));
		double replyTweetCount = DirenajMongoDriver.getInstance().getTweetsCollection().count(query4ReplyCount);
		BasicDBObject query4MentionCount = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId)
				.append("$where", "this.tweet.userMentionEntities.length >0");
		double mentionTweetCount = DirenajMongoDriver.getInstance().getTweetsCollection().count(query4MentionCount);
		double distinctUserCount = DirenajMongoDriver.getInstance().getTweetsCollection()
				.distinct("tweet.user.id", query).size();

		parseWordsInAllCampaign();

		// db.CampaignWords.aggregate([$match: {campaign_id: "CanliOncesi_1"},
		// $group: {_id:null, sum :{ $sum: "$wordCount" }}])
		double totalWordCount = getAllWordCount();
		double totalDistinctWordCount = DirenajMongoDriver.getInstance().getCampaignWordCollection()
				.distinct(MongoCollectionFieldNames.MONGO_WORD, query).size();

		Map<String, Double> wordFrequencies = calculateWordFrequencies();
		Map<String, Double> hashTagCounts = getHashTagCounts();

		BasicDBObject campaignStatistic = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId);
		campaignStatistic.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_TOTAL_TWEET_COUNT, totalTweetCount);
		campaignStatistic.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_RETWEET_COUNT,
				retweetedTweetCount + "-%" + NumberUtils.roundDouble((retweetedTweetCount * 100d) / totalTweetCount));
		campaignStatistic.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_REPLY_TWEET_COUNT,
				replyTweetCount + "-%" + NumberUtils.roundDouble((replyTweetCount * 100d) / totalTweetCount));
		campaignStatistic.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_MENTION_TWEET_COUNT,
				mentionTweetCount + "-%" + NumberUtils.roundDouble((mentionTweetCount * 100d) / totalTweetCount));
		campaignStatistic.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_DISTINCT_USER_TWEET_COUNT,
				distinctUserCount + "-" + NumberUtils.roundDouble(totalTweetCount / distinctUserCount));
		campaignStatistic.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_TOTAL_WORD_COUNT, totalWordCount);
		campaignStatistic.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_TOTAL_DISTINCT_WORD_COUNT, totalDistinctWordCount
				+ "-%" + (100d - NumberUtils.roundDouble((totalDistinctWordCount * 100d / totalWordCount))));
		campaignStatistic.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_WORD_FREQUENCIES, wordFrequencies);
		campaignStatistic.put(MongoCollectionFieldNames.MONGO_CAMPAIGN_HASHTAG_COUNTS, hashTagCounts);
		DirenajMongoDriver.getInstance().getCampaignStatisticsCollection().insert(campaignStatistic);
	}

	public void parseWordsInAllCampaign() {
		Logger.getLogger(CampaignCreator.class).debug("parseWordsInAllCampaign for campaignId : " + campaignId);
		List<DBObject> allCampaignWordRecords = new ArrayList<>(DirenajMongoDriver.getInstance().getBulkInsertSize());
		// get tweets first
		BasicDBObject query = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId);
		DBCursor campaignTweets = DirenajMongoDriver.getInstance().getTweetsCollection().find(query)
				.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		try {
			double totalTweetCount = 0;
			while (campaignTweets.hasNext()) {
				totalTweetCount++;
				Status userTweetObj = Twitter4jUtil.deserializeTwitter4jStatusFromGson(gsonDeserializer,
						campaignTweets.next().get("tweet").toString());
				// get tweet text
				String tweetText = TextUtils.getNotNullValue(userTweetObj.getText());
				String[] tweetWords = tweetText.split(" ");
				HashMap<String, Double> wordCounts = new HashMap<>();
				// count tweet words
				for (String word : tweetWords) {
					word = word.toLowerCase(Locale.US).trim();
					if (!TextUtils.isEmpty(word) && !StopWordsUtil.getInstance().getAllStopWords().contains(word)) {
						word = DirenajMongoDriverUtil.getSuitableColumnName(word);
						double wordCount = 0d;
						if (wordCounts.containsKey(word)) {
							wordCount = wordCounts.get(word);
						}
						wordCounts.put(word, ++wordCount);
					}
				}
				// normalize tweet word counts
				for (Entry<String, Double> wordCount : wordCounts.entrySet()) {
					DBObject campaignTweetWordCount = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID,
							campaignId);
					campaignTweetWordCount.put(MongoCollectionFieldNames.MONGO_WORD, wordCount.getKey());
					campaignTweetWordCount.put(MongoCollectionFieldNames.MONGO_WORD_COUNT, wordCount.getValue());
					allCampaignWordRecords.add(campaignTweetWordCount);
				}
				if (totalTweetCount % DirenajMongoDriver.getInstance().getBulkInsertSize() == 0) {
					allCampaignWordRecords = DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(
							DirenajMongoDriver.getInstance().getCampaignWordCollection(), allCampaignWordRecords,
							false);
				}
			}
			DirenajMongoDriverUtil.insertBulkData2CollectionIfNeeded(
					DirenajMongoDriver.getInstance().getCampaignWordCollection(), allCampaignWordRecords, true);

		} finally {
			campaignTweets.close();
		}
	}

	public Map<String, Double> calculateWordFrequencies() {
		Logger.getLogger(CampaignCreator.class).debug("calculateWordFrequencies for campaignId : " + campaignId);
		Map<String, Double> wordFrequenciesMap = new HashMap<>();
		BasicDBObject query = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId);

		AggregationOptions aggregationOptions = AggregationOptions.builder()
				.batchSize(DirenajMongoDriver.getInstance().getBulkInsertSize()).build();

		// db.CampaignWords.aggregate([{$group : {_id: "$word",wordCount:
		// {$sum:1}}} , {$sort: {'wordCount':-1}}])

		Cursor wordFrequencies = DirenajMongoDriver.getInstance().getCampaignWordCollection()
				.aggregate(Arrays.asList((DBObject) new BasicDBObject("$match", query),
						(DBObject) new BasicDBObject("$group",
								new BasicDBObject("_id", "$" + MongoCollectionFieldNames.MONGO_WORD).append(
										MongoCollectionFieldNames.MONGO_WORD_COUNT, new BasicDBObject("$sum", 1))),
				(DBObject) new BasicDBObject("$sort", new BasicDBObject("wordCount", -1))

		), aggregationOptions);
		try {
			while (wordFrequencies.hasNext()) {
				DBObject nextObject = wordFrequencies.next();
				String word = (String) nextObject.get("_id");
				Double wordCount = ((Integer) nextObject.get("wordCount")).doubleValue();
				wordFrequenciesMap.put(word, wordCount);
			}
		} finally {
			wordFrequencies.close();
		}
		return wordFrequenciesMap;
	}

	public Double getAllWordCount() {
		Logger.getLogger(CampaignCreator.class).debug("getAllWordCount for campaignId : " + campaignId);
		BasicDBObject query = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId);
		// prepare aggregation option
		AggregationOptions aggregationOptions = AggregationOptions.builder()
				.batchSize(DirenajMongoDriver.getInstance().getBulkInsertSize()).build();

		// get cursor
		// db.CampaignWords.aggregate([{$match: {campaign_id: "CanliOncesi_1"}},
		// {$group: {_id:null, sum :{ $sum: "$wordCount" }}}])
		Cursor allWordCount = DirenajMongoDriver.getInstance().getCampaignWordCollection().aggregate(
				Arrays.asList((DBObject) new BasicDBObject("$match", query),
						(DBObject) new BasicDBObject("$group",
								new BasicDBObject("_id", "null").append("sum", new BasicDBObject("$sum", 1)))),
				aggregationOptions);
		// get word count
		Double wordCount = 0d;
		try {
			while (allWordCount.hasNext()) {
				DBObject nextObject = allWordCount.next();
				wordCount = ((Integer) nextObject.get("sum")).doubleValue();
			}
		} finally {
			allWordCount.close();
		}
		return wordCount;
	}

	public Map<String, Double> getHashTagCounts() {
		Logger.getLogger(CampaignCreator.class).debug("getHashTagCounts for campaignId : " + campaignId);
		DirenajDriverVersion2 direnajDriver = new DirenajDriverVersion2();
		try {
			Map<String, Double> hashtagCounts = direnajDriver.getHashtagCounts(campaignId);
			return hashtagCounts;
		} catch (Exception e) {
			Logger.getLogger(CampaignCreator.class).error("Error in getHashTagCounts.", e);
			return null;
		}
	}

	public DrenajCampaignStatus deserializeDrenajCampaignStatusFromGson(Gson gson, String statusInJson) {
		try {
			DrenajCampaignStatus twitter4jStatus = (DrenajCampaignStatus) gson.fromJson(statusInJson,
					DrenajCampaignStatus.class);
			return twitter4jStatus;
		} catch (Exception e) {
			Logger.getLogger(CampaignCreator.class).error("Error during deserializing of tweet : \n" + statusInJson);
			throw e;
		}
	}

	@Override
	public void run() {
		createCampaign();
	}

}
