## Run the project
The project consists of 3 jar files: coordinator.jar, server.jar and client.jar.

We need to start the coordinator first using the following command: <br>
``java -jar Coordinator.jar -jp <jmsport> -p <rmiport>``


After starting the coordinator, we can start the server using: <br>
``java -jar Server.jar -p <selfport> -cp <coordinatorjmsport> -ch <coordinatorhost> -n <servername>``


We can start multiple servers by using the coordinator JMS port.

After starting the coordinator and server, we can start the client by using: <br>
`java -jar Client.jar <port> <hostname>`
