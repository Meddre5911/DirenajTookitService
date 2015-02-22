<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Direnaj Test Center / Hashtag Time Line Analysis</title>
</head>
<body bgcolor="#fdf5e6">
	<form action="hashtagAnalysis" method="post">
		<h1>Hashtag Time Line Analysis</h1>
		<p>
			CampaignID : <b>${campaignId}</b> | Operation : <b>${operation}</b> |
			Limit : <b>${limit}</b>
		</p>
		<hr>
		<table>
			<tr valign=top>
				<td><b>Hashtag Counts</b>
					<ul>
						<%
						    ArrayList<Map.Entry<String, Integer>> counts = (ArrayList<Map.Entry<String, Integer>>) request
						            .getAttribute("hashtagCounts");
						    for (Map.Entry<String, Integer> entry : counts) {
						        String str = "<li><font size=\"2\">(" + entry.getKey() + " : " + entry.getValue() + ")</font></li>";
						        out.println(str);
						    }
						%>
					</ul></td>

				<td align=left>Please Choose Time Interval <input type="text"
					name="timeAmount"> <select name="timeInterval">
						<option value="MINUTE" selected>Minutes</option>
						<option value="HOUR">Hours</option>
				</select>
					<p>
						Hashtag Value That will be analyzed <input type="text"
							name="searchedHashtagName">
					<p align="right">
						<input type="submit" name="Submit" value="Analyze">
					</p> <br>
				</td>

			</tr>
		</table>
	</form>
</body>
</html>