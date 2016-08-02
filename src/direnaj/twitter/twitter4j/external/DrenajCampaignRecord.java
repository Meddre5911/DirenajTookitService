package direnaj.twitter.twitter4j.external;

import java.util.Date;

import direnaj.util.DateTimeUtils;

public class DrenajCampaignRecord {

	// > db.campaigns.findOne({"campaign_id":"Influencers_SM_Influencers"})
	// {
	// "_id" : ObjectId("56ab63b3009a0647fba1774d"),
	// "campaign_type" : "streaming",
	// "description" : "Test to fetch influencers",
	// "created_at" : 736358.5457576335,
	// "campaign_id" : "Influencers_SM_Influencers",
	// "query_terms" : "@MakerFaireRome, @DigitalChampITA, @make_in_italy,
	// @chefuturo, @stefanomicelli, @RiccardoLuna, @Internetfest, @lauradebe,
	// @vectorealism, @zoescope, @MakeTankItaly, @SEEDSandCHIPS, @3DHubs,
	// @riotta, @wireditalia"
	// }

	private String campaign_type;
	private String description;
	private double created_at;
	private String campaign_id;
	private String query_terms;
	private boolean tweetCollectionEnded;
	private Date minCampaignDate;
	private Date maxCampaignDate;

	public DrenajCampaignRecord(String campaign_type, String description, double created_at, String campaign_id,
			String query_terms, String minDateStr, String maxDateStr) throws Exception {
		super();
		this.campaign_type = campaign_type;
		this.description = description;
		this.created_at = created_at;
		this.campaign_id = campaign_id;
		this.query_terms = query_terms;
		setMinCampaignDate(DateTimeUtils.getDate("yyyy-MM-dd HH:mm", minDateStr.trim() + " 00:00"));
		setMaxCampaignDate(DateTimeUtils.getDate("yyyy-MM-dd HH:mm", maxDateStr.trim() + " 23:59"));
		setTweetCollectionEnded(false);
	}

	public String getCampaign_type() {
		return campaign_type;
	}

	public void setCampaign_type(String campaign_type) {
		this.campaign_type = campaign_type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getCreated_at() {
		return created_at;
	}

	public void setCreated_at(double created_at) {
		this.created_at = created_at;
	}

	public String getCampaign_id() {
		return campaign_id;
	}

	public void setCampaign_id(String campaign_id) {
		this.campaign_id = campaign_id;
	}

	public String getQuery_terms() {
		return query_terms;
	}

	public void setQuery_terms(String query_terms) {
		this.query_terms = query_terms;
	}

	public boolean isTweetCollectionEnded() {
		return tweetCollectionEnded;
	}

	public void setTweetCollectionEnded(boolean tweetCollectionEnded) {
		this.tweetCollectionEnded = tweetCollectionEnded;
	}

	public Date getMinCampaignDate() {
		return minCampaignDate;
	}

	public void setMinCampaignDate(Date minCampaignDate) {
		this.minCampaignDate = minCampaignDate;
	}

	public Date getMaxCampaignDate() {
		return maxCampaignDate;
	}

	public void setMaxCampaignDate(Date maxCampaignDate) {
		this.maxCampaignDate = maxCampaignDate;
	}

}
