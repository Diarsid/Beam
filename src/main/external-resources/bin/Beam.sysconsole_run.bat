:: configuration hashcode [0]

@echo off
cmd /c start java -cp .\..\lib\gem-injector-1.0.jar;.;Beam.jar -Djava.security.policy=.\..\config\rmi.policy -Djava.rmi.server.hostname=127.0.0.1 -Xmx3m -XX:MaxHeapFreeRatio=10 -XX:MinHeapFreeRatio=10 com.drs.beam.external.sysconsole.SysConsole