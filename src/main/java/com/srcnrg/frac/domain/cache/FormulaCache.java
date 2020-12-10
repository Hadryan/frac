package com.srcnrg.frac.domain.cache;

import com.srcnrg.frac.domain.Formula;
import com.srcnrg.frac.domain.ScheduledFormula;
import com.srcnrg.frac.domain.dto.DataTransferObject;
import com.srcnrg.frac.domain.dto.FormulaDTO;
import lombok.extern.log4j.Log4j2;
import org.mariuszgromada.math.mxparser.Argument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledFuture;

/**
 * Create, collect, distribute and destroy Formulas as required
 * 
 * TODO: Make these (CREATE/DELETE/UPDATE) calls TRANSACTIONAL
 */
@Service
@Log4j2
public class FormulaCache 
{
	@Autowired private TaskScheduler taskScheduler;
	
	private final static ConcurrentSkipListMap<Integer, Formula> FORMULAS_BY_ID = new ConcurrentSkipListMap<>();
	@SuppressWarnings("rawtypes")
	private final static ConcurrentSkipListMap<Integer, ScheduledFuture> SCHEDULED_FUTURES_BY_FORMULA_ID = new ConcurrentSkipListMap<>();
	private final static ConcurrentSkipListMap<Integer, ConcurrentSkipListSet<Formula>> FORMULAS_BY_DEPENDENCY_ID = new ConcurrentSkipListMap<>() /* see: https://stackoverflow.com/a/6720658/888537 */;
	
	/**
	 * Broadcast (to other Formulas that have this Formula as a dependency) the new calculated value for this Formula.
	 * 
	 * @param f the Formula that has calculated a new value
	 * @param value the new, calculated value
	 */
	public static void broadcastCalculatedValue(int id, double value)
    {
		// TODO: REVIEW: Consider NOT broadcasting a new Formula-calculated value when the value is: NaN?
		if (!Double.isNaN(value))
		{
			updateDependencyValue(id, value);
		}
    }
	
	/**
	 * Update the value of a dependency in all the Formulas
	 * 
	 * @param id identifier for the dependency
	 * @param newValue new value for the dependency
	 */
	public static void updateDependencyValue(int id, double newValue) 
	{
    	if (FORMULAS_BY_DEPENDENCY_ID.containsKey(id))
    	{
    		Flux.fromIterable(FORMULAS_BY_DEPENDENCY_ID.get(id))
    			// TODO: REVIEW: Choose a different Scheduler?
    			.subscribeOn(Schedulers.parallel())
    			.subscribe(formula -> formula.updateExpressionArgumentValue(id, newValue))
    		;
    	}
	}

	/**
	 * Create a new Formula from the supplied metadata.
	 * 
	 * Called directly & indirectly from FormulaService, 
	 * both after loading metadata from storage
	 * and when creating a new Formula.
	 * 
	 * @param dto a carrier of metadata used when creating a Formula
	 */
	public void addNewInstance(FormulaDTO dto) 
	{
		Formula f = null;
		
		if (dto.getPeriod() <= 0)
		{
			f = new Formula(dto);
		}
		else
		{
			f = new ScheduledFormula(dto);
			scheduleFormulaAsRequired(dto, f);
		}
		preLoadFormulaDependencyValues(f);
		cacheInstance(f, dto);
	}
	
	/**
	 * Add the supplied Formula to 2 collections:
	 * 	1. A Map where the Formula can be found by ID
	 * 	2. Some number of Lists held by a Map keyed on Formula dependencies
	 * 
	 * @param f a Formula to cache in some collections
	 * @param dto a carrier of metadata for the Formula
	 */
	private void cacheInstance(Formula f, FormulaDTO dto) 
	{
		FORMULAS_BY_ID.put(dto.getId(), f);
		log.trace("Added Formula (#" + f.getId() + ") to cache.");
		
		for (Integer dependencyId: dto.getDependencyIds())
		{
			ConcurrentSkipListSet<Formula> formulasWithThisDependency = null;
			
			if (!FORMULAS_BY_DEPENDENCY_ID.containsKey(dependencyId))
			{
				formulasWithThisDependency = new ConcurrentSkipListSet<Formula>();
				FORMULAS_BY_DEPENDENCY_ID.put(dependencyId, formulasWithThisDependency);
			}
			else
			{
				formulasWithThisDependency = FORMULAS_BY_DEPENDENCY_ID.get(dependencyId);
			}
			formulasWithThisDependency.add(f);
		}
	}
	
