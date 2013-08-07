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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.mvel2.MVEL.eval;

@Repository
public class ApplicationRepository extends BaseRepository {

    @Autowired
    protected ApplicationRepository(RestTemplate restTemplate, AsyncTaskExecutor asyncTaskExecutor, ObjectMapper objectMapper, String apiBaseUri, String uaaBaseUri) {
        super(restTemplate, asyncTaskExecutor, objectMapper, apiBaseUri, uaaBaseUri);
    }

    public void deleteById(String token, String id) {
        ResponseEntity<String> deleteResponseEntity = apiDelete(token, "v2/applications/".concat(id));
        if (!deleteResponseEntity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            throw new RepositoryException("Unable to delete application", deleteResponseEntity);
        }
    }

    public Application getById(String token, String id) {
        Future<ResponseEntity<String>> applicationResponseFuture = asyncApiGet(token, "v2/apps/".concat(id).concat("?inline-relations-depth=2"));
        Future<ResponseEntity<String>> applicationInstancesResponseFuture = asyncApiGet(token, "v2/apps/".concat(id).concat("/instances"));
        Future<ResponseEntity<String>> serviceInstancesResponseFuture = asyncApiGet(token, "v2/apps/".concat(id).concat("/service_bindings?inline-relations-depth=3"));

        try {
            ResponseEntity<String> applicationResponseEntity = applicationResponseFuture.get();
            if (!applicationResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
                throw new RepositoryException("Unable to get application response", applicationResponseEntity);
            }
            ResponseEntity<String> applicationInstancesResponseEntity = applicationInstancesResponseFuture.get();
            if (!applicationInstancesResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
                throw new RepositoryException("Unable to get application instances response", applicationInstancesResponseEntity);
            }
            ResponseEntity<String> serviceInstancesResponseEntity = serviceInstancesResponseFuture.get();
            if (!serviceInstancesResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
                throw new RepositoryException("Unable to get service instances response", serviceInstancesResponseEntity);
            }


            Application application = Application.fromCloudFoundryModel(getMapper().readValue(applicationResponseEntity.getBody(), new TypeReference<Map<String, Object>>() {}));

            Map<String, Object> applicationInstances = getMapper().readValue(applicationInstancesResponseEntity.getBody(), new TypeReference<Map<String, Object>>() {});
            for (String key : applicationInstances.keySet()) {
                application.addInstance(ApplicationInstance.fromCloudFoundryModel(key, applicationInstances.get(key)));
            }

            Object serviceInstances = getMapper().readValue(serviceInstancesResponseEntity.getBody(), new TypeReference<Map<String, Object>>() {});
            for (Object serviceInstance : eval("resources", serviceInstances, List.class)) {
                application.addServiceInstance(ServiceInstance.fromCloudFoundryModel(eval("entity.service_instance", serviceInstance, Object.class)));
            }
            return application;
        } catch (InterruptedException | ExecutionException e) {
            throw new RepositoryException("Unable to get response entity from future", e);
        } catch (IOException e) {
            throw new RepositoryException("Unable to parse JSON from response", e);
        }
    }

    public ResponseEntity<String> getInstanceLog(String token, String id, String instance, String logName) {
        return new ResponseEntity(apiGet(token, "v2/apps/".concat(id).concat("/instances/").concat(instance).concat("/files/logs/").concat(logName).concat(".log")), HttpStatus.OK);
    }

}
