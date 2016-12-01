package direnaj.domain;

import java.io.Serializable;

public class ComparisonData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String campaign_id;
	private String hashtag;
	private Double sameUserPercentage4ActualCampaign;
	private Double sameUserPercentage4ComparedCampaign;
	private Double sameUserCount;
	private Double totalComparedUserCount4ActualCampaign;
	private Double totalComparedUserCount4ComparedCampaign;
	private String requestId;

	public ComparisonData() {
	}

	public ComparisonData(String campaignId, String hashtag, Double sameUserPercentage4ActualCampaign, String requestId,
			Double sameUserCount, Double totalComparedUserCount4ActualCampaign,
			Double sameUserPercentage4ComparedCampaign, Double totalComparedUserCount4ComparedCampaign) {
		this.setCampaign_id(campaignId);
		this.hashtag = hashtag;
		this.setSameUserPercentage4ActualCampaign(sameUserPercentage4ActualCampaign);
		this.requestId = requestId;
		this.sameUserCount = sameUserCount;
		this.setTotalComparedUserCount4ActualCampaign(totalComparedUserCount4ActualCampaign);
		this.sameUserPercentage4ComparedCampaign = sameUserPercentage4ComparedCampaign;
		this.totalComparedUserCount4ComparedCampaign = totalComparedUserCount4ComparedCampaign;
	}

	public String getHashtag() {
		return hashtag;
	}

	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getCampaign_id() {
		return campaign_id;
	}

	public void setCampaign_id(String campaign_id) {
		this.campaign_id = campaign_id;
	}

	public Double getSameUserCount() {
		return sameUserCount;
	}

	public void setSameUserCount(Double sameUserCount) {
		this.sameUserCount = sameUserCount;
	}

	public Double getSameUserPercentage4ActualCampaign() {
		return sameUserPercentage4ActualCampaign;
	}

	public void setSameUserPercentage4ActualCampaign(Double sameUserPercentage4ActualCampaign) {
		this.sameUserPercentage4ActualCampaign = sameUserPercentage4ActualCampaign;
	}

	public Double getSameUserPercentage4ComparedCampaign() {
		return sameUserPercentage4ComparedCampaign;
	}

	public void setSameUserPercentage4ComparedCampaign(Double sameUserPercentage4ComparedCampaign) {
		this.sameUserPercentage4ComparedCampaign = sameUserPercentage4ComparedCampaign;
	}

	public Double getTotalComparedUserCount4ActualCampaign() {
		return totalComparedUserCount4ActualCampaign;
	}

	public void setTotalComparedUserCount4ActualCampaign(Double totalComparedUserCount4ActualCampaign) {
		this.totalComparedUserCount4ActualCampaign = totalComparedUserCount4ActualCampaign;
	}

	public Double getTotalComparedUserCount4ComparedCampaign() {
		return totalComparedUserCount4ComparedCampaign;
	}

	public void setTotalComparedUserCount4ComparedCampaign(Double totalComparedUserCount4ComparedCampaign) {
		this.totalComparedUserCount4ComparedCampaign = totalComparedUserCount4ComparedCampaign;
	}

}
