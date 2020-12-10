package com.srcnrg.frac.domain;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum InstanceType
{
	// Scalar types:
	ManualDatapoint(1),
	Reading(2),
	// MultiDimensional types:
	Formula(20);
	
	@Getter
	private int value;
	
	InstanceType(int value) { this.value = value; }
	
	public static final String TYPE_ID_PROPERTY_NAME = "typeid";

	private static final Map<Integer, InstanceType> INTEGER_TO_INSTANCE_TYPE = new HashMap<Integer, InstanceType>();
	
	static {
		for (InstanceType type : InstanceType.values()) 
		{
			INTEGER_TO_INSTANCE_TYPE.put(type.getValue(), type);
		}
	}
	
	/**
	 * Convert int to InstanceType
	 * 
	 * @param value the int key into the InstanceType Map
	 * @return InstanceType corresponding to value, or null if value is an unknown
	 */
	public static InstanceType fromInt(int value) 
	{
		return INTEGER_TO_INSTANCE_TYPE.get(value);
	}
}