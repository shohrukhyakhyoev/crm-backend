package com.crm.backend.request;

import com.crm.backend.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * Serves as a data layer for manipulation over request data.
 * It allows application to contact with database with the help of
 * functions together with the specified SQL Query. To accomplish it
 * interface extends JpaRepository class. By this we use Spring Data
 * JPA Framework to map objects in database table. It is called ORM.
 *
 * @author shohrukhyakhyoev
 */
@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("select r from Request r where r.customer.email = :email and r.status != :status")
    List<Request> getAllCustomerRequestsWithoutStatus(String email, RequestStatus status);

    @Query("select r from Request r where r.status = :status order by r.creationTime asc")
    List<Request> getAllNotAssignedRequests(RequestStatus status);

    @Query("select r from Request r where r.agent.email = :email and r.status = :status")
    List<Request> getAgentHistory(String email, RequestStatus status);

    @Query("select r from Request r where r.agent.email = :email and r.status = :status and r.isScored = :value")
    List<Request> getAllForAgentScore(String email, RequestStatus status, Boolean value);

    @Query("select r from Request r where r.agent.email = :email and r.status != :status1 and r.status != :status2")
    List<Request> getAgentDashboard(String email, RequestStatus status1, RequestStatus status2);

    @Query("select r from Request r where r.customer.email = :email")
    List<Request> findAllByCustomerEmail(String email);

    @Query("select r from Request r where r.customer.email = :email and r.status = :status")
    Optional<Request> findAllByCustomerAndStatus(String email, RequestStatus status);

    @Query("select r from Request r where r.status = :status order by r.assignedTime asc")
    List<Request> getAllAssignedRequests(RequestStatus status);
}
