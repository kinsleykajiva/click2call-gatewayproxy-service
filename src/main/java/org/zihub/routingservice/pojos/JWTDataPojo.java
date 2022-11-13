package org.zihub.routingservice.pojos;

public class JWTDataPojo {

    private int userId;
    private int role;
    private int companyId;
    private String fullName;
    private String jwt;

    public JWTDataPojo(int userId, int role, int companyId, String fullName , String jwt) {
        this.userId = userId;
        this.role = role;
        this.companyId = companyId;
        this.fullName = fullName;
        this.jwt = jwt;
    }




    public String getJwt() {
        return jwt;
    }

    public int getUserId() {
        return userId;
    }

    public int getRole() {
        return role;
    }

    public int getCompanyId() {
        return companyId;
    }

    public String getFullName() {
        return fullName;
    }
}
