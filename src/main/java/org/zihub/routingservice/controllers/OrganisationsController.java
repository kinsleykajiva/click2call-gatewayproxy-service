package org.zihub.routingservice.controllers;


import org.zihub.routingservice.dbaccess.Organisation;
import org.zihub.routingservice.dbaccess.Team;
import org.zihub.routingservice.dbaccess.User;
import org.zihub.routingservice.pojos.JWTDataPojo;
import org.zihub.routingservice.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zihub.routingservice.utils.Utils;


import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.http.entity.ContentType.*;


@Slf4j
@RestController
@RequestMapping("/auth/secured/api/v1/organisations")
public class OrganisationsController {
    @Value("${app.secrete}")
    private String SecreteKey;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private OrganisationRepository organisationRepository;
    @Autowired
    private UserRepository userRepository; // list of people in the company
    @Autowired
    private TeamRepository teamRepository;// teams list of team names

    @Autowired
    private TeamGroupRepository teamGroupRepository; // list of people in teamsed
    @Autowired
    private RoleRepository roleRepository;


    @PostMapping(value = "/update", produces = "application/json")
    public ResponseEntity<?> updateOrg(@RequestBody Map<String, Object> payload) {
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

            var orgName = String.valueOf(payload.get("orgName"));
            if (orgName.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Name is required"
                                )
                        );
            }
            var orgId = String.valueOf(payload.get("orgId"));

            var orgUrl = String.valueOf(payload.get("orgUrl"));
            var orgTax = String.valueOf(payload.get("orgTax"));
            var orgCountry = String.valueOf(payload.get("orgCountry"));
            var orgUnitStreet = String.valueOf(payload.get("orgUnitStreet"));
            var orgTown = String.valueOf(payload.get("orgTown"));
            var orgState = String.valueOf(payload.get("orgState"));
            var orgPostCode = String.valueOf(payload.get("orgPostCode"));

            List<Organisation> org = organisationRepository.findAllById(Collections.singleton(Integer.parseInt(orgId + "")));

            org.get(0).setTitle(orgName);
            org.get(0).setCity(orgTown.isEmpty() ? null : orgTown);
            org.get(0).setCompanyTax(orgTax.isEmpty() ? null : orgTax);
            org.get(0).setPostalCode(orgPostCode.isEmpty() ? null : orgPostCode);
            org.get(0).setProvince(orgState.isEmpty() ? null : orgState);
            org.get(0).setCountry(orgCountry.isEmpty() ? null : orgCountry);
            org.get(0).setUrl(orgUrl.isEmpty() ? null : orgUrl);
            org.get(0).setCompanyAddress(orgUnitStreet.isEmpty() ? null : orgUnitStreet);


            Organisation result = organisationRepository.save(org.get(0));

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "updated Details",
                            "data", Map.of(
                                    "Organisation", result

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

    @PostMapping(value = "/update-profile-image", produces = "application/json")
    public ResponseEntity<?> updateOrgProfileImage(@RequestParam("file") MultipartFile file, @RequestParam(value = "orgId") String orgId) {
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

            if (file.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                        "success", false,
                                        "message", "File is Required"
                                )
                        );
            }
            //Check if the file is an image
            if (!Arrays.asList(IMAGE_PNG.getMimeType(),
                    IMAGE_BMP.getMimeType(),
                    IMAGE_GIF.getMimeType(),
                    IMAGE_JPEG.getMimeType()).contains(file.getContentType())) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                        "success", false,
                                        "message", "File type is rejected"
                                )
                        );
            }
            //get file metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("Content-Type", file.getContentType());
            metadata.put("Content-Length", String.valueOf(file.getSize()));
            //Save Image in S3 and then save
            String path = String.format("%s/%s", "clicktocallbucket/profiles", UUID.randomUUID());
            String fileName = String.format("%s", file.getOriginalFilename().replaceAll(" ", ""));
            log.error("xxxxxxxxxxxxxyyy::   " + orgId);
            log.error("xxxxxxxxxxxxx");
            log.error(path);
            log.error(fileName);
            try {
                // https://docs.aws.amazon.com/AmazonS3/latest/userguide/example-bucket-policies.html#example-bucket-policies-use-case-2
                String url = Utils.uploadFileToAWSS3(path, fileName, Optional.of(metadata), file.getInputStream());
                // fileStore.upload(path, fileName, Optional.of(metadata), file.getInputStream());
                String fullUrl = url + "/" + fileName;
                log.info(url + ":::::url:::");
                List<Organisation> org = organisationRepository.findAllById(Collections.singleton(Integer.parseInt(orgId + "")));

                org.get(0).setProfileImageUrl(fullUrl);
                organisationRepository.save(org.get(0));

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                "success", true,
                                "message", "uploaded File",
                                "data", Map.of(

                                        "fileName", fileName,
                                        "url", fullUrl
                                ))
                        );

            } catch (IOException e) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Failed to upload File"
                                )
                        );
            }

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

    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getAll() {
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

            List<Organisation> organisations = organisationRepository.findByCompanyId(decoded.getCompanyId());


            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "List of organisations in Company",
                            "data", Map.of(
                                    "orgs", organisations
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

    // this call is expensive
    @RequestMapping(value = "/organisations", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getAllUsersInOrgnisation() {
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

            List<Organisation> organisations = organisationRepository.findByCompanyId(decoded.getCompanyId());
            Set<User> users = new HashSet<>();
            for (Organisation organisation : organisations) {
                var teamsList = teamRepository.findTeamsByOrganisationId(organisation.getId());

                teamsList.forEach(team -> {
                    var teamGropsList = teamGroupRepository.findTeamGroupByTeamId(team.getId());
                    teamGropsList.forEach(teamGroup -> {
                        var user_ = userRepository.findUserByIdAndCompanyId(teamGroup.getUserId(), decoded.getCompanyId());
                        if (user_ != null) {

                            var role = roleRepository.findRoleById(user_.getRoleId());
                            if (role != null) {
                                user_.setRole(role);
                            }
                            users.add(user_);
                            teamGroup.setUser(user_);
                        }

                    });
                    team.setUsersCounter(teamGropsList.size());
                    team.setTeamGroups(teamGropsList);
                });
                var usersResult = userRepository.findUserByCompanyId(decoded.getCompanyId());
                usersResult.forEach(userR -> {
                    var role = roleRepository.findRoleById(userR.getRoleId());
                    userR.setRole(role);
                });
                organisation.setUsers( Set.copyOf(usersResult));

                organisation.setTeamsList(teamsList);
            }

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "List of organisations in Company",
                            "data", Map.of(
                                    "orgs", organisations
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


    /**
     * this call could have been made in teams controller but this will help make it better
     */
    @RequestMapping(value = "/remove-user", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> removeUserFromOrganisation(@RequestBody Map<String, Object> payload) {
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
            int userId = Integer.parseInt(payload.get("userId").toString());
            int orgId = Integer.parseInt(payload.get("orgId").toString());
            log.error("userId: " + userId + " orgId: " + orgId);

            // 1. start by removing from teams
            var resut = teamRepository.findTeamsByOrganisationIdAndCompanyId(orgId, decoded.getCompanyId());
            for (Team team : resut) {
                log.error("userId: " + userId + " team.getId(): " + team.getId());
                teamGroupRepository.deleteTeamGroupByUserIdAndTeamId(userId, team.getId());
            }
            // 2. remove user from other items in the organisation
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,

                                    "message", "removed user from organisation"
                            )
                    );
        } catch (Exception e) {
            log.error("error", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }
    }

    @RequestMapping(value = "/save-new", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> saveNewOrg(@RequestBody Map<String, Object> payload) {
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


            var OrganisationName = String.valueOf(payload.get("OrganisationName"));
            Organisation save = new Organisation();
            save.setTitle(OrganisationName);
            save.setCompanyId(decoded.getCompanyId());
            save.setSavedByUserId(decoded.getUserId());
            Organisation organisation = organisationRepository.save(save);


            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "new Orgnisation saved",
                            "data", Map.of(
                                    "orgId", organisation.getId()
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


}
