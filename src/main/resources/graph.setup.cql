//  Get all Vertices:
// 	MATCH (n) return n;
MATCH path = (:Formula)-[:Depends_On]->() return path;

// Delete everything in the graph:
MATCH (n) DETACH DELETE n;

// NODE/VERTICE NAMING RULE:
//	Use uppercased words BUT if the first word is an all-caps abbreviation then LOWERCASE the abbreviation.
//
// Readings, ManaualDatapoints, Formulas & Rules 'ImmutableName' property:
//	Generally, use 'Name' for the human-identifiable Node property name - this ALSO appears, by default, in AgensBrowser UI.
//	- use 'ImmutableName' for the Node property name that is used when generating code
//			- the value of ImmutableName SHOULD be a valid Java identifier
//				but the current code(builders) removes intervening whitespace and handles the (un)capitalization

// Formulas, Readings and Manual Datapoints:

// 	Created below:
//
//		From Confluence 'System Formulae' document:
//
//			FORMULA: Total Net Lift
//			FORMULA: Theoretical PCP Lift Torque
//			FORMULA: PCP Torque
//			FORMULA: Averaged PCP Torque
//			FORMULA: Total Friction Torque
//			FORMULA: Calculated Fluid Level

//			READING: Tubing Pressure
//			READING: Casing Pressure
//			READING: Electrical Torque
//
//			MANUAL_DATAPOINT: Specific Gravity
//			MANUAL_DATAPOINT: Fluid Shot
//			MANUAL_DATAPOINT: Flowing Friction Losses
//			MANUAL_DATAPOINT: pcp Displacement
//			MANUAL_DATAPOINT: TopDrive Gear Ratio
//			MANUAL_DATAPOINT: pcp Torque Sum
//			MANUAL_DATAPOINT: pcp Torque Count
//
//		From Saurabh's homework project:
//
//			FORMULA: Drive Shutdown Torque
//			FORMULA: Drive Slowdown Torque
//			FORMULA: Motor FullLoad Torque
//
//			MANUAL_DATAPOINT: Motor ServiceFactor
//			MANUAL_DATAPOINT: Drive Shutdown Safety Factor
//			MANUAL_DATAPOINT: Drive GearRatio
//			MANUAL_DATAPOINT: Drive Slowdown Safety Factor
//			MANUAL_DATAPOINT: Drive Motor HorsePower
//			MANUAL_DATAPOINT: Motor RatedSpeed
//
//
// TODO: Persist UnitTypeIDs:example unit for domain:
//	0: none
//	1: specific
//	2: m
//	3: psi
//	4: mCubed/day/100rpm
//	5: lbs-ft
//	6: hp
//	7: rpm
//	8: temperature e.g. C/F/K
//	9: percentage
	

// Constraints on Formulas:
//	NOTE: AgensGraph allows ONLY ONE constraint PER Vertex/Edge Property:
//FAIL w/SQLException:==> CREATE  CONSTRAINT Formula_Name_exists_and_is_unique ON Formula ASSERT ((name IS NOT NULL) AND (name IS UNIQUE));
//DROP    CONSTRAINT Formula_Name_unique ON Formula;
//CREATE  CONSTRAINT Formula_Name_unique ON Formula ASSERT name IS UNIQUE;

//DROP    CONSTRAINT Formula_ID_unique ON Formula;
//CREATE  CONSTRAINT Formula_ID_unique ON Formula ASSERT Id IS UNIQUE;

// Constraints on Readings:
//DROP    CONSTRAINT Reading_Name_unique ON Reading;
//CREATE  CONSTRAINT Reading_Name_unique ON Reading ASSERT name IS UNIQUE;

//DROP    CONSTRAINT Reading_ID_unique ON Reading;
//CREATE  CONSTRAINT Reading_ID_unique ON Reading ASSERT Id IS UNIQUE;

// Constraints on Manual_Datapoints:
//DROP    CONSTRAINT Manual_Datapoint_Name_unique ON Manual_Datapoint;
//CREATE  CONSTRAINT Manual_Datapoint_Name_unique ON Manual_Datapoint ASSERT name IS UNIQUE;

//DROP    CONSTRAINT Manual_Datapoint_ID_unique ON Manual_Datapoint;
//CREATE  CONSTRAINT Manual_Datapoint_ID_unique ON Manual_Datapoint ASSERT Id IS UNIQUE;
	
// Get all Formulas:
//		WITH Site/Device:
//			MATCH p = ()<-[:Depends_On]-(:Formula)<-[:Calculates_With]-(:Device) return p
//		WITHOUT Site/Device:
//			MATCH p = ()<-[:Depends_On]-(:Formula) return p
//          or: MATCH path = (:Formula)-[:Depends_On]->() return path;

// CREATE (:Device { Name: 'SBC@iWellSite R&D Labs', 				Id: -2 });
// CREATE (:Device { Name: 'IO@iWellSite R&D Labs', 				Id: -1 });
// CREATE (:Device { Name: 'External Device', 						Id: 0 });

//MATCH (md_3:Manual_Datapoint { Id: 3 })
//MATCH (r_4:Reading { Id: 4 })
//MATCH (f_6:Formula { Id: 6 })
//CREATE (Formula_30:Formula {
//Name: 'Formula_30',
//ImmutableName: 'Formula_30',
//Id: 30,
//typeId: 20,
//Expression: '$3$ * $4$ - $6$'
//})
//CREATE (md_3)<-[:Depends_On]-(Formula_30)
//CREATE (r_4)<-[:Depends_On]-(Formula_30)
//CREATE (f_6)<-[:Depends_On]-(Formula_30)

CREATE (:Manual_Datapoint { Name: 'Specific Gravity', ImmutableName: 'ManualDatapoint_1',					Id: 1, unitTypeId: 1, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'Fluid Shot', ImmutableName: 'ManualDatapoint_2', 							Id: 2, unitTypeId: 2, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'Flowing Friction Losses',  ImmutableName: 'ManualDatapoint_3',	Id: 3, unitTypeId: 3, typeId: 1 });
CREATE (:Reading { Name: 'Tubing Pressure',  ImmutableName: 'Reading_4',							Id: 4, unitTypeId: 3, typeId: 2, highShutdown: 3000, highSlowdown: 2000, lowShutdown: 100, lowSlowdown: 150 });
CREATE (:Reading { Name: 'Casing Pressure',  ImmutableName: 'Reading_5',							Id: 5, unitTypeId: 3, typeId: 2 });

// MATCH (sbc:Device { Name: 'SBC@iWellSite R&D Labs' })
MATCH (specificGravity:Manual_Datapoint { Id: 1 })
MATCH (fluidShot:Manual_Datapoint { Id: 2 })
MATCH (flowingFrictionLosses:Manual_Datapoint { Id: 3 })
MATCH (tubingPressure:Reading { Id: 4 })
MATCH (casingPressure:Reading { Id: 5 })
CREATE (totalNetLift:Formula {
    Name: 'Total Net Lift',
    ImmutableName: 'Formula_6',
    Id: 6,
    unitTypeId: 2,
   	typeId: 20,
    Expression: '$2$ * $1$ + ($4$ - $5$ + $3$) / 1.42'
})
CREATE (specificGravity)<-[:Depends_On]-(totalNetLift)
CREATE (fluidShot)<-[:Depends_On]-(totalNetLift)
CREATE (flowingFrictionLosses)<-[:Depends_On]-(totalNetLift)
CREATE (tubingPressure)<-[:Depends_On]-(totalNetLift)
CREATE (casingPressure)<-[:Depends_On]-(totalNetLift);
// CREATE (sbc)-[:Calculates_With]->(totalNetLift);

