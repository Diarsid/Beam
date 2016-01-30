:: configuration hashcode [0]

@echo off
cmd /c start java -cp .\..\lib\h2-1.3.176.jar;.\..\lib\gem-injector-1.0.jar;.;Beam.jar -Djava.security.policy=.\..\config\rmi.policy -Djava.rmi.server.hostname=127.0.0.1 -Xms25m -Xmx25m -XX:MaxHeapFreeRatio=10 -XX:MinHeapFreeRatio=10 -XX:+UseG1GC com.drs.beam.core.Beam