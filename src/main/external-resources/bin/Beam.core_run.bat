:: configuration hashcode [0]

@echo off
cmd /c start java -cp .;Beam.jar;./../lib/h2-1.3.176.jar;./../lib/gem-injector-1.0.jar;./../lib/javax.servlet-api-3.1.0.jar;./../lib/jetty-http-9.3.6.v20151106.jar;./../lib/jetty-io-9.3.6.v20151106.jar;./../lib/jetty-server-9.3.6.v20151106.jar;./../lib/jetty-util-9.3.6.v20151106.jar;./../lib/jetty-servlet-9.3.6.v20151106.jar -Djava.security.policy=.\..\config\rmi.policy -Djava.rmi.server.hostname=127.0.0.1 -Xms25m -Xmx25m -XX:MaxHeapFreeRatio=10 -XX:MinHeapFreeRatio=10 -XX:+UseG1GC com.drs.beam.core.Beam
