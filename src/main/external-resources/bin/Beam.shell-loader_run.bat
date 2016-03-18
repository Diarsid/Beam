:: configuration hashcode [-397838223]

@echo off
java -cp .;Beam.jar;./../lib/gem-injector-1.0.jar -Djava.rmi.server.hostname=127.0.0.1 -Xms3m -Xmx3m -XX:MaxHeapFreeRatio=10 -XX:MinHeapFreeRatio=10 diarsid.beam.starter.BeamPartsShellLoader
