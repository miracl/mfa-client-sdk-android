/* **************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ***************************************************************/
package com.miracl.mpinsdk.net;


import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


public class HTTPConnector implements IHTTPRequest {

    private final static String OS_CLASS_HEADER = "X-MIRACL-OS-Class";
    private final static String OS_CLASS_VALUE  = "android";

    private Hashtable<String, String> requestHeaders;
    private Hashtable<String, String> queryParams;
    private String                    requestBody;
    private int                       timeout;
    private String                    errorMessage;
    private int                       statusCode;
    private Hashtable<String, String> responseHeaders;
    private String                    responseData;

    private static String toString(InputStream is) throws IOException {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(is, "UTF-8");
            char[] buf = new char[512];
            StringBuilder str = new StringBuilder();
            int i;
            while ((i = isr.read(buf)) != -1) {
                str.append(buf, 0, i);
            }
            return str.toString();
        } finally {
            if (isr != null) {
                isr.close();
            }
        }
    }

    public HTTPConnector() {
        super();
        timeout = DEFAULT_TIMEOUT;
    }

    @Override
    public void SetHeaders(Hashtable<String, String> headers) {
        this.requestHeaders = headers;
    }

    @Override
    public void SetQueryParams(Hashtable<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    @Override
    public void SetContent(String data) {
        this.requestBody = data;
    }

    @Override
    public void SetTimeout(int seconds) {
        if (seconds <= 0) {
            throw new IllegalArgumentException();
        }
        this.timeout = seconds;
    }

    @Override
    public boolean Execute(int method, String url) {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException();
        }

        String fullUrl = addQueryParamsToUrl(url);

        try {
            responseData = sendRequest(fullUrl, HttpMethodMapper(method), requestBody, requestHeaders);
        } catch (FileNotFoundException e) {
            // No data in response
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getLocalizedMessage();
            return false;
        }

        return true;
    }

    @Override
    public String GetExecuteErrorMessage() {
        return errorMessage;
    }

    @Override
    public int GetHttpStatusCode() {
        return statusCode;
    }

    @Override
    public Hashtable<String, String> GetResponseHeaders() {
        return responseHeaders;
    }

    @Override
    public String GetResponseData() {
        return responseData;
    }

    private String sendRequest(String serviceURL, String httpMethod, String requestBody,
                               Hashtable<String, String> requestProperties) throws IOException {

        HttpURLConnection connection = null;
        DataOutputStream dos = null;
        String response;

        try {
            connection = getConnection(serviceURL, !TextUtils.isEmpty(requestBody));

            connection.setRequestMethod(httpMethod);
            connection.setConnectTimeout(timeout);

            // Set request properties
            connection.setRequestProperty(OS_CLASS_HEADER, OS_CLASS_VALUE);
            if (requestProperties != null && !requestProperties.isEmpty()) {

                Enumeration<String> keyEnum = requestProperties.keys();
                while (keyEnum.hasMoreElements()) {
                    String key = keyEnum.nextElement();
                    connection.setRequestProperty(key, requestProperties.get(key));
                }
            }

            // Set request body
            if (!TextUtils.isEmpty(requestBody)) {
                dos = new DataOutputStream(connection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dos, "UTF-8"));
                writer.write(requestBody);
                writer.close();
            }

            // Starts the query
            connection.connect();

            try {
                statusCode = connection.getResponseCode();
            } catch (IOException e) {
                statusCode = connection.getResponseCode();
                if (statusCode != 401) {
                    throw e;
                }
            }

            setResponseHeaders(connection.getHeaderFields());

            response = toString(connection.getInputStream());
        } finally {
            if (dos != null) {
                dos.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return response;
    }

    private void setResponseHeaders(Map<String, List<String>> headers) {
        responseHeaders = new Hashtable<>();

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String key = entry.getKey();

            if (key != null) {
                List<String> propertyList = entry.getValue();
                StringBuilder properties = new StringBuilder();

                for (String property : propertyList) {
                    properties.append(property);
                }

                responseHeaders.put(entry.getKey(), properties.toString());
            }
        }
    }

    private HttpURLConnection getConnection(String serviceURL, boolean output) throws IOException {

        HttpURLConnection httpConnection = (HttpURLConnection) new URL(serviceURL).openConnection();
        httpConnection.setDoInput(true);
        httpConnection.setDoOutput(output);
        return httpConnection;
    }

    private String HttpMethodMapper(int method) {
        switch (method) {
            case GET:
                return HTTP_GET;
            case POST:
                return HTTP_POST;
            case PUT:
                return HTTP_PUT;
            case DELETE:
                return HTTP_DELETE;
            case OPTIONS:
                return HTTP_OPTIONS;
            default:
                return HTTP_PATCH;
        }
    }

    private String addQueryParamsToUrl(String url) {
        StringBuilder urlBuilder = new StringBuilder(url);

        if (queryParams != null && !queryParams.isEmpty()) {
            Enumeration<String> keyEnum = queryParams.keys();
            urlBuilder.append("?");

            while (keyEnum.hasMoreElements()) {
                String key = keyEnum.nextElement();
                urlBuilder.append(key).append("=").append(queryParams.get(key));

                if (keyEnum.hasMoreElements()) {
                    urlBuilder.append("&");
                }
            }
        }
        return urlBuilder.toString();
    }
}
