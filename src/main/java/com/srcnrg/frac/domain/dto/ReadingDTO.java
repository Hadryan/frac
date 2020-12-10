package com.srcnrg.frac.domain.dto;

import com.srcnrg.frac.domain.InstanceType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * DTO for Reading metadata held in the JVM and persistent storage.
 * 
 * Used when transferring metadata to/from storage and REST endpoints.
 */
@Data
@EqualsAndHashCode(callSuper=false)
@ToString(includeFieldNames=true,callSuper=true)
public class ReadingDTO extends DataTransferObject 
{
	private static final long serialVersionUID = 5917648438805387551L;

	protected int typeId = InstanceType.Reading.getValue();
	
	@Builder
	public ReadingDTO(int id, String name, int typeId, double value) 
	{
		super(id, name, FAKE_DEVICE_ID, typeId, value);
	}

	/**
	 * Create a deep copy of this object, ignoring the 'value' field; for use when comparing during Formula updates.
	 */
	public ReadingDTO clone() 
	{
		return ReadingDTO
				.builder()
					.id(this.getId())
					.name(this.getName())
					.typeId(this.getTypeId())
				.build();
	}
}
