package org.zihub.routingservice.dbaccess;

import javax.persistence.*;

@Table(name = "widgets")
@Entity
public class Widget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "companyId")
    private Integer companyId;

    @Column(name = "nameShown", length = 200)
    private String nameShown;

    @Column(name = "topBarMessage", length = 200)
    private String topBarMessage;

    @Column(name = "backgroundHexColorCode", length = 11)
    private String backgroundHexColorCode;

    @Lob
    @Column(name = "codeSnippets")
    private String codeSnippets;

    @Lob
    @Column(name = "linkedInUrl")
    private String linkedInUrl;

    @Lob
    @Column(name = "whatsAppUrl")
    private String whatsAppUrl;

    @Lob
    @Column(name = "messengerUrl")
    private String messengerUrl;

    @Lob
    @Column(name = "iconUrl")
    private String iconUrl;

    @Column(name = "enableSocialMediaBarOption",nullable = false)
    private Integer enableSocialMediaBarOption = 0;


    @Column(name = "isActive",nullable = false)
    private Integer isActive = 1;


    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public Integer getEnableSocialMediaBarOption() {
        return enableSocialMediaBarOption;
    }

    public void setEnableSocialMediaBarOption(Integer enableSocialMediaBarOption) {
        this.enableSocialMediaBarOption = enableSocialMediaBarOption;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getMessengerUrl() {
        return messengerUrl;
    }

    public void setMessengerUrl(String messengerUrl) {
        this.messengerUrl = messengerUrl;
    }

    public String getWhatsAppUrl() {
        return whatsAppUrl;
    }

    public void setWhatsAppUrl(String whatsAppUrl) {
        this.whatsAppUrl = whatsAppUrl;
    }

    public String getLinkedInUrl() {
        return linkedInUrl;
    }

    public void setLinkedInUrl(String linkedInUrl) {
        this.linkedInUrl = linkedInUrl;
    }

    public String getCodeSnippets() {
        return codeSnippets;
    }

    public void setCodeSnippets(String codeSnippets) {
        this.codeSnippets = codeSnippets;
    }

    public String getBackgroundHexColorCode() {
        return backgroundHexColorCode;
    }

    public void setBackgroundHexColorCode(String backgroundHexColorCode) {
        this.backgroundHexColorCode = backgroundHexColorCode;
    }

    public String getTopBarMessage() {
        return topBarMessage;
    }

    public void setTopBarMessage(String topBarMessage) {
        this.topBarMessage = topBarMessage;
    }

    public String getNameShown() {
        return nameShown;
    }

    public void setNameShown(String nameShown) {
        this.nameShown = nameShown;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}