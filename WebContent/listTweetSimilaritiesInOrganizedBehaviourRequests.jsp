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
    
	var retrievedRequestId = $("#retrievedRequestId").val();
	
    $("#example").dataTable( {
        "bProcessing": true,
        "bServerSide": true,
        "sAjaxSource": "./MongoPaginationServlet",
        "iDisplayLength": 50,
        "iDisplayStart":0,
        "fnServerParams": function ( aoData ) {
            aoData.push( { "name": "pageType", "value": "requestTweetSimilaritiesInRequest" } );
            aoData.push( { "name": "retrievedRequestId", "value": retrievedRequestId } );
          },
        "aoColumns": [
            { "mData": "requestId"},
            { "mData": "tweetId" },
            { "mData": "tweetText" },
            { "mData": "SIMILARITY_WITH_OTHER_TWEETS.MOST_SIMILAR" },
            { "mData": "SIMILARITY_WITH_OTHER_TWEETS.VERY_SIMILAR" },
            { "mData": "SIMILARITY_WITH_OTHER_TWEETS.SIMILAR" },
            { "mData": "SIMILARITY_WITH_OTHER_TWEETS.SLIGHTLY_SIMILAR" },
            { "mData": "SIMILARITY_WITH_OTHER_TWEETS.NOTR" },
            { "mData": "SIMILARITY_WITH_OTHER_TWEETS.NON_SIMILAR" }
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
                <th>Request Id</th>
                <th>Tweet Id</th>
                <th>Tweet Text</th>
                <th>MOST_SIMILAR (btw:0-30)</th>
                <th>VERY_SIMILAR (btw:30-45)</th>
                <th>SIMILAR      (btw:45-60) </th>
                <th>SLIGHTLY_SIMILAR (btw:60-90)</th>
                <th>NOTR (btw:90-120)</th>
                <th>NON_SIMILAR (btw:120-180)</th>
            </tr>
        </thead>       
    </table>
    </td></tr></table>
</form>

</body>
</html>