	/**
	 * Populate the data structures. 
	 * 	- ASSUME that no concurrent calls will be made:
	 * 		i.e. this method will ONLY be called once, after initial load of the metadata from the DB
	 * 
	 * @param formulasMetadata a Collection of metadata for Formulas
	 */
	public void cacheNewInstances(List<FormulaDTO> formulasMetadata) 
	{
		for (FormulaDTO dto: formulasMetadata)
		{
			addNewInstance(dto);
		}
	}

	/**
	 * @param dto a carrier of metadata for the Formula
	 * @return an indication of whether or not the given type (i.e. Formula, ManualDatapoint or Reading) is a dependency of an existing Formula
	 */
	public boolean isInUse(DataTransferObject dto) 
	{
		return FORMULAS_BY_DEPENDENCY_ID.containsKey(dto.getId());
	}

	/**
	 * Remove the given Formula
	 * 
	 * @param f the Formula to be removed from this cache's collections
	 */
	private void removeExistingInstance(Formula f) 
	{
		unscheduleExistingFormulaAsRequired(f);
		removeFromCache(f);
	}
	
	public void removeExistingInstance(FormulaDTO dto) 
	{
		removeExistingInstance(FORMULAS_BY_ID.get(dto.getId())) ;
	}

	public void removeFromCache(Formula existingFormula) 
	{
		// Remove an existing Formula from the Dependencies' Set:
		for (Argument arg: existingFormula.getExpressionArguments())
		{
			// The Argument 'description' field is overloaded, to hold the ID of the related dependency 
			Integer dependencyId = Integer.valueOf(arg.getDescription());
			if (FORMULAS_BY_DEPENDENCY_ID.containsKey(dependencyId))
			{
				ConcurrentSkipListSet<Formula> formulasWithThisDependency = FORMULAS_BY_DEPENDENCY_ID.get(dependencyId);
				formulasWithThisDependency.remove(existingFormula);
				
				if (formulasWithThisDependency.isEmpty())
				{
					FORMULAS_BY_DEPENDENCY_ID.remove(dependencyId, formulasWithThisDependency);
				}
			}
		}

		FORMULAS_BY_ID.remove(existingFormula.getId());
		log.trace("Removed Formula (#" + existingFormula.getId() + ") from cache.");
	}

	private void scheduleFormulaAsRequired(FormulaDTO dto, final Formula newFormula) 
	{
		if (newFormula instanceof ScheduledFormula)
		{	
		    Runnable task = new Runnable() {
		       public void run() {
		    	   newFormula.calculate();
		       }
		    };
		    ScheduledFuture<?> future = this.taskScheduler.scheduleAtFixedRate(task, dto.getPeriod());
		    SCHEDULED_FUTURES_BY_FORMULA_ID.put(dto.getId(), future);
		}
	}
	
	/**
	 * Change the Argument values from the default NaN value to mirror the current dependency values that are already cached.
	 * 
	 * @param newFormula
	 */
	private void preLoadFormulaDependencyValues(Formula f) 
	{
		Flux.fromIterable(f.getExpressionArguments())
			// TODO: REVIEW: Choose a different Scheduler?
			.subscribeOn(Schedulers.parallel())
			.subscribe(arg -> {
				// The Argument 'description' field is overloaded, to hold the ID of the related dependency 
				int id = Integer.valueOf(arg.getDescription());
				DataTransferObject dtoForArgument = MetadataCache.getDTOById(id);
				double newArgValue = dtoForArgument.getValue();
				if (Double.NaN != newArgValue)
				{
					f.updateExpressionArgumentValue(id, dtoForArgument.getValue());
				}
			})
		;
	}

	private void unscheduleExistingFormulaAsRequired(Formula existingFormula) 
	{
		if (existingFormula instanceof ScheduledFormula)
		{
			// Stop existing ScheduledFormula
			ScheduledFuture<?> taskFuture = SCHEDULED_FUTURES_BY_FORMULA_ID.get(existingFormula.getId());
			if (null != taskFuture)
			{
				boolean shouldInterruptTask = true;
				taskFuture.cancel(shouldInterruptTask);
				while (!taskFuture.isCancelled())
				{
					// wait for scheduled task to complete...
				}
				SCHEDULED_FUTURES_BY_FORMULA_ID.remove(existingFormula.getId());
			}
		}
	}


	public void updateExistingInstance(FormulaDTO dto) 
	{
		if (null != dto)
		{
			Formula existingFormula = FORMULAS_BY_ID.get(dto.getId());
			final Formula newFormula = ((null == dto.getPeriod()) || (dto.getPeriod() <= 0)) ? new Formula(dto) : new ScheduledFormula(dto);
			
			removeExistingInstance(existingFormula);
			scheduleFormulaAsRequired(dto, newFormula);
			preLoadFormulaDependencyValues(newFormula);
			cacheInstance(newFormula, dto);
		
			existingFormula = null;
			System.gc();
		}
	}
}
