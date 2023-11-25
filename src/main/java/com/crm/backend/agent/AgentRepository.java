package com.crm.backend.agent;

import com.crm.backend.agent.Agent;
import com.crm.backend.enums.AgentStatus;
import com.crm.backend.enums.Role;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@ComponentScan
@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    @Query("select u from User u where u.role = :role and u.status = :status order by u.score desc")
    List<Agent> findAgentByStatusOrderByScoreDesc(Role role, AgentStatus status);

}
