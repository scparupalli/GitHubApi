package com.github.responsePOJO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gitgub.requestPOJO.RepositoryOwner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryResponse {
    @JsonProperty("id")
    private int id;

    @JsonProperty("node_id")
    private String nodeId; 
    
    @JsonProperty("name")
    private String name;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("owner")
    private RepositoryOwner owner;

    @JsonProperty("private")
    private boolean isPrivate;
    
    @JsonProperty("html_url")
    private String htmlUrl;

}
