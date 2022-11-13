package org.zihub.routingservice.repositories;

import org.zihub.routingservice.dbaccess.Team;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Integer> {


    List<Team> findTeamsById(int id);
    List<Team> findTeamsByOrganisationId(int organisationId);


    List<Team> findTeamsByCompanyId(int companyId);
    List<Team> findTeamsByOrganisationIdAndCompanyId(int organisationId, int companyId);


}