package org.zihub.routingservice.dbaccess;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Date;

@Table(name = "companies")
@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "domain")
    private String domain;

    @Lob
    @Column(name = "dnsTxtRecord")
    private String dnsTxtRecord;

    @Lob
    @Column(name = "apiWidgetAccessToken")
    private String apiWidgetAccessToken;

    @Column(name = "dateCreated",nullable = false, updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @CreationTimestamp
    private Date dateCreated;

    @Column(name = "deleteDate",nullable = true, updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @CreationTimestamp
    private Date deleteDate;

    @Column(name = "isDomainVerified",nullable = false)
    private int isDomainVerified;
    @Column(name = "isDeleted",nullable = false)
    private int isDeleted = 0;

    @Column(name = "companyAccessPackageId")
    private int companyAccessPackageId;

    public int getCompanyAccessPackageId() {
        return companyAccessPackageId;
    }

    public void setCompanyAccessPackageId(int companyAccessPackageId) {
        this.companyAccessPackageId = companyAccessPackageId;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public int getIsDomainVerified() {
        return isDomainVerified;
    }

    public void setIsDomainVerified(int isDomainVerified) {

        this.isDomainVerified =isDomainVerified ;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getApiWidgetAccessToken() {
        return apiWidgetAccessToken;
    }

    public void setApiWidgetAccessToken(String apiWidgetAccessToken) {
        this.apiWidgetAccessToken = apiWidgetAccessToken;
    }

    public String getDnsTxtRecord() {
        return dnsTxtRecord;
    }

    public void setDnsTxtRecord(String dnsTxtRecord) {
        this.dnsTxtRecord = dnsTxtRecord;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}