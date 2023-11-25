package com.crm.backend.request;

import com.crm.backend.enums.AgentStatus;
import com.crm.backend.enums.RequestStatus;
import com.crm.backend.enums.Role;
import com.crm.backend.exception.ApiRequestException;
import com.crm.backend.others.*;
import com.crm.backend.agent.Agent;
import com.crm.backend.user.User;
import com.crm.backend.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


/**
 * Serves as a service layer for requests associated with manipulation over request's data.
 *
 * @author shohrukhyakhyoev
 */
@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserService userService;

    @Autowired
    public RequestService(RequestRepository requestRepository,
                          UserService userService) {
        this.requestRepository = requestRepository;
        this.userService = userService;
    }


    /**
     * Gets list of all requests.
     *
     * @return List of request objects.
     */
    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }



    /**
     * Gets list of all requests of a particular user with given email.
     * Two users one with email and second with id is fetched.
     * If user with id is the same as the user with email, it
     * is then permitted to access the user: as the user should
     * be able to access its own request details.
     * If not, then it is checked whether user with id is ADMIN.
     * As ADMINS are permitted to access any user details.
     * But if the ADMIN is requesting to get its own request, then
     * exception is thrown as ADMINS don't have associated requests.
     * <p>
     * If user role is AGENT, then only requests with PROCESSED (finished)
     * status are returned.
     *
     * @param email represents email of an app user.
     * @param id represents id of an app user.
     * @return List of request objects.
     */
    public List<Request> getUserRequests(String email, Long id) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ApiRequestException("There is no user with this email: " + email));

        User superUser = userService.findById(id)
                .orElseThrow(() -> new ApiRequestException("There is no user with this id: " + id));

        if (Objects.equals(superUser.getEmail(), email) || Objects.equals(superUser.getRole().name(), "ADMIN")) {

            if (user.getRole().name().equals("AGENT"))
                return requestRepository.getAgentHistory(email, RequestStatus.PROCESSED);
            else if (user.getRole().name().equals("CUSTOMER"))
                return requestRepository.findAllByCustomerEmail(email);
            else
                throw new ApiRequestException("Admins don't have requests!");
        }

        else {
            throw new ApiRequestException("You are not allowed to access others data.");
        }
    }


    /**
     * Gets list of all requests of a particular user with given email and id.
     * Firstly, it is verified that user fetched with the given id has the same
     * email as the given email field. It is security method to let only the
     * owner of data to access it. If so, then method returns list of requests of
     * agent with any status except of PROCESSED. Otherwise, exception is thrown.
     *
     * @param email represents email of an app user.
     * @param id represents id of an app user.
     * @return List of request objects.
     * @throws ApiRequestException
     */
    public List<Request> getAgentDashboardRequests(String email, Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ApiRequestException("There is no user with this id: " + id));

        if (Objects.equals(user.getEmail(), email)) {
            throw new ApiRequestException("You can't access the agent's dashboard!");
        }

        return requestRepository.getAgentDashboard(email, RequestStatus.REQUESTED, RequestStatus.PROCESSED);
    }



    /**
     * Creates new request.
     * Firstly, it is verified whether user with given id exists and it is
     * customer. Then it is checked whether this customer already has a
     * request on the board with any status except of PROCESSED ones. If so,
     * then exception is thrown as customers can create requests while already
     * having active request.
     *
     * If customers doesn't have any active request, then it is checked if
     * there are free agents available. If so, request is created being alongside
     * assigned to an agent. Request is set to status ASSIGNED and agent to BUSY.
     * Otherwise, agent attr of request will be set to null
     * until agent is assigned to it.
     *
     * @param customer_id represents id of a customer user.
     * @return ResponseEntity object.
     * @throws ApiRequestException
     */
    @Transactional
    public ResponseEntity<ApiResponse> createRequest(Long customer_id) {
        User customer = userService.findById(customer_id)
                .orElseThrow(() -> new ApiRequestException("User with id: " + customer_id + " doesn't exist."));

        if (!customer.getRole().name().equals("CUSTOMER")) {
            throw new ApiRequestException("Failed! Only customers can create request!");
        }

        if (!requestRepository.getAllCustomerRequestsWithoutStatus(
                customer.getEmail(), RequestStatus.PROCESSED).isEmpty()) {
            throw new ApiRequestException("Failed to create request. You already have one request on the board!");
        }

        String message;
        RequestStatus status;
        Agent agent;
        LocalDateTime time;
        List<Agent> agents = userService.findAgentByStatusOrderByScoreDesc(Role.AGENT, AgentStatus.FREE);

        if (agents.isEmpty()) {
            message = "Request is created. Please, wait until it is assigned to a free agent.";
            agent = null;
            time = null;
            status = RequestStatus.REQUESTED;
        } else {
            message = "Request is assigned to the agent. Please, wait until agent confirms the request.";
            agent = agents.get(0);
            agent.setStatus(AgentStatus.BUSY);
            time = LocalDateTime.now();
            status = RequestStatus.ASSIGNED;
        }

            Request newRequest = Request.builder()
                .agent(agent)
                .customer(customer)
                .status(status)
                .score(0.0D)
                .isScored(Boolean.FALSE)
                .creationTime(LocalDateTime.now())
                .assignedTime(time)
                .message(message)
                .build();

        requestRepository.save(newRequest);

        checkIsSaved(newRequest);

        return new ResponseEntity<>(
                new ApiResponse(newRequest.getMessage()),
                HttpStatus.OK);

    }



    /**
     * Checks if new request object is saved or not by searching
     * for it on db by its id.
     *
     * @param request Request object.
     * @throws ApiRequestException
     */
    public void checkIsSaved(Request request) {
        requestRepository.findById(request.getId())
                .orElseThrow(() -> new ApiRequestException("Failed to create the request!"));
    }



    /**
     * Used by agent to confirm request.
     * Firstly, it is verified whether user with given id exists and it is
     * agent.
     * Then it is checked if the given customer has active request with the
     * status of ASSIGNED on the board. If not, exception is thrown.
     * Then it is checked if this agent is mapped to the request of the given
     * customer with status of ASSIGNED: so checks if really assigned or not.
     * If not, exception is thrown as only assigned agent can confirm his/her
     * own request.
     * <p>
     * If all conditions are satisfied, then status of request is set to
     * CONFIRMED and appropriate message is then created for customer,
     * saying request is confirmed by agent, and you will be contacted soon.
     *
     * @param customer_email represents email of a customer user.
     * @param id represents id of an agent user.
     * @return ResponseEntity object.
     * @throws ApiRequestException
     */
    @Transactional
    public ResponseEntity<ApiResponse> confirmRequest(String customer_email, Long id) {
        User agent = userService.findById(id)
                .orElseThrow(() -> new ApiRequestException("User with email: " + id + " doesn't exist."));

        if (!agent.getRole().name().equals("AGENT")) {
            throw new ApiRequestException("Only agents can confirm request!");
        }

        Request request = requestRepository.
                findAllByCustomerAndStatus(customer_email, RequestStatus.ASSIGNED)
                .orElseThrow(() -> new ApiRequestException("Assigned request mapped with this customer email: " + customer_email
                + " doesn't exist."));

        if (!request.getAgent().getEmail().equals(agent.getEmail())) {
            throw new ApiRequestException("You can't confirm requests of other agents!");
        }

        request.setStatus(RequestStatus.CONFIRMED);
        request.setConfirmationTime(LocalDateTime.now());
        request.setMessage("Your request is confirmed by the agent. You will be contacted soon.");

        return new ResponseEntity<>(new ApiResponse("Request is confirmed!"), HttpStatus.OK);

    }



    /**
     * Used by agent to finish/end request.
     * Firstly, it is verified whether user with given id exists and it is
     * agent.
     * Then it is checked if the given customer has active request with the
     * status of CONFIRMED on the board. If not, exception is thrown.
     * Then it is checked if this agent is mapped to the request of the given
     * customer with status of CONFIRMED: so checks if really confirmed or not.
     * If not, exception is thrown as only assigned agent can finish his/her
     * own request.
     *
     * If all conditions are satisfied, then status of request is set to
     * PROCESSED and appropriate message is then created for customer,
     * saying request is processed/finished by agent.
     *
     * Then if there are any requests on the board which are not yet assigned,
     * the top request is then assigned to the agent who just became free.
     * If so, status of new request is set to ASSIGNED, while status of agent
     * is not changed from BUSY. If there aren't any such requests, then agent
     * status is set to FREE.
     *
     * @param customer_email represents email of a customer user.
     * @param id represents id of an agent user.
     * @return ResponseEntity object.
     * @throws ApiRequestException
     */
    @Transactional
    public ResponseEntity<ApiResponse> finishRequest(String customer_email, Long id) {
        Agent agent = (Agent) userService.findById(id)
                .orElseThrow(() -> new ApiRequestException("User with email: " + id + " doesn't exist."));

        if (!agent.getRole().name().equals("AGENT")) {
            throw new ApiRequestException("Only agents can finish request!");
        }

        Request request = requestRepository.
                findAllByCustomerAndStatus(customer_email, RequestStatus.CONFIRMED)
                .orElseThrow(() -> new ApiRequestException("Assigned request mapped with this customer email: " + customer_email
                        + " doesn't exist."));

        if (!request.getAgent().getEmail().equals(agent.getEmail())) {
            throw new ApiRequestException("You can't finish requests of other agents!");
        }

        request.setStatus(RequestStatus.PROCESSED);
        request.setFinishTime(LocalDateTime.now());
        request.setMessage("Your request has been successfully processed.");

        List<Request> requests = requestRepository.getAllNotAssignedRequests(RequestStatus.REQUESTED);

        if (!requests.isEmpty()) {
            requests.get(0).setAgent(agent);
            requests.get(0).setStatus(RequestStatus.ASSIGNED);
            requests.get(0).setAssignedTime(LocalDateTime.now());
            requests.get(0).setMessage("Request is assigned to the agent. Please, wait until agent confirms the request.");
        } else {
            agent.setStatus(AgentStatus.FREE);
        }

        return new ResponseEntity<>(new ApiResponse("Request is finished!"), HttpStatus.OK);
    }


    /**
     * Used by customer to delete request.
     * Firstly, as for the security reason, two users one with the given
     * email and second with id are fetched and compared to each other.
     * If they are same and type of CUSTOMER, then request is processed;
     * otherwise, exception with corresponding message is thrown.
     *
     * If all conditions are met, then we search if there is a request on the board
     * with status of REQUESTED and belonging to our customer user. Only not assigned
     * requests can be deleted. Thus, if there is no such request, exception is thrown.
     * Otherwise, it is deleted and ResponseEntity with success message is returned.
     *
     * @param customer_email represents id of customer user.
     * @param customer_id represents id of customer user.
     * @return ResponseEntity object
     * @throws ApiRequestException
     */
    public ResponseEntity<ApiResponse> deleteNotAssignedRequest(String customer_email, Long customer_id) {
        User customer = userService.findById(customer_id)
                .orElseThrow(() -> new ApiRequestException("User with id: " + customer_id + " doesn't exist."));

        if (!customer.getRole().name().equals("CUSTOMER")) {
            throw new ApiRequestException("Failed! Only customers can delete request!");
        }

        if (!customer.getEmail().equals(customer_email)) {
            throw new ApiRequestException("Failed! You can't delete other customer's requests!");
        }

        Request request = requestRepository.findAllByCustomerAndStatus(
                customer_email, RequestStatus.REQUESTED)
                .orElseThrow(() -> new ApiRequestException("Request doesn't exist!"));

        requestRepository.delete(request);

        return new ResponseEntity<>(new ApiResponse("Request is deleted."), HttpStatus.OK);

    }


    /**
     * Calculates score of agent.
     * First, all requests wuth status of PROCESSED associated with
     * the given agent are fetched. Then we loop through them and get
     * sum of all scores given to the requests by customers.
     *
     * Then the average of this sum is returned.
     *
     * @param agent represents agent user.
     * @return
     */
    private Double calculateAgentScore(Agent agent) {
        List<Request> requests = requestRepository
                .getAllForAgentScore(agent.getEmail(), RequestStatus.PROCESSED, Boolean.TRUE);

        Double score = 0.0D;

        if (!requests.isEmpty()) {
            for (Request r: requests) {
                score += r.getScore();
            }

            return (score / requests.toArray().length);
        }

        return score;

    }


    /**
     * Used by customer to set score for processed request.
     * Firstly, it is checked if the user with tbe id in req object exists,
     * and it is role of Customer.
     *
     * Then we check if the request exists with id provided in req object.
     *
     * Then it is checked if the customer is the mapped to the customer of request.
     *
     * If then request status is PROCESSED, then the given score is set to the request and
     * isScored value of request is also updated to True. Otherwise, exception is thrown, as
     * score is set only to requests with status of PROCESSED.
     *
     * @param req SetFeedbackRequest object,
     * @return ResponseEntity object.
     */
    @Transactional
    public ResponseEntity<ApiResponse> setFeedBackScoreForAgent(SetFeedbackRequest req) {
        User customer = userService.findById(req.getCustomerId())
                .orElseThrow(() -> new ApiRequestException("User with id: " + req.getCustomerId() + " doesn't exist."));

        if (!customer.getRole().name().equals("CUSTOMER")) {
            throw new ApiRequestException("Failed! Only customers can set request!");
        }

        // checking it for security purposes
        if (!customer.getEmail().equals(req.getCustomerEmail())) {
            throw new ApiRequestException("Failed! You can't set feedback for other customer's requests!");
        }

        Request request = requestRepository.findById(req.getRequestId())
                .orElseThrow(() -> new ApiRequestException("Request doesn't exist!"));

        if (!request.getCustomer().getEmail().equals(req.getCustomerEmail())) {
            throw new ApiRequestException("Failed! Request doesn't belong to you!");
        }

        if (!request.getStatus().name().equals("PROCESSED")) {
            throw new ApiRequestException("You can set feedback score only to PROCESSED requests.");
        }

        request.setScore(req.getScore());
        request.setIsScored(Boolean.TRUE);

        request.getAgent().setScore(calculateAgentScore(request.getAgent()));

        return new ResponseEntity<>(new ApiResponse("Feedback score is set to the request."), HttpStatus.OK);

    }


}
