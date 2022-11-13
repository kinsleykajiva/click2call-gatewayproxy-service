package org.zihub.routingservice.controllers;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.uuid.Generators;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.zihub.routingservice.dbaccess.*;
import org.zihub.routingservice.pojos.JWTDataPojo;
import org.zihub.routingservice.repositories.*;
import org.zihub.routingservice.services.OTPService;
import org.zihub.routingservice.services.StorageService;
import org.zihub.routingservice.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

import static org.apache.http.entity.ContentType.*;
import static org.apache.http.entity.ContentType.IMAGE_JPEG;
import static org.bouncycastle.cms.RecipientId.password;
import static org.zihub.routingservice.utils.Utils.makeRequestServices;
import static org.zihub.routingservice.utils.Utils.randomString;


@RestController
@Slf4j
@RequestMapping("/auth/api/v1/users")
public class UsersController {

    @Value("${app.secrete}")
    private String SecreteKey;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamGroupRepository teamGroupRepository; //
    @Autowired
    private LastLoginRepository  lastLoginRepository; //
    @Autowired
    private OrganisationRepository organisationRepository; //
    @Autowired
    private WidgetRepository widgetRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    public OTPService otpService;

    @Autowired
    private TeamRepository teamRepository;// teams list of team names

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
    public String index() {


        return "index response";
    }

