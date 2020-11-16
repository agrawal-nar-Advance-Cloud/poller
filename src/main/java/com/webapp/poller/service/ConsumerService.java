package com.webapp.poller.service;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class ConsumerService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final String TOPIC = "weather";

    Map<String,List<String>> watchInfo = new LinkedHashMap<>();

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    static final Summary publishTime = Summary.build().name("publish_time_weather").labelNames("topic").help("Publish time on weather topic.").register();
    static final Counter publishCount = Counter.build().name("message_published_total_weather").labelNames("topic").help("Total message published on weather topic.").register();
    static final Counter consumedCount = Counter.build().name("message_consumed_total_watch").labelNames("topic").help("Total message consumed on watch topic.").register();

    @KafkaListener(topics = "watch", groupId = "poller", containerFactory = "watchKafkaListenerFactory" )
    public void consumeJson(String watch) {

        try {
            consumedCount.labels("watch").inc();
            JsonObject jsonObject = new JsonParser().parse(watch).getAsJsonObject();
            String zipcode = jsonObject.get("zipcode").getAsString();
            String Status = jsonObject.get("status").getAsString();
            String watchID = jsonObject.get("watchID").getAsString();

            log.info("Watch consumed("+ Status +"),TOPIC:watch, WatchID:"+watchID);

            List<String> w = watchInfo.getOrDefault(zipcode, new ArrayList<>());

            if (Status.equals("Created")) {
                w.add(watch);
            } else {
                w.removeIf(x -> {
                    JsonObject jsonObjectUp = new JsonParser().parse(x).getAsJsonObject();
                    String ID = jsonObjectUp.get("watchID").getAsString();
                    return watchID.equals(ID);
                });
                if (Status.equals("Updated")) {
                    w.add(watch);
                }
            }
            watchInfo.put(zipcode, w);
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    @Scheduled(fixedRate = 30000)
    public void getWeatherInformation() {
        for (Map.Entry<String, List<String>> watches : watchInfo.entrySet()) {
            String zipcode = watches.getKey();

            String URL = "https://api.openweathermap.org/data/2.5/weather?zip=" + zipcode + "&units=imperial&appid=d9c347c35fb2f44eacf9625d8d403250";
            String weather = restTemplate.getForObject(URL, String.class);
            log.info("Call to weather API, Zipcode:"+zipcode);
            String weatherInfo =weather.substring(1,(weather.length()));
            for (String watch : watches.getValue()) {
                String w = (watch.substring(0,(watch.length()-1))).concat(",");
                String message = w.concat(weatherInfo);
                Summary.Timer requestTimer = publishTime.labels("weather").startTimer();
                kafkaTemplate.send(TOPIC, message);
                requestTimer.observeDuration();
                publishCount.labels("weather").inc();
                log.info("Watch published, TOPIC:"+ TOPIC);
            }
        }

    }

}
