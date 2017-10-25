package co.energenes.quikchat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Country {

    @Expose
    @SerializedName("country_prefix")
    private String mCountryPrefix;

    @Expose
    @SerializedName("dial_code")
    private Long mDialCode;

    @Expose
    @SerializedName("id")
    private Long mId;

    @Expose
    @SerializedName("name")
    private String mName;


    @Override
    public String toString() {
        return mName;
    }

    public String getCountryPrefix() {
        return mCountryPrefix;
    }

    public void setCountryPrefix(String countryPrefix) {
        mCountryPrefix = countryPrefix;
    }

    public Long getDialCode() {
        return mDialCode;
    }

    public void setDialCode(Long dialCode) {
        mDialCode = dialCode;
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

}
