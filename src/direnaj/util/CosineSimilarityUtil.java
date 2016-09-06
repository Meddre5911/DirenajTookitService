package direnaj.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;

import direnaj.driver.MongoCollectionFieldNames;

public class CosineSimilarityUtil {

	/**
	 * @param tfIdfList
	 * 
	 *            An example tfIdf object
	 * 
	 *            DBObject tfIdfValues = new
	 *            BasicDBObject(MongoCollectionFieldNames.MONGO_WORD, word)
	 *            .append(MongoCollectionFieldNames.MONGO_WORD_TF_IDF_VALUE,
	 *            wordTfIdfValue) .append(MongoCollectionFieldNames.
	 *            MONGO_WORD_TF_IDF_VALUE_SQUARE, wordTfIdfValue *
	 *            wordTfIdfValue);
	 * 
	 * @return
	 */
	public static double calculateVectorLength(List<BasicDBObject> tfIdfList) {
		double vectorLength = 0d;
		for (BasicDBObject wordTfIdfValue : tfIdfList) {
			vectorLength += (double) wordTfIdfValue.get(MongoCollectionFieldNames.MONGO_WORD_TF_IDF_VALUE_SQUARE);
		}
		return Math.sqrt(vectorLength);
	}

	public static double calculateVectorLengthBasedOnComparedWordList(List<String> wordList2Compare,
			List<BasicDBObject> tfIdfList2Compute) {
		double vectorLength = 0d;
		for (BasicDBObject wordTfIdfValue : tfIdfList2Compute) {
			String word = (String) wordTfIdfValue.get(MongoCollectionFieldNames.MONGO_WORD);
			if (!TextUtils.isEmpty(word) && wordList2Compare.contains(word)) {
				vectorLength += (double) wordTfIdfValue.get(MongoCollectionFieldNames.MONGO_WORD_TF_IDF_VALUE_SQUARE);
			}
		}
		return Math.sqrt(vectorLength);
	}

	public static double calculateDotProduct(ArrayList<String> queryTweetWords, Map<String, Double> tweetWordTfIdfMap,
			Map<String, Double> comparedTweetWordTfIdfMap) {
		double dotProduct = 0d;
		for (String tweetWord : queryTweetWords) {
			if (comparedTweetWordTfIdfMap.containsKey(tweetWord)) {
				dotProduct += (tweetWordTfIdfMap.get(tweetWord) * comparedTweetWordTfIdfMap.get(tweetWord));
			}
		}
		return dotProduct;
	}

	public static Map<String, Double> getEmptyMap4SimilarityDecisionTree() {
		Map<String, Double> similarityDecisionTree = new HashMap<>();
		similarityDecisionTree.put(MongoCollectionFieldNames.NON_SIMILAR, 0d);
		similarityDecisionTree.put(MongoCollectionFieldNames.SLIGHTLY_SIMILAR, 0d);
		similarityDecisionTree.put(MongoCollectionFieldNames.SIMILAR, 0d);
		similarityDecisionTree.put(MongoCollectionFieldNames.VERY_SIMILAR, 0d);
		similarityDecisionTree.put(MongoCollectionFieldNames.MOST_SIMILAR, 0d);
		return similarityDecisionTree;
	}

	public static void addSimilarities2General(Map<String, Double> similarityComparisonOfAllTweets,
			Map<String, Double> similarityOfTweetWithOtherTweets, double allTweetCount) {
		if (allTweetCount > 1d) {
			allTweetCount = allTweetCount - 1;
		}

		similarityComparisonOfAllTweets.put(MongoCollectionFieldNames.NON_SIMILAR, similarityComparisonOfAllTweets
				.get(MongoCollectionFieldNames.NON_SIMILAR)
				+ (similarityOfTweetWithOtherTweets.get(MongoCollectionFieldNames.NON_SIMILAR) / allTweetCount));

		similarityComparisonOfAllTweets.put(MongoCollectionFieldNames.SLIGHTLY_SIMILAR, similarityComparisonOfAllTweets
				.get(MongoCollectionFieldNames.SLIGHTLY_SIMILAR)
				+ (similarityOfTweetWithOtherTweets.get(MongoCollectionFieldNames.SLIGHTLY_SIMILAR) / allTweetCount));

		similarityComparisonOfAllTweets.put(MongoCollectionFieldNames.SIMILAR,
				similarityComparisonOfAllTweets.get(MongoCollectionFieldNames.SIMILAR)
						+ (similarityOfTweetWithOtherTweets.get(MongoCollectionFieldNames.SIMILAR) / allTweetCount));

		similarityComparisonOfAllTweets.put(MongoCollectionFieldNames.VERY_SIMILAR, similarityComparisonOfAllTweets
				.get(MongoCollectionFieldNames.VERY_SIMILAR)
				+ (similarityOfTweetWithOtherTweets.get(MongoCollectionFieldNames.VERY_SIMILAR) / allTweetCount));

		similarityComparisonOfAllTweets.put(MongoCollectionFieldNames.MOST_SIMILAR, similarityComparisonOfAllTweets
				.get(MongoCollectionFieldNames.MOST_SIMILAR)
				+ (similarityOfTweetWithOtherTweets.get(MongoCollectionFieldNames.MOST_SIMILAR) / allTweetCount));
	}

