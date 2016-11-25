<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Organized Behaviour Detection Requests Input Data</title>

<link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
<script type="text/javascript" src="js/jquery-1.11.2.min.js"></script>
<script type="text/javascript" src="js/jquery.dataTables.js"></script>


</head>
<body>

<!-- 		document.put("originalRequestId", originalRequestId); -->
<!-- 		document.put("requestId", requestData.getRequestId()); -->
<!-- 		document.put("isHashtagRequest", requestData.isHashtagSpecificRequest()); -->
<!-- 		document.put("lowerTimeInterval", requestData.getLowerTime()); -->
<!-- 		document.put("upperTimeInterval", requestData.getUpperTime()); -->

<script type="text/javascript">

$(document).ready(function() {
    
	var retrievedRequestId = $("#retrievedRequestId").val();
	
    $("#example").dataTable( {
        "bProcessing": true,
        "bServerSide": true,
        "sAjaxSource": "./MongoPaginationServlet",
        "iDisplayLength": 50,
        "iDisplayStart":0,
        "fnServerParams": function ( aoData ) {
            aoData.push( { "name": "pageType", "value": "requestTweetSimilarityCalculationsInRequest" } );
            aoData.push( { "name": "retrievedRequestId", "value": retrievedRequestId } );
          },
        "aoColumns": [
            { "mData": "originalRequestId"},
            { "mData": "requestId", 
                "mRender": function ( data, type, row ) {
                 		if(row.processCompleted==false){
                 			return data;
                 		}else {
  	            	  return '<a href=listTweetSimilaritiesInOrganizedBehaviourRequests.jsp?pageType=requestTweetSimilaritiesInRequest&requestId='+data+'>'+data+'</a>';
                 		}
                 }
            },
//             { "mData": "MOST_SIMILAR" },
//             { "mData": "VERY_SIMILAR" },
//             { "mData": "SIMILAR" },
//             { "mData": "SLIGHTLY_SIMILAR" },
//             { "mData": "NON_SIMILAR" },

            { "mData": "isHashtagRequest" },
            { "mData": "lowerTimeInterval" },
            { "mData": "upperTimeInterval" },
            { "mData": "DistinctUserCount" },
            { "mData": "TotalTweetCount" },
            { "mData": "TweetCountUserCountRatio" },
            { "mData": "totalRetweetCount" },
            { "mData": "totalRetweetCountDistinctRetweetCountRatio" },
            
            
            { "mData": "hashtagRatio" },
            { "mData": "urlRatio" },
            { "mData": "mentionRatio" },
            { "mData": "mediaRatio" },

            { "mData": "distinctRetweetUserCount" },
            { "mData": "distinctRetweetPostCount" },
            { "mData": "distinctRetweetUserDividedByRatio" },
            { "mData": "distinctRetweetRatio" },
            { "mData": "distinctRetweetUserRatio" },
            
            { "mData": "distinctNonRetweetUserCount" },
            { "mData": "distinctNonRetweetPostCount" },
            { "mData": "distinctNonRetweetUserDividedByRatio" },
            { "mData": "distinctNonRetweetRatio" },
            { "mData": "distinctNonRetweetUserRatio" },
            
            { "mData": "totalMentionUserCount" },
            { "mData": "distinctMentionCount" },
            { "mData": "totalDistinctMentionRatio" },
            
            { "mData": "retweetedMentionCount" },
            { "mData": "distinctRetweetedMentionUserCount" },
            { "mData": "retweetedTotalDistinctMentionRatio" },
            { "mData": "nonRetweetedMentionCount" },
            { "mData": "nonRetweetedDistinctMentionCount" },
            { "mData": "nonRetweetedTotalDistinctMentionRatio" }
            
        ]
    } );

} );

</script>


	

<form action="">
<input type="hidden" name="retrievedRequestId" id="retrievedRequestId" value="<%=request.getParameter("requestId")%>">

<h2 >Organized Behaviour Detection Requests Input Data<br><br></h2>
<table width="70%" style="border: 3px;background: rgb(243, 244, 248);"><tr><td>
    <table id="example" class="display" cellspacing="0" width="100%">
        <thead>
            <tr>
<!--                 <th>MostSimilarTweetPercentage</th> -->
<!--                 <th>VerySimilarTweetPercentage</th> -->
<!--                 <th>SimilarTweetPercentage</th> -->
<!--                 <th>SlightlySimilarTweetPercentage</th> -->
<!--                 <th>NonSimilarTweetPercentage</th> -->

                <th>Original Request Id</th>
                <th>Request Id</th>
                <th>HashTag Request</th>
                <th>Lower Time Interval</th>
                <th>Upper Time Interval</th>
                <th>Distinct User Count</th>
                <th>Total Tweet Count</th>
                <th>Tweet Distinct User Ratio</th>
                <th>Total Retweet Count</th>
                <th>Total_Retweet_Count/Distinct_Retweet_Count_Ratio</th>
                
                <th>Hashtag Ratio</th>
                <th>Url Ratio</th>
                <th>Mention Ratio</th>
                <th>Media Ratio</th>
                
        		<th>Distinct_Retweet_UserCount</th>
             	<th>Distinct_Retweet_PostCount</th>
             	<th>Distinct_RetweetCount/User_Count_Ratio</th>
             	<th>Distinct_RetweetCount/TotalTweetCount</th>
             	<th>Distinct_RetweetUser/TotalDistinctUserCount</th>

            	 <th>Distinct_NonRetweet_UserCount</th>
        	     <th>Distinct_NonRetweet_PostCount</th>
    	         <th>Distinct_NonRetweetCount/User_Count_Ratio</th>
	             <th>Distinct_NonRetweetCount/TotalTweetCount</th>
            	 <th>Distinct_NonRetweetUser/TotalDistinctUserCount</th>


    		     <th>Total_Mention_Counts</th>
	             <th>Distinct_Mentioned_User_Count</th>
	             <th>Total_Distinct_Mention_Count_Ratio</th>

	             <th>Retweeted_Mention_Count</th>
	             <th>Retweeted_Distinct_Mention_Count</th>
	             <th>RetweetedMention/Distinct_Mention_Ratio</th>
	             <th>NonRetweeted_Mention_Count</th>
	             <th>NonRetweeted_Distinct_Mention_Count</th>
	             <th>NonRetweeted_Mention/DistinctMention_Ratio</th>
	             
            </tr>
        </thead>       
    </table>
    </td></tr></table>
</form>

</body>
</html>