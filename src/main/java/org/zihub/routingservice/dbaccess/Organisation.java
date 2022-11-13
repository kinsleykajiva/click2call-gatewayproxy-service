package org.zihub.routingservice.dbaccess;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Table(name = "organisations")
@Entity
public class Organisation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "companyId")
    private Integer companyId;

    @Column(name = "savedByUserId")
    private Integer savedByUserId;

    @Column(name = "dateCreated",nullable = false, updatable = false)
    @CreationTimestamp
    private Instant dateCreated;

    @Lob
    @Column(name = "url")
    private String url;

    @Column(name = "companyTax", length = 222)
    private String companyTax;

    @Lob
    @Column(name = "companyAddress")
    private String companyAddress;

    @Lob
    @Column(name = "unitNumberStreetName")
    private String unitNumberStreetName;

    @Lob
    @Column(name = "city")
    private String city;

    @Lob
    @Column(name = "province")
    private String province;

    @Column(name = "postalCode", length = 100)
    private String postalCode;
    @Column(name = "country", length = 200)
    private String country;

    @Lob
    @Column(name = "profileImageUrl")
    private String profileImageUrl;



    @Transient
    private Set<User> users;

    @Transient
    private List<Team> teamsList;


    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public List<Team> getTeamsList() {
        return teamsList;
    }

    public void setTeamsList(List<Team> teamsList) {
        this.teamsList = teamsList;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUnitNumberStreetName() {
        return unitNumberStreetName;
    }

    public void setUnitNumberStreetName(String unitNumberStreetName) {
        this.unitNumberStreetName = unitNumberStreetName;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getCompanyTax() {
        return companyTax;
    }

    public void setCompanyTax(String companyTax) {
        this.companyTax = companyTax;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Integer getSavedByUserId() {
        return savedByUserId;
    }

    public void setSavedByUserId(Integer savedByUserId) {
        this.savedByUserId = savedByUserId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}