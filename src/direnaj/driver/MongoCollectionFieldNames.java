package direnaj.driver;

public interface MongoCollectionFieldNames {

	public static final String MONGO_REQUEST_ID = "requestId";
	public final static String MONGO_USER_ID = "userId";

	public static final String MONGO_TWEET_ID = "tweetId";
	public static final String MONGO_RETWEETED_TWEET_ID = "retweetedTweetId";
	public static final String MONGO_TWEET_TEXT = "tweetText";
	public static final String MONGO_IS_HASHTAG_TWEET = "isHashtagTweet";
	public static final String MONGO_TWEET_CREATION_DATE = "tweetCreationDate";

	public static final String MONGO_TWEET_FOUND = "tweetFound";

	public static final String MONGO_WORD = "word";
	public static final String MONGO_WORD_TF = "TF";
	public static final String MONGO_WORD_COUNT = "wordCount";
	public static final String MONGO_WORD_IDF = "IDF";

	public static final String MONGO_WORD_TF_IDF_LIST = "WORD_TF_IDF_LIST";
	public static final String MONGO_WORD_TF_IDF_HASHMAP = "WORD_TF_IDF_HASHMAP";
	public static final String MONGO_WORD_TF_IDF_VALUE = "TF_IDF_Value";
	public static final String MONGO_WORD_TF_IDF_VALUE_SQUARE = "TF_IDF_Value_Square";
	public static final String MONGO_TWEET_SIMILARITY_WITH_OTHER_TWEETS = "SIMILARITY_WITH_OTHER_TWEETS";

	public static final String MONGO_TOTAL_TWEET_COUNT = "TotalTweetCount";
	public static final String MONGO_DISTINCT_USER_COUNT = "DistinctUserCount";
	public static final String MONGO_ALL_TWEET_IDS = "AllTweetIds";
	public static final String MONGO_ALL_USER_IDS = "AllUserIds";
	public static final String MONGO_TWEET_WORDS = "TweetWords";

	// Similarity Ranges
	// represents similarity between -1 & -1/2
	public static final String NON_SIMILAR = "NON_SIMILAR";
	// represents similarity between -1/2 & 0
	public static final String NOTR = "NOTR";
	// represents similarity between 0 & 1/2
	public static final String SLIGHTLY_SIMILAR = "SLIGHTLY_SIMILAR";
	// represents similarity between 1/2 & sqrt(2)/2
	public static final String SIMILAR = "SIMILAR";
	// represents similarity between sqrt(2)/2 & sqrt(3)/2
	public static final String VERY_SIMILAR = "VERY_SIMILAR";
	// represents similarity between sqrt(3)/2 & 1
	public static final String MOST_SIMILAR = "MOST_SIMILAR";

	// user variables
	public static final String MONGO_USER_POST_TWEET_ID = "postTweetId";

	public static final String MONGO_LATEST_TWEET_TIME = "latestTweetTimeInRequest";
	public static final String MONGO_EARLIEST_TWEET_TIME = "earliestTweetTimeInRequest";

	// Cosine Similarity Requests
	public static final String MONGO_RESUME_PROCESS = "resumeProcess";
	public static final String MONGO_RESUME_BREAKPOINT = "resumeBreakPoint";
	public static final String MONGO_GENARAL_SIMILARITY_CALCULATION = "calculateGeneralSimilarity";
	public static final String MONGO_HASHTAG_SIMILARITY_CALCULATION = "calculateHashTagSimilarity";
	public static final String MONGO_BYPASS_TWEET_COLLECTION = "bypassTweetCollection";

	// legacy direnaj
	public static final String MONGO_CAMPAIGN_ID = "campaign_id";
	public static final String MONGO_REQUEST_CAMPAIGN_ID = "campaignId";

