package com.crm.backend.request;


import com.crm.backend.enums.AgentStatus;
import com.crm.backend.enums.RequestStatus;
import com.crm.backend.enums.Role;
import com.crm.backend.agent.Agent;
import com.crm.backend.agent.AgentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


/**
 * Scheduler class.
 *
 * @author shohrukhyakhyoev
 */
@Configuration
@EnableScheduling
public class RequestConfirmationScheduler {

    private final RequestRepository requestRepository;
    private final AgentRepository agentRepository;

    @Autowired
    public RequestConfirmationScheduler(RequestRepository requestRepository, AgentRepository agentRepository) {
        this.requestRepository = requestRepository;
        this.agentRepository = agentRepository;
    }


    /**
     * Function which is executed regularly at fixed rate.
     * It reassigns agent to the requests which are assigned to some agent, but
     * not confirmed by them for more than 30 minutes.
     * If such requests exist and there are free agents, request is reassigned.
     * If there is no free agent, request is not reassigned.
     */
    @Transactional
    @Scheduled(fixedRate = 1800000)
    public void reassignAgent() {

        // if assigned more than 30 mins and there is free agent, reassign
        // else set request status again to requested

        List<Request> requests = requestRepository.getAllAssignedRequests(RequestStatus.ASSIGNED);

        if (!requests.isEmpty()) {

            requests.forEach(request -> {
                Duration period = Duration.between(request.getAssignedTime(), LocalDateTime.now());

                if (period.toMinutes() >= 30) {
                    List<Agent> agents = agentRepository
                            .findAgentByStatusOrderByScoreDesc(Role.AGENT, AgentStatus.FREE);

                    Agent prevAgent = request.getAgent();
                    prevAgent.setStatus(AgentStatus.OFF);
                    prevAgent.setScore(prevAgent.getScore() - 0.5);

                    if (!agents.isEmpty()) {
                        request.setAgent(agents.get(0));
                        request.setAssignedTime(LocalDateTime.now());
                        request.setMessage("As waiting time for your agent is expired, we assigned you to new agent. Please, wait until your request is confirmed by the agent.");
                    } else {
                        request.setStatus(RequestStatus.REQUESTED);
                        request.setAgent(null);
                        request.setAssignedTime(null);
                        request.setMessage("Waiting time for your agent is expired. We reassign your request to new agent as soon as an agent will be available.");
                    }
                }

            });

        }

    }
}



























