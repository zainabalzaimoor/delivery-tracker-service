package com.app.deliverytracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;


public class ConcurrencyTest implements CommandLineRunner {
    @Autowired
    private final DriverAssignmentService service;

    public ConcurrencyTest(DriverAssignmentService driverAssignmentService) {
        this.service = driverAssignmentService;
    }

    @Override
    public void run(String... args) throws Exception {
        Runnable task1 = () -> {
            try {
                service.assignDriver(4L, 3L);
                System.out.println(Thread.currentThread().getName() + " SUCCESS");
            } catch (Exception e){
                System.out.println(Thread.currentThread().getName() + " FAILED: " + e.getMessage());
            }
        };
        new Thread(task1, "Thread-1").start();
        new Thread(task1, "Thread-2").start();

    }
}
