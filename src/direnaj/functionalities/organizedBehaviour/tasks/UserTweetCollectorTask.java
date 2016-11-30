package direnaj.functionalities.organizedBehaviour.tasks;

import java.util.Date;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.apache.log4j.Logger;

import direnaj.domain.User;
import direnaj.twitter.twitter4j.Twitter4jUtil;

public class UserTweetCollectorTask implements Runnable {

	private CyclicBarrier cyclicBarrier;
	private boolean collectEarliestTweets;
	private User user;
	private Boolean calculateOnlyTopTrendDate;
	private Date minCampaignDate;
	private Date maxCampaignDate;
	private String campaignId;

	public UserTweetCollectorTask(CyclicBarrier cyclicBarrier, boolean collectEarliestTweets, User user,
			Boolean calculateOnlyTopTrendDate, Date minCampaignDate, Date maxCampaignDate, String campaignId) {
		this.cyclicBarrier = cyclicBarrier;
		this.collectEarliestTweets = collectEarliestTweets;
		this.user = user;
		this.calculateOnlyTopTrendDate = calculateOnlyTopTrendDate;
		this.minCampaignDate = minCampaignDate;
		this.maxCampaignDate = maxCampaignDate;
		this.campaignId = campaignId;
	}

	@Override
	public void run() {
		try {
			if (collectEarliestTweets) {
				Twitter4jUtil.getEarliestTweets(user, calculateOnlyTopTrendDate, minCampaignDate, campaignId);
				Logger.getLogger(Twitter4jUtil.class.getSimpleName())
						.debug("Earliest Tweets has been collected for User Name :" + user.getUserScreenName()
								+ ", User Id :" + user.getUserId());
			} else {
				Twitter4jUtil.getRecentTweets(user, calculateOnlyTopTrendDate, maxCampaignDate, campaignId);
				Logger.getLogger(Twitter4jUtil.class.getSimpleName())
						.debug("Recent Tweets has been collected for User Name :" + user.getUserScreenName()
								+ ", User Id :" + user.getUserId());
			}
			cyclicBarrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			Logger.getLogger(UserTweetCollectorTask.class).error("TweetSimilarityRangeCalculatorTask gets Exception",
					e);
		}

	}

}
