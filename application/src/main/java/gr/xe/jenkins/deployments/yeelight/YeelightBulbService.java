package gr.xe.jenkins.deployments.yeelight;

import gr.xe.YeelightBulb.bulb.BulbService;
import gr.xe.jenkins.deployments.DeployStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: zep
 * Date: 23/11/2017
 * Time: 12:20 μμ
 * Company: www.xe.gr
 */
@Service
public class YeelightBulbService {


    private final BulbService bulbService;
    private static final Logger logger = LoggerFactory.getLogger(YeelightBulbService.class);
    private State lastStatus=new State(DeployStatus.idle,new Date());
    private int SECONDS_TO_WAIT=10;

    public YeelightBulbService(BulbService bulbService) {
        this.bulbService = bulbService;
    }

    public void sendAction(DeployStatus action) {
        lastStatus=new State(action,new Date());
        switch (action) {
            case deploy:
            case deploy_initiated:
                sendDeploy();
                break;
            case success:
                sendSuccess();
                break;
            case fail:
                sendFail();
                break;
        }
    }

    private void sendFail() {
        bulbService.failure();
    }

    private void sendSuccess() {
        bulbService.success();
    }

    private void sendDeploy() {
        try {
            bulbService.deployment();
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("An error appeared in this method");
        }
    }

    public void updateStatus(int jobMap) {
        boolean statusHasChanedAtLeastOnce = lastStatus.getStatus() != null;
        boolean statusIsNotIdle = lastStatus.getStatus() != DeployStatus.idle;
        boolean noJobsRunning=jobMap==0;
        if (statusHasChanedAtLeastOnce && statusIsNotIdle&&noJobsRunning) {
            if (shouldResetStatus(lastStatus.getLastUpdate())) {
                lastStatus=new State(DeployStatus.idle, new Date());
                bulbService.reset();
            }
        }
    }

    /**
     * Reset after SECONDS_TO_WAIT have expired
     * @param lastUpdate last update time
     * @return true if should reset
     */
    private boolean shouldResetStatus(Date lastUpdate) {
        if (lastUpdate!=null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(lastUpdate);
            cal.add(Calendar.SECOND, SECONDS_TO_WAIT);
            Date toPass=new Date(cal.getTimeInMillis());
            return new Date().after(toPass);
        }
        else return false;
    }


    class State{
        private DeployStatus status;
        private Date lastUpdate;

        public State(DeployStatus status, Date lastUpdate) {
            this.status = status;
            this.lastUpdate = lastUpdate;
        }

        public DeployStatus getStatus() {
            return status;
        }

        public void setStatus(DeployStatus status) {
            this.status = status;
        }

        public Date getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(Date lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }
}
