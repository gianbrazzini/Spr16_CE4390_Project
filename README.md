# CS 4390 Semester Project

##### Collaborators #####
- Gian Brazzini
- Maisha Mehrin

#### Assignment ####
Implement a file transfer protocol.

Design the protocol ... what message or messages you will design to make connection, send file names you are sending and then when do you close the connection (wait for ack that complete file is received ?) How does the other end know file is complete ? Do you send a check sum of file in a message ?  etc etc ... What messages does the receiver send to ack ?

You will use TCP or UDP to transfer the file.
1. open sockets
2. send files using appropriate transport protocol.
3. Break the file into packets and re-assemble them at destination.

##### Your grading will be based on:
1. Demo - you will be asked to send text file, jpeg, pdf etc .. files.  The received desitnation should display the correct file.
2. Interview during demo - questions about the project and code (to make sure you wrote the code)
3. Report - about 2 pages to document your design

