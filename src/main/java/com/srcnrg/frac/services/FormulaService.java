package com.srcnrg.frac.services;

import com.srcnrg.frac.domain.InstanceType;
import com.srcnrg.frac.domain.cache.FormulaCache;
import com.srcnrg.frac.domain.cache.MetadataCache;
import com.srcnrg.frac.domain.dto.DataTransferObject;
import com.srcnrg.frac.domain.dto.FormulaDTO;
import com.srcnrg.frac.domain.dto.ManualDatapointDTO;
import com.srcnrg.frac.domain.dto.ReadingDTO;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wrangle Formulas.
 * 
 * TODO: Make these (CREATE/DELETE/UPDATE) service calls TRANSACTIONAL
 */
@Service
@Log4j2
@Data
public class FormulaService 
{
	/**
	 * TODO: Move this to a utility class (it is used by the FormulaService & AgensDAO)
	 * 
	 * @param dto
	 * @return
	 */
	public static boolean formulaDependenciesHaveBeenProvided(FormulaDTO dto) 
	{
		return null != dto.getDependencyIds() && dto.getDependencyIds().length > 0;
	}

	/**
	 * TODO: Move this to a utility class (it is used by the FormulaService & AgensDAO)
	 * 
	 * @param newDTO
	 * @param currentDTO
	 * @return
	 */
	public static boolean formulaDependenciesProvidedAreDifferentFromCurrent(FormulaDTO newDTO, FormulaDTO currentDTO) 
	{
		List<Integer> currentIds = currentDTO
									.getDependencyDTOs().values()
										.stream()
											.map((dto) -> (Integer) dto.getId())
											.sorted()
											.collect(Collectors.toList());
		List<Integer> newIds = Arrays
								.stream(newDTO.getDependencyIds())
									.boxed()
									.sorted()
									.collect(Collectors.toList());
		return !newIds.equals(currentIds);
	}
	
    @Autowired
	FormulaCache formulaCacheManager;

	@Autowired
    private FormulaGraphService formulaGraphService;

	
	@PostConstruct
    private void afterConstruction() throws InterruptedException 
    {
    	formulaGraphService.buildAndCacheFormulaInstances();
    }

	public Mono<FormulaDTO> deleteFormula(FormulaDTO dto) 
	{
		try {
			dto = findCachedFormulaDTOById(dto.getId()).block();
			if (null == dto)
			{
				String msg = "DELETE Formula request made for non-existent Formula.";
				log.warn(msg);
				throw new IllegalArgumentException(msg);
			}
			
			if (formulaCacheManager.isInUse(dto))
			{
				String msg = "DELETE Formula request made for Formula that is in-use.";
				log.warn(msg);
				throw new IllegalArgumentException(msg);
			}
			
			int rowsEffected = formulaGraphService.deleteDTO(dto);
			if (rowsEffected >= 1) // i.e. Formula + # of relationships with dependencies
			{
				MetadataCache.deleteDTO(dto);
				formulaCacheManager.removeExistingInstance(dto);
				return Mono.just(dto);
			} else {
				throw new IllegalStateException("Deletion of the Formula failed.");
			}
    	}
    	catch (Throwable t)
    	{
//    		dto.setName(t.getMessage());
//    		return Mono.just(dto);
    		throw new IllegalStateException("Deletion of the Formula failed due to: " + t.getMessage());
    	}
	}
	
	public Mono<ManualDatapointDTO> deleteManualDatapoint(ManualDatapointDTO dto) 
	{
		// Check the ID, make sure the DTO exists in cache
		dto = findCachedManualDatapointDTOById(dto.getId()).block();
		if (null == dto)
		{
			String msg = "DELETE ManualDatapoint request made for non-existent ManualDatapoint.";
			log.warn(msg);
			throw new IllegalArgumentException(msg);
		}
		
		// Check if this DTO is a dependency of any Formulas or Rules 
		// i.e. a DTO for this type has already been instantiated
		// while building Formulas
		if (formulaCacheManager.isInUse(dto))
		{
			String msg = "DELETE ManualDatapoint request made for ManualDatapoint that is in-use.";
			log.warn(msg);
			throw new IllegalArgumentException(msg);
		}

		try {
			if (1 == formulaGraphService.deleteDTO(dto)) 
			{
				MetadataCache.deleteDTO(dto);
				return Mono.just(dto);
			} else {
				throw new IllegalStateException("Deletion of the ManualDatapoint failed.");
			}
    	}
    	catch (Throwable t)
    	{
    		return Mono.just(dto);
    	}
	}
	
