package direnaj.functionalities.organizedBehaviour.main;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.functionalities.organizedBehaviour.CampaignComparer;

public class CampaignSetComparer {

	public static void main(String[] args) {

		try {

			String actualComparisonDefinitionInitial = args[0];
			String comparedComparisonDefinitionInitial = args[1];

			Logger.getLogger(CampaignSetComparer.class).debug("Different Comparisons will be compared ! ");
			Logger.getLogger(CampaignSetComparer.class).debug("Comparison Initials are : "
					+ actualComparisonDefinitionInitial + " - " + comparedComparisonDefinitionInitial);

			List<Entry<String, String>> comparedCampaignHashtagInfo = new ArrayList<>();
			List<Entry<String, String>> actualCampaignHashtagInfo = new ArrayList<>();

			DBCursor allComparisons = DirenajMongoDriver.getInstance().getOrgBehaviourCampaignComparisons().find();

			while (allComparisons.hasNext()) {
				DBObject comparisonObject = allComparisons.next();
				String requestDefinition = (String) comparisonObject
						.get(MongoCollectionFieldNames.MONGO_COMPARISON_REQUEST_DEFINITION);
				if (requestDefinition.startsWith(actualComparisonDefinitionInitial)) {
					retrieveCampaignHashtagInfo(comparisonObject, actualCampaignHashtagInfo);
				} else if (requestDefinition.startsWith(comparedComparisonDefinitionInitial)) {
					retrieveCampaignHashtagInfo(comparisonObject, comparedCampaignHashtagInfo);
				}
			}

			ExecutorService executorService = Executors.newFixedThreadPool(5);

			for (Entry<String, String> entrySet : actualCampaignHashtagInfo) {
				try {
					String actualCampaignId = entrySet.getKey();
					String actualHashtag = entrySet.getValue();

					String cartesianRequestDefinition = "SC_" + actualComparisonDefinitionInitial + "_"
							+ comparedComparisonDefinitionInitial + "_" + actualCampaignId + "_" + actualHashtag
							+ "_Analysis";

					CampaignComparer campaignComparer = new CampaignComparer(actualCampaignId, actualHashtag,
							comparedCampaignHashtagInfo, cartesianRequestDefinition);

					executorService.submit(campaignComparer);
				} catch (Exception e) {
					Logger.getLogger(CampaignSetComparer.class).error("Exception taken.", e);
				}
			}
			executorService.shutdown();
			while (!executorService.isTerminated()) {
			}

			try {
				executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				Logger.getLogger(CampaignSetComparer.class).error(e);
			}
		} catch (Exception e) {
			Logger.getLogger(CampaignSetComparer.class).error("General Error.", e);
		}
	}

	private static void retrieveCampaignHashtagInfo(DBObject comparisonObject,
			List<Entry<String, String>> comparedCampaignHashtagInfo) {
		String campaignId = (String) comparisonObject.get(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID);
		String hashtag = (String) comparisonObject.get(MongoCollectionFieldNames.MONGO_COMPARISON_ACTUAL_HASHTAG);
		Entry<String, String> entry = new AbstractMap.SimpleEntry<String, String>(campaignId.trim(), hashtag.trim());
		comparedCampaignHashtagInfo.add(entry);
	}

}