    @RequestMapping(value = "/roles", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getRoles() {

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
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "roles ",
                                    "data", Map.of(
                                            "roles", roleRepository.findAll())
                            )
                    );
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource "
                            )
                    );
        }

    }

    @RequestMapping(value = "/user", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getUser(@RequestParam(value = "userId") String userId) {


        try {

            User user = userRepository.findUserById(Integer.parseInt(userId));
            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "User Not found"
                                )
                        );
            }

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "User Primary Details",
                                    "data", Map.of(
                                            "userDetails", user)
                            )
                    );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource "
                            )
                    );
        }
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getUsers(@RequestParam(value = "companyId") String companyId) {


        try {

            var users = userRepository.findUserByCompanyId(Integer.parseInt(companyId));
            if (users == null) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Account not activated,invalid link used"
                                )
                        );
            }
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "Users list in a company",
                                    "data", Map.of(
                                            "users", users)
                            )
                    );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource "
                            )
                    );
        }
    }

    @RequestMapping(value = "/company-users", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getCompanyUsers() {
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

            var users = userRepository.findUserByCompanyId(decoded.getCompanyId());
            if (users == null) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", ""
                                )
                        );
            }
            List<Map<Object, Object>> usersList = new ArrayList<>();
            users.forEach(user -> {
//            log.info("user : "+user.getId());
                var teamGroups = teamGroupRepository.findTeamGroupByUserId(user.getId());
                teamGroups.forEach(t -> {
                    var teams = teamRepository.findTeamsById(t.getTeamId());
                    teams.forEach(team -> {
                        team.setUsersCounter(teamGroups.size());
                        team.setOrganisation(organisationRepository.findByIdAndCompanyId(decoded.getCompanyId(), team.getOrganisationId()));
                    });
                    //   t.setTeams(teams);
                    t.setTeam(teams.get(0));


                });
                user.setTeamGroups(teamGroups);


            });
            int[] i = {0};
            users.forEach(user -> {
                var returnVal = new HashMap<>(Map.of());
                Role role = roleRepository.getOne(user.getRoleId());
                if (i[0]++ == 0) {//first user
                    returnVal.put("isPrimary", true);
                } else {
                    returnVal.put("isPrimary", false);
                }
                returnVal.put("id", user.getId());
                returnVal.put("isDeleted", user.getIsDeleted());
                returnVal.put("isEmailAddressVerified", user.getIsEmailAddressVerified());
                returnVal.put("fullName", user.getFullName());
                returnVal.put("email", user.getEmail());
                returnVal.put("roleId", user.getId());
                returnVal.put("role", role.getTitle());
                returnVal.put("dateCreated", user.getDateCreated());
                returnVal.put("teamGroups", user.getTeamGroups());

                usersList.add(returnVal);
            });

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "Users list in a company",
                                    "data", Map.of(
                                            "users", usersList)
                            )
                    );
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource "
                            )
                    );
        }


    }


    @RequestMapping(value = "/activate-account", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> activateUserAccount(@RequestBody Map<String, Object> payload) {

        try {
            String key = String.valueOf(payload.get("key"));
            User user = userRepository.findByAccountActivationCode(key);
            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Account not activated,invalid link used"
                                )
                        );
            }
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "Account Activated",
                                    "data", Map.of(
                                            "email", user.getEmail(),
                                            "fullName", user.getFullName())
                            )
                    );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to acticate Account"
                            )
                    );
        }


    }

    @RequestMapping(value = "/cron-job/notify-users-to-activate-verify-account", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> askUsersToActivateUserAccount(@RequestBody Map<String, Object> payload) {

        try {
            //  String key = String.valueOf(payload.get("key"));
            var users = userRepository.findUserByIsEmailAddressVerified(0);
            if (users == null) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "No Account Found"
                                )
                        );
            }
            users.forEach(user -> {
                try {
                    UUID uuid = Generators.randomBasedGenerator().generate();
                    String activationCode = (uuid + randomString(14))
                            .replace("-", "")
                            .replace(":", "");
                    user.setAccountActivationCode(activationCode);

                    userRepository.save(user);

                    String jsonPost = new JSONObject()
                            .put("email", String.valueOf(user.getEmail()))
                            .put("name", user.getFullName())
                            .put("user_full_name", user.getFullName())
                            .put("link_activate", Utils.WEBSITE_BaseUrl + "account-handlers?verify=" + activationCode)
                            .toString();


                    String emailResponse = makeRequestServices(Utils.SERVICES_NAMES.NOTIFICATIONS_SERVICE
                            + "users/account-verification-verification", jsonPost);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "emails Sent")

                    );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to acticate Account"
                            )
                    );
        }


    }
    @Autowired
    private StorageService service;
    @RequestMapping(value = "/update-user-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> updateuserProfile(@RequestParam(value = "file" ,required = false) MultipartFile file,
                                              @RequestParam(value = "fullName") String fullName,
                                              @RequestParam(value = "email") String email
    ) {
        JWTDataPojo decoded;
        Map<String,String> ErrorMessage = new HashMap();
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




            String filesUploadResult = "";

            List<String> fileUrls = new ArrayList<>();
            if(file != null && !file.isEmpty()){
            try{



                filesUploadResult = Utils.uploadFileToAWSS3Node("agents/"+decoded.getUserId() ,file);
                var obj=  new JSONObject(filesUploadResult+"");
                if(! obj.getBoolean("success")){
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(Map.of(
                                            "success", false,
                                            "succefilesUploadResultss", filesUploadResult,
                                            "message", "File upload Failed is rejected"
                                    )
                            );
                }
                var fileUrl = obj.getJSONObject("data").getJSONArray("fileObjects");

                for (int i = 0; i < fileUrl.length(); i++) {
                    var fileObject = fileUrl.getJSONObject(i);
                    fileUrls.add(fileObject.getJSONObject("data").getString("Location"));

                }
            }catch (Exception e) {
                ErrorMessage.put("stat2" , e.getMessage());
                /*return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                        "success", false,

                                "succefilesUploadResultss", e.getMessage(),
                                        "message", "File upload Failed is rejected"
                                )
                        );*/
            }
            }



            User user = userRepository.findUserById(decoded.getUserId());

            if (user == null) {

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "User not found"
                                )
                        );
            }
            if(fileUrls.size() > 0) {
                user.setProfileImageUrl(fileUrls.get(0));
            }
            user.setFullName(fullName);
            user.setEmail(email);

            userRepository.save(user);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "profile uploaded successfully",
                                    "data", Map.of(
                                            "profileImageUrl", user.getProfileImageUrl(),
                                            "fullName", user.getFullName(),
                                            "email", user.getEmail()
                                    )
                            )

                    );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "error", e.getMessage(),
                                    "error23", ErrorMessage,
                                    "message", "Failed to access  Account resource"
                            )
                    );
        }

    }
    @RequestMapping(value = "/activate-verify-account", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> verifiyUserAccount(@RequestBody Map<String, Object> payload) {

        try {
              String key = String.valueOf(payload.get("verify"));
            var user = userRepository.findUserByAccountActivationCode(key);
            if (user == null) {

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Code Invalid or expired Please wait for another link to be sent to your email ,if no email is sent (in 12 hrs) please know that your account is already verified"
                                )
                        );
            }
            user.setIsEmailAddressVerified(1);
            user.setAccountActivationCode(key + "-done");
            userRepository.save(user);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "Please note that the account has been verified via email successfully")

                    );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to activate Account"
                            )
                    );
        }


    }


    @RequestMapping(value = "/reset-account-password", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, Object> payload) {

        try {
            String email = String.valueOf(payload.get("email"));
            String newPassword = String.valueOf(payload.get("newPassword"));
            String otp = String.valueOf(payload.get("otp"));
            User user = userRepository.findUserByEmail(email);

            if (otp == null || otp.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "otp cannot be empty"
                                )
                        );
            }
            if (email == null || email.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "email cannot be empty"
                                )
                        );
            }
            if (newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Password cannot be empty"
                                )
                        );
            }

            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Account not found"
                                )
                        );
            }


            int serverOtp = otpService.getOtp(email);
            log.info("serverOtp: " + serverOtp);
            if ((serverOtp >= 0) && (serverOtp == Integer.parseInt(otp))) {
                otpService.clearOTP(email);


                user.setPasswordHash(passwordEncoder.encode(newPassword));
                userRepository.save(user);

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", true,
                                        "message", "Account password reset",
                                        "data", Map.of(
                                                "email", user.getEmail(),
                                                "password", newPassword,
                                                "fullName", user.getFullName())
                                )
                        );
            } else {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Failed, try again , Code Expired or wrong code"
                                )
                        );
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to acticate Account"
                            )
                    );
        }


    }


    @RequestMapping(value = "/refered-register", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> referedRegistration(@RequestBody Map<String, Object> payload) {

        if (!payload.containsKey("email") || !payload.containsKey("companyId") || !payload.containsKey("password") || !payload.containsKey("fullName") || !payload.containsKey("userId")) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Missing Fields"
                            )
                    );
        }
        int userId = Integer.parseInt(String.valueOf(payload.get("userId")));
        int companyId = Integer.parseInt(String.valueOf(payload.get("companyId")));
        String email = (String.valueOf(payload.get("email")));
        String fullName = (String.valueOf(payload.get("fullName")));
        String password = (String.valueOf(payload.get("password")));
        //
        if (companyRepository.findById(companyId) == null) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Access Denied ,please revisit the link and open again"
                            )
                    );
        }

        if (userRepository.findUserById(userId) == null) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Access Denied ,please revisit the link and open again"
                            )
                    );
        }

        if (userRepository.findUserByEmail(email) != null) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "User Already has account please login"
                            )
                    );
        }
        User user = new User();
        user.setRoleId(3);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setCompanyId(companyId);
        user.setPasswordHash(passwordEncoder.encode(password));
        User reesult = userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "success", true,
                        "message", "Registered new User, Please  login .",
                        "data", reesult
                ));
    }


    @RequestMapping(value = "/register-company-user", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> addRegisterNewCompanyUser(@RequestBody Map<String, Object> payload) {
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
            if (!payload.containsKey("email") || !payload.containsKey("password") || !payload.containsKey("fullName")) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Missing Fields"
                                )
                        );
            }

            String email = (String.valueOf(payload.get("email")));
            String fullName = (String.valueOf(payload.get("fullName")));
            String password = (String.valueOf(payload.get("password")));
            String userType = (String.valueOf(payload.get("userType")));


            if (userRepository.findUserByEmailAndCompanyId(email, decoded.getCompanyId()) != null) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "User Already has account please login"
                                )
                        );
            }
            User user = new User();
            user.setRoleId(Integer.valueOf(userType));
            user.setFullName(fullName);
            user.setEmail(email);
            user.setIsEmailAddressVerified(0);
            user.setCompanyId(decoded.getCompanyId());
            user.setPasswordHash(passwordEncoder.encode(password));
            User reesult = userRepository.save(user);
            Company senderCompny = companyRepository.findById(decoded.getCompanyId());

            // send email to notify user
            final String json = new JSONObject()
                    .put("email", email)
                    .put("password", password)
                    .put("name", "")
                    .put("theSenderName", decoded.getFullName())
                    .put("Username", email)
                    .put("companyName", senderCompny.getTitle())
                    .put("userFullName", fullName)
                    .toString();
            makeRequestServices(decoded, Utils.SERVICES_NAMES.NOTIFICATIONS_SERVICE + "users/user-account-created-by-user", json);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "Registered new User, Please  login .Also check email for inviation",
                            "data", reesult
                    ));
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

    @RequestMapping(value = "/delete-company-user", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> deleteUserAcc(@RequestBody Map<String, Object> payload) {
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
            if (!payload.containsKey("id")) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Missing Fields"
                                )
                        );
            }

            String id = (String.valueOf(payload.get("id")));
            User user = userRepository.getOne(Integer.valueOf(id));

            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Access Error ,user not found"
                                )
                        );
            }

            user.setIsDeleted(1);
            userRepository.save(user);
            Company senderCompny = companyRepository.findById(decoded.getCompanyId());

            // send email to notify user
            final String json = new JSONObject()
                    .put("email", user.getEmail())
                    .put("password", password)
                    .put("name", user.getFullName())
                    .put("theSenderName", decoded.getFullName())
                    .put("Username", user.getFullName())
                    .put("companyName", senderCompny.getTitle())
                    .put("userFullName", user.getFullName())
                    .toString();
            makeRequestServices(decoded, Utils.SERVICES_NAMES.NOTIFICATIONS_SERVICE + "users/user-account-deleted-by-user", json);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "User Account Deleted"
                    ));
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

    @RequestMapping(value = "/activate-company-user", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> activateUserAcc(@RequestBody Map<String, Object> payload) {
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
            if (!payload.containsKey("id")) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Missing Fields"
                                )
                        );
            }

            String id = (String.valueOf(payload.get("id")));
            User user = userRepository.getOne(Integer.valueOf(id));

            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Access Error ,user not found"
                                )
                        );
            }

            user.setIsDeleted(0);
            userRepository.save(user);
            Company senderCompny = companyRepository.findById(decoded.getCompanyId());

            // send email to notify user
            final String json = new JSONObject()
                    .put("email", user.getEmail())
                    .put("password", password)
                    .put("name", user.getFullName())
                    .put("theSenderName", decoded.getFullName())
                    .put("Username", user.getFullName())
                    .put("companyName", senderCompny.getTitle())
                    .put("userFullName", user.getFullName())
                    .toString();
            makeRequestServices(decoded, Utils.SERVICES_NAMES.NOTIFICATIONS_SERVICE + "users/user-account-activate-by-user", json);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "User Account Deleted"
                    ));
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

    @RequestMapping(value = "/request-reset-account-password-user", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> resetEmailCode(@RequestBody Map<String, Object> payload) {

        try {
            if (!payload.containsKey("email")) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Missing Fields"
                                )
                        );
            }

            String email = (String.valueOf(payload.get("email")));

            User user = userRepository.findUserByEmail(email);
            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Wrong Details"
                                )
                        );
            }
            Company company = companyRepository.findByIdAndIsDeleted(user.getCompanyId(), 0);
            if (company == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Failed to send code"
                                )
                        );
            }
            int otp = otpService.generateOTP(email);

            final String json = new JSONObject()
                    .put("email", email)
                    .put("code", otp + "")
                    .put("expire_mins", OTPService.EXPIRE_MINS + "")


                    .toString();
            makeRequestServices(Utils.SERVICES_NAMES.NOTIFICATIONS_SERVICE + "users/reset-user-password-code", json);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "User Account Deleted"
                    ));
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


    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> payload) {


        if (!payload.containsKey("email") || !payload.containsKey("password")) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Missing Fields"
                            )
                    );
        }
        var email = String.valueOf(payload.get("email"));
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Wrong Details"
                            )
                    );
        }

        var loginDetails = new LastLogin();
        loginDetails.setClient("Browser");
        loginDetails.setUserId(user.getId());
        lastLoginRepository.save(loginDetails);

        Company company = companyRepository.findByIdAndIsDeleted(user.getCompanyId(), 0);
        if (company == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Company Account Deleted"
                            )
                    );
        }

        if (!passwordEncoder.matches(String.valueOf(payload.get("password")), user.getPasswordHash())) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to login user,please check credentials again"
                            )
                    );
        }
        Algorithm algorithm = Algorithm.HMAC256(SecreteKey);

        Map<String, Object> payloadClaims = new HashMap<>();
        payloadClaims.put("role", user.getRoleId());
        payloadClaims.put("companyId", user.getCompanyId());
        payloadClaims.put("fullName", user.getFullName());

        String accessToken = JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000 * 24 * 5)) /*5 days */
                .withIssuer(request.getRequestURI().toString())
                .withPayload(payloadClaims)
                .withClaim("userId", user.getId())


                .sign(algorithm);
        String refreshToken = JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000 * 24 * 6)) /*6 days */
                .withIssuer(request.getRequestURI().toString())
                .sign(algorithm);

        var dataObject = Map.of(
                "fullName", user.getFullName() == null ? "" : user.getFullName(),
                "profileImage", user.getProfileImageUrl() == null ? "" : user.getProfileImageUrl(),
                "userId", user.getId(),
                "role", user.getRoleId(),
                "WIDGET_API_KEY", company.getApiWidgetAccessToken().replace("api-",""),
                "email", user.getEmail(),
                "companyId", user.getCompanyId(),
                "token", Map.of(
                        "accessToken", accessToken,
                        "refreshToken", refreshToken
                )

        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "success", true,
                        "message", "User Logged in",
                        "data", dataObject)
                );
    }

    @RequestMapping(value = "/register-company", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> registerCompanyUser(@RequestBody Map<String, Object> payload) throws IOException {
        UUID uuid = Generators.randomBasedGenerator().generate();
        UUID uuid2 = Generators.timeBasedGenerator().generate();
        UUID activationCode = Generators.timeBasedGenerator().generate();

        if (!payload.containsKey("fullName") || !payload.containsKey("emailAddress") || !payload.containsKey("password")) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Missing Fields"
                            )
                    );
        }

        if (userRepository.findUserByEmail(String.valueOf(payload.get("emailAddress"))) != null) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Duplicate Email details , please Login"
                            )
                    );
        }
        if (companyRepository.findByTitle(String.valueOf(payload.get("companyName"))) != null) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Duplicate Company Name details , please Login"
                            )
                    );
        }
        Company company = new Company();
        company.setCompanyAccessPackageId(8);// set this as the default package that has all
        company.setApiWidgetAccessToken("api-" + uuid);
        company.setDnsTxtRecord("click2call-" + uuid2);
        company.setDomain(null);
        company.setTitle(String.valueOf(payload.get("companyName")));

        Company result = companyRepository.save(company);
        log.info("result: " + result.getId());
        // lets save user details
        User user = new User();
        user.setRoleId(1);
        user.setCompanyId(result.getId());
        user.setEmail(String.valueOf(payload.get("emailAddress")));
        user.setFullName(String.valueOf(payload.get("fullName")));
        user.setPasswordHash(passwordEncoder.encode(String.valueOf(payload.get("password"))));

        user = userRepository.save(user);

        var scriptCode =
                "&lt;script src=\"https://clicktocallbucket.s3.af-south-1.amazonaws.com/assets/js/widgetsrc-v-1.0.0.js?access=" +
                        uuid + "\" &gt;  &lt;/script&gt; ";
        // save widget
        Widget widget = new Widget();
        widget.setCompanyId(company.getId());
        widget.setCodeSnippets(scriptCode);
        widget.setBackgroundHexColorCode(String.valueOf(payload.get("themeColor")));
        widget.setNameShown(String.valueOf(payload.get("widgetName")));
        widget.setTopBarMessage(String.valueOf(payload.get("topbarMessage")));
        widgetRepository.save(widget);

        // lets Create te default Organization
        Organisation org = new Organisation();
        org.setTitle("Main");
        org.setCompanyId(company.getId());
        org.setSavedByUserId(user.getId());
        Organisation organisation = organisationRepository.save(org);

        String activattionLink = Utils.SERVICES_NAMES.WEBSITE_BASEURL + "auth?type=login-activation-attempted&key=" + activationCode;


        String jsonPost = new JSONObject()
                .put("email", String.valueOf(user.getEmail()))
                .put("name", user.getFullName())
                .put("activattionLink", activattionLink)
                .toString();


        String emailResponse = makeRequestServices(null, Utils.SERVICES_NAMES.NOTIFICATIONS_SERVICE
                + "users/account-creation", jsonPost);

        String urlRefer = "/auth?type=referral&owner-name=" + user.getFullName() + "&owner-email=" + user.getEmail() + "&owner-id=" + user.getId() + "&company=" + company.getTitle() + "&company-id=" + company.getId() + "&toemail=";
        var dataObject = Map.of(
                "scriptSrc", uuid,
                "companyId", company.getId(),
                "company", company.getTitle(),
                "userId", user.getId(),
                "userFullName", user.getFullName(),
                "refer", urlRefer,
                "org", organisation
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "success", true,
                        "message", "Registered new company and registered as a new user .",
                        "data", dataObject, "email", emailResponse)
                );

    }


}
