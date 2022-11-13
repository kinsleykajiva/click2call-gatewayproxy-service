package org.zihub.routingservice.dbaccess;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;


@Table(name = "users")

@Entity
public class User {


    @Transient
    private List<Role>  roles ;
    @Transient
    private Role  role ;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "fullName", length = 200)
    private String fullName;

    @Column(name = "email", length = 200)
    private String email;

    @Lob
    @Column(name = "passwordHash")
    private String passwordHash;

    @Lob
    @Column(name = "profileImageUrl")
    private String profileImageUrl;
    @Lob
    @Column(name = "accountActivationCode")
    private String accountActivationCode;

    @Column(name = "roleId")
    private Integer roleId;

    @Column(name = "companyId")
    private Integer companyId;

    @Column(name = "isEmailAddressVerified",updatable = true,nullable = false)
    private int isEmailAddressVerified=0;

    @Column(name = "isDeleted",updatable = true,nullable = false)
    private int isDeleted=0;

    @Column(name = "lastTimeOnline")
    private Instant lastTimeOnline;

    @Column(name = "statusId",nullable = false)
    private int statusId=2;

    @Column(name = "dateCreated",nullable = false, updatable = false)
    @CreationTimestamp
    private Instant dateCreated;

    @Transient
    private List<TeamGroup> teamGroups;


    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public List<TeamGroup> getTeamGroups() {
        return teamGroups;
    }

    public void setTeamGroups(List<TeamGroup> teamGroups) {
        this.teamGroups = teamGroups;
    }


    public List<Role>  getRoles() {

        return roles;
    }
    @JsonIgnore
    public String getAccountActivationCode() {
        return accountActivationCode;
    }

    public void setAccountActivationCode(String accountActivationCode) {
        this.accountActivationCode = accountActivationCode;
    }

    public void setIsEmailAddressVerified(int isEmailAddressVerified) {
        this.isEmailAddressVerified = isEmailAddressVerified;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }
    @JsonIgnore
    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public Instant getLastTimeOnline() {
        return lastTimeOnline;
    }

    public void setLastTimeOnline(Instant lastTimeOnline) {
        this.lastTimeOnline = lastTimeOnline;
    }

    public Integer getIsEmailAddressVerified() {
        return isEmailAddressVerified;
    }

    public void setIsEmailAddressVerified(Integer isEmailAddressVerified) {
        this.isEmailAddressVerified = isEmailAddressVerified;
    }
   @JsonIgnore
    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}