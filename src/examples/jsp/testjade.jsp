<%@ page import="jade.core.*"%>
<jsp:useBean id="snooper" class="examples.jsp.Snooper" scope="application">
<% try {
    // Does not work for the moment
    // String [] args = {"-platform", "buffer:examples.jsp.Buffer"};
    String [] args = {"-container"};        
    jade.Boot.main(args);
    System.out.println("Jade Inited()");
    System.out.println("Start");
    snooper.doStart("snooper");
   } catch (Exception ex) {
       out.println(ex);
   }
 %>
</jsp:useBean>
<% 
  snooper.snoop(request.getRemoteHost()+" "+(new java.util.Date())+" "+request.getRequestURI()); %>
<HTML>
<BODY>
It works !!!!
</BODY>
</HTML>