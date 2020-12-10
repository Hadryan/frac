package com.srcnrg.frac.domain.cache;

import com.srcnrg.frac.domain.Formula;
import com.srcnrg.frac.domain.InstanceType;
import com.srcnrg.frac.domain.dto.DataTransferObject;
import com.srcnrg.frac.domain.dto.FormulaDTO;
import com.srcnrg.frac.domain.dto.ManualDatapointDTO;
import com.srcnrg.frac.domain.dto.ReadingDTO;
import lombok.extern.log4j.Log4j2;
import org.mariuszgromada.math.mxparser.Argument;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages cached DataTransferObjects.
 * 
 * TODO: Make these (CREATE/DELETE/UPDATE) calls TRANSACTIONAL
 * 
 */
@Log4j2
public class MetadataCache 
{
	private final static ConcurrentHashMap<Integer, DataTransferObject> DTOS_BY_ID = new ConcurrentHashMap<>();

	/**
	 * Add a DataTransferObject to the cache
	 * 
	 * @param dto the DataTransferObject to cache
	 */
	public static void addDTO(DataTransferObject dto) 
	{
		if (null != dto) 
		{
			DTOS_BY_ID.put(dto.getId(), dto);
		}
		log.trace("addMetadataDTO(" + dto.toString() + ")");
	}
	
	/**
	 * Remove all DataTransferObjects from the cache
	 * 
	 * @return a count of the DataTransferObjects removed from the cache
	 */
	public static int deleteAllDTOs()
	{
		int returnValue = DTOS_BY_ID.size();
		DTOS_BY_ID.clear();
		return returnValue;
	}
	
	/**
	 * Remove a DataTransferObject from the cache
	 * 
	 * @param dto the DataTransferObject to remove from the cache
	 */
	public static void deleteDTO(DataTransferObject dto) 
	{
		DTOS_BY_ID.remove(dto.getId());
	}

	/**
	 * Look for a DataTransferObject in the cache
	 * 
	 * @param immutableName an identifier for DataTransferObjects in the cache
	 * @return an indication of whether or not a DataTransferObject identified by the given name is cached
	 */
	public static boolean dtoExists(String immutableName)
	{
		return DTOS_BY_ID.containsKey(immutableName);
	}

	/**
	 * Look for a collection of DataTransferObjects in the cache
	 * 
	 * @param dependencyIds a collection of identifiers for DataTransferObjects in the cache
	 * @return an indication of whether or not all DataTransferObjects identified by the given IDs are cached
	 */
	public static boolean dtosExist(int[] dependencyIds) 
	{
		// TODO: REVIEW: Code could surely be clearer/more concise.
		boolean allDTOsDoExist = false;
		OUTER:
		for (int i = 0; i < dependencyIds.length; i++) 
		{
			int id = dependencyIds[i];
			boolean foundDTOWithThisId = false;
			INNER:
			for (DataTransferObject dto: DTOS_BY_ID.values())
			{
				if (!foundDTOWithThisId)
				{
					foundDTOWithThisId |= (id == dto.getId());
				}
				else
				{
					allDTOsDoExist = true;
					continue OUTER;
				}
			}
			if (foundDTOWithThisId)
			{
				allDTOsDoExist = true;
				continue OUTER;
			}
			else
			{
				return false;
			}
		}
		// TODO: REVIEW: Surely a more FP way to do this; explore: 
		// METADATA_DTOS.reduceValues(parallelismThreshold, reducer)
		return allDTOsDoExist;
	}

