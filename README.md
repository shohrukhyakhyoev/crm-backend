# Customer Relationship Management System

This is CRM system developed in Spring Boot. There are 3 types of users: CUSTOMER, AGENT and ADMIN.

Customers can create request, delete request (only if not assigned to the agent), live feedback score to agents, view history.
Agents can confirm/finish requests. They can also change their status (OFF, FREE, BUSY) and view their history.

Agents are assigned to requests of customers based on their feedback score. If there are several FREE agents, the one having the highest score will be assigned to the request.
When agents change their status from OFF to FREE, system automatically assigns them to unassigned request if it exists. 
When agent finish the request (once it is processed), status of agent is changed from BUSY to FREE if there are no unassigned requests on the board. Otherwise, agent is again
assigned, but this time to new request.

If agent doesn't confirm the requests within 30 minutes after being assigned to the request, system automatically reassigns the agent of the request to another agent if free agents are available.
If so, the previous agent's score is reduced as a fine for not confirming the request on time.

To get more about the project, clone the project and test the endpoints on Postman.


