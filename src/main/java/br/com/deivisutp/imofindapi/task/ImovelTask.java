package br.com.deivisutp.imofindapi.task;

import br.com.deivisutp.imofindapi.service.ScrappingService;
import br.com.deivisutp.imofindapi.util.DataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

@Configuration
@EnableScheduling
public class ImovelTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImovelTask.class);
    private static final String TIME_ZONE = "America/Sao_Paulo";
    private static final String DD_MM_YYYY_HH_MM_SS = "dd/MM/yyyy HH:mm:ss";

    @Autowired
    private ScrappingService scrappingService;

    /*
    @Scheduled(cron = "0/30 * 19-23 * * WED", zone = TIME_ZONE)
    public void taskImovelWednesday() {
        startScheduling("taskPartidaWednesday()");
    }

    @Scheduled(cron = "0/30 * 19-23 * * THU", zone = TIME_ZONE)
    public void taskImovelThursday() {
        startScheduling("taskPartidaThursday()");
    }

    @Scheduled(cron = "0/30 * 16-23 * * SAT", zone = TIME_ZONE)
    public void taskImovelSaturday() {
        startScheduling("taskPartidaSaturday()");
    }

    @Scheduled(cron = "0/30 * 16-23 * * SUN", zone = TIME_ZONE)
    public void taskImovelSundayAfternoon() {
        startScheduling("taskPartidaSundayAfternoon()");
    }

    @Scheduled(cron = "0/30 * 11-13 * * SUN", zone = TIME_ZONE)
    public void taskImovelSundayMorning() {
        startScheduling("taskPartidaSundayMorning()");
    }

     */

  //  @Scheduled(cron = "0 0 6,22 * * *", zone = TIME_ZONE)
   // public void taskImovel() {
      //  startScheduling("taskImovel()");
   // }

    private void startScheduling(String day) {
        this.saveLogInfo(String.format("%s: %s", day, DataUtil.formataDateEmString(new Date(), DD_MM_YYYY_HH_MM_SS)));

        scrappingService.verificaImoveisPeriodo();
    }

    private void saveLogInfo(String msg) {
        LOGGER.info(msg);
    }
}