	public Mono<ReadingDTO> deleteReading(ReadingDTO dto) 
	{
		dto = findCachedReadingDTOById(dto.getId()).block();
		
		if (null == dto)
		{
			String msg = "DELETE Reading request made for non-existent Reading.";
			log.warn(msg);
			throw new IllegalArgumentException(msg);
		}
		
		if (formulaCacheManager.isInUse(dto))
		{
			String msg = "DELETE Reading request made for Reading that is in-use.";
			log.warn(msg);
			throw new IllegalArgumentException(msg);
		}

		try {
			if (1 == formulaGraphService.deleteDTO(dto)) 
			{
				MetadataCache.deleteDTO(dto);
				return Mono.just(dto);
			} else {
				throw new IllegalStateException("Deletion of the Reading failed.");
			}
    	}
    	catch (Throwable t)
    	{
    		return Mono.just(dto);
    	}
	}
	
	/**
	 * Used by integration-like JUnits tests
	 * TODO: Remove this.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Flux<DataTransferObject> findAllDTOs() 
	{
		List<DataTransferObject> dtos = new ArrayList<>();
		dtos.addAll((Collection<? extends FormulaDTO>) MetadataCache.getDTOsByType(InstanceType.Formula));
		dtos.addAll((Collection<? extends ReadingDTO>) MetadataCache.getDTOsByType(InstanceType.Reading));
		dtos.addAll((Collection<? extends ManualDatapointDTO>) MetadataCache.getDTOsByType(InstanceType.ManualDatapoint));
		return Flux.fromIterable(dtos);
	}
	
	@SuppressWarnings("unchecked")
	public Flux<FormulaDTO> findAllCachedFormulaDTOs()
	{
		List<FormulaDTO> dtos = new ArrayList<>();
		dtos.addAll((Collection<? extends FormulaDTO>) MetadataCache.getDTOsByType(InstanceType.Formula));
		return Flux.fromIterable(dtos);
	}

	@SuppressWarnings("unchecked")
	public Flux<ManualDatapointDTO> findAllCachedManualDatapointDTOs() 
	{
		List<ManualDatapointDTO> dtos = new ArrayList<>();
		dtos.addAll((Collection<? extends ManualDatapointDTO>) MetadataCache.getDTOsByType(InstanceType.ManualDatapoint));
		return Flux.fromIterable(dtos);
	}
	
	@SuppressWarnings("unchecked")
	public Flux<ReadingDTO> findAllCachedReadingDTOs() 
	{
		List<ReadingDTO> dtos = new ArrayList<ReadingDTO>();
		dtos.addAll((Collection<? extends ReadingDTO>) MetadataCache.getDTOsByType(InstanceType.Reading));
		return Flux.fromIterable(dtos);
	}
	
	public Mono<FormulaDTO> findCachedFormulaDTOById(int id) 
	{
		return Mono.justOrEmpty((FormulaDTO) MetadataCache.getDTOById(id));
	}
	
	public Mono<ManualDatapointDTO> findCachedManualDatapointDTOById(int id) 
	{
		return Mono.justOrEmpty((ManualDatapointDTO) MetadataCache.getDTOById(id));
	}
	
	public Mono<ReadingDTO> findCachedReadingDTOById(int id) 
	{
		return Mono.justOrEmpty((ReadingDTO) MetadataCache.getDTOById(id));
	}
	
	/**
	 * Convenience method.
	 * 
	 * @param dto carrying Formula metadata
	 * @return an indication of whether or not an Expression value is carried by the DTO
	 */
	private boolean formulaExpressionHasBeenProvided(FormulaDTO dto) 
	{
		return null != dto.getExpression();
	}
	
	/**
	 * Convenience method.
	 * 
	 * @param dto carrying Formula metadata
	 * @return an indication of whether or not a Name value is carried by the DTO
	 */
	private boolean formulaNameHasBeenProvided(FormulaDTO dto) 
	{
		return null != dto.getName();
	}
	
	/**
	 * Convenience method.
	 * 
	 * @param dto carrying Formula metadata
	 * @return an indication of whether or not a Period value is carried by the DTO
	 */
	private boolean formulaPeriodHasBeenProvided(FormulaDTO dto)
	{
		return null != dto.getPeriod();
	}
	