CREATE (:Manual_Datapoint { Name: 'pcp Displacement', ImmutableName: 'ManualDatapoint_7', 		Id: 7, unitTypeId: 4, typeId: 1 });

MATCH (pcpDisplacement:Manual_Datapoint { Id: 7 })
MATCH (totalNetLift:Formula { Id: 6 })
CREATE (theoreticalPCPLiftTorque:Formula {
    Name: 'Theoretical PCP Lift Torque',
    ImmutableName: 'Formula_8',
    Id: 8,
    unitTypeId: 4,
   	typeId: 20,
    Expression: '($6$ * $7$) / 125'
})
CREATE (pcpDisplacement)<-[:Depends_On]-(theoreticalPCPLiftTorque)
CREATE (totalNetLift)<-[:Depends_On]-(theoreticalPCPLiftTorque);

CREATE (:Manual_Datapoint { Name: 'TopDrive Gear Ratio',  ImmutableName: 'ManualDatapoint_9',		Id: 9, unitTypeId: 0, typeId: 1 });
CREATE (:Reading { Name: 'Electrical Torque',  ImmutableName: 'Reading_10',					Id: 10, unitTypeId: 5, typeId: 2 });

MATCH (topDriveGearRatio:Manual_Datapoint { Id: 9 })
MATCH (electricalTorque:Reading { Id: 10 })
CREATE (pcpTorque:Formula {
    Name: 'pcp Torque',
    ImmutableName: 'Formula_11',
    Id: 11,
    unitTypeId: 5,
   	typeId: 20,
    Expression: '$10$ * $9$'
})
CREATE (topDriveGearRatio)<-[:Depends_On]-(pcpTorque)
CREATE (electricalTorque)<-[:Depends_On]-(pcpTorque);
 
// TODO: Revisit this Formula as it requires multiple point-in-time PCP Torque<F> values as input instead of these (hardcoded) Manual Datapoints:
CREATE (:Manual_Datapoint { Name: 'pcp Torque Sum',  ImmutableName: 'ManualDatapoint_12',		Id: 12, unitTypeId: 5, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'pcp Torque Count',  ImmutableName: 'ManualDatapoint_13',		Id: 13, unitTypeId: 0, typeId: 1 });

MATCH (pcpTorqueSumForPeriod:Manual_Datapoint { Id: 12 })
MATCH (pcpTorqueCountForPeriod:Manual_Datapoint { Id: 13 })
CREATE (pcpAveragedTorque:Formula {
    Name: 'Averaged PCP Torque',
    ImmutableName: 'Formula_14',
    Id: 14,
    unitTypeId: 5,
   	typeId: 20,
    Expression: '$12$ / $13$'
})
CREATE (pcpTorqueSumForPeriod)<-[:Depends_On]-(pcpAveragedTorque)
CREATE (pcpTorqueCountForPeriod)<-[:Depends_On]-(pcpAveragedTorque);

MATCH (pcpAveragedTorque:Formula { Id: 14 })
MATCH (theoreticalPCPLiftTorque:Formula { Id: 8 })
CREATE (totalFrictionTorque:Formula {
    Name: 'Total Friction Torque',
    ImmutableName: 'Formula_15',
    Id: 15,
    unitTypeId: 5,
   	typeId: 20,
    Expression: '$14$ - $8$'
})
CREATE (pcpAveragedTorque)<-[:Depends_On]-(totalFrictionTorque)
CREATE (theoreticalPCPLiftTorque)<-[:Depends_On]-(totalFrictionTorque);

MATCH (pcpAveragedTorque:Formula { Id: 14 })
MATCH (totalFrictionTorque:Formula { Id: 15 })
MATCH (pcpDisplacement:Manual_Datapoint { Id: 7 })
MATCH (tubingPressure:Reading { Id: 4 })
MATCH (casingPressure:Reading { Id: 5 })
MATCH (flowingFrictionLosses:Manual_Datapoint { Id: 3 })
MATCH (specificGravity:Manual_Datapoint { Id: 1 })
CREATE (calculatedFluidLevel:Formula {
    Name: 'Calculated Fluid Level',
    ImmutableName: 'Formula_16',
    Id: 16,
    unitTypeId: 2,
   	typeId: 20,
    Expression: '((($14$ - $15$) / $7$)-(($4$ - $5$ + $3$) / 1.42))/$1$'
})
CREATE (pcpAveragedTorque)<-[:Depends_On]-(calculatedFluidLevel)
CREATE (totalFrictionTorque)<-[:Depends_On]-(calculatedFluidLevel)
CREATE (pcpDisplacement)<-[:Depends_On]-(calculatedFluidLevel)
CREATE (tubingPressure)<-[:Depends_On]-(calculatedFluidLevel)
CREATE (casingPressure)<-[:Depends_On]-(calculatedFluidLevel)
CREATE (flowingFrictionLosses)<-[:Depends_On]-(calculatedFluidLevel)
CREATE (specificGravity)<-[:Depends_On]-(calculatedFluidLevel);

CREATE (:Manual_Datapoint { Name: 'Motor HorsePower',  ImmutableName: 'ManualDatapoint_17',		Id: 17, unitTypeId: 6, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'Motor RatedSpeed',  ImmutableName: 'ManualDatapoint_18',		Id: 18, unitTypeId: 7, typeId: 1 });

MATCH (motorHorsePower:Manual_Datapoint { Id: 17 })
MATCH (motorRatedSpeed:Manual_Datapoint { Id: 18 })
CREATE (motorFullLoadTorque:Formula  {
    Name: 'Motor FullLoad Torque',
    ImmutableName: 'Formula_19',
    Expression: '($17$ * 5252) / $18$',
    Id: 19, 
    unitTypeId: 5, 
   	typeId: 20,
    highShutdown: 0, highSlowdown: 0, lowShutdown: 0, lowSlowdown: 0
})
CREATE (motorHorsePower)<-[:Depends_On]-(motorFullLoadTorque)
CREATE (motorRatedSpeed)<-[:Depends_On]-(motorFullLoadTorque);

CREATE (:Manual_Datapoint { Name: 'Motor ServiceFactor',  ImmutableName: 'ManualDatapoint_20',		Id: 20, unitTypeId: 0, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'Drive Shutdown Safety Factor',  ImmutableName: 'ManualDatapoint_21',		Id: 21, unitTypeId: 0, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'Drive GearRatio',  ImmutableName: 'ManualDatapoint_22',		Id: 22, unitTypeId: 0, typeId: 1 });

MATCH (motorServiceFactor:Manual_Datapoint { Id: 20 })
MATCH (driveShutdownSafetyFactor:Manual_Datapoint { Id: 21 })
MATCH (driveGearRatio:Manual_Datapoint { Id: 22 })
MATCH (motorFullLoadTorque:Formula { Id: 19 })
CREATE (driveShutdownTorque:Formula {
    Name: 'Drive Shutdown Torque',
    ImmutableName: 'Formula_23',
    Expression: '$19$ * $21$ * $20$ * $22$',
    Id: 23, 
    unitTypeId: 5, 
   	typeId: 20,
    highShutdown: 0, highSlowdown: 0, lowShutdown: 0, lowSlowdown: 0
})
CREATE (motorServiceFactor)<-[:Depends_On]-(driveShutdownTorque)
CREATE (driveShutdownSafetyFactor)<-[:Depends_On]-(driveShutdownTorque)
CREATE (driveGearRatio)<-[:Depends_On]-(driveShutdownTorque)
CREATE (motorFullLoadTorque)<-[:Depends_On]-(driveShutdownTorque);

