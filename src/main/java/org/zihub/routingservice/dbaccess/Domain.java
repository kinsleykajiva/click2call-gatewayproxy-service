package org.zihub.routingservice.dbaccess;

import javax.persistence.*;

@Entity
@Table(name = "domains")
public class Domain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "companyId",nullable = false)
    private Integer companyId;


    @Lob
    @Column(name = "domain")
    private String domain;

    @Lob
    @Column(name = "dnsTxtRecord")
    private String dnsTxtRecord;


    @Column(name = "isVerified" ,nullable = false)
    private Integer isVerified = 0;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDnsTxtRecord() {
        return dnsTxtRecord;
    }

    public void setDnsTxtRecord(String dnsTxtRecord) {
        this.dnsTxtRecord = dnsTxtRecord;
    }

    public Integer getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Integer isVerified) {
        this.isVerified = isVerified;
    }
}