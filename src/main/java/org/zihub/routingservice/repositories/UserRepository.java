package org.zihub.routingservice.repositories;

import org.zihub.routingservice.dbaccess.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

   User findUserByEmail(String email);
   User findUserByEmailAndCompanyId(String email, int companyId);
    User findUserById(int id);
    User findUserByAccountActivationCode(String accountActivationCode);
    User findUserByIdAndCompanyId( int id, int companyId);
    User findByAccountActivationCode(String accountActivationCode);
    User save(User user);
     //This is using a named query method
    List<User> findUserByCompanyId(int companyId);
    List<User> findUserByIsEmailAddressVerified(int isEmailAddressVerified);
    List<User> findByCompanyIdAndIsDeleted(int companyId, int isDeleted);

}