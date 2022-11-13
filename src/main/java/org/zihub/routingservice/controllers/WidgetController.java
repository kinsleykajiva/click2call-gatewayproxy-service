package org.zihub.routingservice.controllers;


import org.zihub.routingservice.dbaccess.Company;
import org.zihub.routingservice.dbaccess.Widget;
import org.zihub.routingservice.pojos.JWTDataPojo;
import org.zihub.routingservice.repositories.CompanyRepository;
import org.zihub.routingservice.repositories.WidgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zihub.routingservice.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/auth/api/v1/widget")
public class WidgetController {
    @Autowired
    private HttpServletRequest request;
    @Value("${app.secrete}")
    private String SecreteKey;
    @Autowired
    private WidgetRepository widgetRepository;

    @Autowired
    private CompanyRepository companyRepository;


    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> updateWidget(@RequestBody Map<String, Object> payload) {
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


            var recordId = Integer.parseInt(String.valueOf(payload.get("recordId")));
            var isActive = Integer.parseInt(String.valueOf(payload.get("isActive")));
            Widget widget = widgetRepository.findById(recordId);
            if (widget == null) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Access Rejected "
                                )
                        );
            }

            var WidgetColor = String.valueOf(payload.get("WidgetColor"));
            var WidgetName = String.valueOf(payload.get("WidgetName"));
            var WidgetWelcomeMessage = String.valueOf(payload.get("WidgetWelcomeMessage"));


            Widget save = widgetRepository.findById(recordId);
            save.setBackgroundHexColorCode(WidgetColor);
            save.setNameShown(WidgetName);
            save.setIsActive(isActive);
            save.setTopBarMessage(WidgetWelcomeMessage);


            Widget update = widgetRepository.save(save);


            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "success", true,
                            "message", "Updated Widget",
                            "data", Map.of(
                                    "orgId", update
                            ))
                    );
        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getWidgetAuthess(@RequestParam("integrity") String integrity) {

        try {
            if (integrity == null || integrity.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Failed to load Widget"
                                )
                        );
            }
            integrity = "api-" + integrity;
            Company com = companyRepository.findByApiWidgetAccessToken(integrity);
            if (com == null) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Access Rejected"
                                )
                        );
            }
            Widget widget = widgetRepository.findByCompanyId(com.getId());
            if (widget == null) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Access Rejected Again"
                                )
                        );
            }
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "Widget data",
                                    "data", widget
                            )
                    );

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                                    "success", false,
                                    "message", "Failed to access resource"
                            )
                    );
        }

    }


    @RequestMapping(value = "/name-spaces", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getCompanyWidgetNameSpaces() {
        try {
            List<Company> coms = companyRepository.findAll();

            coms.forEach(c-> c.setApiWidgetAccessToken(c.getApiWidgetAccessToken().replace("api-","")));


            List<String>  namespaces=     coms.stream()
                    .map(Company::getApiWidgetAccessToken)
                    .collect(Collectors.toList());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "all namespaces",
                            "data", Map.of(
                                    "namespaces", namespaces
                            )
                            )
                    );

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
    }

    @RequestMapping(value = "/configs", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getWidgetConfigs() {
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



            Widget widget = widgetRepository.findByCompanyId(decoded.getCompanyId());
            if (widget == null) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of(
                                        "success", false,
                                        "message", "Access Rejected Again"
                                )
                        );
            }
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                                    "success", true,
                                    "message", "Widget data",
                                    "data", widget
                            )
                    );

        } catch (Exception e) {
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