	public Mono<FormulaDTO> insertFormula(FormulaDTO dto) 
	{
		if (null == dto.getName())
		{
			throw new IllegalArgumentException("A name was not provided but is REQUIRED.");
		}
		
		if (null == dto.getExpression())
		{
			throw new IllegalArgumentException("An expression for calculating values was not provided but is REQUIRED.");
		}
		
		if (!MetadataCache.dtosExist(dto.getDependencyIds()))
		{
			throw new IllegalArgumentException("Some of the provided Dependency IDs do not exist.");
		}

		if (null == dto.getPeriod())
		{
			dto.setPeriod(0);
		}
		
		final int nextId = formulaGraphService.getNextId();
		
		dto.setId(nextId);
		dto.setTypeId(InstanceType.Formula.getValue());
		dto.setDependencyDTOs(MetadataCache.getDTOsByIds(dto.getDependencyIds()));
		
		if (dto.isReadyForUse())
		{
        	try 
        	{
        		log.info(dto);
            	int formulasInserted = formulaGraphService.insertGraphEntityFromDTO(dto);
            	log.trace("Persisted Formula (" + dto + "), DAO reported that " + formulasInserted + " Formulas were inserted");
            	if (formulasInserted < 1)
            	{
            		throw new IllegalStateException("Persistence of the Formula failed.");
            	}
        	}
        	catch (Throwable t)
        	{
        		return Mono.just(dto);
        	}
				
        	formulaCacheManager.addNewInstance(dto);
            MetadataCache.addDTO(dto);
		}
    	return Mono.just(dto);
	}

	public Mono<ManualDatapointDTO> insertManualDatapoint(String name) 
	{
		final int nextId = formulaGraphService.getNextId();
		return Mono.just(ManualDatapointDTO
							.builder()
								.id(nextId)
								.name(name)
								.typeId(InstanceType.ManualDatapoint.getValue())
							.build())
	            .flatMap(insertableManualDatapointDTO -> {
	            	try 
	            	{
		            	int manualDataPointsInserted = formulaGraphService.insertGraphEntityFromDTO(insertableManualDatapointDTO);
		            	log.trace("Persisted ManualDataPoint (" + insertableManualDatapointDTO + "), DAO reported that " + manualDataPointsInserted + " dependencies were inserted");
		            	if (manualDataPointsInserted < 1)
		            	{
		            		throw new IllegalStateException("Persistence of the ManualDatapoint failed.");
		            	}
	            	}
	            	catch (Throwable t)
	            	{
	            		return Mono.just(insertableManualDatapointDTO);
	            	}
	            	
	            	MetadataCache.addDTO(insertableManualDatapointDTO);
		            return Mono.just(insertableManualDatapointDTO);
	            });
	}
	
	public Mono<ReadingDTO> insertReading(String name) 
	{
		final int nextId = formulaGraphService.getNextId();
		return Mono.just(ReadingDTO
							.builder()
								.id(nextId)
								.name(name)
								.typeId(InstanceType.Reading.getValue())
							.build())
	            .flatMap(insertableReadingDTO -> {
	            	try {
		            	int readingsInserted = formulaGraphService.insertGraphEntityFromDTO(insertableReadingDTO);
		            	log.trace("Persisted Reading (" + insertableReadingDTO + "), DAO reported that " + readingsInserted + " dependencies were inserted");
		            	if (readingsInserted < 1)
		            	{
		            		throw new IllegalStateException("Persistence of the Reading failed.");
		            	}
	            	}
	            	catch (Throwable t)
	            	{
	            		return Mono.just(insertableReadingDTO);
	            	}
	            	
	            	MetadataCache.addDTO(insertableReadingDTO);
		            return Mono.just(insertableReadingDTO);
	            });
	}

