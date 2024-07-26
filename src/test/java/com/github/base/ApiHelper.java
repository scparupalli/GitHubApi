package com.github.base;

import com.github.utils.PropertiesUtil;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;


public class ApiHelper {
	public static final String BASE_URL = PropertiesUtil.getProperty("base.url");
    public static final String AUTH_TOKEN = PropertiesUtil.getProperty("auth.token");

    static {
        RestAssured.baseURI = BASE_URL;
    }

    public static RequestSpecification getRequestSpecification() {
        System.out.println("Base URL: " + BASE_URL);
        System.out.println("Authorization Token: " + AUTH_TOKEN);
        
        return RestAssured.given()
                .header("Authorization", "Bearer " + AUTH_TOKEN);
    }
}