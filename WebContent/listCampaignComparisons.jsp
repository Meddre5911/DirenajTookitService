<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Campaign Comparison Results</title>

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
            aoData.push( { "name": "pageType", "value": "campaignComparisonList" } );
          },
        "aoColumns": [
            { "mData": "requestId" },
            { "mData": "campaign_id" },
            { "mData": "actualHashtag" },
            { "mData": "requestDefinition" },
            { "mData": "comparedEntities" },
            { "mData": "requestId", 
              "mRender": function ( data, type, row ) {
  	          		  	  return '<a href=campaignComparisonResult.jsp?requestId='+data+'>Results</a>';
                 }
             }
        ]
    } );

} );



</script>

<form action="">
<h2 >Campaign Comparison Results<br><br></h2>
<table width="70%" style="border: 3px;background: rgb(243, 244, 248);"><tr><td>
    <table id="example" class="display" cellspacing="0" width="100%">
        <thead>
            <tr>
                <th>Request Id</th>
                <th>Campaign Id</th>
                <th>Hashtag</th>
                <th>Request Definition</th>
                <th>Compared Entities</th>
                <th>Result</th>
            </tr>
        </thead>       
    </table>
    </td></tr></table>
</form>

</body>
</html>