CREATE (:Manual_Datapoint { Name: 'Drive Slowdown Safety Factor',  ImmutableName: 'ManualDatapoint_24',		Id: 24, unitTypeId: 0, typeId: 1 });

MATCH (driveGearRatio:Manual_Datapoint { Id: 22 })
MATCH (driveSlowdownSafetyFactor:Manual_Datapoint { Id: 24 })
MATCH (motorFullLoadTorque:Formula { Id: 19 })
CREATE (driveSlowdownTorque:Formula {
    Name: 'Drive Slowdown Torque',
    ImmutableName: 'Formula_25',
    Expression: '$19$ * $24$ * $22$',
    Id: 25, 
    unitTypeId: 5, 
   	typeId: 20,
    highShutdown: 0, highSlowdown: 0, lowShutdown: 0, lowSlowdown: 0
})
CREATE (driveGearRatio)<-[:Depends_On]-(driveSlowdownTorque)
CREATE (driveSlowdownSafetyFactor)<-[ r:Depends_On ]-(driveSlowdownTorque)
CREATE (motorFullLoadTorque)<-[:Depends_On]-(driveSlowdownTorque);

// Scheduled Formulae:
//
CREATE (:Reading { Name: 'Celsius Temperature',  ImmutableName: 'Reading_109',						Id: 109, unitTypeId: 8, typeId: 2 });

MATCH (CelsiusTemperature:Reading { Id: 109 })
CREATE (formula:Formula {
    Name: 'Fahrenheit Temperature from Celsius',
    ImmutableName: 'Formula_110',
    Id: 110,
    unitTypeId: 8,
   	typeId: 20,
    Expression: '$109$ * (9/5) + 32',
    period: 60000
})
CREATE (CelsiusTemperature)<-[:Depends_On]-(formula);

CREATE (:Manual_Datapoint { Name: 'Ambient Heat Index Coefficient_1 Fahrenheit', ImmutableName: 'ManualDatapoint_111',	Id: 111, unitTypeId: 0, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'Ambient Heat Index Coefficient_2 Fahrenheit', ImmutableName: 'ManualDatapoint_112',	Id: 112, unitTypeId: 0, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'Ambient Heat Index Coefficient_3 Fahrenheit', ImmutableName: 'ManualDatapoint_113', 	Id: 113, unitTypeId: 0, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'Ambient Heat Index Coefficient_4 Fahrenheit', ImmutableName: 'ManualDatapoint_114', 	Id: 114, unitTypeId: 0, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'Ambient Heat Index Coefficient_5 Fahrenheit', ImmutableName: 'ManualDatapoint_115', 	Id: 115, unitTypeId: 0, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'Ambient Heat Index Coefficient_6 Fahrenheit', ImmutableName: 'ManualDatapoint_116', 	Id: 116, unitTypeId: 0, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'Ambient Heat Index Coefficient_7 Fahrenheit', ImmutableName: 'ManualDatapoint_117', 	Id: 117, unitTypeId: 0, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'Ambient Heat Index Coefficient_8 Fahrenheit', ImmutableName: 'ManualDatapoint_118', 	Id: 118, unitTypeId: 0, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'Ambient Heat Index Coefficient_9 Fahrenheit', ImmutableName: 'ManualDatapoint_119', 	Id: 119, unitTypeId: 0, typeId: 1 });

CREATE (:Reading { Name: 'Relative Humidity', ImmutableName: 'Reading_120', 						Id: 120, unitTypeId: 9, typeId: 2 });

MATCH (c1:Manual_Datapoint { Id: 111 })
MATCH (c2:Manual_Datapoint { Id: 112 })
MATCH (c3:Manual_Datapoint { Id: 113 })
MATCH (c4:Manual_Datapoint { Id: 114 })
MATCH (c5:Manual_Datapoint { Id: 115 })
MATCH (c6:Manual_Datapoint { Id: 116 })
MATCH (c7:Manual_Datapoint { Id: 117 })
MATCH (c8:Manual_Datapoint { Id: 118 })
MATCH (c9:Manual_Datapoint { Id: 119 })
MATCH (T:Formula { Id: 110 })
MATCH (R:Reading { Id: 120 })
CREATE (formula:Formula {
    Name: 'Ambient Heat Index Fahrenheit',
    ImmutableName: 'Formula_121',
    Id: 121,
    unitTypeId: 0,
   	typeId: 20,
    Expression: '$111$ + $112$*$110$ + $113$*$120$ + $114$*$110$*$120$ + $115$*$110$*$110$ + $116$*$120$*$120$ + $117$*$110$*$110$*$120$ + $118$*$110$*$120$*$120$ + $119$*$110$*$110$*$120$*$120$'
})
CREATE (c1)<-[:Depends_On]-(formula)
CREATE (c2)<-[:Depends_On]-(formula)
CREATE (c3)<-[:Depends_On]-(formula)
CREATE (c4)<-[:Depends_On]-(formula)
CREATE (c5)<-[:Depends_On]-(formula)
CREATE (c6)<-[:Depends_On]-(formula)
CREATE (c7)<-[:Depends_On]-(formula)
CREATE (c8)<-[:Depends_On]-(formula)
CREATE (c9)<-[:Depends_On]-(formula)
CREATE (T)<-[:Depends_On]-(formula)
CREATE (R)<-[:Depends_On]-(formula)
;

// Test new Expression syntax:
//CREATE (:Manual_Datapoint { Name: 'Renamable_Manual_Datapoint_122', ImmutableName: 'ManualDatapoint_122', Id: 122, unitTypeId: 1, typeId: 1 });
//CREATE (:Reading { Name: 'Renamable_Reading_123', ImmutableName: 'Reading_123', Id: 123, unitTypeId: 3, typeId: 2 });
//
//MATCH (renamableManualDatapoint122:Manual_Datapoint { Id: 122 })
//MATCH (renamableReading123:Reading { Id: 123 })
//CREATE (testFormula:Formula {
//    Name: 'Test Formula For Renaming Dependencies',
//    ImmutableName: 'Formula_124',
//    Id: 124,
//    unitTypeId: 2,
//   	typeId: 20,
//    Expression: '$122$ + 5 * $123$'
//})
//CREATE (renamableManualDatapoint122)<-[:Depends_On]-(testFormula)
//CREATE (renamableReading123)<-[:Depends_On]-(testFormula);


// SELECT all Formulas:
//
// #MATCH (n) RETURN n;
// # Agensgraph (Browser) DOES NOT return the Relationships with the above query; use this instead:
MATCH p = ()<-[:Depends_On]-(:Formula) return p

// Selects Formulas w/Dependencies and unattached Readings and/or ManualDatapoints (in AgensBrowser UI at least):
// MATCH (md:Manual_Datapoint) OPTIONAL MATCH (r:Reading) OPTIONAL MATCH p = ()<-[:Depends_On]-(:Formula) RETURN *;

// SELECT DriveShutdownTorque Formula and it's dependencies
// MATCH p = (n)<-[:Depends_On]-(:Formula { Name: 'driveShutdownTorqueFormula'} ) return p;

////////////////////////////////////////////////////////////////////////////////////
// Rules, Conditions and Actions:

