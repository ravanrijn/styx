package com.github.styx.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mvel2.MVEL.eval;

@Repository
public class ServiceRepository extends BaseRepository {

    @Autowired
    protected ServiceRepository(RestTemplate restTemplate, AsyncTaskExecutor asyncTaskExecutor, ObjectMapper objectMapper, String apiBaseUri, String uaaBaseUri) {
        super(restTemplate, asyncTaskExecutor, objectMapper, apiBaseUri, uaaBaseUri);
    }

    public List<Service> getAll(String token) {
        String servicesResponse = apiGet(token, "v2/services?inline-relations-depth=1");
        try {
            List<Service> services = new ArrayList<>();

            Object response = getMapper().readValue(servicesResponse, new TypeReference<Map<String, Object>>() {});
            for (Object serviceResource : eval("resources", response, List.class)) {
                services.add(Service.fromCloudFoundryModel(serviceResource));
            }
            return services;
        } catch (IOException e) {
            throw new RepositoryException("Unable to parse JSON from response", e);
        }
    }

}
