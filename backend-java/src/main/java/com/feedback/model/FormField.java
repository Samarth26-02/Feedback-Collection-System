package com.feedback.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Form field model class
 */
public class FormField {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("label")
    private String label;
    
    @JsonProperty("required")
    private boolean required;
    
    @JsonProperty("placeholder")
    private String placeholder;
    
    @JsonProperty("options")
    private String[] options; // For select, radio, checkbox options
    
    @JsonProperty("order")
    private int order;

    // Default constructor
    public FormField() {
        this.required = false;
        this.order = 0;
    }

    // Constructor with parameters
    public FormField(String id, String type, String label, boolean required) {
        this();
        this.id = id;
        this.type = type;
        this.label = label;
        this.required = required;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "FormField{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", label='" + label + '\'' +
                ", required=" + required +
                ", placeholder='" + placeholder + '\'' +
                ", order=" + order +
                '}';
    }
}