// Constraints on RULEs' Properties:
//	NOTE: AgensGraph allows ONLY the use of ONE vertical pipe (at a time; versus e.g. ||) within a Node Property
//		(using DUAL pipes results in their elimination during a CYPHER query for the Property!?).
//	CONSEQUENTLY,
//		the use of dual pipes in a Java expression will not have the desired result (they will be disappeared
//		by the time that the Java code makes use of the Property (mxParser e.g. uses the single pipe syntax)).

// DB constraints on RULEs:
//	NOTE: AgensGraph allows ONLY ONE constraint PER Vertex/Edge Property:
//FAIL w/SQLException:==> CREATE  CONSTRAINT Rule_Name_exists_and_is_unique ON RULE ASSERT ((name IS NOT NULL) AND (name IS UNIQUE));
//DROP    CONSTRAINT Rule_Name_unique ON RULE;
//CREATE  CONSTRAINT Rule_Name_unique ON RULE ASSERT name IS UNIQUE;

//DROP    CONSTRAINT Rule_ID_unique ON RULE;
//CREATE  CONSTRAINT Rule_ID_unique ON RULE ASSERT Id IS UNIQUE;

// Constraints on CONDITION_ACTIONs:
//DROP    CONSTRAINT CONDITION_ACTION_Name_unique ON CONDITION_ACTION;
// DO NOT USE: CREATE  CONSTRAINT CONDITION_ACTION_Name_unique ON CONDITION_ACTION ASSERT name IS UNIQUE;

//DROP    CONSTRAINT CONDITION_ACTION_ID_unique ON CONDITION_ACTION;
//CREATE  CONSTRAINT CONDITION_ACTION_ID_unique ON CONDITION_ACTION ASSERT Id IS UNIQUE;

// Constraints on IF_CONDITIONs:
//DROP    CONSTRAINT IF_CONDITION_Name_unique ON IF_CONDITION;
// DO NOT USE: CREATE  CONSTRAINT IF_CONDITION_Name_unique ON IF_CONDITION ASSERT name IS UNIQUE;

//DROP    CONSTRAINT IF_CONDITION_ID_unique ON IF_CONDITION;
// DO NOT USE: CREATE  CONSTRAINT IF_CONDITION_ID_unique ON IF_CONDITION ASSERT Id IS UNIQUE;

// Constraints on ELSE_IF_CONDITIONs:
//DROP    CONSTRAINT ELSE_IF_CONDITION_Name_unique ON ELSE_IF_CONDITION;
//CREATE  CONSTRAINT ELSE_IF_CONDITION_Name_unique ON ELSE_IF_CONDITION ASSERT name IS UNIQUE;

//DROP    CONSTRAINT ELSE_IF_CONDITION_ID_unique ON ELSE_IF_CONDITION;
//CREATE  CONSTRAINT ELSE_IF_CONDITION_ID_unique ON ELSE_IF_CONDITION ASSERT Id IS UNIQUE;

// Constraints on ELSE_CONDITIONs:
//DROP    CONSTRAINT ELSE_CONDITION_Name_unique ON ELSE_CONDITION;
//CREATE  CONSTRAINT ELSE_CONDITION_Name_unique ON ELSE_CONDITION ASSERT name IS UNIQUE;

//DROP    CONSTRAINT ELSE_CONDITION_ID_unique ON ELSE_CONDITION;
//CREATE  CONSTRAINT ELSE_CONDITION_ID_unique ON ELSE_CONDITION ASSERT Id IS UNIQUE;

// Constraints on Acts_:
//DROP    CONSTRAINT Acts_Name_exists ON Acts_;
//CREATE  CONSTRAINT Acts_Name_exists ON Acts_ ASSERT name IS NOT NULL;

// Constraints on Uses_:
//DROP    CONSTRAINT Uses_Name_exists ON Uses_;
//CREATE  CONSTRAINT Uses_Name_exists ON Uses_ ASSERT name IS NOT NULL;

// Constraints on If_:
//DROP    CONSTRAINT If_Name_exists ON If_;
//CREATE  CONSTRAINT If_Name_exists ON If_ ASSERT name IS NOT NULL;

// Constraints on Else_If_:
//DROP    CONSTRAINT Else_If_index_exists ON Else_If_;
//CREATE  CONSTRAINT Else_If_index_exists ON Else_If_ ASSERT index IS NOT NULL;

// Constraints on Else_Then:
//DROP    CONSTRAINT Else_Then_Name_exists ON Else_Then;
//CREATE  CONSTRAINT Else_Then_Name_exists ON Else_Then ASSERT name IS NOT NULL;

// CQL below uses (outdated) immutableName proerty
//
// 	Created below:
//
//		From top-of-my-head:
//
//			RULE(1): Slowdown Pump when setpoint breached: Test Rule that depends on two independent Formulas (i.e. no other Formulas depend upon either of these). #26
//
//				IF_CONDITION: Expression: 'pcpTorque > DriveSlowdownTorque' #28
//					CONDITION_ACTION { Name: 'SysLog the Rule clause evaluation result', Value: 'System.out.println("The PCP Torque value is above the Drive Slowdown Torque setpoint")' } #27
//
//
//
//
Rule (1): CYPHER
// NOTE: The Formula graph nodes (above) cascade their dependencies with the Depends_On relationship
//		To make the CYPHER that gathers the Rule-Formula Paths simpler
// 			(i.e. stopping after the first Formula, not following the Depends_On relationship to any subsequent Formulas, since we don't care in this case)
//		Conditions will use the Uses_ relationship name instead

// TODO: Use: typeId: 10 for Rule Listeners
   	
   	
CREATE (:RULE { Name: 'Slowdown Pump When Setpoint Breached', ImmutableName: 'Rule_26', Id: 26, typeId: 10 });
CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'PCP Torque greater than setpoint', Id: 27 });

MATCH (Rule:RULE { Id: 26 })
MATCH (LogAboveSetpointValueAction:CONDITION_ACTION { Id: 27 })
MATCH (PCPTorqueFormula:Formula { Id: 11 })
MATCH (DriveSlowdownTorqueFormula:Formula { Id: 25 })
CREATE (SubCondition:IF_CONDITION { Name: 'pcp Torque greater than setpoint',  ImmutableName: 'pcp Torque greater than setpoint', Id: 28, Expression: 'pcpTorque > driveSlowdownTorque' })
CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogAboveSetpointValueAction)
CREATE (Rule)-[ :If_ { Name: 'If' } ]->(SubCondition)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(PCPTorqueFormula)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(DriveSlowdownTorqueFormula);

