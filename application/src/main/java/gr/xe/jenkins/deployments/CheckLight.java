package gr.xe.jenkins.deployments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import gr.xe.jenkins.deployments.yeelight.YeelightBulbService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zep
 * Date: 20/11/2017
 * Time: 11:38 πμ
 * Company: www.xe.gr
 */
@Component
@Service
public class CheckLight {

    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    @Value("${deploy.light.server}")
    public String HTTPS_PROMOTION_XE_GR;
    @Value("${jenkins.url}")
    private String JENKINS_URL = "http://jenkins.in.xe.gr";
    @Value("${oauth.token}")
    private String OAUTH_TOKEN = "enBvbnRpa2FzOjkwYWUxMTU3ZWI2NDFkZTc0NmNkYWM4YjAzNDIzMDJk";
    private Map<String, DeployStatus> jobMap = new LinkedHashMap<>();
    private static DeployStatus lastStatusSend;

    private final YeelightBulbService bulbService;

    public CheckLight(YeelightBulbService bulbService) {
        this.bulbService = bulbService;
    }


    @Scheduled(fixedRate = 1000)
    public void checkJobs() throws IOException {
        ResponseEntity<String> response = getjenkinsResponse(JENKINS_URL + "/api/json");
        bulbService.updateStatus();
        getRunningJobs(response);
        for (String runningJob : jobMap.keySet()) {
            checkJob(runningJob);
        }
    }

    private void getRunningJobs(ResponseEntity<String> response) throws IOException {
        List<String> jobsPat2 = JsonPath.parse(response.getBody()).read("$.jobs[*]");
        final TypeReference<Collection<Jobs>> typeReference = new TypeReference<Collection<Jobs>>() {};
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
        return getjenkinsResponse(String.format(JENKINS_URL + "/job/%s/lastBuild/api/json", jobName));
    }

    private ResponseEntity<String> getjenkinsResponse(String url) {
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + OAUTH_TOKEN);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        return template.postForEntity(url, request, String.class);
    }

    private void handleJobRunning(String jobName, DeployStatus deployStatus) {
        DeployStatus currentStatus = jobMap.get(jobName);
        boolean deploymentStatusChanged = currentStatus != deployStatus && currentStatus != null;
        if (deploymentStatusChanged||lastStatusSend!=deployStatus) {
            handleAction(deployStatus, jobName);
        }
        if (deployStatus != DeployStatus.deploy&& deployStatus != DeployStatus.deploy_initiated) {
            jobMap.remove(jobName);
        }
    }

    private void handleAction(DeployStatus action, String jobName) {
        System.out.println(new Date()+" JobName = " + jobName+". Action = " + action);
        jobMap.put(jobName, action);
        lastStatusSend=action;
        sendBulbAction(action);
        sendLightAction(action);
    }

    private void sendBulbAction(DeployStatus action) {
        bulbService.sendAction(action);
    }

    private void sendLightAction(DeployStatus action) {
        new RestTemplate().exchange(HTTPS_PROMOTION_XE_GR + "/projects/deployLight/set.php?action={action}", HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class,
                action);
    }

}