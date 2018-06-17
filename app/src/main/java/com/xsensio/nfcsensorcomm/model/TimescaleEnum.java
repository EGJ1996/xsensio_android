package com.xsensio.nfcsensorcomm.model;

/**
 * Created by Michael Heiniger on 31.07.17.
 */

public enum TimescaleEnum {
    SECOND("second", "s"),
    MILLISECOND("millisecond", "ms"),
    MICROSECOND("microsecond", "us");

    private String mDescription;
    private String mAbbreviation;

    TimescaleEnum(String description, String abbreviation) {
        mDescription = description;
        mAbbreviation = abbreviation;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getAbbreviation() {
        return mAbbreviation;
    }
}
