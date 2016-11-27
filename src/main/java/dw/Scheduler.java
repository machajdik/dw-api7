package dw;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class Scheduler {

	protected ThreadPoolTaskScheduler generalScheduler;
	
	public void initScheduler() {
		this.generalScheduler = new ThreadPoolTaskScheduler();
		this.generalScheduler.setPoolSize(10);
		this.generalScheduler.initialize();

		
	}
	
}
