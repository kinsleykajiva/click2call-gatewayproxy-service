package org.zihub.routingservice.repositories;


import org.zihub.routingservice.dbaccess.TeamGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamGroupRepository extends JpaRepository<TeamGroup, Integer> {

    List<TeamGroup> findTeamGroupByTeamId(int teamId);

    List<TeamGroup> findTeamGroupByUserId(int userId);

    TeamGroup findTeamGroupByTeamIdAndUserId(int teamId, int userId);

    TeamGroup deleteTeamGroupByTeamId(int teamId);
    TeamGroup deleteAllByTeamId(int teamId);

    TeamGroup deleteTeamGroupByUserId(int userId);

    TeamGroup deleteTeamGroupByUserIdAndTeamId(int userId, int teamId);


    TeamGroup deleteTeamGroupByTeamIdAndUserId(int teamId, int userId);


}