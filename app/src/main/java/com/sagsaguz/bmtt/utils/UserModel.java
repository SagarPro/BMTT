package com.sagsaguz.bmtt.utils;

public class UserModel {

    private String emailId, firstName, lastName, centre, address, phone, password, createdDate, expiryDate, dob, profilePic, notificationARN, userId;
    private Boolean bmttPart1, bmttPart2, bmttPart3;

    public UserModel() {
    }

    public UserModel(String emailId, String firstName, String lastName,String centre, String address, String phone, String password, String createdDate, String expiryDate, String dob, Boolean bmttPart1, Boolean bmttPart2, Boolean bmttPart3, String profilePic, String notificationARN, String userId) {
        this.emailId = emailId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.centre = centre;
        this.address = address;
        this.phone = phone;
        this.password = password;
        this.createdDate = createdDate;
        this.expiryDate = expiryDate;
        this.dob = dob;
        this.bmttPart1 = bmttPart1;
        this.bmttPart2 = bmttPart2;
        this.bmttPart3 = bmttPart3;
        this.profilePic = profilePic;
        this.notificationARN = notificationARN;
        this.userId = userId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCentre() {
        return centre;
    }

    public void setCentre(String centre) {
        this.centre = centre;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Boolean getBmttPart1() {
        return bmttPart1;
    }

    public void setBmttPart1(Boolean bmttPart1) {
        this.bmttPart1 = bmttPart1;
    }

    public Boolean getBmttPart2() {
        return bmttPart2;
    }

    public void setBmttPart2(Boolean bmttPart2) {
        this.bmttPart2 = bmttPart2;
    }

    public Boolean getBmttPart3() {
        return bmttPart3;
    }

    public void setBmttPart3(Boolean bmttPart3) {
        this.bmttPart3 = bmttPart3;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getNotificationARN() {
        return notificationARN;
    }

    public void setNotificationARN(String notificationARN) {
        this.notificationARN = notificationARN;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
