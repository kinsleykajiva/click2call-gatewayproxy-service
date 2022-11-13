package org.zihub.routingservice.repositories;

import org.zihub.routingservice.dbaccess.Status;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StatusRepository extends JpaRepository<Status, Integer> {
}