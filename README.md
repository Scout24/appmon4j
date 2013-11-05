appmon4j 
========

This is a simple lightweight Java Framework for logging performance metrics. 

The goal was to Build a lightweight, easy to use in application monitoring system allowing measurements of "real traffic" performance values in high throughput java applications.



Installation
------------

You can build appmon4j from source. Clone this project and build it with maven.

A packaged release with source and javadoc is available from maven central repository. Just add the following maven dependency.

     <dependency>
       <groupId>de.is24.common</groupId>
       <artifactId>appmon4j</artifactId>
       <version>1.44</version>
     </dependency>`

 
Basic Concepts
-----------

The building blocks of the InApplicationMonitor are Reportables.

All Reportables have a name, which is a simple String. Names can be choosen freely, but it is recommended to use canonical names and a good choice is to use the class name of the monitored class plus a description of the reportable value.

Example de.is24.util.monitoring.InApplicationMonitor.InstanceAccessCount.

Reportable names do not need to and can not be configured in advance. The first use of any given name creates the requested reportable. But sometimes it is useful to initialize a reportable (which is used for errors) so that a monitoring systems configuration can be tested, even if an error has not been counted yet.

Reportables
----------

The most important Reportables are used to measure events. Depending on what you want to know, you either use a simple counter, or if execution time is relevant, use a timer.
### Counter

* Counters are used to count events. 
* Counters can only be incremented by 1

##### Examples

*   number of XYZ errors / Exceptions
*   number of handled events
*   number of failed events

### Timer

*   Timers count and measure timed events.
*   Timers allow adding timer measurements, implicitly incrementing the count

##### Examples

*   DB Query duration
*   rendering duration
*   parsing time of xml input

A timer will expose the following attributes if JMX Connector is configured:

*   count: the number of reported events
*   timerSum: the total amount of reported measurements; this is usually reported as [ms]
*   average: the average measurement per reported event; i.e. timerSum/count
*   stdDeviance (since R27): the standard deviation of the measurements

Additional Reportables are

### StateValueProvider

*   allow access to a numeric value (long), that is already available in the application

##### Examples

*   VM Memory
        Runtime.getRuntime().totalMemory() and Runtime.getRuntime().freeMemory() are automatically added as state values by InApplicationMonitors Constructor
*   uptime
*   number of connections in a connection pool

### MultiStateProvider

*   Expose several State Values by one Reportable instance. Useful if one single Reportable wants to delivery multiple State values, especially if the count of States may vary over time.

##### Examples

* JMXExporter exports all numeric values of all JMXBeans in a given domain, which may change over time.

### Version

could / should actually be generalized into an non numeric state value

*   allow access to a String value, that is already available in the application.

##### Examples

*   In an application composed of several modules the version of each deployed module can be reported.

   de.is24.email.mailer.service Version Mailer : $Id: Mailer.java 29934 2012-08-08 07:20:29Z committer $ $Name: HEAD $

### Historizable

For some events it makes no sense or is not enough to count them or measure the aggregated time. This is especially true for timed events that each time they happen have a more complex result. These cases can be solved by using Historizables.

*   A Historizable consists of a timestamp and a String, describing details of the event
*   The Historizble list allows to monitor if and at which time events have taken place and what was the outcome.
*   The Historizable List for each name is limited to a configurable count to limit memory usage. The default is to keep 5.

##### Example

History of scheduled jobs

     de.is24.batch.bounces Historizable Processor execution :
     09.05.2012 09:55:06 : processing 0 bounces took 6522 ms; ms per bounce : 0
     09.05.2012 09:50:06 : processing 2 bounces took 6068 ms; ms per bounce : 3034
     09.05.2012 09:45:06 : processing 0 bounces took 5885 ms; ms per bounce : 0
     09.05.2012 09:40:06 : processing 2 bounces took 6138 ms; ms per bounce : 3069
     09.05.2012 09:35:06 : processing 0 bounces took 6485 ms; ms per bounce : 0

 
Requirements
-----------

*   Supported Java Version : 1.5 or higher
*   apache log4j Loging library

Getting started
---------------

The main class of appmon4j is de.is24.util.monitoring.InApplicationMonitor. This class is an "old school" singleton, which can be accessed by using the static getInstance() method.
### put data in

To add data / information the following methods are provided

    InApplicationMonitor basic operations
    addTimerMeasurement(String, long)
    addHighRateTimerMeasurement(String name, long timing)
    addSingleEventTimerMeasurement(String name, long timing)
    initializeTimerMeasurement(String name)
    
    incrementCounter(String)
    incrementHighRateCounter(String name)
     
    addHistorizable(Historizable)
    
    registerStateValue(StateValueProvider)
    registerVersion(String, String)

### Code example

    private static final String EMAIL_IS_NULL = Resolver.class.getName() + ".emailIsNull";
    private static final String EMAIL_HANDLING_TIME = Resolver.class.getName() + ".emailHandlingTime" ;
    private static final String EMAIL_HANDLING_TIME_ERROR = Resolver.class.getName() + ".emailHandlingTimeError" ;
    ...
    if (emailAddress == null) {
      InApplicationMonitor.getInstance().incrementCounter(EMAIL_IS_NULL);
      return ;
    }
    long startTime = System.currentTimeMillis();
    try{
      sendEmail(.. params ..);
      InApplicationMonitor.getInstance().addTimerMeasurement(
        EMAIL_HANDLING_TIME, System.currentTimeMillis() - startTime);
    } catch (Exception e){
      InApplicationMonitor.getInstance().addTimerMeasurement(
        EMAIL_HANDLING_TIME_ERROR, System.currentTimeMillis() - startTime);
      log...
    }

get data out
-----------

### Expose metrics by JMX

Instead of using the default InApplicationMonitor instance implicitly created, initialize InApplicationMonitor with a CorePlugin provided with an Instance of JmxAppMon4JNamingStrategy. This enables JMX exposure of all Reportables.
For examples see [appmon4jStandalone.spring.xml](https://github.com/ImmobilienScout24/appmon4j/blob/master/src/main/resources/appmon4jStandalone.spring.xml) .

### Graphing aka statsd / graphite connection

We use statsd and graphite for graphing. 

If you are allready using the appmon4j, putting your metrics into graphite / statsd is a matter of minutes.

*  To send counters and timers to statsd, simply add the appmon4j StatsdPlugin to your configuration.
*  If you have the JMX Mbean of appmon4j enabled you can add and remove Statsd Plugins during runtime.
*  To send StateValues directly to graphite use appmon4js StateValuesToGraphite Class.

You could put the following block into your applications spring config. Please ensure proper replacement of variables, and replace ### with your apps TYP name. Default value for statsd.port is 8125.

     <bean class="de.is24.util.monitoring.statsd.StatsdPlugin" init-method="register">
         <constructor-arg index="0" value="${statsd.host}"/>
         <constructor-arg index="1" value="${statsd.port}"/>
         <constructor-arg index="2" value="###"/>
         <constructor-arg index="3" value="${statsd.samplerate}"/>
     </bean>
 
     <bean class="de.is24.util.monitoring.state2graphite.StateValuesToGraphite">
         <constructor-arg index="0" value="${graphite.host}" />
         <constructor-arg index="1" value="2003" />
         <constructor-arg index="2" value="###" />
     </bean>


### Push numerical values from other JMX Beans to graphite

Requires StateValuesToGraphite plugins 

If you want to expose numeric values of other JMX Beans, you can use JMXExporter to do so.

     <bean class="de.is24.util.monitoring.jmx.JMXExporter">
         <constructor-arg index="0" value="java.lang" />
     </bean>



### ReportVisitor

A Visitor pattern can be used to retriev data from Reportables. The reportInto Method of InApplicationMonitor accepts an implementation of the de.is24.util.monitoring.ReportVisitor interface, and hands in all Reportables for analysys. The following Implementations of ReportVisitor are provided:

*   StringWriterReportVisitor, a very basic implementation, that creates an unsorted String representation of all Reportables handed in.

*   HierarchyReportVisitor. interprets the metrics keys name as a path into a tree, splitting the name at dots (.) and orders metrics accordingly.

*   ValueOrderedReportVisitor, order metrics by value (beta)
*   HistogramLikeValueAnalysisVisitor, a special reporting visitor for Reportables created by HistogramLikeValue instances.

Report Visitor Example

    ReportVisitor visitor = new HierarchyReportVisitor();
    InApplicationMonitor.getInstance().reportInto(visitor);
    System.out.println( visitor.toString());

 

A very simple jsp could look like this:

    <%@page import="java.util.*" %>
    <%@page import="java.lang.*" %>
    <%@page import="de.is24.util.monitoring.visitors.*" %>
    <%@page import="de.is24.util.monitoring.*" %>
    <%!
    public String reportIntoHierarchy() {
      return reportInto(HierarchyReportVisitor.class.getName());
    }
  
    public String reportInto(String className) {
      try {
        Class reportClass = Class.forName(className);
        ReportVisitor visitor = (ReportVisitor) reportClass.newInstance();
        InApplicationMonitor.getInstance().reportInto(visitor);
        return visitor.toString();
      } catch ( Exception e ) {
        return "an error hass occured " + e.toString();
      }
    }
  
    %>
    <html>
      <head>
        <title>Appmon4j dump</title>
      </head>
      <body>
        <pre>
        <%=reportIntoHierarchy()%>
        </pre>
      </body>
    </html>
 
### ReportableObserver

If you are interested in some specific Reportables, or want to be informed, if metrics are added, ReportableObserver is your friend. On Registration to the InApplicationMontor, the instance implementing ReportableObserver will recieve a call to addNewReportable for each Reportable allready registered to the InApplicationMonitor. Additionally the event of a newly registered Reportable will also trigger a call to addNewReportable. It is save to store a reference to the required reportables by the ReportableObserver, for later evaluation. ReportableOberservers not anly longer needed, can be deregistered. 

### MonitorPlugin

If you want to get a hand on the event stream routed throug the InApplicationMonior, you can implement the MonitorPlugin interface and you will get a hand on all counter and timer events. The StatsdPlugin is currently the only provided implemenation of this interface.

 
### Manage

To manage the behavior of InApplicationMonitor from within the JVM you can use:

    isMonitorActive()
    activate()
    deactivate()
    getMaxHistoryEntriesToKeep()
    setMaxHistoryEntriesToKeep(int)

You can also expose the management Interface and the Metrics via JMX, using InApplicationMonitorJMXConnector

UAQ unasked questions
--------------------

 
### Why don't you provide a Timer class with a start() and stop() method, which then automatically reports the result

I have seen to many implementations of timers and timer managing classes which are not thread safe and / or may lead to memory leaks. It's just not kiss (keep it straight and simple)

To call:

    TimerManger.getTimer(name).start()
    do the stuff to measure...
    TimerManager.getTimer(name).stop()
    
    Timer timer = TimerManager.getTimer(name);
    timer.start();
    do the stuff...
    timer.stop();

is IMHO not simpler or easier to use than:

    long start=System.currentTimeMillis();
    do the stuff
    InApplicationMonitor.getInstance().addTimmerMeasurement(name, System.currentTimeMillis() - start);

but the latter is much more flexible because I can decide on the name later:

    long start=System.currentTimeMillis();
    try {
      do the stuff
      if ( variant a) {
        do some other stuff...
        InApplicationMonitor.getInstance().addTimmerMeasurement(name_success_a, System.currentTimeMillis() - start);
      } else{
        InApplicationMonitor.getInstance().addTimmerMeasurement(name_success_b, System.currentTimeMillis() - start);
      }
    } catch(XYZException e){
      InApplicationMonitor.getInstance().addTimmerMeasurement(name_failure, System.currentTimeMillis() - start);
    }

 

 
### I would like to measure this timers in 5 minute intervals, how can I do this

appmon4j itself does not support measurements in time intervals internally, because when deciding on the architecture I assumed that there could be more than one destination interested in the monitoring data. But I did not want appmon4j to know about this. Or in other words I did not want to have any external listener Registration with the option to allow destination specific configurations. This would otherwise lead to some unwanted complexity and extended memory needs.

But still you can have measurements in intervals, you just have to store the previous measurement values and the measurement time and calculate the delta. This makes measurement evaluation actually very flexible and still keeps appmon4j simple.

As an example lets assume we read a timer value every 5 minutes

    08:15:21 timer1: count = 2332 sum = 1123321
    08:20:22 timer1: count = 2346 sum = 1124233

from the last measurement additionally to the total count of event timer1 we learn that in average over all 2346 events the event took 479 milliseconds, additionally we learn that the last 14 events (2346 - 2332) took 912 ms which in average leads to 65 ms per event. Thus in the last five minutes the systems was in average much faster than during the whole uptime.

 
Contribution
------------

appmon4j uses a modified version of StatsdClient.java from the great people at etsy, 
https://github.com/etsy/statsd

appmon4j took some code from codahale metrics, namely the class VirtualMachineMetrics.java
https://github.com/codahale/metrics
