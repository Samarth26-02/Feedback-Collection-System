package com.feedback.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Feedback response model class
 */
public class FeedbackResponse {
    private int id;
    private int formId;
    private String respondentEmail;
    private Map<String, Object> responseData;
    private LocalDateTime submittedAt;

    // Default constructor
    public FeedbackResponse() {
        this.submittedAt = LocalDateTime.now();
    }

    // Constructor with parameters
    public FeedbackResponse(int formId, String respondentEmail, Map<String, Object> responseData) {
        this();
        this.formId = formId;
        this.respondentEmail = respondentEmail;
        this.responseData = responseData;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFormId() {
        return formId;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public String getRespondentEmail() {
        return respondentEmail;
    }

    public void setRespondentEmail(String respondentEmail) {
        this.respondentEmail = respondentEmail;
    }

    public Map<String, Object> getResponseData() {
        return responseData;
    }

    public void setResponseData(Map<String, Object> responseData) {
        this.responseData = responseData;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    @Override
    public String toString() {
        return "FeedbackResponse{" +
                "id=" + id +
                ", formId=" + formId +
                ", respondentEmail='" + respondentEmail + '\'' +
                ", submittedAt=" + submittedAt +
                '}';
    }
}
