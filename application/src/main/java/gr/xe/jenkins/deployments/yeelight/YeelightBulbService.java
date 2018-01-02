package gr.xe.jenkins.deployments.yeelight;

import gr.xe.YeelightBulb.bulb.BulbService;
import gr.xe.jenkins.deployments.DeployStatus;
import java.time.*;
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


    private static final Logger logger = LoggerFactory.getLogger(YeelightBulbService.class);
    private final BulbService bulbService;
    private State lastStatus = new State(DeployStatus.idle, new Date());
    private static int SECONDS_TO_WAIT = 60;
    private static int STARTING_HOUR = 8;
    private static int STARTING_MINUTE = 30;
    private static int ENDING_MINUTE = 15;
    private static int ENDING_HOUR = 18;
    private static boolean isLightOn;
    private static boolean serverJustStarted=true;

    public YeelightBulbService(BulbService bulbService) {
        this.bulbService = bulbService;
    }

    public void sendAction(DeployStatus action) {
        lastStatus = new State(action, new Date());
        if (!isLightOn) {
            bulbService.switchOn();
        }
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
        boolean noJobsRunning = jobMap == 0;
        switchLightOffIfNeeded();
        if (statusHasChanedAtLeastOnce && statusIsNotIdle && noJobsRunning) {
            if (shouldResetStatus(lastStatus.getLastUpdate())) {
                lastStatus = new State(DeployStatus.idle, new Date());
                bulbService.reset();
            }
        }
    }

    private void switchLightOffIfNeeded() {
        if (afterHours()&&(isLightOn||serverJustStarted)) {
            bulbService.switchOff();
            isLightOn=false;
        } else if(!afterHours()&&(!isLightOn||serverJustStarted)){
            bulbService.switchOn();
            isLightOn=true;
        }
         serverJustStarted=false;
    }


    private boolean afterHours() {
        LocalDate now=LocalDate.now();
        LocalDateTime start=now.atTime(STARTING_HOUR,STARTING_MINUTE);
        LocalDateTime end=now.atTime(ENDING_HOUR,ENDING_MINUTE);
        LocalDateTime nowTime=LocalDateTime.now();
        return nowTime.isAfter(end)||nowTime.isBefore(start)||shouldBeClosed();
    }

    private boolean shouldBeClosed() {
        LocalDate now=LocalDate.now();
        boolean isSaturday = now.getDayOfWeek() == DayOfWeek.SATURDAY;
        boolean isSunday= now.getDayOfWeek() == DayOfWeek.SUNDAY;
        return isSaturday||isSunday;
    }

    /**
     * Reset after SECONDS_TO_WAIT have expired
     *
     * @param lastUpdate last update time
     * @return true if should reset
     */
    private boolean shouldResetStatus(Date lastUpdate) {
        if (lastUpdate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(lastUpdate);
            cal.add(Calendar.SECOND, SECONDS_TO_WAIT);
            Date toPass = new Date(cal.getTimeInMillis());
            return new Date().after(toPass);
        } else return false;
    }


    class State {
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
