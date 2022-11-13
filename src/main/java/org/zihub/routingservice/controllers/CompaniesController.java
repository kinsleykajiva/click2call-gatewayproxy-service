package org.zihub.routingservice.controllers;


import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zihub.routingservice.dbaccess.Company;
import org.zihub.routingservice.dbaccess.Domain;
import org.zihub.routingservice.dbaccess.User;
import org.zihub.routingservice.pojos.JWTDataPojo;
import org.zihub.routingservice.repositories.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.zihub.routingservice.repositories.DomainRepository;
import org.zihub.routingservice.repositories.UserRepository;
import org.zihub.routingservice.services.OTPService;
import org.zihub.routingservice.utils.DNS;
import org.zihub.routingservice.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.Map;

import static org.zihub.routingservice.utils.Utils.makeRequestServices;


@RestController
@Slf4j
@RequestMapping("/auth/secured/api/v1/companies")
public class CompaniesController {
    @Autowired
    private HttpServletRequest request;
    @Value("${app.secrete}")
    private String SecreteKey;

    @Autowired
    public OTPService otpService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private DomainRepository domainRepository;

    @RequestMapping(value = "/company", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getCompanyDetails() {
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
                                    "message", "company details ",
                                    "data", Map.of(
                                            "basics", companyRepository.findById(decoded.getCompanyId())
                                    )
                            )
                    );


        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }
    }

    @RequestMapping(value = "/domains", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getDomains() {
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
                                    "message", "List of domains ",
                                    "data", Map.of(
                                            "domains", domainRepository.findByCompanyId(decoded.getCompanyId())
                                    )
                            )
                    );


        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }
    }

    @RequestMapping(value = "/verify-domain", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> verifyDomain(@RequestBody Map<String, Object> payload) {
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


            var id = Integer.parseInt(String.valueOf(payload.get("recordId")));
            Domain domain = domainRepository.getOne(id);
            var result = new DNS().queryText(domain.getDnsTxtRecord());

            var isVerified = result.contains(domain.getDnsTxtRecord());

            domain.setIsVerified(isVerified ? 1 : 0);

            domain = domainRepository.save(domain);


            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "List of domains ",
                                    "data", Map.of(
                                            "domain", domain
                                    )
                            )
                    );


        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }
    }


    @RequestMapping(value = "/delete-domain", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> deleteDomain(@RequestBody Map<String, Object> payload) {

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

            var id = Integer.parseInt(String.valueOf(payload.get("recordId")));
            domainRepository.deleteById(id);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "deleted domain "

                            )
                    );


        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }
    }

    @RequestMapping(value = "/request-delete-account", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> reqDeleteAccount(@RequestBody Map<String, Object> payload) {

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

            Company company = companyRepository.findById(decoded.getCompanyId());

            int otp = otpService.generateOTP(decoded.getCompanyId() + "");
            var users = userRepository.findUserByCompanyId(decoded.getCompanyId());
            String email = null;
            if (!users.isEmpty()) {
                email = users.get(0).getEmail();
            }
            log.info("email: " + email);
            final String json = new JSONObject()
                    .put("email", email)
                    .put("code", otp + "")
                    .put("expire_mins", OTPService.EXPIRE_MINS + "")
                    .put("companyName", company.getTitle())

                    .toString();
            makeRequestServices(decoded, Utils.SERVICES_NAMES.NOTIFICATIONS_SERVICE + "users/delete-company-code", json);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "deleted Acoount Code sent "

                            )
                    );


        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }
    }

    @RequestMapping(value = "/delete-account", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> deleteAccount(@RequestBody Map<String, Object> payload) {

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
            int code = Integer.parseInt((String.valueOf(payload.get("otp"))));
            int serverOtp = otpService.getOtp(decoded.getCompanyId() + "");
            log.info("serverOtp: " + serverOtp);
            if (serverOtp >= 0) {
                if (code == serverOtp) {

                    otpService.clearOTP(decoded.getCompanyId() + "");

                    Company company = companyRepository.findById(decoded.getCompanyId());

                    company.setIsDeleted(1);
                    company.setDeleteDate(new Date(System.currentTimeMillis()));
                    companyRepository.save(company);

                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(Map.of(
                                            "success", true,
                                            "message", "deleted Account  "
                                    )
                            );

                } else {
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(Map.of(
                                            "success", false,
                                            "message", "Failed, try again later , Code Expired or wrong code"
                                    )
                            );
                }
            } else {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Failed, try again later"
                                )
                        );
            }


        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }
    }


    @RequestMapping(value = "/new-domain", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> saveDomain(@RequestBody Map<String, Object> payload) {
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

            var domainString = String.valueOf(payload.get("domain"));
            var dnsTxtRecord = String.valueOf(payload.get("dnsTxtRecord"));


            Domain domain = new Domain();
            var isVerified = false;
            try {
                var result = new DNS().queryText(domainString);
                isVerified = result.contains(dnsTxtRecord);
            } catch (Exception ignored) {

            }
            domain.setDomain(domainString);
            domain.setCompanyId(decoded.getCompanyId());
            domain.setDnsTxtRecord(dnsTxtRecord);
            domain.setIsVerified(isVerified ? 1 : 0);

            domain = domainRepository.save(domain);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "Domain Saved ",
                                    "data", Map.of(
                                            "domain", domain
                                    )
                            )
                    );


        } catch (Exception e) {

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
