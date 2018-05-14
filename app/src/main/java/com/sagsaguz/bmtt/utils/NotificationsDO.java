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

@DynamoDBTable(tableName = "bmtt-mobilehub-1157786907-Notifications")

public class NotificationsDO {
    private String _centre;
    private String _when;
    private String _message;
    private List<String> _who;

    @DynamoDBHashKey(attributeName = "centre")
    @DynamoDBAttribute(attributeName = "centre")
    public String getCentre() {
        return _centre;
    }

    public void setCentre(final String _centre) {
        this._centre = _centre;
    }
    @DynamoDBRangeKey(attributeName = "when")
    @DynamoDBAttribute(attributeName = "when")
    public String getWhen() {
        return _when;
    }

    public void setWhen(final String _when) {
        this._when = _when;
    }
    @DynamoDBAttribute(attributeName = "message")
    public String getMessage() {
        return _message;
    }

    public void setMessage(final String _message) {
        this._message = _message;
    }
    @DynamoDBAttribute(attributeName = "who")
    public List<String> getWho() {
        return _who;
    }

    public void setWho(final List<String> _who) {
        this._who = _who;
    }

}
