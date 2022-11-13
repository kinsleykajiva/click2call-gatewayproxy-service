package org.zihub.routingservice.repositories;

import org.zihub.routingservice.dbaccess.Role;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    List<Role> findAllById(int id);
    Role findRoleById(int id);


}