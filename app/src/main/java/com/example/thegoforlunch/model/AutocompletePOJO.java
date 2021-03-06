package com.example.thegoforlunch.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * POJO of an Autocomplete API request
 */
public class AutocompletePOJO {

    @SerializedName("predictions")
    @Expose
    private List<Prediction> predictions = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<Prediction> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<Prediction> predictions) {
        this.predictions = predictions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    /**
     * Prediction POJO class
     */
    public static class Prediction {

        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("matched_substrings")
        @Expose
        private List<MatchedSubstring> matchedSubstrings = null;
        @SerializedName("place_id")
        @Expose
        private String placeId;
        @SerializedName("reference")
        @Expose
        private String reference;
        @SerializedName("structured_formatting")
        @Expose
        private StructuredFormatting structuredFormatting;
        @SerializedName("terms")
        @Expose
        private List<Term> terms = null;
        @SerializedName("types")
        @Expose
        private List<String> types = null;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<MatchedSubstring> getMatchedSubstrings() {
            return matchedSubstrings;
        }

        public void setMatchedSubstrings(List<MatchedSubstring> matchedSubstrings) {
            this.matchedSubstrings = matchedSubstrings;
        }

        public String getPlaceId() {
            return placeId;
        }

        public void setPlaceId(String placeId) {
            this.placeId = placeId;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public StructuredFormatting getStructuredFormatting() {
            return structuredFormatting;
        }

        public void setStructuredFormatting(StructuredFormatting structuredFormatting) {
            this.structuredFormatting = structuredFormatting;
        }

        public List<Term> getTerms() {
            return terms;
        }

        public void setTerms(List<Term> terms) {
            this.terms = terms;
        }

        public List<String> getTypes() {
            return types;
        }

        public void setTypes(List<String> types) {
            this.types = types;
        }
    }


    /**
     * MainTextMatchedSubstring POJO class
     */
    public static class MainTextMatchedSubstring {

        @SerializedName("length")
        @Expose
        private Integer length;
        @SerializedName("offset")
        @Expose
        private Integer offset;

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }
    }


    /**
     * MatchedSubstring POJO class
     */
    public static class MatchedSubstring {

        @SerializedName("length")
        @Expose
        private Integer length;
        @SerializedName("offset")
        @Expose
        private Integer offset;

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }
    }


    /**
     * StructuredFormatting POJO class
     */
    public static class StructuredFormatting {

        @SerializedName("main_text")
        @Expose
        private String mainText;
        @SerializedName("main_text_matched_substrings")
        @Expose
        private List<MainTextMatchedSubstring> mainTextMatchedSubstrings = null;
        @SerializedName("secondary_text")
        @Expose
        private String secondaryText;

        public String getMainText() {
            return mainText;
        }

        public void setMainText(String mainText) {
            this.mainText = mainText;
        }

        public List<MainTextMatchedSubstring> getMainTextMatchedSubstrings() {
            return mainTextMatchedSubstrings;
        }

        public void setMainTextMatchedSubstrings(List<MainTextMatchedSubstring> mainTextMatchedSubstrings) {
            this.mainTextMatchedSubstrings = mainTextMatchedSubstrings;
        }

        public String getSecondaryText() {
            return secondaryText;
        }

        public void setSecondaryText(String secondaryText) {
            this.secondaryText = secondaryText;
        }
    }


    /**
     * Term POJO class
     */
    public static class Term {

        @SerializedName("offset")
        @Expose
        private Integer offset;
        @SerializedName("value")
        @Expose
        private String value;

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}

