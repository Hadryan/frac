package com.srcnrg.frac;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class AppReadyListener implements ApplicationListener<ApplicationReadyEvent> 
{
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) 
	{
		log.info("The Formula/Rules w/Actions container is ready for use.");
	}
}
