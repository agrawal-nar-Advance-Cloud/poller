package com.webapp.poller.service;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    @KafkaListener(topics = "watch", groupId = "poller", containerFactory = "watchKafkaListenerFactory" )
    public void consumeJson(String watch) {
        JsonObject jsonObject = new JsonParser().parse(watch).getAsJsonObject();
        String zipcode = jsonObject.get("zipcode").getAsString();
        String Status = jsonObject.get("status").getAsString();
        String watchID = jsonObject.get("watchID").getAsString();

        List<String> w = watchInfo.getOrDefault(zipcode, new ArrayList<>());

        if (Status.equals("Created")) {
            w.add(watch);
        } else{
            w.removeIf(x -> {
                    JsonObject jsonObjectUp = new JsonParser().parse(x).getAsJsonObject();
                    String ID = jsonObjectUp.get("watchID").getAsString();
                    return watchID.equals(ID);
            });
            if (Status.equals("Updated")){
                w.add(watch);
            }
        }
        watchInfo.put(zipcode,w);

    }

    @Scheduled(fixedRate = 30000)
    public void getWeatherInformation() {
        for (Map.Entry<String, List<String>> watches : watchInfo.entrySet()) {
            String zipcode = watches.getKey();

            String URL = "https://api.openweathermap.org/data/2.5/weather?zip=" + zipcode + "&units=imperial&appid=d9c347c35fb2f44eacf9625d8d403250";
            String weather = restTemplate.getForObject(URL, String.class);
            String weatherInfo =weather.substring(1,(weather.length()));
            for (String watch : watches.getValue()) {
                String w = (watch.substring(0,(watch.length()-1))).concat(",");
                String message = w.concat(weatherInfo);
                kafkaTemplate.send(TOPIC, message);
            }
        }

    }

}
