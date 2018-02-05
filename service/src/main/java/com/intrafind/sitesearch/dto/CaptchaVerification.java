/*
 * Copyright 2018 IntraFind Software AG. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intrafind.sitesearch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CaptchaVerification {
    private Boolean success;
    @JsonProperty("error-codes")
    private List<String> errorCodes;

    private CaptchaVerification() {
    }

    public CaptchaVerification(Boolean success, List<String> errorCodes) {
        this.success = success;
        this.errorCodes = errorCodes;
    }

    public Boolean getSuccess() {
        return success;
    }

    public List<String> getErrorCodes() {
        return errorCodes;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setErrorCodes(List<String> errorCodes) {
        this.errorCodes = errorCodes;
    }
}