	/**
	 * Retrieve a collection of DataTransferObjects
	 * 
	 * @param dependencyIds a collection of identifiers
	 * @return a collection of DataTransferObjects
	 */
	public static Map<Integer, DataTransferObject> getDTOsByIds(int[] dependencyIds) 
	{
		Map<Integer, DataTransferObject> returnValue = new HashMap<>();
		if (dtosExist(dependencyIds))
		{
			for (int i = 0; i < dependencyIds.length; i++) 
			{
				DataTransferObject dto = getDTOById(dependencyIds[i]);
				returnValue.put(dto.getId(), dto);
			}
		}
		return returnValue;
	}

//	/**
//	 * Retrieve a DataTransferObject by the identifying name
//	 * 
//	 * @param immutableName
//	 * @return
//	 */
//	public static DataTransferObject getDTOByImmutableName(String immutableName) 
//	{
//		if (dtoExists(immutableName)) 
//		{
//			return DTOS_BY_ID.get(immutableName);
//		}
//		return null;
//	}
	
//	/**
//	 * Retrieve a DataTransferObject identified by type and ID
//	 * 
//	 * @param type the InstanceType
//	 * @param id the ID
//	 * @return a cDataTransferObject
//	 */
//	public static DataTransferObject getDTOByTypeAndId(InstanceType type, int id) 
//	{
//		return DTOS_BY_ID.get(type.name() + "_" + id);
//	}

	/**
	 * Retrieve a DataTransferObject by ID
	 * 
	 * @param id the ID of the DataTransferObject
	 * @return a cached DataTransferObject
	 */
	public static DataTransferObject getDTOById(int id) 
	{
		// TODO: Do this with stream()
		for (DataTransferObject dto: DTOS_BY_ID.values())
		{
			if (id == dto.getId())
			{
				return dto;
			}
		}
		return null;
	}

	/**
	 * Retrieve a collection of DataTransferObjects by type
	 * 
	 * @param type the InstanceType of the collected DataTransferObjects
	 * @return a collection of DataTransferObjects, pre-cast to the desired InstanceType
	 */
	public static List<? extends DataTransferObject> getDTOsByType(InstanceType type) 
	{
		switch (type) {
			case Formula:
				return DTOS_BY_ID
						.entrySet()
							.stream()
							.filter((entry) -> entry.getValue().getTypeId() == InstanceType.Formula.getValue())
							.map((entry) -> (FormulaDTO) entry.getValue())
							.sorted(Comparator.comparingInt(FormulaDTO::getId))
							.collect(Collectors.toList());

			case Reading:
				return DTOS_BY_ID
						.entrySet()
							.stream()
							.filter((entry) -> entry.getValue().getTypeId() == InstanceType.Reading.getValue())
							.map((entry) -> (ReadingDTO) entry.getValue())
							.sorted(Comparator.comparingInt(ReadingDTO::getId))
							.collect(Collectors.toList());

			case ManualDatapoint:
				return DTOS_BY_ID
						.entrySet()
							.stream()
							.filter((entry) -> entry.getValue().getTypeId() == InstanceType.ManualDatapoint.getValue())
							.map((entry) -> (ManualDatapointDTO) entry.getValue())
							.sorted(Comparator.comparingInt(ManualDatapointDTO::getId))
							.collect(Collectors.toList());
				
			default:
				return null;
		}
	}

	/*
	 * The following 2 methods allow for the exposure of current values of DTOs via the REST interface, 
	 * handy when troubleshooting - they are invoked ONLY when trace-level logging is enabled.
	 * 
	 * TODO: REVIEW: 
	 * 	They ARE also used to update Argument values in the cached DTOs, after destroying/recreating/updating ScheduledFormulas.
	 * 		This  keeps ScheduledFormulas in a ready-to-use state but would introduce phantom-like update events to the lifecycle of Formulas.
	 * 	Or, these 2 methods could be completely eliminated, as they support a completely unasked-for feature.
	 */
	public static void updateDTOValue(Argument arg, double value) 
	{
		getDTOById(Integer.valueOf(arg.getArgumentName().split("_")[1])).setValue(value);
	}
	public static void updateFormulaDTOValue(Formula f, double value) 
	{
		getDTOById(f.getId()).setValue(value);
	}
}
