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

@DynamoDBTable(tableName = "bmtt-mobilehub-1157786907-Activities")

public class ActivitiesDO {
    private String _emailID;
    private String _phone;
    private List<String> _attended;
    private List<String> _done;

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
    @DynamoDBAttribute(attributeName = "attended")
    public List<String> getAttended() {
        return _attended;
    }

    public void setAttended(final List<String> _attended) {
        this._attended = _attended;
    }
    @DynamoDBAttribute(attributeName = "done")
    public List<String> getDone() {
        return _done;
    }

    public void setDone(final List<String> _done) {
        this._done = _done;
    }

}
