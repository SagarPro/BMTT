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

@DynamoDBTable(tableName = "bmtt-mobilehub-1157786907-CountValues")

public class CountValuesDO {
    private String _emailId;
    private String _phone;
    private Map<String, List<Integer>> _mcqCounts;
    private Map<String, Integer> _videoCounts;

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
    @DynamoDBAttribute(attributeName = "mcqCounts")
    public Map<String, List<Integer>> getMcqCounts() {
        return _mcqCounts;
    }

    public void setMcqCounts(final Map<String,List<Integer>> _mcqCounts) {
        this._mcqCounts = _mcqCounts;
    }
    @DynamoDBAttribute(attributeName = "videoCounts")
    public Map<String, Integer> getVideoCounts() {
        return _videoCounts;
    }

    public void setVideoCounts(final Map<String, Integer> _videoCounts) {
        this._videoCounts = _videoCounts;
    }

}