	/**
	 * Recreate an existing Formula with new values.
	 * 
	 * @param id
	 * @param newDTO
	 * @return
	 */
	public Mono<FormulaDTO> updateFormula(int id, FormulaDTO newDTO) 
	{
		newDTO.setId(id);
		newDTO.setTypeId(InstanceType.Formula.getValue());
		
		/*
		 *  The following code overwrites a clone of the currently cached DTO for this Formula with the incoming values,
		 *  (the incoming values may only be a subset of all DTO attributes);
		 *  the subsequent DB update will compare this cloned DTO to the currently cached one
		 *  to formulate the correct Cypher UPDATE statement(s).
		 */
		FormulaDTO dtoWithMergedUpdates = ((FormulaDTO) MetadataCache.getDTOById(id)).clone();
		
		if (formulaNameHasBeenProvided(newDTO) && FormulaDTO.nameIsChanging(newDTO, dtoWithMergedUpdates))
		{
			dtoWithMergedUpdates.setName(newDTO.getName());
		}
		
		if (formulaPeriodHasBeenProvided(newDTO) && FormulaDTO.periodIsChanging(newDTO, dtoWithMergedUpdates))
		{
			dtoWithMergedUpdates.setPeriod(newDTO.getPeriod());
		}
		
		if (formulaExpressionHasBeenProvided(newDTO) && FormulaDTO.expressionIsChanging(newDTO, dtoWithMergedUpdates))
		{
			dtoWithMergedUpdates.setExpression(newDTO.getExpression());
		}
		
		if (formulaDependenciesHaveBeenProvided(newDTO))
		{
			if (formulaDependenciesProvidedAreDifferentFromCurrent(newDTO, dtoWithMergedUpdates))
			{
				dtoWithMergedUpdates.setDependencyDTOs(
									FormulaDTO.getClonedDependencyDTOs(
										MetadataCache.getDTOsByIds(
											newDTO.getDependencyIds())));
			}
		}
		

    	try 
    	{
    		log.info(dtoWithMergedUpdates);
    		// DB update will compare the DTO passed here to the currently cached one:
    		formulaGraphService.updateGraphEntityFromDTO(dtoWithMergedUpdates);
    		
			// ONLY update the (metadata) cache AFTER the DAO has used the old copy from the cache for comparison/Cypher derivation:
	    	formulaCacheManager.updateExistingInstance(dtoWithMergedUpdates);
			MetadataCache.addDTO(dtoWithMergedUpdates);
		}
		catch (Throwable t)
		{
			return Mono.empty();
		}
		
		return Mono.just(dtoWithMergedUpdates);
	}
	
	public Mono<ManualDatapointDTO> updateManualDatapoint(int id, String name) 
	{
		return findCachedManualDatapointDTOById(Integer.valueOf(id))
				.map(dto -> { // i.e. work with cached value
					try 
			    	{
						ManualDatapointDTO clonedDTO = dto.clone();
						clonedDTO.setName(name);
						formulaGraphService.updateGraphEntityFromDTO(clonedDTO);
					}
					catch (Throwable t)
					{
						// TODO: REVIEW: Need a better indication of failure-to-update here:
						return null;
					}
					dto.setName(name); // i.e. update cached value	
					// TODO: REVIEW: Following is NOT required since the lambda is already working with the cached value?			
					//MetadataCache.addDTO(dto);
					return dto;
				});
	}

	public Mono<ManualDatapointDTO> updateManualDatapointValue(ManualDatapointDTO newValueCarrier) 
	{
		log.warn(newValueCarrier);

	    return Mono.fromSupplier(
	        () -> {
	        	DataTransferObject cachedDTO = null;
				try {
					cachedDTO = MetadataCache.getDTOById(newValueCarrier.getId());
					cachedDTO.setValue(newValueCarrier.getValue());
					FormulaCache.updateDependencyValue(cachedDTO.getId(), cachedDTO.getValue());
				} catch (Exception e) {
					log.warn(e.getMessage());
				};
	        	return (ManualDatapointDTO) cachedDTO;
	        });
	}

	/**
	 * Accept a new Name property value for the Reading specified by the given ID;
	 * persist the Name change to the JVM cache and the DB.
	 * 
	 * @param id of a Reading already in the DB and cache
	 * @param name the new Name of the Reading to be distributed to the DB and cache
	 * @return
	 */
	public Mono<ReadingDTO> updateReading(int id, String name) 
	{
		return findCachedReadingDTOById(Integer.valueOf(id))
				.map(dto -> { // i.e. work with cached value

					try 
			    	{
						ReadingDTO clonedDTO = dto.clone();
						clonedDTO.setName(name);
						formulaGraphService.updateGraphEntityFromDTO(clonedDTO);
					}
					catch (Throwable t)
					{
						// TODO: REVIEW: Need a better indication of failure-to-update here:
						return null;
					}
					dto.setName(name); // i.e. update cached value
					return dto;
				});
	}

	/**
	 * Entry point (via REST) for accepting changes to a Reading's current value, used by Formulas in their calculations.
	 * 
	 * @param dto
	 * @return
	 */
	public Mono<ReadingDTO> updateReadingValue(ReadingDTO newValueCarrier) 
	{
		log.warn(newValueCarrier);

	    return Mono.fromSupplier(
	        () -> {
	        	DataTransferObject cachedDTO = null;
				try {
					cachedDTO = MetadataCache.getDTOById(newValueCarrier.getId());
					cachedDTO.setValue(newValueCarrier.getValue());
					FormulaCache.updateDependencyValue(cachedDTO.getId(), cachedDTO.getValue());
				} catch (Exception e) {
					log.warn(e.getMessage());
				};
	        	return (ReadingDTO) cachedDTO;
	        });
	}
}
