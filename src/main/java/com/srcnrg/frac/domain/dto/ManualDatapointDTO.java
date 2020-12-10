package com.srcnrg.frac.domain.dto;

import com.srcnrg.frac.domain.InstanceType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * DTO for ManualDatapoint metadata held in the JVM and persistent storage.
 * 
 * Used when transferring metadata to/from storage and REST endpoints.
 */
@Data
@EqualsAndHashCode(callSuper=false)
@ToString(includeFieldNames=true,callSuper=true)
public class ManualDatapointDTO extends DataTransferObject 
{
	private static final long serialVersionUID = -7819637829300763773L;

	protected int typeId = InstanceType.ManualDatapoint.getValue();
			
	@Builder
	public ManualDatapointDTO(int id, String name, int typeId, double value) 
	{
		super(id, name, FAKE_DEVICE_ID, typeId, value);
	}

	/**
	 * Create a deep copy of this object, ignoring the 'value' field; for use when comparing during Formula updates.
	 */
	public ManualDatapointDTO clone() 
	{
		return ManualDatapointDTO
				.builder()
					.id(this.getId())
					.name(this.getName())
					.typeId(this.getTypeId())
				.build();
	}
}