	public static void calculateAvarage(Map<String, Double> similarityComparisonOfAllTweets, double allTweetCount) {
		if (allTweetCount > 1d) {
			allTweetCount = allTweetCount - 1;
		} else if (allTweetCount == 0d) {
			allTweetCount = 1;
		}
		similarityComparisonOfAllTweets.put(MongoCollectionFieldNames.NON_SIMILAR,
				similarityComparisonOfAllTweets.get(MongoCollectionFieldNames.NON_SIMILAR) / allTweetCount);

		similarityComparisonOfAllTweets.put(MongoCollectionFieldNames.SLIGHTLY_SIMILAR,
				similarityComparisonOfAllTweets.get(MongoCollectionFieldNames.SLIGHTLY_SIMILAR) / allTweetCount);

		similarityComparisonOfAllTweets.put(MongoCollectionFieldNames.SIMILAR,
				similarityComparisonOfAllTweets.get(MongoCollectionFieldNames.SIMILAR) / allTweetCount);

		similarityComparisonOfAllTweets.put(MongoCollectionFieldNames.VERY_SIMILAR,
				similarityComparisonOfAllTweets.get(MongoCollectionFieldNames.VERY_SIMILAR) / allTweetCount);

		similarityComparisonOfAllTweets.put(MongoCollectionFieldNames.MOST_SIMILAR,
				similarityComparisonOfAllTweets.get(MongoCollectionFieldNames.MOST_SIMILAR) / allTweetCount);
	}

	public static void findTweetSimilarityRange(Map<String, Double> similarityOfTweetWithOtherTweets, double dotProduct,
			double tweetVectorLength, double comparedTweetVectorLength) {
		String similarityKey = "";
		double cosSimilarity = 0d;
		try {
			if (tweetVectorLength == 0d || comparedTweetVectorLength == 0d) {
				similarityKey = MongoCollectionFieldNames.NON_SIMILAR;
			} else {
				cosSimilarity = dotProduct / (tweetVectorLength * comparedTweetVectorLength);
				if (cosSimilarity == 0d) {
					similarityKey = MongoCollectionFieldNames.NON_SIMILAR;
				} else if (cosSimilarity < 1d / 2d) {
					similarityKey = MongoCollectionFieldNames.SLIGHTLY_SIMILAR;
				} else if (cosSimilarity < Math.sqrt(2d) / 2d) {
					similarityKey = MongoCollectionFieldNames.SIMILAR;
				} else if (cosSimilarity < Math.sqrt(3d) / 2d) {
					similarityKey = MongoCollectionFieldNames.VERY_SIMILAR;
				} else {
					similarityKey = MongoCollectionFieldNames.MOST_SIMILAR;
				}
			}
			Double similarTweetCount = similarityOfTweetWithOtherTweets.get(similarityKey);
			similarityOfTweetWithOtherTweets.put(similarityKey, ++similarTweetCount);
		} catch (NullPointerException e) {
			Logger.getLogger(CosineSimilarityUtil.class)
					.error("Error in findTweetSimilarityRange. Dot Product : " + dotProduct + " - TweetVectorLength : "
							+ tweetVectorLength + " - ComparedTweetVectorLength : " + comparedTweetVectorLength
							+ " - CosSimilarity : " + cosSimilarity, e);
		}
	}

}
