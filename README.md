# DirenajToolkitService

In this repository, you can find the code and data set of the study about 
"Organized Behavior Classification of Tweet Sets using Supervised Learning Methods".

Published article about this study is as below :

```
Erdem Beğenilmiş and Suzan Uskudarli. 2018. Organized Behavior Classification of 
Tweet Sets using Supervised Learning Methods. In WIMS ’18: 8th International Conference
on Web Intelligence, Mining and Semantics, June 25–27, 2018, Novi Sad, Serbia. ACM,
New York, NY, USA, 9 pages.https://doi.org/10.1145/3227609.3227665
```
## Deploying Application 

The application is written as Java Web Application. After pulling the repository in your codebase,
by using an editor like Eclipse or IntellijIdea, you export the "war" of the web application and can
run this on your application server. This code is tested in Tomcat Application Server (Tomcat 7,8).

After deploying and running the application server, below pages should be seen : 


```
/tweetCampaignCreation.html  -  Page for creating a collection based on hashtags between a specific date. (Date can go 1 week before) 
/listToolkitCampaigns        -  Lists the created collections
/displayAllCampaignAnalysis.jsp - Displays statistics of the collections (Table-3 in the article).

/organizedBehaviourDetection.html - Starts the feature generation phase based on the collections to classify Organized vs. Organic Behaviors
/listOrganizedBehaviourRequest    - Lists the analyses

/campaignHashtagComparison.html   - Compares the users of collections
/listCampaignComparisons.jsp      - Lists the comparisons
```

## Important Classes 

The main classes of the project are listed as below :

```
CampaignCreator
OrganizationDetector
StatisticCalculator
```

User interfaces are implemented using JSP.

## Data Set - Collection Seed Tweets & Extended Tweets

The data set can be found in.

In the link - "10.6084/m9.figshare.6683004", two different files are exist. One for Seed Tweets and One for Extended Tweets.

Format of Seed Tweets File is as below : 

```
#StartCampaign
Collection ID|Collection Hashtags|Collection Seed Tweets Date Intervals
collection1|#exampleHashtag|Date1 - Date 2
#StartTweets
tweetId1
tweetId2
#EndTweets
#EndCampaign
```

Format of Extended Tweets File is as below : 

```
#StartAnalysis
Analysis Id|Collection Id|Traced Hashtag|Analyzed Tweets Date Interval|Organic - Organized Label|Political - NonPoliticalLabel|ProTrumpProHillary-NeitherLabel
Analysis1|Collection1|#exampleHashtag|Date1-Date2|ORGANIC|POLITICAL|TRUMP
#StartTweets
tweetId1
tweetId2
#EndTweets
#EndAnalysis
```

Tweet Set sizes can be found in : DirenajToolkitService/organizedBehaviorDataSets/direnajTweetSetSizes.csv

## Training Data Sets (Extracted Features in each analysis)

The classification code and extracted features of training data set can be found in other repository "https://github.com/Meddre5911/ToolkitSparkClassifier".

Training Data sets used for classifiacations can be found in the "ToolkitSparkClassifier/data/organizedBehaviour/paper/" directory.
The code directory is : "ToolkitSparkClassifier/src/main/java/edu/boun/cmpe/drenaj/toolkit/model/"