//          RULE(2): Change Pump Speed when setpoints breached #29
//
//			    IF_CONDITION: ( Expression: 'pcpTorque > driveSlowdownTorque | pcpTorque > driveShutdownTorque' ) #31
// 					CONDITION_ACTION { Name: 'SysLogOut that the Pump setpoints have been breached' }) #30
//				    IF_CONDITION: ( Expression: 'pcpTorque > driveShutdownTorque' ) #36
// 				            CONDITION_ACTION { Name: 'STOP the Pump', Value: 'PumpEnums.Control.STOP' }) #32
//				    ELSE_IF_CONDITION:{ Expression: 'pcpTorque > (5 * electricalTorque)' } #37
// 				            CONDITION_ACTION { Name: 'SysLogOut that the PCP Torque value is above average.', Value: 'System.out.println("The PCP Torque value is above five times the average.");' } #34
//				    ELSE_IF_CONDITION:{ Expression: 'pcpTorque > (10 * electricalTorque)' } #38
// 				            CONDITION_ACTION { Name: 'SysLogOut that the PCP Torque value is above average.', Value: 'System.out.println("The PCP Torque value is above ten times the average.");' } #35
//				    ELSE_CONDITION: { Expression: 'TRUE' } #39
// 				            CONDITION_ACTION { Name: 'SLOW the Pump', Value: PumpEnums.Control.SLOW }) #33
//			    ELSE_IF_CONDITION:{ Expression: 'pcpTorque = driveSlowdownTorque' } #41
// 					CONDITION_ACTION { Name: 'SysLogOut that the PCP Torque value is at the Slowdown setpoint.', Value: 'System.out.println("The PCP Torque value is at the Slowdown setpoint.");' } #40
// 			    IF_CONDITION:{ Expression: 'pcpTorque = driveShutdownTorque' } #43
// 								    CONDITION_ACTION { Name: 'SysLogOut that the PCP Torque value is at the Shutdown setpoint.', Value: 'System.out.println("The PCP Torque value is at the Shutdown setpoint.");' } #42
//			    ELSE_IF_CONDITION:{ Expression: 'pcpTorque = driveShutdownTorque' }#45
// 					CONDITION_ACTION { Name: 'SysLogOut that the PCP Torque value is at the Shutdown setpoint.', Value: System.out.println("The PCP Torque value is at the Shutdown setpoint."); } #44
//			    ELSE_CONDITION: { Expression: 'TRUE' } #47
// 					CONDITION_ACTION { Name: 'Setpoints not violated', Value: 'SystemEnums.DO_NOTHING' }) #46
//
//
Rule (2): CYPHER

CREATE (:RULE { Name: 'Change Pump Speed When Setpoints Breached', ImmutableName: 'Rule_29', Id: 29, typeId: 10 });
CREATE (:CONDITION_ACTION { Name: 'SysLog.SETPOINTS_BREACHED', Value: '[DriveSlowdownTorque|DriveShutdownTorque]', Id: 30 });

// Top-level (IF)Condition:
MATCH (Rule:RULE { Id: 29 })
MATCH (LogAboveSetpointValueAction:CONDITION_ACTION { Id: 30 })
MATCH (PCPTorqueFormula:Formula { Id: 11 })
MATCH (DriveSlowdownTorqueFormula:Formula { Id: 25 })
MATCH (DriveShutdownTorqueFormula:Formula { Id: 23 })
CREATE (SubCondition:IF_CONDITION { Name: 'pcp Torque greater than setpoint(s)', ImmutableName: 'pcp Torque greater than setpoint(s)', Id: 31, Expression: '$11$ > $25$ | $11$ > $23$' })
CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogAboveSetpointValueAction)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(PCPTorqueFormula)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(DriveSlowdownTorqueFormula)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(DriveShutdownTorqueFormula)
CREATE (Rule)-[ :If_ { Name: 'If' } ]->(SubCondition);

//	// Actions for the 2nd-level Conditions:
	CREATE (:CONDITION_ACTION { Name: 'SysLog.STOP', Id: 32 });
	CREATE (:CONDITION_ACTION { Name: 'SysLog.RUN', Value: '3500', Id: 33 });
	CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'The PCP Torque value is above average.', Id: 34 });
	CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'The PCP Torque value is above average.', Id: 35 });
	
//	// 2nd-level (IF)Condition:
	MATCH (SuperCondition:IF_CONDITION { Id: 31 })
	MATCH (StopThePumpAction:CONDITION_ACTION { Id: 32 })
	MATCH (PCPTorqueFormula:Formula { Id: 11 })
	MATCH (DriveShutdownTorqueFormula:Formula { Id: 23 })
	CREATE (SubCondition:IF_CONDITION { Name: 'pcp Torque greater than SHUTDOWN setpoint', ImmutableName: 'pcp Torque greater than SHUTDOWN setpoint', Id: 36, Expression: '$11$ > $23$' })
	CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(StopThePumpAction)
	CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(PCPTorqueFormula)
	CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(DriveShutdownTorqueFormula)
	CREATE (SuperCondition)-[ :If_ { Name: 'If' } ]->(SubCondition);
	
//	// 2nd-level, 1st (ELSE/IF)Condition:
	MATCH (SuperCondition:IF_CONDITION { Id: 31 })
	MATCH (LogAboveAvgValueAction:CONDITION_ACTION { Id: 34 })
	MATCH (ElectricalTorqueReading:Reading { Id: 10 })
	MATCH (PCPTorqueFormula:Formula { Id: 11 })
	CREATE (SubCondition:ELSE_IF_CONDITION { Name: 'pcp Torque greater 5 times average', ImmutableName: 'pcp Torque greater 5 times average', Id: 37, Expression: '$11$ > (5 * $10$)' })
	CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogAboveAvgValueAction)
	CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(ElectricalTorqueReading)
	CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(PCPTorqueFormula)
	CREATE (SuperCondition)-[ :Else_If_ { index: 0 } ]->(SubCondition);
	
//	// 2nd-level, 2nd (ELSE/IF)Condition:
	MATCH (SuperCondition:IF_CONDITION { Id: 31 })
	MATCH (LogAboveAvgValueAction:CONDITION_ACTION { Id: 35})
	MATCH (ElectricalTorqueReading:Reading { Id: 10 })
	MATCH (PCPTorqueFormula:Formula { Id: 11 })
	CREATE (SubCondition:ELSE_IF_CONDITION { Name: 'pcp Torque greater 10 times average', ImmutableName: 'pcp Torque greater 10 times average', Id: 38, Expression: '$11$ > (10 * $10$)' })
	CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogAboveAvgValueAction)
	CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(ElectricalTorqueReading)
	CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(PCPTorqueFormula)
	CREATE (SuperCondition)-[ :Else_If_ { index: 1 } ]->(SubCondition);
	
//	// 2nd-level, (ELSE)Condition:
	MATCH (SuperCondition:IF_CONDITION { Id: 31 })
	MATCH (SlowThePumpAction:CONDITION_ACTION { Id: 33 })
	CREATE (SubCondition:ELSE_CONDITION { Name: 'pcp Torque greater than SLOWDOWN setpoint', ImmutableName: 'pcp Torque greater than SLOWDOWN setpoint', Id: 39 })
	CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(SlowThePumpAction)
	CREATE (SuperCondition)-[ :Else_Then { Name: 'Else' } ]->(SubCondition);

// Top-level, 1st (ELSE/IF)Condition:
CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'The PCP Torque value is at the Slowdown setpoint.', Id: 40 });

MATCH (LogValueAtSetpointAction:CONDITION_ACTION { Id: 40 })
MATCH (Rule:RULE { Id: 29 })
MATCH (PCPTorqueFormula:Formula { Id: 11 })
MATCH (DriveSlowdownTorqueFormula:Formula { Id: 25 })
CREATE (SubCondition:ELSE_IF_CONDITION { Name: 'pcp Torque equal to Slowdown setpoint', ImmutableName: 'pcp Torque equal to Slowdown setpoint', Id: 41, Expression: 'pcpTorque == driveSlowdownTorque' })
CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogValueAtSetpointAction)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(PCPTorqueFormula)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(DriveSlowdownTorqueFormula)
CREATE (Rule)-[ :Else_If_ { index: 0 } ]->(SubCondition);

