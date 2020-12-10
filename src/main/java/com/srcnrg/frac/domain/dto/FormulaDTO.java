package com.srcnrg.frac.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.srcnrg.frac.domain.Formula;
import com.srcnrg.frac.domain.InstanceType;
import lombok.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DTO for Formula metadata, held in the JVM and persistent storage.
 * 
 * Used when:
 * 	- constructing (Formula) runtime instances in the JVM that respond to updates to dependencies' values and
 * 	- transferring metadata to/from storage and REST endpoints
 */
@Data
@EqualsAndHashCode(callSuper=false)
@ToString(includeFieldNames=true,callSuper=true)
public class FormulaDTO extends DataTransferObject
{
	private static final long serialVersionUID = -962360511357838532L;

	/**
	 * Convenience method used by the FormulaService & AgensDAO
	 * 
	 * @param newDTO
	 * @param currentDTO
	 * @return boolean indication of whether or not there is a difference between the provided Period attributes of the DTOs.
	 */
	public static boolean nameIsChanging(FormulaDTO newDTO, FormulaDTO currentDTO) 
	{
		return null != newDTO.getName() && null != newDTO.getName() && !newDTO.getName().equals(currentDTO.getName());
	}

	/**
	 * Convenience method used by the FormulaService & AgensDAO
	 * 
	 * @param newDTO
	 * @param currentDTO
	 * @return boolean indication of whether or not there is a difference between the provided Period attributes of the DTOs.
	 */
	public static boolean expressionIsChanging(FormulaDTO newDTO, FormulaDTO currentDTO) 
	{
		return null != newDTO.getExpression() && null != newDTO.getExpression() && !newDTO.getExpression().equals(currentDTO.getExpression());
	}

	/**
	 * Convenience method used by the FormulaService & AgensDAO
	 * 
	 * @param newDTO
	 * @param currentDTO
	 * @return boolean indication of whether or not there is a difference between the provided Period attributes of the DTOs.
	 */
	public static boolean periodIsChanging(FormulaDTO newDTO, FormulaDTO currentDTO) 
	{
		return null != newDTO.getPeriod() && null != newDTO.getPeriod() && !newDTO.getPeriod().equals(currentDTO.getPeriod());
	}

	/**
	 * Clone a collection of dependencies for use when comparing during Formula updates.
	 * 
	 * @param dependencies a currently-used collection of dependencies
	 * @return a cloned collection of dependencies
	 */
	public static Map<Integer, DataTransferObject> getClonedDependencyDTOs(Map<Integer, DataTransferObject> dependencies)
	{
		Map<Integer, DataTransferObject> returnValue = new HashMap<>();

		for(Integer idAsKey: dependencies.keySet())
		{
			DataTransferObject dependency = dependencies.get(idAsKey);
			
			switch(InstanceType.fromInt(dependency.getTypeId()))
			{
				case Reading:
					returnValue.put(idAsKey, ((ReadingDTO) dependency).clone());
					break;
					
				case ManualDatapoint:
					returnValue.put(idAsKey, ((ManualDatapointDTO) dependency).clone());
					break;
	
				case Formula:
					returnValue.put(idAsKey, ((FormulaDTO) dependency).clone());
					break;
					
				default:
						break;
			}
		}
		
		return returnValue;
	}
	
	@Builder
	public FormulaDTO(int id, String name, int typeId, String expression, Integer period, Map <Integer, DataTransferObject> dependencyDTOs, int[] dependencyIds, double value) 
	{
		super(id, name, FAKE_DEVICE_ID, typeId, value);
		this.expression = expression;
		this.period = period;
		this.dependencyDTOs = dependencyDTOs;
		this.typeId = InstanceType.Formula.getValue();
		
		if (null != this.dependencyDTOs)
		{
			setDependencyIdsFromDependencyDTOs();
		}
		else
		{
			this.dependencyIds = dependencyIds;
		}
	}

	// TODO: Refactor 'expression' & 'dependencyIds' args into ONE object (being mindful of legible DTO-as-JSON requests/responses)
	private String expression;
	
	private Integer period;

	// This field is used/required for ease of PUT/POSTing JSON e.g. "dependencyIds":[1,2,3]
	// @JsonIgnore can inhibit both serialization & deserialization, see: https://fasterxml.github.io/jackson-annotations/javadoc/2.6/com/fasterxml/jackson/annotation/JsonProperty.Access.html
	private int[] dependencyIds;

	@JsonIgnore
	@Getter
	private Map<Integer, DataTransferObject> dependencyDTOs;
	public void setDependencyDTOs(Map<Integer, DataTransferObject> newDependencies)
	{
		this.dependencyDTOs = newDependencies;
		setDependencyIdsFromDependencyDTOs();
	}

	@JsonIgnore
	public Collection<DataTransferObject> getDependencies()
	{
		return dependencyDTOs.values();
	}

	/**
	 * Build a String that is compilable by the mxParser Expression constructor, used in the Formula constructor
	 * 
	 * @return a compilable String
	 */
	@JsonIgnore
	public String getCompilableExpression()
	{
		String compilableExpression = new String(expression);
		for (DataTransferObject dto: dependencyDTOs.values())
		{
			int id = dto.getId();
			compilableExpression = compilableExpression.replaceAll("\\$" + id + "\\$", Formula.getArgumentName(dto));
		}
		return compilableExpression;
	}

	/**
	 * Create a deep copy of this object, ignoring the 'value' field
	 */
	public FormulaDTO clone() 
	{
		return FormulaDTO
				.builder()
					.id(this.getId())
					.expression(this.getExpression())
					.name(this.getName())
					.period((null != this.getPeriod()) ? this.getPeriod() : 0)
					.typeId(this.getTypeId())
					.dependencyDTOs(getClonedDependencyDTOs(this.getDependencyDTOs()))
				.build();
	}
	
	/**
	 * @return
	 */
	public boolean hasFormulaTypeDependencies()
	{
		for (DataTransferObject dto: dependencyDTOs.values()) 
		{
			if (dto instanceof FormulaDTO) { return true; }
		}
		return false;
	}
	
	/**
	 * @return
	 */
	@JsonIgnore
	public boolean isReadyForUse() 
	{
		return isExpressionReadyForEvaluation(); 
	}
	
	private boolean isExpressionReadyForEvaluation() 
	{
		return null != expression && null != dependencyDTOs; 
	}
	
	private void setDependencyIdsFromDependencyDTOs()
	{
		if (null == dependencyDTOs || dependencyDTOs.isEmpty())
		{
			this.dependencyIds = new int[0];
		}
		else
		{
			List<Integer> ids = dependencyDTOs.values().stream()
												.map((dto) -> (Integer) dto.getId())
												.sorted()
												.collect(Collectors.toList());
			this.dependencyIds = new int[ids.size()];
			int index = 0;
			for (Integer id: ids)
			{
				this.dependencyIds[index++] = id;
			}
		}
	}
}
