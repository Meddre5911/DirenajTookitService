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
            aoData.push( { "name": "pageType", "value": "requestToolkitCampaigns" } );
          },
        "aoColumns": [
            { "mData": "campaign_id", 
                "mRender": function ( data, type, row ) {
                 		if(row.tweetCollectionEnded==false){
                 			return data;
                 		}else {
  	            		  return '<a href=displayCampaignAnalysis.jsp?pageType=requestCampaignAnalysis&campaignId='+data+' target="_blank">'+data+'</a>';
                 		}
                 }
            },
            { "mData": "campaign_type" },
            { "mData": "description" },
            { "mData": "query_terms" },
            { "mData": "created_at" },
            { "mData": "tweetCollectionEnded" },
        ]
    } );

} );



</script>

<form action="">
<h2 >Toolkit Campaigns<br><br></h2>
<table width="70%" style="border: 3px;background: rgb(243, 244, 248);"><tr><td>
    <table id="example" class="display" cellspacing="0" width="100%">
        <thead>
            <tr>
                <th>Campaign Id</th>
                <th>Campaign Type</th>
                <th>Campaign Definition</th>
                <th>Campaign Query Terms</th>
                <th>Campaign Creation Time</th>
                <th>Campaign Creation Ended</th>
            </tr>
        </thead>       
    </table>
    </td></tr></table>
</form>

</body>
</html>