	// campaign statistics
	public static final String MONGO_CAMPAIGN_TOTAL_TWEET_COUNT = "totalTweetCount";
	public static final String MONGO_CAMPAIGN_RETWEET_COUNT = "retweetedTweetCount";
	public static final String MONGO_CAMPAIGN_REPLY_TWEET_COUNT = "replyTweetCount";
	public static final String MONGO_CAMPAIGN_MENTION_TWEET_COUNT = "mentionTweetCount";
	public static final String MONGO_CAMPAIGN_DISTINCT_USER_COUNT = "distinctUserCount";
	public static final String MONGO_CAMPAIGN_TOTAL_WORD_COUNT = "totalWordCount";
	public static final String MONGO_CAMPAIGN_TOTAL_DISTINCT_WORD_COUNT = "totalDistinctWordCount";
	public static final String MONGO_CAMPAIGN_WORD_FREQUENCIES = "wordFrequencies";
	public static final String MONGO_CAMPAIGN_HASHTAG_COUNTS = "hashTagCounts";
	public static final String MONGO_CAMPAIGN_HASHTAG_VARIANCE = "hashtagVariance";
	public static final String MONGO_CAMPAIGN_TYPE = "campaign_type";
	public static final String MONGO_CAMPAIGN_RETWEET_COUNT_PERCENTAGE = "campaignRetweetPercentage";
	public static final String MONGO_CAMPAIGN_REPLY_TWEET_COUNT_PERCENTAGE = "campaignReplyPercentage";
	public static final String MONGO_CAMPAIGN_MENTION_TWEET_COUNT_PERCENTAGE =  "campaignMentionPercentage";
	public static final String MONGO_CAMPAIGN_TWEET_COUNT_PER_USER = "campaignTweetCountPerUser";
	public static final String MONGO_CAMPAIGN_TOTAL_DISTINCT_WORD_COUNT_PERCENTAGE = "campaignDistinctWordCountPercentage";
	public static final String MONGO_CAMPAIGN_HASHTAG_STANDARD_DEVIATION = "campaignHastagStandardDeviation";

	// preprocess input data
	public static final String MONGO_USER_CREATION_DATE = "creationDate";
	public static final String MONGO_USER_CREATION_DATE_IN_RATA_DIE = "creationDateInRataDie";
	public static final String MONGO_USER_SCREEN_NAME = "userScreenName";
	public static final String MONGO_USER_CLOSENESS_CENTRALITY = "closenessCentrality";
	public static final String MONGO_USER_FRIEND_FOLLOWER_RATIO = "friendFollowerRatio";
	public static final String MONGO_URL_RATIO = "urlRatio";
	public static final String MONGO_HASHTAG_RATIO = "hashtagRatio";
	public static final String MONGO_MENTION_RATIO = "mentionRatio";
	public static final String MONGO_RETWEET_RATIO = "retweetRatio";
	public static final String MONGO_MEDIA_RATIO = "mediaRatio";
	
	
	public static final String MONGO_TOTAL_TWEET_COUNT_DISTINCT_USER_RATIO = "TweetCountUserCountRatio";
	public static final String MONGO_USER_POST_TWITTER_DEVICE_RATIO = "postTwitterDeviceRatio";
	public static final String MONGO_USER_POST_MOBILE_DEVICE_RATIO = "postMobileDeviceRatio";
	public static final String MONGO_USER_THIRD_PARTY_DEVICE_RATIO = "postThirdPartyDeviceRatio";
	public static final String MONGO_USER_PROTECTED = "isProtected";
	public static final String MONGO_USER_VERIFIED = "isVerified";

	public static final String MONGO_USER_FAVORITE_COUNT = "favoriteCount";
	public static final String MONGO_USER_STATUS_COUNT = "statusCount";
	public static final String MONGO_USER_HASHTAG_POST_COUNT = "hashtagPostCount";
	public static final String MONGO_USER_DAILY_AVARAGE_POST_COUNT = "avarageDailyPostCount";

