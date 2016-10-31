package direnaj.twitter.twitter4j.external;

import java.util.Arrays;
import java.util.Date;

import twitter4j.ExtendedMediaEntity;
import twitter4j.ExtendedMediaEntityJSONImpl;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.HashtagEntityJSONImpl;
import twitter4j.MediaEntity;
import twitter4j.MediaEntityJSONImpl;
import twitter4j.Place;
import twitter4j.PlaceJSONImpl;
import twitter4j.RateLimitStatus;
import twitter4j.Scopes;
import twitter4j.ScopesImpl;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.URLEntityJSONImpl;
import twitter4j.User;
import twitter4j.UserJSONImpl;
import twitter4j.UserMentionEntity;
import twitter4j.UserMentionEntityJSONImpl;

/**
 * A data class representing one single status of a user.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class DrenajStatusJSONImpl implements Status, java.io.Serializable {
	private static final long serialVersionUID = -6461195536943679985L;

	private Date createdAt;
	private long id;
	private String text;
	private String source;
	private boolean isTruncated;
	private long inReplyToStatusId;
	private long inReplyToUserId;
	private boolean isFavorited;
	private boolean isRetweeted;
	private int favoriteCount;
	private String inReplyToScreenName;
	private GeoLocation geoLocation = null;
	private PlaceJSONImpl place = null;
	// this field should be int in theory, but left as long for the serialized
	// form compatibility - TFJ-790
	private long retweetCount;
	private boolean isPossiblySensitive;
	private String lang;

	private long[] contributorsIDs;

	private DrenajStatusJSONImpl retweetedStatus;
	private UserMentionEntityJSONImpl[] userMentionEntities;
	private URLEntityJSONImpl[] urlEntities;
	private HashtagEntityJSONImpl[] hashtagEntities;
	private MediaEntityJSONImpl[] mediaEntities;
	private ExtendedMediaEntityJSONImpl[] extendedMediaEntities;
	private HashtagEntityJSONImpl[] symbolEntities;
	private long currentUserRetweetId = -1L;
	private ScopesImpl scopes;
	private UserJSONImpl user = null;
	private String[] withheldInCountries = null;
	private DrenajStatusJSONImpl quotedStatus;
	private long quotedStatusId = -1L;
	
	private String[] campaign_id;

	@Override
	public int compareTo(Status that) {
		long delta = this.id - that.getId();
		if (delta < Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		} else if (delta > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) delta;
	}

	@Override
	public Date getCreatedAt() {
		return this.createdAt;
	}

	@Override
	public long getId() {
		return this.id;
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public String getSource() {
		return this.source;
	}

	@Override
	public boolean isTruncated() {
		return isTruncated;
	}

	@Override
	public long getInReplyToStatusId() {
		return inReplyToStatusId;
	}

	@Override
	public long getInReplyToUserId() {
		return inReplyToUserId;
	}

	@Override
	public String getInReplyToScreenName() {
		return inReplyToScreenName;
	}

	@Override
	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	@Override
	public Place getPlace() {
		return place;
	}

	@Override
	public long[] getContributors() {
		return contributorsIDs;
	}

	@Override
	public boolean isFavorited() {
		return isFavorited;
	}

	@Override
	public boolean isRetweeted() {
		return isRetweeted;
	}

	@Override
	public int getFavoriteCount() {
		return favoriteCount;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public boolean isRetweet() {
		return retweetedStatus != null;
	}

	@Override
	public Status getRetweetedStatus() {
		return retweetedStatus;
	}

	@Override
	public int getRetweetCount() {
		return (int) retweetCount;
	}

	@Override
	public boolean isRetweetedByMe() {
		return currentUserRetweetId != -1L;
	}

	@Override
	public long getCurrentUserRetweetId() {
		return currentUserRetweetId;
	}

	@Override
	public boolean isPossiblySensitive() {
		return isPossiblySensitive;
	}

	@Override
	public UserMentionEntity[] getUserMentionEntities() {
		return userMentionEntities;
	}

	@Override
	public URLEntity[] getURLEntities() {
		return urlEntities;
	}

	@Override
	public HashtagEntity[] getHashtagEntities() {
		return hashtagEntities;
	}

	@Override
	public MediaEntity[] getMediaEntities() {
		return mediaEntities;
	}

	@Override
	public ExtendedMediaEntity[] getExtendedMediaEntities() {
		return extendedMediaEntities;
	}

	@Override
	public SymbolEntity[] getSymbolEntities() {
		return symbolEntities;
	}

	@Override
	public Scopes getScopes() {
		return scopes;
	}

	@Override
	public String[] getWithheldInCountries() {
		return withheldInCountries;
	}

	@Override
	public long getQuotedStatusId() {
		return quotedStatusId;
	}

	@Override
	public Status getQuotedStatus() {
		return quotedStatus;
	}

	@Override
	public String getLang() {
		return lang;
	}

	@Override
	public int hashCode() {
		return (int) id;
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		return obj instanceof Status && ((Status) obj).getId() == this.id;
	}

	@Override
	public String toString() {
		return "StatusJSONImpl{" + "createdAt=" + createdAt + ", id=" + id + ", text='" + text + '\'' + ", source='"
				+ source + '\'' + ", isTruncated=" + isTruncated + ", inReplyToStatusId=" + inReplyToStatusId
				+ ", inReplyToUserId=" + inReplyToUserId + ", isFavorited=" + isFavorited + ", isRetweeted="
				+ isRetweeted + ", favoriteCount=" + favoriteCount + ", inReplyToScreenName='" + inReplyToScreenName
				+ '\'' + ", geoLocation=" + geoLocation + ", place=" + place + ", retweetCount=" + retweetCount
				+ ", isPossiblySensitive=" + isPossiblySensitive + ", lang='" + lang + '\'' + ", contributorsIDs="
				+ Arrays.toString(contributorsIDs) + ", retweetedStatus=" + retweetedStatus + ", userMentionEntities="
				+ Arrays.toString(userMentionEntities) + ", urlEntities=" + Arrays.toString(urlEntities)
				+ ", hashtagEntities=" + Arrays.toString(hashtagEntities) + ", mediaEntities="
				+ Arrays.toString(mediaEntities) + ", symbolEntities=" + Arrays.toString(symbolEntities)
				+ ", currentUserRetweetId=" + currentUserRetweetId + ", user=" + user + ", withHeldInCountries="
				+ Arrays.toString(withheldInCountries) + ", quotedStatusId=" + quotedStatusId + ", quotedStatus="
				+ quotedStatus + '}';
	}

	@Override
	public int getAccessLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RateLimitStatus getRateLimitStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setTruncated(boolean isTruncated) {
		this.isTruncated = isTruncated;
	}

	public void setInReplyToStatusId(long inReplyToStatusId) {
		this.inReplyToStatusId = inReplyToStatusId;
	}

	public void setInReplyToUserId(long inReplyToUserId) {
		this.inReplyToUserId = inReplyToUserId;
	}

	public void setFavorited(boolean isFavorited) {
		this.isFavorited = isFavorited;
	}

	public void setRetweeted(boolean isRetweeted) {
		this.isRetweeted = isRetweeted;
	}

	public void setFavoriteCount(int favoriteCount) {
		this.favoriteCount = favoriteCount;
	}

	public void setInReplyToScreenName(String inReplyToScreenName) {
		this.inReplyToScreenName = inReplyToScreenName;
	}

	public void setGeoLocation(GeoLocation geoLocation) {
		if (geoLocation != null)
			this.geoLocation = geoLocation;
	}

	public void setPlace(PlaceJSONImpl place) {
		if (place != null)
			this.place = place;
	}

	public void setRetweetCount(long retweetCount) {
		this.retweetCount = retweetCount;
	}

	public void setPossiblySensitive(boolean isPossiblySensitive) {
		this.isPossiblySensitive = isPossiblySensitive;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setContributorsIDs(long[] contributorsIDs) {
		if (contributorsIDs != null)
			this.contributorsIDs = contributorsIDs;
	}

	public void setRetweetedStatus(DrenajStatusJSONImpl retweetedStatus) {
		if (retweetedStatus != null)
			this.retweetedStatus = retweetedStatus;
	}

	public void setUserMentionEntities(UserMentionEntityJSONImpl[] userMentionEntities) {
		if (userMentionEntities != null)
			this.userMentionEntities = userMentionEntities;
	}

	public void setUrlEntities(URLEntityJSONImpl[] urlEntities) {
		if (urlEntities != null)
			this.urlEntities = urlEntities;
	}

	public void setHashtagEntities(HashtagEntityJSONImpl[] hashtagEntities) {
		if (hashtagEntities != null)
			this.hashtagEntities = hashtagEntities;
	}

	public void setMediaEntities(MediaEntityJSONImpl[] mediaEntities) {
		if (mediaEntities != null)
			this.mediaEntities = mediaEntities;
	}

	public void setExtendedMediaEntities(ExtendedMediaEntityJSONImpl[] extendedMediaEntities) {
		if (extendedMediaEntities != null)
			this.extendedMediaEntities = extendedMediaEntities;
	}

	public void setSymbolEntities(HashtagEntityJSONImpl[] symbolEntities) {
		if (symbolEntities != null)
			this.symbolEntities = symbolEntities;
	}

	public void setCurrentUserRetweetId(long currentUserRetweetId) {
		this.currentUserRetweetId = currentUserRetweetId;
	}

	public void setScopes(ScopesImpl scopes) {
		if (scopes != null)
			this.scopes = scopes;
	}

	public void setUser(UserJSONImpl user) {
		if (user != null)
			this.user = user;
	}

	public void setWithheldInCountries(String[] withheldInCountries) {
		if (withheldInCountries != null)
			this.withheldInCountries = withheldInCountries;
	}

	public void setQuotedStatus(DrenajStatusJSONImpl quotedStatus) {
		if (quotedStatus != null)
			this.quotedStatus = quotedStatus;
	}

	public void setQuotedStatusId(long quotedStatusId) {
		this.quotedStatusId = quotedStatusId;
	}

	@Override
	public String[] getCampaign_id() {
		return campaign_id;
	}

}