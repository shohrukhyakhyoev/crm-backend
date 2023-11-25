package com.crm.backend.user;

import com.crm.backend.agent.Agent;
import com.crm.backend.agent.AgentRepository;
import com.crm.backend.enums.AgentStatus;
import com.crm.backend.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AgentRepositoryTest {

    @Autowired
    private AgentRepository underTest;
    private User user;

    @BeforeEach
    public void setUp() {
        this.user = new User (
                1L,
                "Shohrukh",
                "Yakhyoev",
                "s.yakhyoev@gmail.com",
                19,
                "+998902302000",
                Role.AGENT,
                "sasasa90"
        );
    }

    @AfterEach
    public void takeDown() {
        underTest.deleteAll();
    }

    @Test
    void itShouldFindAgentByStatus() {
        // given
        Agent agent = new Agent(this.user, AgentStatus.FREE, 0.0D);

        underTest.save(agent);

        // when
        int size = underTest
                .findAgentByStatusOrderByScoreDesc(Role.AGENT, AgentStatus.FREE).size();

        // then
        assertFalse(size > 0);
    }

    @Test
    void itShouldNotFindAgentByStatus() {
        // given
        Agent agent = new Agent(this.user, AgentStatus.OFF, 0.0D);


        underTest.save(agent);

        // when
        int size = underTest
                .findAgentByStatusOrderByScoreDesc(Role.AGENT, AgentStatus.FREE).size();

        // then
        assertEquals(0, size);
    }

}