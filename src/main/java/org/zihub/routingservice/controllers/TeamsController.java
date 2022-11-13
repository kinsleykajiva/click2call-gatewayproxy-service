package org.zihub.routingservice.controllers;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zihub.routingservice.dbaccess.Team;
import org.zihub.routingservice.dbaccess.TeamGroup;
import org.zihub.routingservice.dbaccess.User;
import org.zihub.routingservice.pojos.JWTDataPojo;
import org.zihub.routingservice.repositories.RoleRepository;
import org.zihub.routingservice.repositories.TeamGroupRepository;
import org.zihub.routingservice.repositories.TeamRepository;
import org.zihub.routingservice.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zihub.routingservice.utils.Utils;


import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.zihub.routingservice.utils.Utils.*;


@Slf4j
@RestController
@RequestMapping("/auth/secured/api/v1/teams")
public class TeamsController {
    @Value("${app.secrete}")
    private String SecreteKey;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private TeamRepository teamRepository;// teams list of team names

    @Autowired
    private TeamGroupRepository teamGroupRepository; // list of people in teams

    @Autowired
    private UserRepository userRepository; // list of people in the company
    @Autowired
    private RoleRepository roleRepository;


    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getAllTeams() {
        JWTDataPojo decoded;
        try {
            decoded = Utils.tokenDecoder(request, SecreteKey);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "access", false,
                                    "message", "Failed to access resource "
                            )
                    );
        }
        try {
            log.info("decoded:XXXXX::: "+decoded.getUserId());
            List<Team> teams = teamRepository.findTeamsByCompanyId(decoded.getCompanyId());
            teams.forEach(team -> {
                var group = teamGroupRepository.findTeamGroupByTeamId(team.getId());
                team.setUsersCounter(group.size());
            });

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "List of Teams in Company",
                            "data", Map.of(
                                    "teamsNamesList", teams
                            ))
                    );
        } catch (Exception e) {
            log.error("error", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }

    }

    @RequestMapping(value = "/users", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getOrgUsers() {
        JWTDataPojo decoded;
        try {
            decoded = Utils.tokenDecoder(request, SecreteKey);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "access", false,
                                    "message", "Failed to access resource "
                            )
                    );
        }
        try {

            List<User> userList = userRepository.findUserByCompanyId(decoded.getCompanyId());
            userList.forEach(user -> {

                var teamGroups = teamGroupRepository.findTeamGroupByUserId(user.getId());
                teamGroups.forEach(t -> {
                    var teams = teamRepository.findTeamsById(t.getTeamId());
                    teams.forEach(team -> team.setUsersCounter(teamGroups.size()));
                    t.setTeams(teams);

                });
                var roles = roleRepository.findAllById(user.getRoleId());
                user.setTeamGroups(teamGroups);
                user.setRoles(roles);

            });


            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "List of users Teams",
                            "data", Map.of(
                                    "usersInCompany", userList
                            ))
                    );
        } catch (Exception e) {
            log.error("error", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }

    }

    @RequestMapping(value = "/save-new-team", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> saveNewTeam(@RequestBody Map<String, Object> payload) {
        JWTDataPojo decoded;
        try {
            decoded = Utils.tokenDecoder(request, SecreteKey);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "access", false,
                                    "message", "Failed to access resource "
                            )
                    );
        }
        try {


            var teamName = String.valueOf(payload.get("teamName"));
            var organisationId = Integer.parseInt(String.valueOf(payload.get("organisationId")));
            var teamDescription = String.valueOf(payload.get("teamDescription"));
            var usersIds = String.valueOf(payload.get("usersIds"));

            Team team = new Team();
            team.setOrganisationId(organisationId);
            team.setCompanyId(decoded.getCompanyId());
            team.setCreatedByUserId(decoded.getUserId());
            team.setDescription(teamDescription);
            team.setTitle(teamName);

            Team TeamResult = teamRepository.save(team);
            // save the parent user

            var teamGroup_ = new TeamGroup();
            teamGroup_.setTeamId(TeamResult.getId());
            teamGroup_.setUserId((decoded.getUserId()));
            teamGroupRepository.save(teamGroup_);

            if(usersIds == null || usersIds.isEmpty() || usersIds.equals("null")){
                List<Team> teams = teamRepository.findTeamsByCompanyId(decoded.getCompanyId());
                teams.forEach(team1 -> {
                    var group = teamGroupRepository.findTeamGroupByTeamId(team1.getId());
                    team1.setUsersCounter(group.size());
                });

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", true,
                                        "message", "Saved Team Details",
                                        "data", Map.of(
                                                 "teamsNamesList", teams,
                                                "newlyCreatedTeam", TeamResult,
                                                "newlyCreatedTeamGroup", teamGroupRepository.findTeamGroupByTeamId(TeamResult.getId()))
                                )
                        );
            }

            if (usersIds.trim().contains(",")) {

                String[] usersIdsArray = usersIds.split(",");
                Arrays.stream(usersIdsArray).forEach(userId -> {
                    if (userId != null && !userId.isEmpty() && !userId.equals("null")) {

                        var teamGroup = new TeamGroup();
                        teamGroup.setTeamId(TeamResult.getId());
                        teamGroup.setUserId(Integer.parseInt(userId));
                        teamGroupRepository.save(teamGroup);
                    }
                });



            } else {

                var teamGroup = new TeamGroup();
                teamGroup.setTeamId(TeamResult.getId());
                teamGroup.setUserId(Integer.parseInt(usersIds));
                teamGroupRepository.save(teamGroup);


            }

            List<Team> teams = teamRepository.findTeamsByCompanyId(decoded.getCompanyId());
            teams.forEach(team1 -> {
                var group = teamGroupRepository.findTeamGroupByTeamId(team1.getId());
                team1.setUsersCounter(group.size());
            });

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "Saved Team Details",
                                    "data", Map.of(
                                            "teamsNamesList", teams,
                                            "newlyCreatedTeam", TeamResult,
                                            "newlyCreatedTeamGroup", teamGroupRepository.findTeamGroupByTeamId(TeamResult.getId()))
                            )
                    );



        } catch (Exception e) {
            log.error("error", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }

    }
    @RequestMapping(value = "/edit-team", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> editTeam(@RequestBody Map<String, Object> payload) {
        JWTDataPojo decoded;
        try {
            decoded = Utils.tokenDecoder(request, SecreteKey);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "access", false,
                                    "message", "Failed to access resource "
                            )
                    );
        }
        try {


            int recordId = Integer.parseInt(String.valueOf(payload.get("recordId")));
            var teamName = String.valueOf(payload.get("teamName"));
            var organisationId = Integer.parseInt(String.valueOf(payload.get("organisationId")));
            var teamDescription = String.valueOf(payload.get("teamDescription"));
            var usersIds = String.valueOf(payload.get("usersIds"));

            Team team =teamRepository.getOne(recordId);
            team.setOrganisationId(organisationId);

            team.setDescription(teamDescription);
            team.setTitle(teamName);

            Team TeamResult = teamRepository.save(team);
            // first delete all the team group members
            teamGroupRepository.deleteAllByTeamId(team.getId());
            // save the parent user

            var teamGroup_ = new TeamGroup();
            teamGroup_.setTeamId(TeamResult.getId());
            teamGroup_.setUserId((decoded.getUserId()));
            teamGroupRepository.save(teamGroup_);

            if(usersIds == null || usersIds.isEmpty() || usersIds.equals("null")){
                List<Team> teams = teamRepository.findTeamsByCompanyId(decoded.getCompanyId());
                teams.forEach(team1 -> {
                    var group = teamGroupRepository.findTeamGroupByTeamId(team1.getId());
                    team1.setUsersCounter(group.size());
                });

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", true,
                                        "message", "Saved Team Details",
                                        "data", Map.of(
                                                 "teamsNamesList", teams,
                                                "newlyCreatedTeam", TeamResult,
                                                "newlyCreatedTeamGroup", teamGroupRepository.findTeamGroupByTeamId(TeamResult.getId()))
                                )
                        );
            }

            if (usersIds.trim().contains(",")) {

                String[] usersIdsArray = usersIds.split(",");
                Arrays.stream(usersIdsArray).forEach(userId -> {
                    if (userId != null && !userId.isEmpty() && !userId.equals("null")) {

                        var teamGroup = new TeamGroup();
                        teamGroup.setTeamId(TeamResult.getId());
                        teamGroup.setUserId(Integer.parseInt(userId));
                        teamGroupRepository.save(teamGroup);
                    }
                });



            } else {

                var teamGroup = new TeamGroup();
                teamGroup.setTeamId(TeamResult.getId());
                teamGroup.setUserId(Integer.parseInt(usersIds));
                teamGroupRepository.save(teamGroup);


            }

            List<Team> teams = teamRepository.findTeamsByCompanyId(decoded.getCompanyId());
            teams.forEach(team1 -> {
                var group = teamGroupRepository.findTeamGroupByTeamId(team1.getId());
                team1.setUsersCounter(group.size());
            });

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "Saved Team Details",
                                    "data", Map.of(
                                            "teamsNamesList", teams,
                                            "newlyCreatedTeam", TeamResult,
                                            "newlyCreatedTeamGroup", teamGroupRepository.findTeamGroupByTeamId(TeamResult.getId()))
                            )
                    );



        } catch (Exception e) {
            log.error("error", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }

    }
    @RequestMapping(value = "/add-team-member", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> addNewMemeber(@RequestBody Map<String, Object> payload) {
        JWTDataPojo decoded;
        try {
            decoded = Utils.tokenDecoder(request, SecreteKey);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "access", false,
                                    "message", "Failed to access resource "
                            )
                    );
        }
        try {

            if(payload.get("users") == null || payload.get("users").toString().trim().isEmpty()){
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Missing users"
                                )
                        );
            }
            if(payload.get("teamId") == null || payload.get("teamId").toString().trim().isEmpty()){
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,

                                        "message", "Missing teamId"
                                )
                        );
            }

            var users = String.valueOf(payload.get("users"));
            final var teamId = String.valueOf(payload.get("teamId"));
            ObjectMapper mapper = new ObjectMapper();


            Map<String, Object> map = mapper.readValue(users, new TypeReference<>() {});
            log.info("users: {}", users);
            map.forEach((key1, value1) -> {
                log.info("Key : " + key1 + " Value : " + value1);
                var splitString=  value1.toString()
                        .replace("{" , "")
                        .replace("}","")
                        .split(",");
                // {userTypeId=1}
                Arrays.stream(splitString).forEach(s -> {
                    if (s != null && !s.isEmpty() && !s.equals("null")) {
                        final int[] idx = {0};
                        Arrays.stream(s.split("=")).forEach(ss -> {
                            if (ss != null && !ss.isEmpty() && !ss.equals("null")) {
                                if (idx[0] == 1) {
                                    int userTypeId = Integer.parseInt(ss);
                                    log.info("userTypeId: {}", userTypeId);
                                    var checkUserExist = userRepository.findUserByEmail(key1);
                                    var team = teamRepository.getOne(Integer.parseInt(teamId));
                                    if (checkUserExist == null) {

                                        notifyNewUserToJoinTeam(decoded, key1,team.getTitle(), Integer.parseInt(teamId));

                                    }else{
                                        notifyUserToJoinedTeam(decoded, key1,team.getTitle(), Integer.parseInt(teamId), checkUserExist.getFullName());
                                        // if the user is in the team already
                                       var teamChecker = teamGroupRepository.findTeamGroupByTeamIdAndUserId(Integer.parseInt(teamId), checkUserExist.getId());
                                       if(teamChecker == null) {
                                           // save to team group
                                           var grp = new TeamGroup();

                                           grp.setUserId(checkUserExist.getId());
                                           grp.setTeamId(Integer.valueOf(teamId));

                                           // new TeamGroup(team.getId(), checkUserExist.getId(), userTypeId)
                                           teamGroupRepository.save(grp);
                                       }
                                    }
                                }
                            }
                            idx[0]++;
                        });

                    }
                });

            });

            //  {"cvc@gfg.com":{"userTypeId":"1"}}




            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "Saved Team Member Details"

                            )
                    );

        } catch (Exception e) {
            log.error("error", e);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to complete request"
                            )
                    );
        }

    }

    @RequestMapping(value = "/available-users", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getAllUsers() {
        JWTDataPojo decoded;
        try {
            decoded = Utils.tokenDecoder(request, SecreteKey);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "access", false,
                                    "message", "Failed to access resource "
                            )
                    );
        }
        try {

            var result = userRepository.findUserByCompanyId(decoded.getCompanyId());

           /* var usersArray = new ArrayList<>();

            result.forEach(user -> {
                var response =
                Map.of("fullName", user.getFullName(),"email", user.getEmail(),"profileImageUrl", user.getProfileImageUrl());

                usersArray.add(response);
            });*/


            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "List of users to join  a team",
                            "data", Map.of(

                                    "usersList", result
                            ))
                    );
        } catch (Exception e) {
            log.error("error", e);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }

    }

    @RequestMapping(value = "/groups/all", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getAllGroupsTeams(@RequestParam("teamId") String teamId) {
        JWTDataPojo decoded;
        try {
            decoded = Utils.tokenDecoder(request, SecreteKey);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "access", false,
                                    "message", "Failed to access resource "
                            )
                    );
        }
        try {


            int id = Integer.parseInt(teamId);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "List of Team members in a team group",
                            "data", Map.of(
                                    "teamIdUsed", id,
                                    "teamsNamesList", teamGroupRepository.findTeamGroupByTeamId(id)
                            ))
                    );
        } catch (Exception e) {
            log.error("error", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }

    }

    @RequestMapping(value = "/delete-team", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> deleteTeam(@RequestBody Map<String, Object> payload) {
        JWTDataPojo decoded;
        try {
            decoded = Utils.tokenDecoder(request, SecreteKey);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "access", false,
                                    "message", "Failed to access resource "
                            )
                    );
        }
        try {



            var id = Integer.parseInt(String.valueOf(payload.get("teamId")));
           List<TeamGroup> tgrp =  teamGroupRepository.findTeamGroupByTeamId(id);
            teamGroupRepository.deleteAll(tgrp);

            teamRepository.deleteById(id);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "Deleted team if id " + id
                            )
                    );
        } catch (Exception e) {
            log.error("error", e);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }

    }

}
