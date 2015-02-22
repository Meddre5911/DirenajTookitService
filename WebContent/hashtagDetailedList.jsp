<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*"%>
<%@ page import="org.joda.time.DateTime"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Direnaj Test Center / Hashtag Time Line Analysis</title>
</head>
<body bgcolor="#fdf5e6">
	<form action="hashtagAnalysis" method="post">
		<h1>Hashtag Time Line Analysis - Chart Display</h1>
		<p>
			Analyzed Hashtag : <b>${searchedHashtagName}</b> | Time Interval : <b>${timeInterval}</b>
			| Time Duration : <b>${timeAmount}</b>
		</p>
		<p>
			See in Chart: <br> <img
				src="chartDisplay?timeInterval=${timeInterval}&searchedHashtagName=${searchedHashtagName}">

		</p>
		<hr>
		<table>
			<tr>
				<td>
					<%
					    HashMap<DateTime, Integer> timelyIntervalAnalysis = (HashMap<DateTime, Integer>) request
					            .getAttribute("hashTagTimelyIntervalAnalysis");
					    Object[] allDates = timelyIntervalAnalysis.keySet().toArray();
					    Arrays.sort(allDates);
					    for (Object entry : allDates) {
					        DateTime entryValue = (DateTime) entry;
					        String str = "<li><font size=\"2\"> Tweets Posted In That Time Interval - " + entryValue + " : "
					                + timelyIntervalAnalysis.get(entryValue) + "</font></li>";
					        out.println(str);
					    }
					%>
				</td>
			</tr>
		</table>
	</form>
</body>
</html>