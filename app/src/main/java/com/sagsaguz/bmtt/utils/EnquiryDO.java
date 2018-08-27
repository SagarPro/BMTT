package com.sagsaguz.bmtt.utils;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "bmtt-mobilehub-1157786907-Enquiry")

public class EnquiryDO implements Serializable {
    private String _enqNo;
    private String _enqId;
    private String _enqAssignTo;
    private String _enqCreatedDate;
    private String _enqEmail;
    private String _enqLocation;
    private String _enqName;
    private String _enqPhone;
    private String _enqStatus;
    private String _enqFollowUp;

    @DynamoDBHashKey(attributeName = "enqNo")
    @DynamoDBAttribute(attributeName = "enqNo")
    public String getEnqNo() {
        return _enqNo;
    }

    public void setEnqNo(final String _enqNo) {
        this._enqNo = _enqNo;
    }
    @DynamoDBRangeKey(attributeName = "enqId")
    @DynamoDBAttribute(attributeName = "enqId")
    public String getEnqId() {
        return _enqId;
    }

    public void setEnqId(final String _enqId) {
        this._enqId = _enqId;
    }
    @DynamoDBAttribute(attributeName = "enqAssignTo")
    public String getEnqAssignTo() {
        return _enqAssignTo;
    }

    public void setEnqAssignTo(final String _enqAssignTo) {
        this._enqAssignTo = _enqAssignTo;
    }
    @DynamoDBAttribute(attributeName = "enqCreatedDate")
    public String getEnqCreatedDate() {
        return _enqCreatedDate;
    }

    public void setEnqCreatedDate(final String _enqCreatedDate) {
        this._enqCreatedDate = _enqCreatedDate;
    }
    @DynamoDBAttribute(attributeName = "enqEmail")
    public String getEnqEmail() {
        return _enqEmail;
    }

    public void setEnqEmail(final String _enqEmail) {
        this._enqEmail = _enqEmail;
    }
    @DynamoDBAttribute(attributeName = "enqLocation")
    public String getEnqLocation() {
        return _enqLocation;
    }

    public void setEnqLocation(final String _enqLocation) {
        this._enqLocation = _enqLocation;
    }
    @DynamoDBAttribute(attributeName = "enqName")
    public String getEnqName() {
        return _enqName;
    }

    public void setEnqName(final String _enqName) {
        this._enqName = _enqName;
    }
    @DynamoDBAttribute(attributeName = "enqPhone")
    public String getEnqPhone() {
        return _enqPhone;
    }

    public void setEnqPhone(final String _enqPhone) {
        this._enqPhone = _enqPhone;
    }
    @DynamoDBAttribute(attributeName = "enqStatus")
    public String getEnqStatus() {
        return _enqStatus;
    }

    public void setEnqStatus(final String _enqStatus) {
        this._enqStatus = _enqStatus;
    }
    @DynamoDBAttribute(attributeName = "enqFollowUp")
    public String getEnqFollowUp() {
        return _enqFollowUp;
    }

    public void setEnqFollowUp(final String _enqFollowUp) {
        this._enqFollowUp = _enqFollowUp;
    }

}
