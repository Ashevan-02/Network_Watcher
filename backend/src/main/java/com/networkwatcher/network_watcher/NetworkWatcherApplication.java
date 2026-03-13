package com.networkwatcher.network_watcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NetworkWatcherApplication {

	public static void main(String[] args) {
		SpringApplication.run(NetworkWatcherApplication.class, args);
	}

}
