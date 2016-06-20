package direnaj.twitter.twitter4j.external;

import twitter4j.Status;

public class DrenajCampaignStatus {

	private String retrieved_by;
	private String campaign_id;
	private double record_retrieved_at;
	private double drenaj_service_version;
	private Status tweet;
	
	public DrenajCampaignStatus(String campaignId,Status status,double recordRetrievalTime) {
		this.retrieved_by = "drenaj_toolkit";
		this.drenaj_service_version= 1.1d;
		this.campaign_id = campaignId;
		this.record_retrieved_at = recordRetrievalTime;
		this.tweet = status;
	}

	public String getRetrieved_by() {
		return retrieved_by;
	}

	public String getCampaign_id() {
		return campaign_id;
	}

	public double getRecord_retrieved_at() {
		return record_retrieved_at;
	}

	public double getDrenaj_service_version() {
		return drenaj_service_version;
	}

	public Status getTweet() {
		return tweet;
	}

	public void setRetrieved_by(String retrieved_by) {
		this.retrieved_by = retrieved_by;
	}

	public void setCampaign_id(String campaign_id) {
		this.campaign_id = campaign_id;
	}

	public void setRecord_retrieved_at(double record_retrieved_at) {
		this.record_retrieved_at = record_retrieved_at;
	}

	public void setDrenaj_service_version(double drenaj_service_version) {
		this.drenaj_service_version = drenaj_service_version;
	}

	public void setTweet(Status status) {
		this.tweet = status;
	}
}
