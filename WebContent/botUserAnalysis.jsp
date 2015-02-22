<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*"%>
<%@ page import="direnaj.domain.*"%>
<%@ page import="java.util.Map.Entry"%>
<%@ page import="java.math.BigDecimal"%>
<%@ page import="direnaj.functionalities.sna.communityDetection.*"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Direnaj Test Center / Bot User Analysis</title>
</head>
<body>

	<h1>Users</h1>
	<table>
		<tr>
			<td>User Name</td>
			<td>Friend Count</td>
			<td>Follower Count</td>
			<td>Posted Tweet Count</td>
			<td>Protected</td>
			<td>Verified</td>
			<td>Account Creation Date</td>
			<td>URL Ratio</td>
			<td>Hashtag Ratio</td>
			<td>Mention Ratio</td>
			<td>Friend/Follower Ratio</td>
			<td>Human Probability</td>
		</tr>
			<%
			 ArrayList<Entry<User, BigDecimal>> sortedValues = ( ArrayList<Entry<User, BigDecimal>>)request.getAttribute("sortedValues");
				for(Entry<User, BigDecimal> record:sortedValues){
				    User user = record.getKey();
				    String str = "<tr><td><a href=\"https://twitter.com/"+ user.getUserScreenName()+ "\" target=\"_blank\">"+ user.getUserScreenName()+"</a></td> ";
				    str+="<td>"+ user.getFriendsCount()+"</td>";
				    str+="<td>"+ user.getFollowersCount()+"</td>";
				    str+="<td>"+ user.getPosts().size()+"</td>";
				    str+="<td>"+ user.isProtected()+"</td>";
				    str+="<td>"+ user.isVerified()+"</td>";
				    str+="<td>"+ user.getCreationDate()+"</td>";
				    str+="<td>"+ user.getAccountProperties().getUrlRatio()+"</td>";
				    str+="<td>"+ user.getAccountProperties().getHashtagRatio()+"</td>";
				    str+="<td>"+ user.getAccountProperties().getMentionRatio()+"</td>";
				    str+="<td>"+ user.getAccountProperties().getFriendFollowerRatio()+"</td>";
				    str+="<td>"+ record.getValue()+"</td>";
				    str+="</tr>";
				    out.println(str);
				}
			%>
	</table>

</body>
</html>