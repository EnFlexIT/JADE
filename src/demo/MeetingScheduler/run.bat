cd ..\..
java -classpath %classpath%;.;..\classes;..\lib\jade.jar;..\lib\jadeTools.jar;..\lib\Base64.jar;demo\MeetingScheduler\CalendarBean.jar; jade.Boot -port 1200 -gui Agent1:demo.MeetingScheduler.MeetingSchedulerAgent Agent2:demo.MeetingScheduler.MeetingSchedulerAgent
