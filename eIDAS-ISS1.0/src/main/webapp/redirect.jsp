<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html dir="ltr">
<META HTTP-EQUIV="Refresh" CONTENT="0;URL=<s:property value="%{failUrl}"/>">
<body>
	<img src="load.gif">
</body>
<%-- 
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@page import="eu.stork.ss.Constants" %>
<%@page import="eu.stork.ss.SPUtil" %>
<%@page import="java.util.Properties" %>
<%
	Properties configs = SPUtil.loadConfigs(Constants.SP_PROPERTIES);
	String site = configs.getProperty(Constants.SF_URL);

   // New location to be redirected
   //String site = new String("http://cosign.ddns.net/OBA/SitePages/ProcessFailed.aspx");
   response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
   response.setHeader("Location", site); 
%>
--%>