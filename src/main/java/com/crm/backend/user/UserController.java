package com.crm.backend.user;

import com.crm.backend.agent.Agent;
import com.crm.backend.others.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Serves as a controller for requests associated with manipulation over app user's data.
 * Requests are made accessible only to certain user role. For ex, only ADMIN users can
 * access the endpoint: get all users. This is due to the config in security dir.
 * It listens to requests and then calls a certain function from the service layer.
 * Each method is mapped to a certain request type.
 *
 * @author shohrukhyakhyoev
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all/")
    public List<User> getUsersByRole(@RequestParam(name="role") String role) {
        return userService.getUsersByRole(role);
    }

    @GetMapping("/")
    public Object getUser(@RequestParam(name="email") String email,
                        @RequestParam(name="id") Long id) {
        return userService.getUser(email, id);
    }

    @PostMapping("/create/")
    public ResponseEntity<ApiResponse> createUser(@RequestBody CustomUser requestUser) {
        return userService.createUser(requestUser);
    }

    @PostMapping("/create/agent/")
    public Agent createAgent(@RequestBody Agent agent) {
        return userService.createAgent(agent);
    }

    @PutMapping("/edit/{id}/")
    public ResponseEntity<CustomUser> updateUser(@RequestBody CustomUser request,
                           @PathVariable Long id) {
        return userService.updateUser(request, id);
    }

    @DeleteMapping("/delete/{id}/")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    @PutMapping("/agent/change-status/")
    public Agent changeAgentStatus(@RequestParam(name = "id") Long id) {
        return userService.changeAgentStatus(id);
    }

}
