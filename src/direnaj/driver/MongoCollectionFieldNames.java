package direnaj.driver;

public interface MongoCollectionFieldNames {

	public static final String MONGO_REQUEST_ID = "requestId";
	public final static String MONGO_USER_ID = "userId";

	public static final String MONGO_TWEET_ID = "tweetId";
	public static final String MONGO_TWEET_TEXT = "tweetText";
	public static final String MONGO_IS_HASHTAG_TWEET = "isHashtagTweet";
	public static final String MONGO_TWEET_CREATION_DATE = "tweetCreationDate";

	public static final String MONGO_WORD = "word";
	public static final String MONGO_WORD_TF = "TF";
	public static final String MONGO_WORD_IDF = "IDF";

	public static final String MONGO_WORD_TF_IDF_LIST = "WORD_TF_IDF_LIST";
	public static final String MONGO_WORD_TF_IDF_HASHMAP = "WORD_TF_IDF_HASHMAP";
	public static final String MONGO_WORD_TF_IDF_VALUE = "TF_IDF_Value";
	public static final String MONGO_WORD_TF_IDF_VALUE_SQUARE = "TF_IDF_Value_Square";
	public static final String MONGO_TWEET_SIMILARITY_WITH_OTHER_TWEETS = "SIMILARITY_WITH_OTHER_TWEETS";

	public static final String MONGO_TOTAL_TWEET_COUNT = "TotalTweetCount";
	public static final String MONGO_ALL_TWEET_IDS = "AllTweetIds";
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
	
	
}
