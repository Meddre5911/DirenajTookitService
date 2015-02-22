package direnaj.functionalities.sna;

import java.math.BigDecimal;

public class BotAccountCalculationWeight {

    private BigDecimal friendFollowerWeight;
    private BigDecimal verifiedAccountWeight;
    private BigDecimal protectedAccountWeight;
    private BigDecimal creationDateWeight;
    private BigDecimal hashtagRatioWeight;
    private BigDecimal urlRatioWeight;
    private BigDecimal mentionRatioWeight;

    public BigDecimal getFriendFollowerWeight() {
        return friendFollowerWeight;
    }

    public void setFriendFollowerWeight(BigDecimal friendFollowerWeight) {
        this.friendFollowerWeight = friendFollowerWeight;
    }

    public BigDecimal getVerifiedAccountWeight() {
        return verifiedAccountWeight;
    }

    public void setVerifiedAccountWeight(BigDecimal verifiedAccountWeight) {
        this.verifiedAccountWeight = verifiedAccountWeight;
    }

    public BigDecimal getProtectedAccountWeight() {
        return protectedAccountWeight;
    }

    public void setProtectedAccountWeight(BigDecimal protectedAccountWeight) {
        this.protectedAccountWeight = protectedAccountWeight;
    }

    public BigDecimal getCreationDateWeight() {
        return creationDateWeight;
    }

    public void setCreationDateWeight(BigDecimal creationDateWeight) {
        this.creationDateWeight = creationDateWeight;
    }

    public BigDecimal getHashtagRatioWeight() {
        return hashtagRatioWeight;
    }

    public void setHashtagRatioWeight(BigDecimal hashtagRatioWeight) {
        this.hashtagRatioWeight = hashtagRatioWeight;
    }

    public BigDecimal getUrlRatioWeight() {
        return urlRatioWeight;
    }

    public void setUrlRatioWeight(BigDecimal urlRatioWeight) {
        this.urlRatioWeight = urlRatioWeight;
    }

    public BigDecimal getMentionRatioWeight() {
        return mentionRatioWeight;
    }

    public void setMentionRatioWeight(BigDecimal mentionRatioWeight) {
        this.mentionRatioWeight = mentionRatioWeight;
    }

}