// 	2nd-level, (IF)Condition (pertaining to above parent ELSE/IF):
	CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'The PCP Torque value is at the Shutdown setpoint.', Id: 42 });
	
	MATCH (LogValueAtSetpointAction:CONDITION_ACTION { Id: 42 })
	MATCH (SuperCondition:ELSE_IF_CONDITION { Id: 41 })
	MATCH (PCPTorqueFormula:Formula { Id: 11 })
	MATCH (DriveShutdownTorqueFormula:Formula { Id: 23 })
	CREATE (SubCondition:IF_CONDITION { Name: 'pcp Torque greater than SHUTDOWN setpoint', ImmutableName: 'pcp Torque greater than SHUTDOWN setpoint', Id: 43, Expression: 'pcpTorque == driveShutdownTorque' })
	CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogValueAtSetpointAction)
	CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(PCPTorqueFormula)
	CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(DriveShutdownTorqueFormula)
	CREATE (SuperCondition)-[ :If_ { Name: 'If' } ]->(SubCondition);

// Top-level, 2nd (ELSE/IF)Condition:
CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'The PCP Torque value is at the Shutdown setpoint.', Id: 44 });

MATCH (LogValueAtSetpointAction:CONDITION_ACTION { Id: 44 })
MATCH (Rule:RULE { Id: 29 })
MATCH (PCPTorqueFormula:Formula { Id: 11 })
MATCH (DriveShutdownTorqueFormula:Formula { Id: 23 })
CREATE (SubCondition:ELSE_IF_CONDITION { Name: 'pcp Torque equal to Shutdown setpoint', ImmutableName: 'pcp Torque equal to Shutdown setpoint', Id: 45, Expression: 'pcpTorque == driveShutdownTorque' })
CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogValueAtSetpointAction)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(PCPTorqueFormula)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(DriveShutdownTorqueFormula)
CREATE (Rule)-[ :Else_If_ { index: 1 } ]->(SubCondition);

// Top-level (ELSE)Condition:
CREATE (:CONDITION_ACTION { Name: 'SysLog.NOP', Id: 46 });

MATCH (NoOperationAction:CONDITION_ACTION { Id: 46 })
MATCH (Rule:RULE { Id: 29 })
CREATE (SubCondition:ELSE_CONDITION { Name: 'Setpoints not violated.', ImmutableName: 'Setpoints not violated.', Id: 47 })
CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(NoOperationAction)
CREATE (Rule)-[ :Else_Then { Name: 'Else' } ]->(SubCondition);


//			RULE(3): Nonsensical test Rule that depends on two Readings and two Formulas (one Formula that is NOT a dependency of another Formula, and one that IS) #100
//
//				IF_CONDITION: Expression: '(pcpTorque > theoreticalPCPLiftTorque) | (tubingPressure == driveGearRatio)' #102
//					CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'Nonsensical test Rule that depends on a Reading and two Formulas (one Formula that is NOT a dependency of another Formula, and one that IS) fired.', Id: 101 } #101
//
//
//
//
Rule (3): CYPHER

CREATE (:RULE { Name: 'Nonsensical Test Rule That Depends On A Reading And Two Formulas', ImmutableName: 'Rule_100', Id: 100, typeId: 10 });
CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'Nonsensical test Rule that depends on a Reading and two Formulas (one Formula that is NOT a dependency of another Formula, and one that IS) fired.', Id: 101 });

MATCH (NonsensicalRule:RULE { Id: 100 })
MATCH (LogAction:CONDITION_ACTION { Id: 101 })
MATCH (PCPTorqueFormula:FORMULA { Id: 11 })
MATCH (TheoreticalPCPLiftTorqueFormula:FORMULA { Id: 8 })
MATCH (TubingPressureReading:READING { Id: 4 })
MATCH (DriveGearRatioManualDatapoint:MANUAL_DATAPOINT { Id: 22 })
CREATE (SubCondition:IF_CONDITION { Name: 'Nonsensical test Rule that depends on two Readings and two Formulas', ImmutableName: 'Nonsensical test Rule that depends on two Readings and two Formulas', Id: 102, Expression: '(pcpTorque > theoreticalPCPLiftTorque) | (tubingPressure == driveGearRatio)' })
CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogAction)
CREATE (NonsensicalRule)-[ :If_ { Name: 'If' } ]->(SubCondition)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(PCPTorqueFormula)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(TheoreticalPCPLiftTorqueFormula)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(TubingPressureReading)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(DriveGearRatioManualDatapoint);

//			RULE(4): Test Rule For Scheduling #103
//
//				IF_CONDITION: Expression: 'pcpTorque > 1000' #105
//					CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'Test Rule For Scheduling.', Id: 101 } #104
//
Rule (4): CYPHER

CREATE (:RULE { Name: 'Test Rule For Scheduling', ImmutableName: 'Rule_103', Id: 103, period: 7000, typeId: 10 });
CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'Test Rule For Scheduling.', Id: 104 });

MATCH (SchedulingRule:RULE { Id: 103 })
MATCH (LogAction:CONDITION_ACTION { Id: 104 })
MATCH (PCPTorqueFormula:FORMULA { Id: 11 })
CREATE (SubCondition:IF_CONDITION { Name: 'PCP Torque is greater than one thousand', ImmutableName: 'PCP Torque is greater than one thousand', Id: 105, Expression: 'pcpTorque > 1000' })
CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogAction)
CREATE (SchedulingRule)-[ :If_ { Name: 'If' } ]->(SubCondition)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(PCPTorqueFormula);


//			RULE(5): Test Realtime Rule For Electrical Torque #106
//
//				IF_CONDITION: Expression: 'pcpTorque > 1000 | electricalTorque > 300' #108
//					CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'Test Rule For Scheduling.', Id: 101 } #107
//
Rule (5): CYPHER

CREATE (:RULE { Name: 'Test Realtime Rule For Electrical Torque', ImmutableName: 'Rule_106', Id: 106, typeId: 10 });
//CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'pcpTorque > 1000 | electricalTorque > 300.', Id: 107 });
CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'pcpTorque > 1000.', Id: 107 });

MATCH (SchedulingRule:RULE { Id: 106 })
MATCH (LogAction:CONDITION_ACTION { Id: 107 })
MATCH (PCPTorqueFormula:FORMULA { Id: 11 })
//MATCH (ElectricalTorqueReading:Reading { Id: 10 })
//CREATE (SubCondition:IF_CONDITION { Name: 'Should receive Electrical Torque event.', ImmutableName: 'Should receive Electrical Torque event.', Id: 108, Expression: 'pcpTorque > 1000 | electricalTorque > 300' })
CREATE (SubCondition:IF_CONDITION { Name: 'Should receive PCP Torque event.', ImmutableName: 'Should receive PCP Torque event.', Id: 108, Expression: 'pcpTorque > 1000' })
CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogAction)
CREATE (SchedulingRule)-[ :If_ { Name: 'If' } ]->(SubCondition)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(PCPTorqueFormula)
//CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(ElectricalTorqueReading)
;


