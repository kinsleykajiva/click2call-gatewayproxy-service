package org.zihub.routingservice.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateAuthReq {

    boolean success;
    String message;
    AuthData data;

    @JsonProperty("success")
    public boolean getSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }



    @JsonProperty("message")
    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }



    @JsonProperty("data")
    public AuthData getData() {
        return this.data;
    }

    public void setData(AuthData data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthData {
        @JsonProperty("userId")
        public int getUserId() {
            return this.userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        int userId;

        @JsonProperty("applicationId")
        public int getApplicationId() {
            return this.applicationId;
        }

        public void setApplicationId(int applicationId) {
            this.applicationId = applicationId;
        }

        int applicationId;
    }
}
