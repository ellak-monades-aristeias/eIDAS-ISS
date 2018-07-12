<%-- 
    Document   : monitoring
    Created on : 25 ÎÎ¿Îµ 2015, 11:17:36 Î¼Î¼
    Author     : Fay
--%>

<%@page import="java.util.ArrayList"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.io.File"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.io.BufferedReader"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Read Text</title>
        <link href="css/monitoring.css" rel="stylesheet" type="text/css" />
    </head>
    <body>
  
<table class="responstable">
<%       
String  line;
  BufferedReader br = null;   
    try {

        
        // in my test each character was one byte
        ArrayList<Integer> byteoffset = new ArrayList<Integer>();

        br = new BufferedReader(new FileReader("webapps/ISSPlus/monitoring.log"));
        Integer l = 0;
        while ((line = br.readLine()) != null) {
%>

<tr >
<td> <%=line.substring(0, 19)%></td>
<td> <%=line.substring(20,line.length())%></td>
</tr>

<%       
        //    out.println(line);
            Integer num_bytes = line.getBytes().length;
           
            byteoffset.add( l==0 ? num_bytes : byteoffset.get(l-1)+num_bytes );
            l++;
        }

    } catch ( Exception e) {
                %>
                <h1>There was a problem with the log file.</h1>
                <%  
    }

  //  }
         //   out.println(sb.toString()); 
        %>
        </table>
    </body>
</html>