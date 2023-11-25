package com.crm.backend.user;

import com.crm.backend.agent.Agent;
import com.crm.backend.agent.AgentService;
import com.crm.backend.enums.AgentStatus;
import com.crm.backend.enums.RequestStatus;
import com.crm.backend.enums.Role;
import com.crm.backend.exception.ApiRequestException;
import com.crm.backend.others.ApiResponse;
import com.crm.backend.request.Request;
import com.crm.backend.request.RequestRepository;
import com.crm.backend.security.EmailValidator;
import com.crm.backend.token.TokenService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * Serves as a service layer for requests associated with manipulation over app user's data.
 *
 * @author shohrukhyakhyoev
 */
@AllArgsConstructor
@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final AgentService agentService;
    @Autowired
    private final RequestRepository requestRepository;
    @Autowired
    private final TokenService tokenService;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final EmailValidator emailValidator;



    /**
     * Gets details of app user with a specified id.
     *
     * @param id represents an id of an app user.
     * @return Optional of app user.
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }



    /**
     * Gets details of app user with a specified email.
     *
     * @param email represents an email of an app user.
     * @return Optional of app user.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }



    /**
     * Gets details of agent user with a specified id.
     *
     * @param id represents an id of an app user.
     * @return Optional of agent object.
     */
    public Optional<Agent> findAgentById(Long id) {
        return agentService.findById(id);
    }



    /**
     * Gets details of all agent users with particular status.
     * Agents are ordered based on their scores. This method is
     * used when agent is searched for a new request. So sorting
     * in this fashion, we make the agent with the highest score to
     * be prioritized as agent at 0 index (first one in the list)
     * is assigned to the request.
     *
     * @param role represents a particular role of an app user.
     * @param agentStatus represents a particular status of an agent.
     * @return list containing details of agent users.
     */
    public List<Agent> findAgentByStatusOrderByScoreDesc(Role role, AgentStatus agentStatus) {
        return agentService.findAgentByStatusOrderByScoreDesc(role, agentStatus);
    }



    /**
     * Saves new app user.
     *
     * @param user represents a new app user.
     */
    public void saveUser(User user) {
        userRepository.save(user);
    }



    /**
     * Gets details of an app users with a particular email.
     * Two users one with email and second with id is fetched.
     * If user with id is the same as the user with email, it
     * is then permitted to access the user: as the user should
     * be able to access its own details.
     * If not, then it is checked whether user with id is ADMIN.
     * As ADMINS are permitted to access any user details.
     *
     * @param email represents an email of an app user.
     * @param id represents an id of an app user.
     * @return list containing details of app users.
     * @throws ApiRequestException
     */
    public Object getUser(String email, Long id) {
        User superUser = userRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("There is no user with this id."));

        if (Objects.equals(superUser.getEmail(), email)) {
            return superUser;
        } else if (Objects.equals(superUser.getRole().name(), "ADMIN")) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ApiRequestException("There is no user with this email."));
        } else {
            throw new ApiRequestException("You are not allowed to access others data.");
        }
    }



    /**
     * Gets details of an app users with a particular role.
     * Method is available only to ADMIN users.
     *
     * @param roleString represents a role of an app user.
     * @throws ApiRequestException
     */
    public List<User> getUsersByRole(String roleString) {
        return switch (roleString) {
            case "ADMIN" -> userRepository.findByRole(Role.ADMIN);
            case "AGENT" -> userRepository.findByRole(Role.AGENT);
            case "CUSTOMER" -> userRepository.findByRole(Role.CUSTOMER);
            default -> throw new ApiRequestException("Invalid role string: " + roleString);
        };

    }



    /**
     * Updates status of an agent user with a specific id.
     * If status is FREE, then it changes status to OFF, as
     * when agent wants to change it status when it is FREE:
     * this means agent finishes his/her work and want to turn
     * offline.
     * Otherwise, when it is OFF, it means agent is offline but
     * now wants to start the work. So thus, we check if there are
     * unassigned yet requests on the board. If there are, we assign
     * the top request to the agent and its status set to BUSY (as
     * it is now assigned to the customer request). Plus, assigned
     * request's status is set to ASSIGNED.
     * If not, then agent's status is set to FREE.
     *
     * @param id represents id of user to be updated.
     * @return ResponseEntity object.
     * @throws ApiRequestException
     */
    @Transactional
    public Agent changeAgentStatus(Long id) {
        Agent agent = findAgentById(id)
                .orElseThrow(() -> new ApiRequestException("Agent with id " + id + " doesn't exist!"));

        if (agent.getStatus().name().equals("FREE")) {
            agent.setStatus(AgentStatus.OFF);
        } else if (agent.getStatus().name().equals("OFF")) {
            List<Request> requests = requestRepository.getAllNotAssignedRequests(RequestStatus.REQUESTED);
            if (!requests.isEmpty()) {
                agent.setStatus(AgentStatus.BUSY);
                requests.get(0).setAgent(agent);
                requests.get(0).setStatus(RequestStatus.ASSIGNED);
                requests.get(0).setAssignedTime(LocalDateTime.now());
                requests.get(0).setMessage("Request is assigned to the agent. Please, wait until agent confirms the request.");
            } else {
                agent.setStatus(AgentStatus.FREE);
            }
        } else {
            throw new ApiRequestException("You are in BUSY status. You can't now change your status manually.");
        }
        return agent;
    }



    /**
     * Adds new user.
     * Firstly, email of new user is validated: whether it is unique and
     * valid. If so, new user is added. Otherwise, exception is thrown.
     *
     * @param requestUser object of a new app user.
     * @return ResponseEntity object.
     * @throws ApiRequestException
     */
    public ResponseEntity<ApiResponse> createUser(CustomUser requestUser) {
        validateEmail(requestUser.getEmail());

        User user = User.builder()
                .firstName(requestUser.getFirstName())
                .lastName(requestUser.getLastName())
                .email(requestUser.getEmail())
                .age(requestUser.getAge())
                .phoneNumber(requestUser.getPhoneNumber())
                .password(passwordEncoder.encode(requestUser.getPassword()))
                .role(requestUser.getRole())
                .build();

        userRepository.save(user);

        isSaved(user.getEmail());

        return new ResponseEntity<>(new ApiResponse("New user has been successfully added."), HttpStatus.OK);

    }



    /**
     * Adds new agent user.
     * Firstly, email of new user is validated: whether it is unique and
     * valid. If so, new user is added. Otherwise, exception is thrown.
     *
     * @param agent  object of a new agent user.
     * @return ResponseEntity object.
     * @throws ApiRequestException
     */
    public Agent createAgent(Agent agent) {
        agent.setPassword(passwordEncoder.encode(agent.getPassword()));
        agent.setScore(0.0D);
        agent.setStatus(AgentStatus.OFF);
        return agentService.saveAgent(agent);
    }



    /**
     * Deletes existing user with the given id.
     * Firstly, it is checked whether a user with such id exists.
     * If so, user is deleted. Otherwise, exception is thrown.
     *
     * @param id object of a new app user containing all details.
     * @return ResponseEntity object.
     * @throws ApiRequestException
     */
    public ResponseEntity<ApiResponse> deleteUser(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new ApiRequestException("This user doesn't exist.");
        }

        tokenService.deleteUserTokens(id);

        userRepository.deleteById(id);

        return new ResponseEntity<>(new ApiResponse("User is deleted"), HttpStatus.OK);

    }



    /**
     * Updates existing user with the given details of CustomUser object.
     * Two users one with id from CustomUser request and second with id
     * is fetched. If user with id is the same as the user from CustomUser
     * object, it is then permitted to update the user: as the user should
     * be able to update its own details. If not, then it is checked whether
     * user with id is ADMIN. As ADMINS are permitted to update any user details.
     *
     * @param id object of a new app user containing all details.
     * @return ResponseEntity object.
     * @throws ApiRequestException
     */
    @Transactional
    public ResponseEntity<CustomUser> updateUser(CustomUser request, Long id) {
        User user1 = userRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("User with id:" + id + " doesn't exist."));

        User user2 = userRepository.findById(request.getId())
                .orElseThrow(() -> new ApiRequestException("This user doesn't exist."));

        if (user1.getRole().name().equals("ADMIN")
                        || user1.getEmail().equals(user2.getEmail())) {
            if (!Objects.equals(user2.getEmail(), request.getEmail())) {
                validateEmail(request.getEmail());
                user2.setEmail(request.getEmail());
            }

            if (isNew(user2.getFirstName(), request.getFirstName())) {
                user2.setFirstName(request.getFirstName());
            }

            if (isNew(user2.getLastName(), request.getLastName())) {
                user2.setLastName(request.getLastName());
            }

            if (isNew(user2.getPhoneNumber(), request.getPhoneNumber())) {
                user2.setPhoneNumber(request.getPhoneNumber());
            }

            if (request.getAge() != null && request.getAge() > 0 &&
                    !Objects.equals(user2.getAge(), request.getAge())) {
                user2.setAge(request.getAge());
            }

            return new ResponseEntity<>(toCustomUser(user2), HttpStatus.OK);
        }

        throw new ApiRequestException("You are not allowed to edit other's details.");

    }



    /**
     * Checks if 2 strings are different or not.
     *
     * @param old represents old str.
     * @param new_ represents new str.
     * @return boolean object.
     * @throws ApiRequestException
     */
    public boolean isNew(String old, String new_) {
        return new_ != null
                && !new_.isEmpty()
                && !Objects.equals(old, new_);
    }



    /**
     * Checks if the user with given email exists or not.
     * Used to check if user was successfully added. If it can't
     * fetch the user, then throws exception.
     *
     * @param email represents an email of an app user.
     * @throws ApiRequestException
     */
    public void isSaved(String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            throw new ApiRequestException("New app user addition failed!");
        }
    }



    /**
     * Validates email checking if user with given email already
     * exists or not. If not, then checks if str email is valid or
     * not. If even one condition is not met, exception is thrown.
     *
     * @param email represents an email of an app user.
     * @throws ApiRequestException
     */
    public void validateEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ApiRequestException("This email already exists!");
        }

        if (!emailValidator.isValid(email)) {
            throw new ApiRequestException("Email isn't valid.");
        }
    }



    /**
     * Returns new CustomUser object filling its details
     * with the details of given user object.
     *
     * @param user represents an app user.
     * @return CustomUser object.
     */
    public CustomUser toCustomUser(User user) {
        return new CustomUser(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getAge(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getPassword());
    }

}
