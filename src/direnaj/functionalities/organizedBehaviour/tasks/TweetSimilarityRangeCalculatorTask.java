package direnaj.functionalities.organizedBehaviour.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;

import direnaj.util.CosineSimilarityUtil;

public class TweetSimilarityRangeCalculatorTask implements Runnable {

	private CyclicBarrier cyclicBarrier;
	private BasicDBObject tweetTFIdfQueryObj;
	private String actualTweetId;
	private String actualTweetRetweetId;
	private ArrayList<String> queryTweetWords;
	private List<BasicDBObject> tfIdfList;
	private Map<String, Double> tweetWordTfIdfMap;
	private double tweetVectorLength;
	private Map<String, Double> similarityOfTweetWithOtherTweets;

	public TweetSimilarityRangeCalculatorTask(CyclicBarrier cyclicBarrier, BasicDBObject tweetTFIdfQueryObj,
			String actualTweetId, String actualTweetRetweetId, ArrayList<String> queryTweetWords,
			List<BasicDBObject> tfIdfList, Map<String, Double> tweetWordTfIdfMap, double tweetVectorLength,
			Map<String, Double> similarityOfTweetWithOtherTweets) {
		this.cyclicBarrier = cyclicBarrier;
		this.tweetTFIdfQueryObj = tweetTFIdfQueryObj;
		this.actualTweetId = actualTweetId;
		this.actualTweetRetweetId = actualTweetRetweetId;
		this.queryTweetWords = queryTweetWords;
		this.tfIdfList = tfIdfList;
		this.tweetWordTfIdfMap = tweetWordTfIdfMap;
		this.tweetVectorLength = tweetVectorLength;
		this.similarityOfTweetWithOtherTweets = similarityOfTweetWithOtherTweets;
	}

	@Override
	public void run() {
		try {
//			Logger.getLogger(TweetSimilarityRangeCalculatorTask.class)
//					.trace("TweetSimilarityRangeCalculatorTask starts execution.");
			CosineSimilarityUtil.calculateTweetSimilarityRangesInSingleThread(tweetTFIdfQueryObj, actualTweetId,
					actualTweetRetweetId, queryTweetWords, tfIdfList, tweetWordTfIdfMap, tweetVectorLength,
					similarityOfTweetWithOtherTweets);
//			Logger.getLogger(TweetSimilarityRangeCalculatorTask.class)
//					.trace("TweetSimilarityRangeCalculatorTask finished execution.");
			cyclicBarrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			Logger.getLogger(TweetSimilarityRangeCalculatorTask.class)
					.error("TweetSimilarityRangeCalculatorTask gets Exception", e);
		}

	}

}
