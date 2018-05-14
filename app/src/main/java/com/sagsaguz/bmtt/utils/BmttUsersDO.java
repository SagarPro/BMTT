package com.sagsaguz.bmtt.utils;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "bmtt-mobilehub-1157786907-BmttUsers")

public class BmttUsersDO {
    private String _emailId;
    private String _phone;
    private String _address;
    private Boolean _bmttPart1;
    private Boolean _bmttPart2;
    private Boolean _bmttPart3;
    private String _centre;
    private String _createdDate;
    private String _dob;
    private String _expiryDate;
    private String _firstName;
    private String _lastName;
    private String _password;
    private String _profilePic;
    private String _notificationARN;

    @DynamoDBHashKey(attributeName = "emailId")
    @DynamoDBAttribute(attributeName = "emailId")
    public String getEmailId() {
        return _emailId;
    }

    public void setEmailId(final String _emailId) {
        this._emailId = _emailId;
    }
    @DynamoDBRangeKey(attributeName = "phone")
    @DynamoDBAttribute(attributeName = "phone")
    public String getPhone() {
        return _phone;
    }

    public void setPhone(final String _phone) {
        this._phone = _phone;
    }
    @DynamoDBAttribute(attributeName = "address")
    public String getAddress() {
        return _address;
    }

    public void setAddress(final String _address) {
        this._address = _address;
    }
    @DynamoDBAttribute(attributeName = "bmttPart1")
    public Boolean getBmttPart1() {
        return _bmttPart1;
    }

    public void setBmttPart1(final Boolean _bmttPart1) {
        this._bmttPart1 = _bmttPart1;
    }
    @DynamoDBAttribute(attributeName = "bmttPart2")
    public Boolean getBmttPart2() {
        return _bmttPart2;
    }

    public void setBmttPart2(final Boolean _bmttPart2) {
        this._bmttPart2 = _bmttPart2;
    }
    @DynamoDBAttribute(attributeName = "bmttPart3")
    public Boolean getBmttPart3() {
        return _bmttPart3;
    }

    public void setBmttPart3(final Boolean _bmttPart3) {
        this._bmttPart3 = _bmttPart3;
    }
    @DynamoDBAttribute(attributeName = "centre")
    public String getCentre() {
        return _centre;
    }

    public void setCentre(final String _centre) {
        this._centre = _centre;
    }
    @DynamoDBAttribute(attributeName = "createdDate")
    public String getCreatedDate() {
        return _createdDate;
    }

    public void setCreatedDate(final String _createdDate) {
        this._createdDate = _createdDate;
    }
    @DynamoDBAttribute(attributeName = "dob")
    public String getDob() {
        return _dob;
    }

    public void setDob(final String _dob) {
        this._dob = _dob;
    }
    @DynamoDBAttribute(attributeName = "expiryDate")
    public String getExpiryDate() {
        return _expiryDate;
    }

    public void setExpiryDate(final String _expiryDate) {
        this._expiryDate = _expiryDate;
    }
    @DynamoDBAttribute(attributeName = "firstName")
    public String getFirstName() {
        return _firstName;
    }

    public void setFirstName(final String _firstName) {
        this._firstName = _firstName;
    }
    @DynamoDBAttribute(attributeName = "lastName")
    public String getLastName() {
        return _lastName;
    }

    public void setLastName(final String _lastName) {
        this._lastName = _lastName;
    }
    @DynamoDBAttribute(attributeName = "password")
    public String getPassword() {
        return _password;
    }

    public void setPassword(final String _password) {
        this._password = _password;
    }
    @DynamoDBAttribute(attributeName = "profilePic")
    public String getProfilePic() {
        return _profilePic;
    }

    public void setProfilePic(final String _profilePic) {
        this._profilePic = _profilePic;
    }
    @DynamoDBAttribute(attributeName = "notificationARN")
    public String getNotificationARN() {
        return _notificationARN;
    }

    public void setNotificationARN(final String _notificationARN) {
        this._notificationARN = _notificationARN;
    }
}
