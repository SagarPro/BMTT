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

@DynamoDBTable(tableName = "bmtt-mobilehub-1157786907-FileSubmission")

public class FileSubmissionDO {
    private String _emailID;
    private String _phone;
    private String _file1;
    private String _file2;
    private String _file3;
    private String _file4;

    @DynamoDBHashKey(attributeName = "emailID")
    @DynamoDBAttribute(attributeName = "emailID")
    public String getEmailID() {
        return _emailID;
    }

    public void setEmailID(final String _emailID) {
        this._emailID = _emailID;
    }
    @DynamoDBRangeKey(attributeName = "phone")
    @DynamoDBAttribute(attributeName = "phone")
    public String getPhone() {
        return _phone;
    }

    public void setPhone(final String _phone) {
        this._phone = _phone;
    }
    @DynamoDBAttribute(attributeName = "file1")
    public String getFile1() {
        return _file1;
    }

    public void setFile1(final String _file1) {
        this._file1 = _file1;
    }
    @DynamoDBAttribute(attributeName = "file2")
    public String getFile2() {
        return _file2;
    }

    public void setFile2(final String _file2) {
        this._file2 = _file2;
    }
    @DynamoDBAttribute(attributeName = "file3")
    public String getFile3() {
        return _file3;
    }

    public void setFile3(final String _file3) {
        this._file3 = _file3;
    }
    @DynamoDBAttribute(attributeName = "file4")
    public String getFile4() {
        return _file4;
    }

    public void setFile4(final String _file4) {
        this._file4 = _file4;
    }

}
