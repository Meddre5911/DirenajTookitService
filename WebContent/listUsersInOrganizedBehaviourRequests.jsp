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
            aoData.push( { "name": "pageType", "value": "requestInputData" } );
            aoData.push( { "name": "retrievedRequestId", "value": retrievedRequestId } );
          },
        "aoColumns": [
            { "mData": "requestId"},
            { "mData": "userId" },
            { "mData": "userScreenName",
            	  "mRender": function ( data, type, row ) {
	            	  return '<a href=https://twitter.com/'+data+' target="_blank">'+data+'</a>';
               }
            },
            { "mData": "closenessCentrality" },
            { "mData": "postThirdPartyDeviceRatio" },
            { "mData": "postMobileDeviceRatio" },
            { "mData": "postTwitterDeviceRatio" },
            { "mData": "friendFollowerRatio" },
            { "mData": "hashtagRatio" },
            { "mData": "mentionRatio" },
            { "mData": "urlRatio" },
            { "mData": "isProtected" },
            { "mData": "isVerified" },
            { "mData": "creationDate" },
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
                <th>User Id</th>
                <th>User Screen Name</th>
                <th>Closeness Centrality</th>
                <th>Third Party Device Ratio</th>
                <th>Mobile Device Ratio</th>
                <th>Twitter Device Ratio</th>
                <th>Friend Follower Ratio</th>
                <th>Hashtag Ratio</th>
                <th>Mention Ratio</th>
                <th>Url Ratio</th>
                <th>Protected</th>
                <th>Verified</th>
                <th>Creation Time</th>
            </tr>
        </thead>       
    </table>
    </td></tr></table>
</form>

</body>
</html>