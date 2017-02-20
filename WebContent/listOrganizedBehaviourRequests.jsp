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
    
	var rows_selected = [];
	var table = $("#example").DataTable( {
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
            { "mData": "organizedClass" },
            { "mData": "_id", 
              "mRender": function ( data, type, row, meta ) {
                		if(row.processCompleted==false){
                 			return "";
                 		}else {
                 		  return '<input type="checkbox" name="extractFeature" value="'+data+'">';
                 		}
                 }
              },
            { "mData": "_id", 
              "mRender": function ( data, type, row) {
                		if(row.resumeProcess==false){
                 			return "";
                 		}else {
  	          		  	  return '<a href=organizedBehaviourDetection?actionType=resume&requestId='+data+'>Resume</a>';
                 		}
                 }
              },
        ],
          'rowCallback': function(row, data, dataIndex){
              // Get row ID
              var rowId = data._id;

              // If row ID is in the list of selected row IDs
              if($.inArray(rowId, rows_selected) !== -1){
                 $(row).find('input[type="checkbox"]').prop('checked', true);
                 $(row).addClass('selected');
              }
           }
    } );


	// Handle click on checkbox
	$('#example tbody').on('click', 'input[type="checkbox"]', function(e){
	   var $row = $(this).closest('tr');

	   // Get row data
	   var data = table.row($row).data();

	   // Get row ID
	   var rowId = data._id;

	   // Determine whether row ID is in the list of selected row IDs 
	   var index = $.inArray(rowId, rows_selected);

	   // If checkbox is checked and row ID is not in list of selected row IDs
	   if(this.checked && index === -1){
	      rows_selected.push(rowId);

	   // Otherwise, if checkbox is not checked and row ID is in list of selected row IDs
	   } else if (!this.checked && index !== -1){
	      rows_selected.splice(index, 1);
	   }

	   if(this.checked){
	      $row.addClass('selected');
	   } else {
	      $row.removeClass('selected');
	   }

	   // Prevent click event from propagating to parent
	   e.stopPropagation();
	});

	// Handle click on table cells with checkboxes
	$('#example').on('click', 'tbody td, thead th:first-child', function(e){
	   $(this).parent().find('input[type="checkbox"]').trigger('click');
	});

	// Handle form submission event 
	$('#frm-example').on('submit', function(e){
	   var form = this;

	   // Iterate over all selected checkboxes
	   $.each(rows_selected, function(index, rowId){
	      // Create a hidden element 
	      $(form).append(
	          $('<input>')
	             .attr('type', 'hidden')
	             .attr('name', 'willBeExtracted')
	             .val(rowId)
	      );
	   });
	});
	

} );


</script>

<form name ="frm-example" id="frm-example" action="organizedBehaviourExtractFeature" method="post">
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
                <th>Organized Class</th>
                <th>Extract Features</th>
                <th>Resume Request</th>
            </tr>
        </thead>       
    </table>
    </td></tr></table>
    <br>
    <p />
	UserID: <input type="text" name="userID" value="" size="20">
	<p />
	Password: <input type="password" name="pass" value="" size="20">
	<p />
    <input type="submit" value="Extract Features For Classification" name="SUB">
</form>

</body>
</html>