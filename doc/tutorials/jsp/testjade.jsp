<H3>testjade.jsp</H3>
<PRE>
&lt;%@ page <FONT COLOR="#0000ff">import</FONT>=<FONT COLOR="#ff0000">&quot;jade.core.*&quot;</FONT>%&gt;
&lt;jsp:useBean id=<FONT COLOR="#ff0000">&quot;snooper&quot;</FONT> <FONT COLOR="#0000ff">class</FONT>=<FONT COLOR="#ff0000">&quot;examples.jsp.Snooper&quot;</FONT> scope=<FONT COLOR="#ff0000">&quot;application&quot;</FONT>&gt;
&lt;% <FONT COLOR="#0000ff">try</FONT> {
    <FONT COLOR="#9a1900">// Does not work for the moment</FONT>
    <FONT COLOR="#9a1900">// String [] args = {&quot;-platform&quot;, &quot;buffer:examples.jsp.Buffer&quot;};</FONT>
    String [] args = {&quot;-container&quot;};        
    jade.Boot.main(args);
    System.out.println(<FONT COLOR="#ff0000">&quot;Jade Inited()&quot;</FONT>);
    System.out.println(<FONT COLOR="#ff0000">&quot;Start&quot;</FONT>);
    snooper.doStart(<FONT COLOR="#ff0000">&quot;snooper&quot;</FONT>);
   } <FONT COLOR="#0000ff">catch</FONT> (Exception ex) {
       out.println(ex);
   }
 %&gt;
&lt;/jsp:useBean&gt;
&lt;% 
  snooper.snoop(request.getRemoteHost()+<FONT COLOR="#ff0000">&quot; &quot;</FONT>+(<FONT COLOR="#0000ff">new</FONT> java.util.Date())+<FONT COLOR="#ff0000">&quot; &quot;</FONT>+request.getRequestURI()); %&gt;
&lt;HTML&gt;
&lt;BODY&gt;
It works !!!!
&lt;/BODY&gt;
&lt;/HTML&gt;
  </PRE><P STYLE="margin-bottom: 0in">


