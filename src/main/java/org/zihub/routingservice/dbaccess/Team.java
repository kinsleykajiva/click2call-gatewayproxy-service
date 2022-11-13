package org.zihub.routingservice.dbaccess;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Date;
import java.util.List;

@Table(name = "teams")
@Entity
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;
    @Column(name = "description", nullable = false, length = 200)
    private String description;

    @Column(name = "createdByUserId")
    private Integer createdByUserId;

    @Column(name = "companyId")
    private Integer companyId;

    @Column(name = "organisationId")
    private Integer organisationId;

    @Column(name = "dateCreated",nullable = false, updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @CreationTimestamp
    private Date dateCreated;

    @Transient
    private int usersCounter;
    @Transient
    private List<TeamGroup> teamGroups;

    @Transient
    private Organisation organisation;


    public List<TeamGroup> getTeamGroups() {
        return teamGroups;
    }

    public void setTeamGroups(List<TeamGroup> teamGroups) {
        this.teamGroups = teamGroups;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public int getUsersCounter() {
        return usersCounter;
    }

    public void setUsersCounter(int usersCounter) {
        this.usersCounter = usersCounter;
    }

    /* @OneToMany(targetEntity = TeamGroup.class ,cascade = CascadeType.ALL)
    @JoinColumn(name = "teamId",referencedColumnName = "id")
    private List<TeamGroup> teamGroups;


    public List<TeamGroup> getTeamGroups() {
        return teamGroups;
    }*/


    public Integer getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Integer organisationId) {
        this.organisationId = organisationId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Integer getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Integer createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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