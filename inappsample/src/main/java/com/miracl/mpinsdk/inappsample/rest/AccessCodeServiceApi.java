/***************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ***************************************************************/
package com.miracl.mpinsdk.inappsample.rest;

import com.miracl.mpinsdk.inappsample.rest.model.AccessCodeInfo;
import com.miracl.mpinsdk.inappsample.rest.model.AuthorizeUrlInfo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * REST Controller
 */
public interface AccessCodeServiceApi {

    /**
     * GET request for authorization URL
     *
     * @return ServiceConfiguration
     */
    @GET("/authzurl")
    Call<AuthorizeUrlInfo> getAuthURL();

    /**
     * POST request to validate user access code
     *
     * @param body
     *   AccessCodeInfo
     */
    @Headers("Content-type: application/json")
    @POST("/authtoken")
    Call<ResponseBody> setAuthToken(@Body AccessCodeInfo body);
}