//			RULE(6): Test (Scheduled) Rule w/Java expression (vs. mxParser Expression) #109
//
//				IF_CONDITION: Expression: '(666/0) > 1000' #114
//					CONDITION_ACTION { Name: 'SysLog.MSG', Value: '(666/0) > 1000', Id: 101 } #110
//			    ELSE_IF_CONDITION:{ Expression: 'Double.MAX_VALUE < Double.POSITIVE_INFINITY' } #115
// 						CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'Double.MAX_VALUE < Double.POSITIVE_INFINITY' } #111
//			    ELSE_IF_CONDITION:{ Expression: 'Double.MAX_VALUE < Double.MAX_VALUE + 1' } #116
// 						CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'Double.MAX_VALUE < Double.MAX_VALUE + 1' } #112
//			    ELSE_CONDITION: { Expression: 'TRUE' } #117
// 						CONDITION_ACTION { Name: 'SysLog.MSG', Value: "Rule w/Java expression ELSE'd" }) #113
//
//
Rule (6): CYPHER
Deprecated...
// Test (Scheduled) Rule w/Java expression (vs. mxParser Expression)
//CREATE (:RULE { Name: 'Test Scheduled Rule With Java Expression Versus MXParser Expression', ImmutableName: 'Rule_109', Id: 109, period: 120000, typeId: 10 });
//CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: '(666/0) > 1000', Id: 110 });

//MATCH (SchedulingRule:RULE { Id: 109 })
//MATCH (LogAction:CONDITION_ACTION { Id: 110 })
//CREATE (SubCondition:IF_CONDITION { Name: 'Divide by zero', ImmutableName: 'Divide by zero', Id: 114, Expression: '(666/0) > 1000' })
//CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogAction)
//CREATE (SchedulingRule)-[ :If_ { Name: 'If' } ]->(SubCondition);

// Top-level, 1st (ELSE/IF)Condition:
//CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'Double.MAX_VALUE < Double.POSITIVE_INFINITY', Id: 111 });

//MATCH (Rule:RULE { Id: 109 })
//MATCH (LogAction:CONDITION_ACTION { Id: 111 })
//CREATE (SubCondition:ELSE_IF_CONDITION { Name: 'Max less than Infinity', ImmutableName: 'Max less than Infinity', Id: 115, Expression: 'Double.MAX_VALUE < Double.POSITIVE_INFINITY' })
//CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogAction)
//CREATE (Rule)-[ :Else_If_ { index: 0 } ]->(SubCondition);

// Top-level, 2nd (ELSE/IF)Condition:
//CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: '(Double.MAX_VALUE < Double.MAX_VALUE + 1)', Id: 112 });

//MATCH (Rule:RULE { Id: 109 })
//MATCH (LogAction:CONDITION_ACTION { Id: 112 })
//CREATE (SubCondition:ELSE_IF_CONDITION { Name: 'Max less than max plus one', ImmutableName: 'Max less than max plus one', Id: 116, Expression: 'Double.MAX_VALUE < (Double.MAX_VALUE + 1)' })
//CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogAction)
//CREATE (Rule)-[ :Else_If_ { index: 1 } ]->(SubCondition);

// Top-level (ELSE)Condition:

//CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'Scheduled Rule w/Java expression ELSEd', Id: 113 });

//MATCH (Rule:RULE { Id: 109 })
//MATCH (LogAction:CONDITION_ACTION { Id: 113 })
//CREATE (SubCondition:ELSE_CONDITION { Name: 'ELSE condition fallthru', ImmutableName: 'ELSE condition fallthru', Id: 117 })
//CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogAction)
//CREATE (Rule)-[ :Else_Then { Name: 'Else' } ]->(SubCondition);



//			RULE(7): Evil Scheduled Rule #
//
//				IF_CONDITION: Expression: 'false) etc.' #
//					CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'false) else if (true) { System.out.println("Bye!"); System.exit(666); } else if (false', Id:  } #
//
    
Rule (7): CYPHER
Deprecated...
//CREATE (:RULE { Name: 'Evil Scheduled Rule', ImmutableName: 'Rule_118', Id: 118, period: 20000, typeId: 10 });
//CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'Evil Scheduled Rule ran.', Id: 119 });

//MATCH (SchedulingRule:RULE { Id: 118 })
//MATCH (LogAction:CONDITION_ACTION { Id: 119 })
//CREATE (SubCondition:IF_CONDITION { Name: 'Java expression injection.', ImmutableName: 'Java expression injection.', Id: 120, Expression: 'false) {} else if (true) { System.out.println("Evil (scheduled) Rule exiting the system!"); System.exit(666); } else if (false' })
//CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogAction)
//CREATE (SchedulingRule)-[ :If_ { Name: 'If' } ]->(SubCondition);


//			RULE(8): Scheduled Rule With Isolated Dependency #121
//
//				IF_CONDITION: Expression: 'pcpTorque > 1000' #126
//					CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'Scheduled Rule With Isolated Dependency.', Id: 122 } #122
//			    ELSE_CONDITION: { Expression: 'TRUE' } #125
// 						CONDITION_ACTION { Name: 'SysLog.MSG', Value: "Scheduled Rule With Isolated Dependency ELSE'd" }) #124
//
Rule (8): CYPHER

CREATE (:RULE { Name: 'Scheduled Rule With Isolated Dependency', ImmutableName: 'Rule_121', Id: 121, period: 10000, typeId: 10 });
CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'Scheduled Rule With Isolated Dependency.', Id: 122 });
CREATE (:CONDITION_ACTION { Name: 'SysLog.MSG', Value: 'Scheduled Rule With Isolated Dependency ELSEd', Id: 124 });
CREATE (:Manual_Datapoint { Name: 'Some Datapoint', ImmutableName: 'ManualDatapoint_123', 		Id: 123, unitTypeId: 0, typeId: 1 });

MATCH (Rule:RULE { Id: 121 })
MATCH (LogAction:CONDITION_ACTION { Id: 122 })
MATCH (ManualDatapoint:Manual_Datapoint { Id: 123 })
CREATE (SubCondition:IF_CONDITION { Name: 'Some Datapoint is greater than one thousand', ImmutableName: 'Some Datapoint is greater than one thousand', Id: 126, Expression: 'someDatapoint > 1000' })
CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogAction)
CREATE (Rule)-[ :If_ { Name: 'If' } ]->(SubCondition)
CREATE (SubCondition)-[ :Uses_ { Name: 'Uses' } ]->(ManualDatapoint);

MATCH (Rule:RULE { Id: 121 })
MATCH (LogAction:CONDITION_ACTION { Id: 124 })
CREATE (SubCondition:ELSE_CONDITION { Name: 'ELSE condition fallthru', ImmutableName: 'ELSE condition fallthru', Id: 125 })
CREATE (SubCondition)-[ :Acts_ { Name: 'Does' } ]->(LogAction)
CREATE (Rule)-[ :Else_Then { Name: 'Else' } ]->(SubCondition);


Miscellaneous stuff e.g. find Paths, remove nodes, etc. :

Add some UNUSED (by Formulas/Rules) Readings & ManualDatapoints:

CREATE (:Manual_Datapoint { Name: 'Unused_Manual_Datapoint_127', ImmutableName: 'ManualDatapoint_127', Id: 127, unitTypeId: 1, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'Unused_Manual_Datapoint_128', ImmutableName: 'ManualDatapoint_128', Id: 128, unitTypeId: 1, typeId: 1 });
CREATE (:Manual_Datapoint { Name: 'Unused_Manual_Datapoint_129', ImmutableName: 'ManualDatapoint_129', Id: 129, unitTypeId: 1, typeId: 1 });
CREATE (:Reading { Name: 'Unused_Reading_130', ImmutableName: 'Reading_130', Id: 130, unitTypeId: 3, typeId: 2 });
CREATE (:Reading { Name: 'Unused_Reading_131', ImmutableName: 'Reading_131', Id: 131, unitTypeId: 3, typeId: 2 });
CREATE (:Reading { Name: 'Unused_Reading_132', ImmutableName: 'Reading_132', Id: 132, unitTypeId: 3, typeId: 2 });

