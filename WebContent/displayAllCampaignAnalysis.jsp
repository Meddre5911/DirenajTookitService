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

<script type="text/javascript">

$(document).ready(function() {
    
	var campaignId = $("#campaignId").val();
	
    $("#example").dataTable( {
        "bProcessing": true,
        "bServerSide": true,
        "sAjaxSource": "./MongoPaginationServlet",
        "iDisplayLength": 50,
        "iDisplayStart":0,
        "fnServerParams": function ( aoData ) {
            aoData.push( { "name": "pageType", "value": "requestAllCampaignAnalysis" } );
          },
        "aoColumns": [
            { "mData": "campaign_id"},
            { "mData": "queryHashtags"},
            { "mData": "totalTweetCount" },
            { "mData": "retweetedTweetCount"  },
            { "mData": "replyTweetCount" },
            { "mData": "mentionTweetCount" },
            { "mData": "distinctUserCount" },
            { "mData": "totalWordCount" },
            { "mData": "totalDistinctWordCount" },
            { "mData": "campaign_id", 
                "mRender": function ( data, type, row ) {
  	            	return '<a href=campaignWordFrequency.jsp?campaignId='+data+' target="_blank">ClickForWordFrequency</a>';
                 }
            },
            { "mData": "campaign_id", 
                "mRender": function ( data, type, row ) {
  	            	return '<a href=campaignHashtagCounts.jsp?campaignId='+data+' target="_blank">ClickForHashtagCounts</a>';
                 }
            },
        ]
    } );

} );

</script>

<form action="">
<input type="hidden" name="campaignId" id="campaignId" value="<%=request.getParameter("campaignId")%>">

<h2 >Campaign Statistics<br><br></h2>
<table width="70%" style="border: 3px;background: rgb(243, 244, 248);"><tr><td>
    <table id="example" class="display" cellspacing="0" width="100%">
        <thead>
            <tr>
                <th>Campaign_Id</th>
                <th>Hashtag Query</th>
                <th>Total_Tweet_Count</th>
                <th>Retweeted_Tweet_Count-TotalPercentage</th>
                <th>Reply_Tweet_Count-TotalPercentage</th>
                <th>Mention_Tweet_Count-TotalPercentage</th>
                <th>Distinct_User_Count-Tweet_Per_User</th>
                <th>Total_Word_Count</th>
                <th>Total_Distinct_Word_Count-Usage_of_Same_Word_Set</th>
                <th>Word_Frequencies</th>
                <th>HashTag_Counts</th>
            </tr>
        </thead>       
    </table>
    </td></tr></table>
</form>

</body>
</html>