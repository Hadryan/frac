package com.srcnrg.frac.domain;

import com.srcnrg.frac.domain.dto.FormulaDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This class (AND functionality in FormulaCache) mimics the legacy, generated code e.g.
 * 
	package com.iwellsite.core.formula.device_56789;
	
	import com.iwellsite.core.code.formula.builder.AbstractFormulaListener;
	import com.iwellsite.core.event.EventSubscriber;
	import java.util.Optional;
	import lombok.extern.log4j.Log4j2;
	import org.mariuszgromada.math.mxparser.Argument;
	import org.mariuszgromada.math.mxparser.Expression;
	import org.springframework.scheduling.annotation.Scheduled;
	
	@Log4j2
	public class Formula_11FormulaListener extends AbstractFormulaListener {
	  private static volatile Argument r_10 = new Argument("r_10");
	
	  @EventSubscriber
	  public void on_r_10_Change(Reading_10_Event event) {
	    r_10.setArgumentValue(event.getValue());
	  }
	
	  @Scheduled(fixedRate = 500000)
	  public void calculateAndSink() {
	    calculateAndSink(Optional.empty());
	  }
	
	  public Formula_11FormulaListener() {
	    deviceId = "device_56789";
	    formulaId = 11;
	    formulaName = "PCP Torke";
	    formulaExpression = new Expression("r_10", r_10);
	    checkExpressionSyntax();
	  }
	}
	
	See ScheduledAnnotationBeanPostProcessor & esp. TaskScheduler (used under the covers by @Scheduled, see above):
	
	https://www.logicbig.com/tutorials/spring-framework/spring-core/task-scheduling.html
	https://spring.io/guides/gs/scheduling-tasks/
	https://www.javarticles.com/2016/05/spring-configuring-taskscheduler-examples.html
	https://www.mkyong.com/spring-batch/spring-batch-and-spring-taskscheduler-example/
	http://mbcoder.com/dynamic-task-scheduling-with-spring/
	https://stackoverflow.com/questions/39152599/interrupt-spring-scheduler-task-before-next-invocation
	https://stackoverflow.com/questions/15250928/how-to-change-springs-scheduled-fixeddelay-at-runtime
	https://stackoverflow.com/questions/15250928/how-to-change-springs-scheduled-fixeddelay-at-runtime/51333059#51333059
	https://stackoverflow.com/questions/14630539/scheduling-a-job-with-spring-programmatically-with-fixedrate-set-dynamically/14632758#14632758
	https://stackoverflow.com/questions/37412246/how-to-set-cron-expression-dynamically-with-database-values-for-different-jobs-i
	https://stackoverflow.com/questions/4499177/creating-spring-framework-task-programmatically
	
	https://stackoverflow.com/questions/28452400/how-to-enable-taskscheduler-in-spring-boot
	https://stackoverflow.com/questions/34436205/injecting-the-taskscheduler-with-spring
	AND: com.iwellsite.core.config.AsynchronousSpringEventsConfig's @Bean TaskScheduler taskExecutor
	
	See also (possibly): ScheduledTaskRegistrar & SchedulingConfigurer

 *
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ScheduledFormula extends Formula
{
	public ScheduledFormula(FormulaDTO dto) 
	{
		super(dto);
	}
}
