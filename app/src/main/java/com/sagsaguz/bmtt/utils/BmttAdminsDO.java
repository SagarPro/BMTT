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

@DynamoDBTable(tableName = "bmtt-mobilehub-1157786907-BmttAdmins")

public class BmttAdminsDO {
    private String _emailId;
    private String _phone;
    private String _centre;
    private String _password;
    private String _notificationARN;
    private String _centerCode;
    private String _location;

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
    @DynamoDBAttribute(attributeName = "centre")
    public String getCentre() {
        return _centre;
    }

    public void setCentre(final String _centre) {
        this._centre = _centre;
    }
    @DynamoDBAttribute(attributeName = "password")
    public String getPassword() {
        return _password;
    }

    public void setPassword(final String _password) {
        this._password = _password;
    }
    @DynamoDBAttribute(attributeName = "notificationARN")
    public String getNotificationARN() {
        return _notificationARN;
    }

    public void setNotificationARN(final String _notificationARN) {
        this._notificationARN = _notificationARN;
    }
    @DynamoDBAttribute(attributeName = "centerCode")
    public String getCenterCode() {
        return _centerCode;
    }

    public void setCenterCode(final String _centerCode) {
        this._centerCode = _centerCode;
    }
    @DynamoDBAttribute(attributeName = "location")
    public String getLocation() {
        return _location;
    }

    public void setLocation(final String _location) {
        this._location = _location;
    }

}
