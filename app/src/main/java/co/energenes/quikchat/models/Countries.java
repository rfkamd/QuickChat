
package co.energenes.quikchat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Countries {

    @Expose
    @SerializedName("countries")
    private List<Country> mCountries;

    public List<Country> getCountries() {
        return mCountries;
    }

    public void setCountries(List<Country> countries) {
        mCountries = countries;
    }

}
