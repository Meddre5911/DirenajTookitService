import traceback
def flatMapFunction(s1):
	mylist = []
	try:
		hashtagArr =  s1.hashtags.replace('[','').replace(']','').split(',')
		print(hashtagArr)
		for hashtag in hashtagArr :
			mylist.append((hashtag,s1.userid,s1.ratio))
		print(mylist)
	except:
		 tb = traceback.format_exc()
		 print(tb)
	return mylist


from pyspark.sql.functions import col
from pyspark.sql.functions import *

# read file
iraTweets = spark.read.csv('/sparkAnalysis/ira_tweets_csv_hashed.csv',header=True)
iraTweets.cache()

# calculate ratio
userRatios = iraTweets.filter((iraTweets.hashtags != '[]') & (iraTweets.hashtags.isNotNull()) & (iraTweets.hashtags != '0')) \
.select("hashtags","userid", (col("follower_count")/(col("follower_count") + col("following_count"))).alias('ratio'))

# expand hashtags
expandedRatios = userRatios.rdd.flatMap(lambda s1 : flatMapFunction(s1))
expandedRatiosDF = spark.createDataFrame(expandedRatios).toDF("hashtag","userid","ffratio")

expandedRatiosDF.cache()

consideredHashtags = expandedRatiosDF.groupBy("hashtag").count().filter(col("count") > 50).select("hashtag")
# filter hashtag with tweets < 50
expandedRatiosDF = expandedRatiosDF.join(consideredHashtags, "hashtag")

# get max ffratio for same users
ffRatios = expandedRatiosDF.groupBy("hashtag","userid").agg({"ffratio":"max"}).drop("userid").withColumnRenamed("max(ffratio)","ffratio")

# calculate mean variance
ffRatios.groupBy("hashtag").agg(stddev_pop("ffratio"),var_pop("ffratio"),min("ffratio"),max("ffratio"),avg("ffratio")).show()

ffRatios.groupBy("hashtag").agg(avg("ffratio").alias("UserFriendFollowerRatiosMV_friendFollowerRatio_average"),
							var_pop("ffratio").alias("UserFriendFollowerRatiosMV_friendFollowerRatio_population_variance"), \
							stddev_pop("ffratio").alias('UserFriendFollowerRatiosMV_friendFollowerRatio_population_standard_deviation'), \
							min("ffratio").alias("UserFriendFollowerRatiosMV_friendFollowerRatio_minValue"), \
							max("ffratio").alias("UserFriendFollowerRatiosMV_friendFollowerRatio_maxValue") \
).coalesce(1).write.csv("/sparkAnalysis/IraTestDataFriendFollowerRatios.csv")


