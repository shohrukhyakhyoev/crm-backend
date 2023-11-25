package com.crm.backend.agent;

import com.crm.backend.enums.AgentStatus;
import com.crm.backend.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


/**
 * Serves as a service layer for requests associated with manipulation over agent's data.
 *
 * @author shohrukhyakhyoev
 */
@Service
public class AgentService {

    @Autowired
    private AgentRepository agentRepository;

    public Optional<Agent> findById(Long id) {
        return agentRepository.findById(id);
    }

    public Agent saveAgent(Agent agent) {
        return agentRepository.save(agent);
    }

    public List<Agent> findAgentByStatusOrderByScoreDesc(Role role, AgentStatus agentStatus) {
        return agentRepository.findAgentByStatusOrderByScoreDesc(role, agentStatus);
    }

}
