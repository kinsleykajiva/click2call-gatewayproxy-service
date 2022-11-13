package org.zihub.routingservice.repositories;

import org.zihub.routingservice.dbaccess.Company;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CompanyRepository extends JpaRepository<Company, Integer> {


    Company findByTitle(String nameTitle);
    Company findById(int id);
    Company findByIdAndIsDeleted(int id, int isDeleted);

    Company findByApiWidgetAccessToken(String apiWidgetAccessToken);



}