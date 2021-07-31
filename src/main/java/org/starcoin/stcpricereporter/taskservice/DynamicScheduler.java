package org.starcoin.stcpricereporter.taskservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;
import org.starcoin.stcpricereporter.chainlink.PriceFeedRecord;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import static org.starcoin.stcpricereporter.chainlink.utils.CsvUtils.readCsvPriceFeedRecords;


@Service
public class DynamicScheduler implements SchedulingConfigurer {

    private static Logger LOGGER = LoggerFactory.getLogger(DynamicScheduler.class);

    private String ethereumHttpServiceUrl = "https://mainnet.infura.io/v3/72637bfa15a940dcadcec25a6fe0fca1";

    // ScheduledTaskRegistrar scheduledTaskRegistrar;

    // ScheduledFuture future;

    @Bean
    public TaskScheduler poolScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        scheduler.setPoolSize(2);
        scheduler.initialize();
        return scheduler;
    }

    // We can have multiple tasks inside the same registrar as we can see below.
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
//        if (scheduledTaskRegistrar == null) {
//            scheduledTaskRegistrar = taskRegistrar;
//        }
        if (taskRegistrar.getScheduler() == null) {
            taskRegistrar.setScheduler(poolScheduler());
        }

        String csvFilePath = "src/main/resources/EthereumPriceFeeds-Mainnet.csv";
        Reader in;
        try {
            in = new FileReader(csvFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        List<PriceFeedRecord> priceFeedRecords = readCsvPriceFeedRecords(in)
                .stream().filter(f -> f.getEnabled() != null && f.getEnabled()).collect(Collectors.toList());
        System.out.println(priceFeedRecords);

        for (PriceFeedRecord p : priceFeedRecords) {
            taskRegistrar.getScheduler().schedule(
                    new PriceUpdateTask(ethereumHttpServiceUrl, p.getPair(), p.getProxy()), //() -> scheduleFixed(),
                    t -> {
                        Calendar nextExecutionTime = new GregorianCalendar();
                        Date lastActualExecutionTime = t.lastActualExecutionTime();
                        nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
                        nextExecutionTime.add(Calendar.SECOND, 7);
                        return nextExecutionTime.getTime();
                    });

        }

//        // or cron way
//        taskRegistrar.addTriggerTask(() -> scheduleCron(repo.findById("next_exec_time").get().getConfigValue()), t -> {
//            CronTrigger crontrigger = new CronTrigger(repo.findById("next_exec_time").get().getConfigValue());
//            return crontrigger.nextExecutionTime(t);
//        });
    }

//    public void scheduleFixed() {
//        LOGGER.info("scheduleFixed: Next execution time of this will always be 5 seconds");
//    }

//    public void scheduleCron(String cron) {
//        LOGGER.info("scheduleCron: Next execution time of this taken from cron expression -> {}", cron);
//    }
//
//    /**
//     * @param mayInterruptIfRunning {@code true} if the thread executing this task
//     *                              should be interrupted; otherwise, in-progress tasks are allowed to complete
//     */
//    public void cancelTasks(boolean mayInterruptIfRunning) {
//        LOGGER.info("Cancelling all tasks");
//        future.cancel(mayInterruptIfRunning); // set to false if you want the running task to be completed first.
//    }

//    public void activateScheduler() {
//        LOGGER.info("Re-Activating Scheduler");
//        configureTasks(scheduledTaskRegistrar);
//    }

}