package com.srcnrg.frac.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.util.concurrent.Executors;

@Configuration
public class FormulaCacheConfig {

	@Bean
	public TaskScheduler taskScheduler() 
	{
		return new ConcurrentTaskScheduler(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2));
	}
}
