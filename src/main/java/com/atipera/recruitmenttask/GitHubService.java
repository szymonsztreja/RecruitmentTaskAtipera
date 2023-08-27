package com.atipera.recruitmenttask;

import com.atipera.recruitmenttask.model.BadRequest;
import com.atipera.recruitmenttask.model.Branch;
import com.atipera.recruitmenttask.model.GitHubRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GitHubService {

    private static final String GET_REPOS = "https://api.github.com/users/%s/repos";
    private static final String GET_BRANCHES = "https://api.github.com/repos";

    @Value("${github.api.key}")
    private String API_KEY;

    public ResponseEntity<String> getAllRepos(String username) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder(
                URI.create(String.format(GET_REPOS, username)))
                .headers("Accept", "application/json",
                        "X-GitHub-Api-Version", "2022-11-28",
                        "Authorization",  "Bearer" + API_KEY)
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode  = response.statusCode();
        ObjectMapper objectMapper= new ObjectMapper();
        GitHubRepository[] repos;
        BadRequest badRequest = new BadRequest();

        if ( statusCode == HttpStatus.OK.value()) {
            repos = objectMapper.readValue(response.body(), GitHubRepository[].class);

            for ( GitHubRepository rep : repos) {
                for ( Branch branch : getBranches(rep.getOwner().login(), rep.getName())) {
                    rep.setBranch(branch);
                }
            }

            String json = objectMapper.writeValueAsString(repos);
            return ResponseEntity.ok(json);
        } else if (statusCode == HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()) {
            return ResponseEntity.ok(createBadRequestFromHttpCode(response));
        } else if ( statusCode == HttpStatus.NOT_FOUND.value()) {
            return ResponseEntity.ok(createBadRequestFromHttpCode(response));
        }
        return ResponseEntity
                .status(statusCode)
                .body(response.body());
    }

    private String createBadRequestFromHttpCode(HttpResponse<String> response) throws JsonProcessingException {
        BadRequest badRequest = new BadRequest();
        ObjectMapper objectMapper = new ObjectMapper();
        int statusCode = response.statusCode();

        JsonNode rootNode = objectMapper.readTree(response.body());
        if (statusCode == HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()) {
            badRequest.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
        }
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            badRequest.setStatus(HttpStatus.NOT_FOUND.value());
        }
        badRequest.setMessage(rootNode.get("message").toString());
        return objectMapper.writeValueAsString(badRequest);
    }

    private Branch[] getBranches(String owner, String repo) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();

        String getBranches = String.format("%s/%s/%s/branches", GET_BRANCHES, owner, repo);

        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(getBranches))
                .headers("Accept", "application/json",
                        "X-GitHub-Api-Version", "2022-11-28",
                        "Authorization",  "Bearer" + API_KEY)
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Branch[] branches = new Branch[0];
        if ( response.statusCode() == 200 ) {
            ObjectMapper objectMapper= new ObjectMapper();
            branches = objectMapper.readValue(response.body(), Branch[].class);

        }

        return branches;
    }



}
