
package com.books.leemon.buchstabenkiste.models.pojo;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Epub {

    @SerializedName("isAvailable")
    @Expose
    private Boolean isAvailable;

    /**
     * 
     * @return
     *     The isAvailable
     */
    public Boolean getIsAvailable() {
        return isAvailable;
    }

    /**
     * 
     * @param isAvailable
     *     The isAvailable
     */
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

}
