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
            { "mData": "isHashtagRequest" },
            { "mData": "lowerTimeInterval" },
            { "mData": "upperTimeInterval" }
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
                <th>Original Request Id</th>
                <th>Request Id</th>
                <th>HashTag Request</th>
                <th>Lower Time Interval</th>
                <th>Upper Time Interval</th>
            </tr>
        </thead>       
    </table>
    </td></tr></table>
</form>

</body>
</html>