package dw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DwApi7Application {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DwApi7Application.class, args);
		
		Scheduler scheduler = (Scheduler)context.getBean("scheduler");
		scheduler.initScheduler();
		
	}
}
