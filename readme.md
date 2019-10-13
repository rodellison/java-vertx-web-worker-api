# Java Vertx Web Worker API

A Java VERT.X based web app template that provides a web/api front end for handling requests, 
that then get handled by long running processes.  Effectively, an order taking API..

  
_______________
General Processing Flow
1. VerticleLauncher started, creates all worker and standard verticles.
2. A standard Vertx web handler Verticle listens for incoming requests. 
3. The handler listens for various URI and upon receipt, starts a few CompletableFutures to vertx messagebus
send the respective parts of the posted request to other vertx workers.   
4. CompetableFutures, using .allOf/.whenComplete, join the replies from the Workers to signify completion.

___
For this project, a '**coffee-shop**' flow is simulated..
1. User submits an order request to front line cashier.
2. Cashier takes order, and submits request to a Barista and Kitchen Worker respectively.
3. Cashier provides unque id reply to user. 
4. Barista and Kitchen Worker processes requests in parallel.   **Long Running Processes**
5. When both are complete, a package order is submitted to the Line Worker.
6. Line Worker receives order to package, and processes **Long Running Process**
7. Line Worker places finished order on counter for pickup


_______________
**Compile** using:

mvn clean compile

**Package** fat jar using:

mvn package

**Deploy to API Gateway** 

serverless deploy

<hr>

