package com.app.deliverytracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/sse")
public class LocationSseController {

    // store active connections per order
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // CUSTOMER CONNECTS HERE (ONE TIME)
    @GetMapping("/track/{orderId}")
    public SseEmitter trackOrder(@PathVariable Long orderId) {

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitters.put(orderId, emitter);

        emitter.onCompletion(() -> emitters.remove(orderId));
        emitter.onTimeout(() -> emitters.remove(orderId));

        return emitter;
    }

    // PUSH LOCATION UPDATE TO CUSTOMER
    public void sendLocationUpdate(Long orderId, Object data) {

        SseEmitter emitter = emitters.get(orderId);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("location-update")
                        .data(data));
            } catch (Exception e) {
                emitters.remove(orderId);
            }
        }
    }

    public void closeStream(Long orderId, String message){
        SseEmitter emitter = emitters.get(orderId);

        if(emitter != null){
            try {
                emitter.send(SseEmitter.event()
                        .name("completed")
                        .data(message));

                emitter.complete();
            } catch (Exception e){
                emitter.completeWithError(e);
            }
            emitters.remove(orderId);
        }
    }
}