// ...and MATCH them (and any Formulas):
MATCH (md:Manual_Datapoint) WHERE md.id > 125 OPTIONAL MATCH (r:Reading) WHERE r.id > 125 OPTIONAL MATCH p = ()<-[:Depends_On]-(:Formula) RETURN *;
// alternatively, use the lack of r'ship to MATCH: DOES NOT WORK!?
MATCH (md:Manual_Datapoint) WHERE NOT (md)<-[:Depends_On]

// Find Rule(s) and Paths to them from Inputs, thru the Conditions using them i.e. Readings, Formulas, Manual Datapoints:
//

Simplified Match:

MATCH p1 =  (:RULE)-[*]->()-[:ACTS_]->() 
OPTIONAL MATCH p2 = (:RULE)-[*]->()-[:USES_]->() 
RETURN *;

// Match first-order(only? TBD) Conditions to find their Actions:
// MATCH p1 =  (:RULE)-[*]->()-[:ACTS_]->(:CONDITION_ACTION)
// Match all dependencies:
//OPTIONAL MATCH p2 = (:RULE)-[*]->()-[:USES_]->()
// Match all Manual Datapoints:
//OPTIONAL MATCH p4 = (:RULE)-[*]->()-[:USES_]->(:MANUAL_DATAPOINT)
// Match all Readings:
// OPTIONAL MATCH p3 = (:RULE)-[*]->()-[:USES_]->(:READING)
// Match all (TODO: first-order) Formulas:
// TODO: Stop matching after the 1st Formula i.e. DO NOT cascade down the dependency chain
// OPTIONAL MATCH p4 = (:RULE)-[*]->()-[:USES_]->(:FORMULA)
// RETURN *;

// OR, simply get Paths from Formulas to Rules: MATCH p = (r:Rule)-[*:USES_]->(f:Formula) RETURN p;
// FYI, this (equivalent?) doesn't work (returns only the Rule) w/AgensGraph: MATCH p = (r:Rule)//>(f:Formula) RETURN p;
// TODO: Try these:
// 	MATCH p = (r:Rule)-[*]-()-[:USES_]->(f:Formula) RETURN p;
// 	MATCH p = (r:Rule)-[*]-()-[:USES_]->() RETURN p;

// Find individual pieces:
//MATCH (:RULE) return *;
//MATCH (:CONDITION_ACTION) RETURN *;
//MATCH (:IF_CONDITION) RETURN *;
//MATCH (:ELSE_IF_CONDITION) RETURN *;
//MATCH (:ELSE_CONDITION) RETURN *;

// Remove individual pieces:
//MATCH (r:RULE) DETACH DELETE r;
//MATCH (a:CONDITION_ACTION) DETACH DELETE a;
//MATCH (c:IF_CONDITION) DETACH DELETE c;
//MATCH (eic:ELSE_IF_CONDITION) DETACH DELETE eic;
//MATCH (ec:ELSE_CONDITION) DETACH DELETE ec;

// Remove Relationships from a Formula:
//MATCH ()<-[r:Depends_On]-(:Formula { id: 8 }) DELETE r;
//MATCH (f:Formula { id: 8 }) return f;
//MATCH (n)<-[r:Depends_On]-(f:Formula { id: 8 }) return DISTINCT n,r,f;

// Delete everything in the graph:
//		MATCH (n) DETACH DELETE n;

// Tack-on a Reading to Rule #1 so I can check the CYPHER for finding the Path elements:
// MATCH (var1:Reading { Name: 'Electrical Torque' })
// MATCH (c:IF_CONDITION { Name: 'pcp Torque greater than setpoint' })
// CREATE (c)-[ :Uses_ { Name: 'Uses' } ]->(var1);

// Tack-on a 2nd Action to Rule #1 so I can check the CYPHER for finding the Path elements:
// CREATE (:CONDITION_ACTION { Name: 'Minimize the Path CYPHER', Id: 66627 });
// MATCH (a:CONDITION_ACTION { Name: 'Minimize the Path CYPHER' })
// MATCH (c:IF_CONDITION { Name: 'pcp Torque greater than setpoint' })
// CREATE (c)-[ :Acts_ { Name: 'Does' } ]->(a);

ID Generation possibilities:
See: https://rob.conery.io/2014/05/28/a-better-id-generator-for-postgresql/
And, in the rare case that you're using the SQuirreL JDBC client, here: https://stackoverflow.com/a/42363575/888537

CREATE SCHEMA shard_1;
CREATE SEQUENCE shard_1.global_id_sequence;

CREATE OR REPLACE FUNCTION shard_1.id_generator(OUT result bigint) AS $$
DECLARE
    our_epoch bigint := 1314220021721;
    seq_id bigint;
    now_millis bigint;
    // the id of this DB shard, must be set for each schema shard you have - you could pass this as a parameter too
    shard_id int := 1;
BEGIN
    SELECT nextval('shard_1.global_id_sequence') % 1024 INTO seq_id;

    SELECT FLOOR(EXTRACT(EPOCH FROM clock_timestamp()) * 1000) INTO now_millis;
    result := (now_millis - our_epoch) << 23;
    result := result | (shard_id << 10);
    result := result | (seq_id);
END;
$$ LANGUAGE PLPGSQL;

Usage:

select shard_1.id_generator();

// Create a Vertex using the Function-derived ID:
// TODO: Make this work with CURRVAL() too so it can be used on 2 properties at once:
CREATE (:Manual_Datapoint { Name: 'Some Datapoint w/FnGenerated ID', ImmutableName: 'Some Datapoint w/FnGenerated ID', 		Id:  (SELECT shard_1.id_generator()), unitTypeId: 0, typeId: 1 });


// Create a Vertex using a SEQUENCE-derived ImmutableName AND ID:

CREATE (
md:Manual_Datapoint { 
   	Name: 'Some Datapoint w/sequenced ID', 
	ImmutableName: (SELECT concat_ws('_', 'Some Datapoint With Generated ID', NEXTVAL('shard_1.global_id_sequence'))), 		
	Id:  (SELECT CURRVAL('shard_1.global_id_sequence'))
});

// Sadly, it seems there's no way to return the ID used ;^(
// (without wrapping up a series of statements into a transaction?)
// Doing CREATE and then CURRVAL() in one DAO method seems to work but may not be safe w/concurrent threads?

// Find it (weakly) by Name property:
MATCH (md:Manual_Datapoint { Name: 'Some Datapoint w/sequenced ID' }) RETURN md;
returns: {
   "name": "Some Datapoint w/sequenced ID",
   "immutablename": "Some Datapoint With Generated ID_3",
   "id": 3
}

Or, introduce a temporary property/value:
CREATE (
md:Manual_Datapoint { 
   	Name: 'Some Datapoint w/sequenced ID', 
	ImmutableName: (SELECT concat_ws('_', 'Some Datapoint With Generated ID', NEXTVAL('shard_1.global_id_sequence'))), 		
	Id:  (SELECT CURRVAL('shard_1.global_id_sequence')),
	idForInitialRetrieval: 666777888999
}
) 
//RETURN md
//RETURN  (SELECT CURRVAL('shard_1.global_id_sequence'))
;
//SELECT CURRVAL('shard_1.global_id_sequence');
//MATCH (md:Manual_Datapoint { idForInitialRetrieval: 666777888999 } ) RETURN md.id;
// TODO: remove the property after first-time-use.
