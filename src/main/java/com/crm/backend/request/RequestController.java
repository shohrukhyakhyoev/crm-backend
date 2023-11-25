package com.crm.backend.request;

import com.crm.backend.others.ApiResponse;
import com.crm.backend.others.SetFeedbackRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Serves as a controller for requests associated with manipulation over request's data.
 *
 * @author shohrukhyakhyoev
 */
@RestController
@RequestMapping("/api/v1/request")
public class RequestController {

    private final RequestService requestService;

    @Autowired
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/all/")
    public List<Request> getRequests() {
        return requestService.getAllRequests();
    }

    // history for agent and both history & dashboard for customer
    @GetMapping("/")
    public List<Request> getUserRequests(@RequestParam(name="email") String email,
                                                   @RequestParam(name="id") Long id) {
        return requestService.getUserRequests(email, id);
    }

    @GetMapping("/agent/dashboard/")
    public List<Request> getAgentDashboardRequests(@RequestParam(name="email") String agent_email,
                                           @RequestParam(name="id") Long agent_id) {
        return requestService.getAgentDashboardRequests(agent_email, agent_id);
    }

    @PostMapping("/create/")
    public ResponseEntity<ApiResponse> createRequest(
            @RequestParam(name= "id") Long customer_id) {
        return requestService.createRequest(customer_id);
    }

    @DeleteMapping("/delete/")
    public ResponseEntity<ApiResponse> deleteNotAssignedRequest(
            @RequestParam(name = "email") String customer_email,
            @RequestParam(name = "id") Long customer_id) {
        return requestService.deleteNotAssignedRequest(customer_email, customer_id);
    }

    @PutMapping("/confirm/")
    public ResponseEntity<ApiResponse> confirmRequest(@RequestParam(name = "email") String customer_email,
                                                 @RequestParam(name = "id") Long agent_id) {
        return requestService.confirmRequest(customer_email, agent_id);
    }

    @PutMapping("/finish/")
    public ResponseEntity<ApiResponse> finishRequest(@RequestParam(name = "email") String customer_email,
                                                 @RequestParam(name = "id") Long agent_id) {
        return requestService.finishRequest(customer_email, agent_id);
    }

    @PutMapping("/rate/")
    public ResponseEntity<ApiResponse> setFeedBackScoreForAgent(
            @RequestBody SetFeedbackRequest request) {
        return requestService.setFeedBackScoreForAgent(request);
    }

}
