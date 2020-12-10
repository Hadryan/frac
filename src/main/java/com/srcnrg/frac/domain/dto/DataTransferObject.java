package com.srcnrg.frac.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Data carrier used when instantiating a Formula, persisting metadata to the datastore 
 * and exposing metadata regarding:
 * 
 * 	- (JVM-resident) Readings, ManualDatapoints or Formulas,
 * 	- (DB-resident) Readings, ManualDatapoints or Formulas
 */
@Data
@AllArgsConstructor
// @NoArgsConstructor is used when testing /all REST endpoint
@NoArgsConstructor
@ToString(includeFieldNames=true)
public class DataTransferObject implements Serializable
{
	private static final long serialVersionUID = 4544904069379636787L;
	public final static String FAKE_DEVICE_ID = "device_56789";
	public static final String TYPE_ID_PROPERTY_NAME = "typeid";
	
	// NOTE: The ORDER of these fields effects/matches the argument ordering in subclasses' constructor calls to super()!
	private int id;

	private String name;

    @JsonIgnore
	private String deviceId = FAKE_DEVICE_ID;
	
	protected int typeId;

	private double value;
	
	@JsonIgnore
	public String getRawArgumentNameForExpression()
	{
		return "$" + id + "$";
	}
}
