package gr.xe.LightjenkinsCheck;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zep
 * Date: 20/11/2017
 * Time: 11:38 πμ
 * Company: www.xe.gr
 */
@Component
@EnableConfigurationProperties
public class CheckLight {

    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    private Map<String, DeployStatus> jobMap = new LinkedHashMap<>();

    @Scheduled(fixedRate = 1000)
    public void checkJobs() throws IOException {
        ResponseEntity<String> response = getjenkinsResponse("http://jenkins.in.xe.gr/api/json");
        getRunningJobs(response);
        for (String runningJob : jobMap.keySet()) {
            checkJob(runningJob);
        }
    }

    private void getRunningJobs(ResponseEntity<String> response) throws IOException {
        List<String> jobsPat2 = JsonPath.parse(response.getBody()).read("$.jobs[*]");
        final TypeReference<Collection<Jobs>> typeReference = new TypeReference<Collection<Jobs>>() {
        };
        ObjectMapper objectMapper = new ObjectMapper();
        List<Jobs> job = objectMapper.convertValue(jobsPat2, typeReference);
        job.stream()
                .filter(j -> j.getColor().endsWith("_anime") && jobMap.entrySet().stream()
                        .filter(e -> j.getName().equals(e.getKey())).count() == 0)
                .forEach(j -> jobMap.put(j.getName(), DeployStatus.deploy_initiated));
    }

    private void checkJob(String jobName) {
        ResponseEntity<String> response = getJobStatus(jobName);
        String result = JsonPath.read(response.getBody(), "$.result");
        DeployStatus currentStatus = getCurrentJobStatus(result);
        handleJobRunning(jobName, currentStatus);
    }

    private DeployStatus getCurrentJobStatus(String result) {
        if (result == null) {
            return DeployStatus.deploy;
        } else {
            if (result.equalsIgnoreCase(SUCCESS)) {
                return DeployStatus.success;
            } else {
                return DeployStatus.fail;
            }
        }
    }

    private ResponseEntity<String> getJobStatus(String jobName) {
        return getjenkinsResponse(String.format("http://jenkins.in.xe.gr/job/%s/lastBuild/api/json", jobName));
    }

    private ResponseEntity<String> getjenkinsResponse(String url) {
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic enBvbnRpa2FzOjkwYWUxMTU3ZWI2NDFkZTc0NmNkYWM4YjAzNDIzMDJk");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        return template.postForEntity(url, request, String.class);
    }

    private void handleJobRunning(String jobName, DeployStatus deployStatus) {
        DeployStatus currentStatus = jobMap.get(jobName);
        boolean deploymentStatusChanged = currentStatus != deployStatus && currentStatus != null;
        if (deploymentStatusChanged) {
            sendLightAction(deployStatus, jobName);
        }
        if (deployStatus != DeployStatus.deploy&& deployStatus != DeployStatus.deploy_initiated) {
            jobMap.remove(jobName);
        }
    }

    private void sendLightAction(DeployStatus action, String jobName) {
        System.out.println("action = " + action);
        System.out.println("jobName = " + jobName);
        jobMap.put(jobName, action);
        new RestTemplate().exchange("https://promotion.xe.gr/projects/deployLight/set.php?action={action}", HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class,
                action);
    }

    enum DeployStatus {
        deploy_initiated, deploy, success, fail
    }

}