package direnaj.functionalities.organizedBehaviour;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.mongodb.BasicDBObject;

import direnaj.domain.ExtractedFeature;
import direnaj.util.TextUtils;

public class OrganizedBehaviourFeatureExtractor implements Runnable {

	private static final String FEATURE_EXTRACTION_PATH = "/home/direnaj/toolkit/toolkitExtractedFeatures/";

	private String[] featureExtractionIds;

	public OrganizedBehaviourFeatureExtractor(String[] featureExtractionIds) {
		this.featureExtractionIds = featureExtractionIds;
	}

	private void extractFeatures() {
		PrintWriter writer = null;
		try {
			String fileName = FEATURE_EXTRACTION_PATH + "extractedFeatures_" + TextUtils.getTimeStamp() + ".txt";
			writer = new PrintWriter(fileName, "UTF-8");
			boolean isKeyAdded = false;
			for (String requestId : featureExtractionIds) {
				StringBuilder trainingData = new StringBuilder();
				StringBuilder trainingDataKeys = new StringBuilder();
				try {
					BasicDBObject query = new BasicDBObject("requestId", requestId);

					ExtractedFeature userHashtagTweetCounts4Classification = FeatureExtractorUtil
							.extractUserRoughHashtagTweetCountsFeatures4Classification("RoughHashtagPostCounts", query);
					trainingData.append(userHashtagTweetCounts4Classification.getFeatureValue());
					trainingDataKeys.append(userHashtagTweetCounts4Classification.getFeatureKey());

					
					
					if (!isKeyAdded) {
						writer.println(trainingDataKeys.toString());
						isKeyAdded = true;
					}
					writer.println(trainingData.toString());

				} catch (JSONException e) {
					Logger.getLogger(OrganizedBehaviourFeatureExtractor.class)
							.error("Error in extractFeatures in OrganizedBehaviourFeatureExtractor for requestId : "
									+ requestId, e);
				}

			}
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {
			e1.printStackTrace();
			Logger.getLogger(OrganizedBehaviourFeatureExtractor.class)
					.error("General Error in OrganizedBehaviourFeatureExtractor", e1);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	@Override
	public void run() {
		extractFeatures();
	}

}
