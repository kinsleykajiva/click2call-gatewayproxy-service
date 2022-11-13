package org.zihub.routingservice.repositories;

import org.zihub.routingservice.dbaccess.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface OrganisationRepository extends JpaRepository<Organisation, Integer> {


    List<Organisation> findByCompanyId(int companyId);
    Organisation findByIdAndCompanyId(int companyId, int id);
}