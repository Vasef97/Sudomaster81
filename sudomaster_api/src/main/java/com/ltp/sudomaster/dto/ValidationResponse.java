package com.ltp.sudomaster.dto;

import java.util.List;

public class ValidationResponse {

    private Boolean isValid;
    private Boolean isComplete;
    private List<String> errors;
    private String status;

    public ValidationResponse() {}

    public ValidationResponse(Boolean isValid, Boolean isComplete, List<String> errors, String status) {
        this.isValid = isValid;
        this.isComplete = isComplete;
        this.errors = errors;
        this.status = status;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public Boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(Boolean isComplete) {
        this.isComplete = isComplete;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
