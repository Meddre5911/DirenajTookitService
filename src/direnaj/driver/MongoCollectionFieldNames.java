package direnaj.driver;

public interface MongoCollectionFieldNames {

	public static final String MONGO_REQUEST_ID = "requestId";
	public final static String MONGO_USER_ID = "userId";

	public static final String MONGO_TWEET_ID = "tweetId";
	public static final String MONGO_TWEET_TEXT = "tweetText";
	public static final String MONGO_IS_HASHTAG_TWEET = "isHashtagTweet";

	public static final String MONGO_WORD = "word";
	public static final String MONGO_WORD_TF = "TF";
	public static final String MONGO_WORD_IDF = "IDF";

	public static final String MONGO_WORD_TF_IDF_LIST = "WORD_TF_IDF_LIST";
	public static final String MONGO_WORD_TF_IDF_VALUE = "TF_IDF_Value";
	public static final String MONGO_WORD_TF_IDF_VALUE_SQUARE = "TF_IDF_Value_Square";

	public static final String MONGO_TOTAL_TWEET_COUNT = "TotalTweetCount";
	public static final String MONGO_ALL_TWEET_IDS = "AllTweetIds";
	public static final String MONGO_TWEET_WORDS = "TweetWords";

}
