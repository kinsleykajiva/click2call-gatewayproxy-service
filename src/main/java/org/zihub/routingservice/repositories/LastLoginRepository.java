package org.zihub.routingservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zihub.routingservice.dbaccess.LastLogin;

import java.util.List;

public interface LastLoginRepository extends JpaRepository<LastLogin, Integer> {


    List<LastLogin> findByUserId(Integer userId);

}