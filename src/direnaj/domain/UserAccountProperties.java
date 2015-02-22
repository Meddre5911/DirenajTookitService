package direnaj.domain;

import java.math.BigDecimal;

import direnaj.functionalities.sna.BotAccountCalculationWeight;

public class UserAccountProperties {

    // the number of external urls in tweets over the number of tweets posted by the account
    private double urlRatio;
    // the number of hashtags in tweets over the number of tweets posted by the account
    private double hashtagRatio;
    // the number of mentions in tweets over the number of tweets posted by the account
    private double mentionRatio;
    // follower_no / (follower + friend_no)
    private double friendFollowerRatio;
    // spam Url Count / all Posted Url count 
    private double spamLinkRatio;
    // the number of duplicate tweets over the number of tweets posted by the account
    private double duplicateTweetRatio;
    private boolean isEarlierThanMarch2007;
    private boolean isVerified;
    private boolean isProtected;

    public double getUrlRatio() {
        return urlRatio;
    }

    public void setUrlRatio(double urlRatio) {
        this.urlRatio = urlRatio;
    }

    public double getHashtagRatio() {
        return hashtagRatio;
    }

    public void setHashtagRatio(double hashtagRatio) {
        this.hashtagRatio = hashtagRatio;
    }

    public double getMentionRatio() {
        return mentionRatio;
    }

    public void setMentionRatio(double mentionRatio) {
        this.mentionRatio = mentionRatio;
    }

    public double getFriendFollowerRatio() {
        return friendFollowerRatio;
    }

    public void setFriendFollowerRatio(double friendFollowerRatio) {
        this.friendFollowerRatio = friendFollowerRatio;
    }

    public double getSpamLinkRatio() {
        return spamLinkRatio;
    }

    public void setSpamLinkRatio(double spamLinkRatio) {
        this.spamLinkRatio = spamLinkRatio;
    }

    public double getDuplicateTweetRatio() {
        return duplicateTweetRatio;
    }

    public void setDuplicateTweetRatio(double duplicateTweetRatio) {
        this.duplicateTweetRatio = duplicateTweetRatio;
    }

    public boolean isEarlierThanMarch2007() {
        return isEarlierThanMarch2007;
    }

    public void setEarlierThanMarch2007(boolean isEarlierThanMarch2007) {
        this.isEarlierThanMarch2007 = isEarlierThanMarch2007;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public BigDecimal calculateHumanProbability(BotAccountCalculationWeight botAccountCalculationWeight) {
        // initial zero probabilities
        BigDecimal verifiedUserProbability = BigDecimal.ZERO;
        BigDecimal protectedUserProbability = BigDecimal.ZERO;
        BigDecimal creationDateProbability = BigDecimal.ZERO;
        // calculate probabilities
        BigDecimal friendFollowerProbability = botAccountCalculationWeight.getFriendFollowerWeight().multiply(
                new BigDecimal(friendFollowerRatio));
        if (isVerified) {
            verifiedUserProbability = botAccountCalculationWeight.getVerifiedAccountWeight();
        } 
        if (isProtected) {
            protectedUserProbability = botAccountCalculationWeight.getProtectedAccountWeight();
        }
        if (isEarlierThanMarch2007) {
            creationDateProbability = botAccountCalculationWeight.getCreationDateWeight();
        }
        BigDecimal hashtagProbability = botAccountCalculationWeight.getHashtagRatioWeight().multiply(
                new BigDecimal(hashtagRatio));
        BigDecimal urlProbability = botAccountCalculationWeight.getUrlRatioWeight().multiply(new BigDecimal(urlRatio));
        BigDecimal mentionProbability = botAccountCalculationWeight.getMentionRatioWeight().multiply(
                new BigDecimal(mentionRatio));
        BigDecimal totalProbability = friendFollowerProbability.add(verifiedUserProbability)
                .add(protectedUserProbability).add(creationDateProbability).add(hashtagProbability).add(urlProbability)
                .add(mentionProbability);
        return totalProbability;
    }

}
