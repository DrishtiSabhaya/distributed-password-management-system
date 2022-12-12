# BSDS-Final Project

[Please find the project report in res/]

## Overview

The project uses log4j, java rmi and jms as dependencies which are added to the pom.xml file. Generate jars for the 2 drivers in `src\main\java\server\driver` and the client present in `src\main\java\client` and then move to the running instructions. Jars are already generated and are int the `res` folder.

## Running instructions

[If you face errors while running the JAR file, please try deleting activemq-data directory]

Navigate to res/ with the 3 jar files.

To run the coordinator-

`java -jar Coordinator.jar -jp <jmsport> -p <rmiport>`

Example

`java -jar Coordinator.jar -jp 50001 -p 50000`

To run the server-

`java -jar Server.jar -p <selfport> -cp <coordinatorjmsport> -ch <coordinatorhost> -n <servername>`

Example

`java -jar Server.jar -p 50003 -cp 50001 -ch localhost -n ser1`

To run the Client

`java -jar Client.jar <port> <hostname>`

Example

`java -jar Client.jar 50003 localhost`

Please use a port not being used by some other process.

There is a video that demos the running of the project.
https://www.youtube.com/watch?v=1IKwztH2NtY

Note.
Code has been tested with local host and private network Ip. Currently might not work for public IP.

https://github.ccs.neu.edu/shalekar/bsds-proj
currently private to prevent any sort of plagarism issues. Contact a team member if you want access.