	// Cosine Similarity Request variables
	public static final String MONGO_COS_SIM_REQ_RATA_DIE_LOWER_TIME = "lowerTimeIntervalInRataDie";
	public static final String MONGO_COS_SIM_REQ_ORG_REQUEST_ID = "originalRequestId";

	// tweet
	public static final String MONGO_TWEET_HASHTAG_ENTITIES_TEXT = "hashtagEntities.text";
	// retweet
	public static final String MONGO_DISTINCT_RETWEET_USER_COUNT = "distinctRetweetUserCount";
	public static final String MONGO_DISTINCT_RETWEET_POST_COUNT = "distinctRetweetPostCount";
	public static final String MONGO_DISTINCT_RETWEET_USER_COUNT_PERCENTAGE = "distinctRetweetUserCountPercentage";
	public static final String MONGO_DISTINCT_RETWEET_USER_DIVIDED_BY_RATIO = "distinctRetweetUserDividedByRatio";
	public static final String MONGO_DISTINCT_RETWEET_RATIO = "distinctRetweetRatio";
	public static final String MONGO_DISTINCT_RETWEET_USER_RATIO = "distinctRetweetUserRatio";
	// non retweet
	public static final String MONGO_DISTINCT_NON_RETWEET_USER_COUNT = "distinctNonRetweetUserCount";
	public static final String MONGO_DISTINCT_NON_RETWEET_POST_COUNT = "distinctNonRetweetPostCount";
	public static final String MONGO_DISTINCT_NON_RETWEET_USER_COUNT_PERCENTAGE = "distinctNonRetweetUserCountPercentage";
	public static final String MONGO_DISTINCT_NON_RETWEET_USER_DIVIDED_BY_RATIO = "distinctNonRetweetUserDividedByRatio";
	public static final String MONGO_DISTINCT_NON_RETWEET_RATIO = "distinctNonRetweetRatio";
	public static final String MONGO_DISTINCT_NON_RETWEET_USER_RATIO = "distinctNonRetweetUserRatio";

	public static final String MONGO_LOWER_TIME_INTERVAL = "lowerTimeInterval";
	public static final String MONGO_UPPER_TIME_INTERVAL = "upperTimeInterval";
	public static final String MONGO_TOTAL_MENTION_USER_COUNT = "totalMentionUserCount";
	public static final String MONGO_DISTINCT_MENTION_COUNT = "distinctMentionCount";
	public static final String MONGO_TWEET_CREATED_AT = "createdAt";

	public static final String MONGO_USER_TWEET_AVERAGE_HASHTAG_DAYS = "tweetAvarageInHashtagDays";
	public static final String MONGO_TOTAL_RETWEET_COUNT = "totalRetweetCount";
	public static final String MONGO_TOTAL_DISTINCT_MENTION_RATIO = "totalDistinctMentionRatio";
	public static final String MONGO_TOTAL_RETWEET_COUNT_DISTINCT_RETWEET_COUNT_RATIO = "totalRetweetCountDistinctRetweetCountRatio";
	public static final String MONGO_USER_HASHTAG_DAY_AVARAGE_DAY_POST_COUNT_RATIO = "userHashtagDayAverageDayPostCountRatio";
	// mention features
	public static final String MONGO_RETWEETED_MENTION_USER_COUNT = "retweetedMentionCount";
	public static final String MONGO_DISTINCT_RETWEETED_MENTION_USER_COUNT = "distinctRetweetedMentionUserCount";
	public static final String MONGO_RETWEETED_MENTION_DISTINCT_MENTION_RATIO = "retweetedTotalDistinctMentionRatio";
	public static final String MONGO_NON_RETWEETED_MENTION_USER_COUNT = "nonRetweetedMentionCount";
	public static final String MONGO_DISTINCT_NON_RETWEETED_MENTION_USER_COUNT = "nonRetweetedDistinctMentionCount";
	public static final String MONGO_NON_RETWEETED_MENTION_DISTINCT_MENTION_RATIO = "nonRetweetedTotalDistinctMentionRatio";
	
	
	
	
	

}
