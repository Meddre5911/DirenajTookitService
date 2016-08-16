<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Organized Behaviour Detection Requests</title>

<link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
<script type="text/javascript" src="js/jquery-1.11.2.min.js"></script>
<script type="text/javascript" src="js/jquery.dataTables.js"></script>


</head>
<body>

<script type="text/javascript">

$(document).ready(function() {
     
    $("#example").dataTable( {
        "bProcessing": true,
        "bServerSide": true,
        "sAjaxSource": "./MongoPaginationServlet",
        "iDisplayLength": 50,
        "iDisplayStart":0,
        "fnServerParams": function ( aoData ) {
            aoData.push( { "name": "pageType", "value": "requestListPagination" } );
          },
        "aoColumns": [
            { "mData": "_id", 
              "mRender": function ( data, type, row ) {
               		if(row.processCompleted==false){
               			return data;
               		}else {
	            	  return '<a href=listUsersInOrganizedBehaviourRequests.jsp?pageType=requestInputData&requestId='+data+'>'+data+' - User Statistics</a>';
               		}
               }
            },
            { "mData": "_id", 
              "mRender": function ( data, type, row ) {
               		if(row.processCompleted==false){
               			return data;
               		}else {
	            	  return '<a href=listTweetSimilarityCalculationsInRequests.jsp?pageType=requestTweetSimilarityCalculationsInRequest&requestId='+data+'>'+data+' - Calculation Requests</a>';
               		}
               }
            },
            { "mData": "_id", 
              "mRender": function ( data, type, row ) {
               		if(row.processCompleted==false){
               			return "Visual Not Available";
               		}else {
	            	  return '<a href=campaignVisualAnalysis?requestType=visualizeUserCreationTimes&requestId='+data+'>Visual Analysis</a>';
               		}
               }
            },
            { "mData": "requestType" },
            { "mData": "requestDefinition" },
            { "mData": "campaignId" },
            { "mData": "topHashtagCount" },
            { "mData": "tracedHashtag" },
            { "mData": "earliestTweetTimeInRequest" },
            { "mData": "latestTweetTimeInRequest" },
            { "mData": "processCompleted" },
            { "mData": "resumeBreakPoint" },
            { "mData": "_id", 
              "mRender": function ( data, type, row ) {
                		if(row.resumeProcess==false){
                 			return "";
                 		}else {
  	          		  	  return '<a href=organizedBehaviourDetection?actionType=resume&requestId='+data+'>Resume</a>';
                 		}
                 }
              },
            	
            	
        ]
    } );

} );



</script>

<form action="">
<h2 >Organized Behaviour Detection Requests<br><br></h2>
<table width="70%" style="border: 3px;background: rgb(243, 244, 248);"><tr><td>
    <table id="example" class="display" cellspacing="0" width="100%">
        <thead>
            <tr>
                <th>Request Id</th>
                <th>Similarity Calculation Requests</th>
                <th>Visual Analysis</th>
                <th>Request Type</th>
                <th>Request Definition</th>
                <th>Campaign Id</th>
                <th>Top Hashtag Count</th>
                <th>Traced Hashtag</th>
                <th>Earliest Tweet Time</th>
                <th>Latest Tweet Time</th>
                <th>Process Completed</th>
                <th>Request Status</th>
                <th>Resume Request</th>
            </tr>
        </thead>       
    </table>
    </td></tr></table>
</form>

</body>
</html>