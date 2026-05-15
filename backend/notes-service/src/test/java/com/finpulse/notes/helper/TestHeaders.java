package com.finpulse.notes.helper;

import org.springframework.http.HttpHeaders;

public class TestHeaders {

    public static final String USER_ID_1 = "user-001";
    public static final String USER_ID_2 = "user-002";

    public static HttpHeaders forUser(String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId);
        return headers;
    }

    public static HttpHeaders forUser1() {
        return forUser(USER_ID_1);
    }

    public static HttpHeaders forUser2() {
        return forUser(USER_ID_2);
    }
}
