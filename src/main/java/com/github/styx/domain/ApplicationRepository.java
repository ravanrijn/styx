package com.github.styx.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.mvel2.MVEL.eval;

@Repository
public class ApplicationRepository extends BaseRepository {

    private final String apiBaseUri;

    @Autowired
    protected ApplicationRepository(RestTemplate restTemplate, AsyncTaskExecutor asyncTaskExecutor, ObjectMapper objectMapper, String apiBaseUri, String uaaBaseUri) {
        super(restTemplate, asyncTaskExecutor, objectMapper, apiBaseUri, uaaBaseUri);
        this.apiBaseUri = concatSlashIfNeeded(apiBaseUri);
    }

    public void deleteById(String token, String id) {
        apiDelete(token, "v2/applications/".concat(id));
    }

    public Application getById(String token, String id) {
        Future<ResponseEntity<Map<String, Object>>> applicationResponseFuture = asyncApiGet(token, "v2/apps/".concat(id).concat("?inline-relations-depth=2"));
        Future<ResponseEntity<Map<String, Object>>> applicationInstancesResponseFuture = asyncApiGet(token, "v2/apps/".concat(id).concat("/instances"));
        Future<ResponseEntity<Map<String, Object>>> serviceInstancesResponseFuture = asyncApiGet(token, "v2/apps/".concat(id).concat("/service_bindings?inline-relations-depth=3"));

        try {
            ResponseEntity<Map<String, Object>> applicationResponseEntity = applicationResponseFuture.get();
            if (!applicationResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
                throw new RepositoryException("Unable to get application response", applicationResponseEntity);
            }
            ResponseEntity<Map<String, Object>> applicationInstancesResponseEntity = applicationInstancesResponseFuture.get();
            if (!applicationInstancesResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
                throw new RepositoryException("Unable to get application instances response", applicationInstancesResponseEntity);
            }
            ResponseEntity<Map<String, Object>> serviceInstancesResponseEntity = serviceInstancesResponseFuture.get();
            if (!serviceInstancesResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
                throw new RepositoryException("Unable to get service instances response", serviceInstancesResponseEntity);
            }

            Application application = Application.fromCloudFoundryModel(applicationResponseEntity.getBody());

            Map<String, Object> applicationInstances = applicationInstancesResponseEntity.getBody();
            for (String key : applicationInstances.keySet()) {
                application.addInstance(ApplicationInstance.fromCloudFoundryModel(key, applicationInstances.get(key)));
            }
            Object serviceInstances = serviceInstancesResponseEntity.getBody();
            for (Object serviceInstance : eval("resources", serviceInstances, List.class)) {
                application.addServiceInstance(ServiceInstance.fromCloudFoundryModel(eval("entity.service_instance", serviceInstance, Object.class)));
            }
            return application;
        } catch (InterruptedException | ExecutionException e) {
            throw new RepositoryException("Unable to get response entity from future", e);
        }
    }

    public ResponseEntity<String> getInstanceLog(String token, String id, String instance, String logName) {
        String path = "v2/apps/".concat(id).concat("/instances/").concat(instance).concat("/files/logs/").concat(logName).concat(".log");
        return exchange(token, apiBaseUri, HttpMethod.GET, path, null, new ParameterizedTypeReference<String>() {});
    }

    public Application updateApplication(String token, String id, String body) {
        apiPut(token, "v2/apps/".concat(id).concat("?inline-relations-depth=2"), body);

        Map<String, Object> applicationResponse = apiGet(token, "v2/apps/".concat(id).concat("?inline-relations-depth=2"));
        return Application.fromCloudFoundryModel(applicationResponse);
    }
}
