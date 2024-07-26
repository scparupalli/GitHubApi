package com.github.tests.CRUD;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gitgub.requestPOJO.RepositoryOwner;
import com.gitgub.requestPOJO.RepositoryRequest;
import com.github.base.ApiHelper;
import com.github.responsePOJO.RepositoryResponse;
import com.github.utils.PropertiesUtil;

public class GetSingleRepository {
	RequestSpecification requestSpec;
	String owner;
	String repo;
	String username;
	String deleterepo;

	@BeforeClass
	public void setup() {
		requestSpec = ApiHelper.getRequestSpecification();
		owner = PropertiesUtil.getProperty("owner");
		repo = PropertiesUtil.getProperty("repo");
		username = PropertiesUtil.getProperty("username");
	}

	@Test
	public void getSingleRepo() {
		 Response response = ApiHelper.getRequestSpecification()
	                .when()
	                .get("/repos/" + owner + "/" + repo)
	                .then()
	                .statusCode(200)
	                .extract()
	                .response();

	        RepositoryResponse repoResponse = response.as(RepositoryResponse.class);
	        RepositoryOwner repoOwner = repoResponse.getOwner();

	        System.out.println("Owner Login: " + repoOwner.getLogin());

	        String defaultBranch = response.jsonPath().getString("default_branch");
	        System.out.println("Default Branch: " + defaultBranch);

	        Assert.assertEquals(repoResponse.getFullName(), owner + "/" + repo);
	        Assert.assertEquals(defaultBranch, "main");
	        Assert.assertEquals(response.getHeader("Content-Type"), "application/json; charset=utf-8");
	    }
	    
	@Test
	public void getSingleRepoWithNonExistingName() {
		String owner = PropertiesUtil.getProperty("owner");
        String repo = "non-existing-repo";

        System.out.println("Base URL: " + ApiHelper.BASE_URL);
        System.out.println("Owner: " + owner);
        System.out.println("Repo: " + repo);

        Response response = ApiHelper.getRequestSpecification()
                .when()
                .get("/repos/" + owner + "/" + repo)
                .then()
                .statusCode(404)
                .extract()
                .response();

        Assert.assertEquals(response.jsonPath().getString("message"), "Not Found");
    }

	@Test
	public void getAllRepos() {
		System.out.println("Base URL: " + ApiHelper.BASE_URL);

        Response response = ApiHelper.getRequestSpecification()
                .when()
                .get("/user/repos")
                .then()
                .statusCode(200)
                .extract()
                .response();

        int totalRepos = response.jsonPath().getList("$").size();
        System.out.println("Total number of repositories: " + totalRepos);

        response.jsonPath().getList("$").forEach(repo -> {
            if (!(boolean) ((java.util.Map) repo).get("private")) {
                System.out.println("Public repository: " + ((java.util.Map) repo).get("name"));
            }
        });

        Assert.assertEquals(response.getHeader("Content-Type"), "application/json; charset=utf-8");
    }

	@Test
	public void createRepo() {
		
		String owner = PropertiesUtil.getProperty("owner");
        String repo = "Hello";

        System.out.println("Base URL: " + ApiHelper.BASE_URL);
        System.out.println("Owner: " + owner);
        System.out.println("Repo: " + repo);

        RepositoryRequest requestBody = RepositoryRequest.builder()
                .name(repo)
                .description("This is your first repo!")
                .isPrivate(false)
                .build();

        Response response = ApiHelper.getRequestSpecification()
                .body(requestBody)
                .when()
                .post("/user/repos")
                .then()
                .statusCode(201)
                .extract()
                .response();

        RepositoryResponse repoResponse = response.as(RepositoryResponse.class);

        Assert.assertEquals(repoResponse.getName(), repo);
        Assert.assertEquals(repoResponse.getOwner().getLogin(), owner);
        Assert.assertEquals(response.jsonPath().getString("owner.type"), "User");
    
    }

	@Test
	public void createRepoWithExistingName() {

		RepositoryRequest requestBody = RepositoryRequest.builder()
                .name("Hello")
                .description("This repo already exists.")
                .isPrivate(false)
                .build();

        Response response = ApiHelper.getRequestSpecification()
                .body(requestBody)
                .post("/user/repos");

        int statusCode = response.getStatusCode();
        String responseBody = response.getBody().asString();
        String actualErrorMessage = "";

        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray errors = jsonResponse.getJSONArray("errors");
            if (errors.length() > 0) {
                JSONObject firstError = errors.getJSONObject(0);
                actualErrorMessage = firstError.getString("message");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println("Response Status Code: " + statusCode);
        System.out.println("Actual error message: " + actualErrorMessage);

        Assert.assertEquals(statusCode, 422, "Expected status code does not match.");
        Assert.assertEquals(actualErrorMessage, "name already exists on this account",
                "Error message should indicate name already exists.");
    
    }

	@Test
	public void updateRepositoryName() {
		
		String owner = PropertiesUtil.getProperty("owner");
        String repo = PropertiesUtil.getProperty("repo");
        String newRepoName = "Updated-Repo-Name";

        System.out.println("Base URL: " + ApiHelper.BASE_URL);
        System.out.println("Owner: " + owner);
        System.out.println("Repo: " + repo);

        String requestBody = "{\"name\":\"" + newRepoName
                + "\",\"description\":\"my repository created using apis after update\",\"private\":false}";

        Response response = ApiHelper.getRequestSpecification()
                .body(requestBody)
                .when()
                .patch("/repos/" + owner + "/" + repo)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Assert.assertEquals(response.jsonPath().getString("name"), newRepoName);
    
	}

	@Test
	public void deleteRepo() {
		String owner = PropertiesUtil.getProperty("owner");
		String repo = PropertiesUtil.getProperty("deleterepo");

		System.out.println("Base URL: " + ApiHelper.BASE_URL);
		System.out.println("Owner: " + owner);
		System.out.println("Repo: " + repo);

		ApiHelper.getRequestSpecification().when().delete("/repos/" + owner + "/" + repo).then().statusCode(204);

		// Verify that the repo has been deleted
		ApiHelper.getRequestSpecification().when().get("/repos/" + owner + "/" + repo).then().statusCode(404);
	}
		
	@Test
	public void deleteRepoWithNonExistingName() {
		String owner = PropertiesUtil.getProperty("owner");
		String repo = "non-existing-repo";

		System.out.println("Base URL: " + ApiHelper.BASE_URL);
		System.out.println("Owner: " + owner);
		System.out.println("Repo: " + repo);

		Response response = ApiHelper.getRequestSpecification().when().delete("/repos/" + owner + "/" + repo).then()
				.statusCode(404).extract().response();
		String errorMessage = response.jsonPath().getString("message");

		Assert.assertEquals(errorMessage, "Not Found");

		Assert.assertEquals(response.jsonPath().getString("message"), "Not Found");

	}
}
