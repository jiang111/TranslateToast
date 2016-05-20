package com.jiang.android.translatetoast.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiang on 5/20/16.
 */

public class TranslateModel {

    @SerializedName("errorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("query")
    @Expose
    private String query;
    @SerializedName("translation")
    @Expose
    private List<String> translation = new ArrayList<String>();
    @SerializedName("basic")
    @Expose
    private Basic basic;
    @SerializedName("web")
    @Expose
    private List<Web> web = new ArrayList<Web>();

    /**
     * @return The errorCode
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode The errorCode
     */
    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @return The query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query The query
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return The translation
     */
    public List<String> getTranslation() {
        return translation;
    }

    /**
     * @param translation The translation
     */
    public void setTranslation(List<String> translation) {
        this.translation = translation;
    }

    /**
     * @return The basic
     */
    public Basic getBasic() {
        return basic;
    }

    /**
     * @param basic The basic
     */
    public void setBasic(Basic basic) {
        this.basic = basic;
    }

    /**
     * @return The web
     */
    public List<Web> getWeb() {
        return web;
    }

    /**
     * @param web The web
     */
    public void setWeb(List<Web> web) {
        this.web = web;
    }

    @Override
    public String toString() {
        return "TranslateModel{" +
                "errorCode=" + errorCode +
                ", query='" + query + '\'' +
                ", translation=" + translation +
                ", basic=" + basic +
                ", web=" + web +
                '}';
    }

    public static class Basic {
        @SerializedName("phonetic")
        @Expose
        private String phonetic;
        @SerializedName("uk-phonetic")
        @Expose
        private String ukPhonetic;
        @SerializedName("us-phonetic")
        @Expose
        private String usPhonetic;
        @SerializedName("explains")
        @Expose
        private List<String> explains = new ArrayList<String>();

        /**
         * @return The phonetic
         */
        public String getPhonetic() {
            return phonetic;
        }

        /**
         * @param phonetic The phonetic
         */
        public void setPhonetic(String phonetic) {
            this.phonetic = phonetic;
        }

        /**
         * @return The ukPhonetic
         */
        public String getUkPhonetic() {
            return ukPhonetic;
        }

        /**
         * @param ukPhonetic The uk-phonetic
         */
        public void setUkPhonetic(String ukPhonetic) {
            this.ukPhonetic = ukPhonetic;
        }

        /**
         * @return The usPhonetic
         */
        public String getUsPhonetic() {
            return usPhonetic;
        }

        /**
         * @param usPhonetic The us-phonetic
         */
        public void setUsPhonetic(String usPhonetic) {
            this.usPhonetic = usPhonetic;
        }

        /**
         * @return The explains
         */
        public List<String> getExplains() {
            return explains;
        }

        /**
         * @param explains The explains
         */
        public void setExplains(List<String> explains) {
            this.explains = explains;
        }

        @Override
        public String toString() {
            return "Basic{" +
                    "phonetic='" + phonetic + '\'' +
                    ", ukPhonetic='" + ukPhonetic + '\'' +
                    ", usPhonetic='" + usPhonetic + '\'' +
                    ", explains=" + explains +
                    '}';
        }
    }

    public static class Web {
        @SerializedName("key")
        @Expose
        private String key;
        @SerializedName("value")
        @Expose
        private List<String> value = new ArrayList<String>();

        /**
         * @return The key
         */
        public String getKey() {
            return key;
        }

        /**
         * @param key The key
         */
        public void setKey(String key) {
            this.key = key;
        }

        /**
         * @return The value
         */
        public List<String> getValue() {
            return value;
        }

        /**
         * @param value The value
         */
        public void setValue(List<String> value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Web{" +
                    "key='" + key + '\'' +
                    ", value=" + value +
                    '}';
        }
    }
}
