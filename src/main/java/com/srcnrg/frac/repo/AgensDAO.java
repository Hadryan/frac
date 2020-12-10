package com.srcnrg.frac.repo;

import com.srcnrg.frac.domain.InstanceType;
import com.srcnrg.frac.domain.cache.MetadataCache;
import com.srcnrg.frac.domain.dto.DataTransferObject;
import com.srcnrg.frac.domain.dto.FormulaDTO;
import com.srcnrg.frac.domain.dto.ManualDatapointDTO;
import com.srcnrg.frac.domain.dto.ReadingDTO;
import com.srcnrg.frac.services.FormulaService;
import lombok.extern.log4j.Log4j2;
import net.bitnine.agensgraph.graph.Path;
import net.bitnine.agensgraph.graph.Vertex;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *	DAO for AgensGraph; it is expected that this object will eventually be superseded by a R2DBC Repository (hence the package name).
 * NOTE: Consider refixing statements with: SET graph_path=frac;
 * OR use: ALTER USER agens SET graph_path = 'frac';
 */
@Repository(value="dao")
@Log4j2
public class AgensDAO
{
//	private static final String CYPHER__VERTEX_LABEL_PREFIX = ": ";
	private static final String CYPHER__RETURN_VALUE_TOKEN = "x";
//	private static final String SQL__SEQUENCE__TOKEN = "'global_id_sequence'";
	
	private static final String CYPHER__ID_PROPERTY_REFERENCE = CYPHER__RETURN_VALUE_TOKEN + ".id";
	private static final String CYPHER__ORDER_BY__CLAUSE = " ORDER BY " + CYPHER__ID_PROPERTY_REFERENCE + " ASC";
	
	// Using Sequence directly in Cypher, for ID:
	// private static final String CYPHER__CREATE_MANUAL_DATAPOINT = "CREATE (:Manual_Datapoint { Name: ?, TypeId: ?, ImmutableName: (SELECT CONCAT_WS('_', 'ManualDatapoint', NEXTVAL(" + SQL__SEQUENCE__TOKEN + "))), Id: (SELECT CURRVAL(" + SQL__SEQUENCE__TOKEN + "))} )";
	// Using pre-reserved ID (from Sequence):
	private static final String CYPHER__CREATE_MANUAL_DATAPOINT = "CREATE (:Manual_Datapoint { Name: ?, TypeId: ?, Id: ? } )";

	// Using Sequence directly in Cypher, for ID:
	// private static final String CYPHER__CREATE_READING = "CREATE (:Reading { Name: ?, TypeId: ?, ImmutableName: (SELECT CONCAT_WS('_', 'Reading', NEXTVAL(" + SQL__SEQUENCE__TOKEN + "))), Id: (SELECT CURRVAL(" + SQL__SEQUENCE__TOKEN + "))} )";
	// Using pre-reserved ID (from Sequence):
	private static final String CYPHER__CREATE_READING = "CREATE (:Reading { Name: ?, TypeId: ?, Id: ? } )";
	
	private static final String CYPHER__DELETE_FORMULA = "MATCH (" + CYPHER__RETURN_VALUE_TOKEN + ":Formula { id: ? } ) DETACH DELETE " + CYPHER__RETURN_VALUE_TOKEN;
	private static final String CYPHER__DELETE_MANUAL_DATAPOINT = "MATCH (" + CYPHER__RETURN_VALUE_TOKEN + ":Manual_Datapoint { id: ? } ) DELETE " + CYPHER__RETURN_VALUE_TOKEN;
	private static final String CYPHER__DELETE_READING = "MATCH (" + CYPHER__RETURN_VALUE_TOKEN + ":Reading { id: ? } ) DELETE " + CYPHER__RETURN_VALUE_TOKEN;

	private static final String CYPHER__UPDATE_MANUAL_DATAPOINT = "MATCH (" + CYPHER__RETURN_VALUE_TOKEN + ":Manual_Datapoint { id: ? } ) SET " + CYPHER__RETURN_VALUE_TOKEN + ".name = ? ";
	private static final String CYPHER__UPDATE_READING = "MATCH (" + CYPHER__RETURN_VALUE_TOKEN + ":Reading { id: ? } ) SET " + CYPHER__RETURN_VALUE_TOKEN + ".name = ? ";
	
	
	private static final String CYPHER__GET_FORMULAS = "MATCH (" + CYPHER__RETURN_VALUE_TOKEN + ":Formula) RETURN " + CYPHER__RETURN_VALUE_TOKEN + CYPHER__ORDER_BY__CLAUSE;
	private static final String CYPHER__GET_MANUAL_DATAPOINTS = "MATCH (" + CYPHER__RETURN_VALUE_TOKEN + ":Manual_Datapoint) RETURN " + CYPHER__RETURN_VALUE_TOKEN;
	private static final String CYPHER__GET_READINGS = "MATCH (" + CYPHER__RETURN_VALUE_TOKEN + ":Reading) RETURN " + CYPHER__RETURN_VALUE_TOKEN;
	
	private static final String CYPHER__GET_PATHS_TO_FORMULA_DEPENDENCIES = "MATCH " + CYPHER__RETURN_VALUE_TOKEN + " = ()<-[:Depends_On]-(:Formula) RETURN DISTINCT " + CYPHER__RETURN_VALUE_TOKEN;
	private static final String CYPHER__GET_PATHS_TO_RULE__DEPENDENCIES_AND_ACTIONS = "MATCH " + CYPHER__RETURN_VALUE_TOKEN + " = (:RULE)-[*]->()-[finalRelationship]->() WHERE label(finalRelationship) = 'acts_' OR label(finalRelationship) = 'uses_' RETURN DISTINCT " + CYPHER__RETURN_VALUE_TOKEN;
	
//	private static final String SQL__GET_CURRENT_SEQUENCE_ID = "SELECT CURRVAL(" + SQL__SEQUENCE__TOKEN + ")";
	private static final String SQL__GET_NEXT_SEQUENCE_ID = "SELECT NEXTVAL('global_id_sequence')";
	
	@Autowired
	@Qualifier("agensJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
	
	// TODO: Make these (CREATE/DELETE/UPDATE) DAO calls TRANSACTIONAL
	
	@SuppressWarnings("unchecked")
	private <T extends PGobject> Collection<T> getGraphEntities(String cypher)
	{
		Collection<T> returnValue = new ArrayList<>();
		try {
			ColumnMapRowMapper rowMapper = new ColumnMapRowMapper();
			Collection<Map<String, Object>> graphEntities = jdbcTemplate.query(cypher, rowMapper);

			for (Map<String, Object> graphEntityMap: graphEntities) 
			{
				returnValue.add((T) graphEntityMap.get(CYPHER__RETURN_VALUE_TOKEN));
			}
//			return returnValue;
		} catch (BadSqlGrammarException bsqlge) {
			log.error(bsqlge.getMessage());
//			return null;
		}
		return returnValue;
	}
	
	/**
	 *  Get Paths to Formulas; each Path starts from a [ Reading | Manual_Datapoint | Formula ]-labeled AgensGraph Vertex
     * 	and terminates on a Formula-labeled Vertex 
     * 		i.e. some Formulas are dependencies in other Formulas.
     *
	 * @return List of Paths to a Formula.
	 */
	public Collection<Path> getPathsToFormulaDependencies()
	{
		return getGraphEntities(CYPHER__GET_PATHS_TO_FORMULA_DEPENDENCIES);
	}

	/**
	 *  Get Paths to Rules; each Path starts from a [ Reading | Manual_Datapoint | Formula ]-labeled AgensGraph Vertex
     * 	and terminates on a Rule-labeled Vertex
     *
	 * @return List of Paths to a Rule.
	 */
	public Collection<Path> getPathsToRuleDependenciesAndActions()
	{
		return getGraphEntities(CYPHER__GET_PATHS_TO_RULE__DEPENDENCIES_AND_ACTIONS);
	}

	/**
	 * @return List of all Formula vertices
	 */
	public Collection<Vertex> getAllFormulas() 
	{
		return getGraphEntities(CYPHER__GET_FORMULAS);
	}
	
	/**
	 * @return List of all Reading vertices
	 */
	public Collection<Vertex> getAllReadings() 
	{
		return getGraphEntities(CYPHER__GET_READINGS);
	}

	/**
	 * @return List of all ManualDatapoint vertices
	 */
	public Collection<Vertex> getAllManualDatapoints() 
	{
		return getGraphEntities(CYPHER__GET_MANUAL_DATAPOINTS);
	}


	public int insertDTO(DataTransferObject dto) 
	{
		int newRecordCount = 0;
		
		try
		{
			if (dto instanceof ReadingDTO)
			{
				newRecordCount = insertGraphEntity(CYPHER__CREATE_READING, dto);
			}
			else if (dto instanceof ManualDatapointDTO)
			{
				newRecordCount = insertGraphEntity(CYPHER__CREATE_MANUAL_DATAPOINT, dto);
			}
			else if (dto instanceof FormulaDTO)
			{
				newRecordCount = insertFormula((FormulaDTO) dto);
			}
		}
		catch (Throwable t)
		{
			log.error(t.getMessage());
		}
		
		if (newRecordCount < 1)
		{
			throw new IllegalStateException("Failed to INSERT DTO :: " + dto);
		}
		return newRecordCount;
		
	}

//	/**
//	 * TODO: Consolidate with insertDTO() i.e. MANUAL_DATAPOINT_EVENT & READING_EVENT are NOT implemented here.
//	 * 
//	 * @param dto
//	 * @return
//	 */
//	public int insertGraphEntityFromDTO(DataTransferObject dto) 
//	{
//		InstanceType type = InstanceType.fromInt(dto.getTypeId());
//		switch (type) 
//		{
//			case FORMULA_LISTENER:
//				return insertFormula((FormulaDTO) dto);
//			case MANUAL_DATAPOINT_EVENT:
//				return -666_666; // NOT implemented yet
//			case READING_EVENT:
//				return -666;
//			default:
//				break;
//		}
//		return 0;
//	}
	
	public String testMultipleCypherStatementsWithRespectToSequenceUse() 
	{
		// TODO: REVIEW: Not convinced this cannot be derailed by concurrent threads: 
		// (i.e. CURRVAL() should be reliable in same 'session' but not sure that consecutive execute()/queryForObject() IS same 'session'):
		jdbcTemplate.execute("CREATE (:FOOL { ImmutableName: (SELECT concat_ws('_', 'Some Fool With Generated ID', NEXTVAL('shard_1.global_id_sequence'))), id: (SELECT CURRVAL('shard_1.global_id_sequence'))} )");
		return jdbcTemplate.queryForObject("SELECT CURRVAL('shard_1.global_id_sequence');", Integer.class).toString();
		
		// No workee:
//		return jdbcTemplate.queryForObject("CREATE (:FOOL { ImmutableName: (SELECT concat_ws('_', 'Some Fool With Generated ID', NEXTVAL('shard_1.global_id_sequence'))), id: (SELECT CURRVAL('shard_1.global_id_sequence'))} ); SELECT CURRVAL('shard_1.global_id_sequence');", Integer.class).toString();
		
		// From: https://stackoverflow.com/a/1668361/888537
		// And: https://docs.spring.io/spring/docs/2.5.x/reference/jdbc.html#jdbc-auto-genereted-keys
//		
//		final String INSERT_SQL = "insert into my_test (name) values(?)";
//		final String name = "Rob";
//		KeyHolder keyHolder = new GeneratedKeyHolder();
//		jdbcTemplate.update(
//		    new PreparedStatementCreator() {
//		        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
//		            PreparedStatement ps =
//		                connection.prepareStatement(INSERT_SQL, new String[] {"id"});
//		            ps.setString(1, name);
//		            return ps;
//		        }
//		    },
//		    keyHolder);
//		// keyHolder.getKey() now contains the generated key
	}

	public int updateDTO(DataTransferObject dto) 
	{
		InstanceType type = InstanceType.fromInt(dto.getTypeId());
		switch (type) {
			case Formula:
				return updateFormula((FormulaDTO) dto);
			case ManualDatapoint:
				return updateGraphEntityById(CYPHER__UPDATE_MANUAL_DATAPOINT, dto.getId(), dto.getName());
			case Reading:
				return updateGraphEntityById(CYPHER__UPDATE_READING, dto.getId(), dto.getName());
			default:
				break;
			
		}
		return 0;
	}

	/**
	 * Compare a new DTO to the corresponding currently cached one, 
	 * build Cypher statements that update the persisted state of a Formula;
	 * and execute the constructed Cypher.
	 * 
	 * @param newDTO a DTO that exactly mirrors (other than the included new updates) the currently cached DTO for this Formula
	 * @return a numeric indication of the number of Formulas updated (should be 1 in success case)
	 */
	private int updateFormula(FormulaDTO newDTO) 
	{
		int id = newDTO.getId();
		FormulaDTO currentDTO = (FormulaDTO) MetadataCache.getDTOById(id);

		if (null != newDTO && null != currentDTO)
		{
			// TODO: Wrap all the Cypher statements in a transaction OR accumulate/execute all at once:
			StringBuilder cypher = new StringBuilder("MATCH (" + CYPHER__RETURN_VALUE_TOKEN + ":Formula { id: " + id + "}) ") ;
			
			// If the names don't match build: SET x.name = dto.getName()
			if (FormulaDTO.nameIsChanging(newDTO, currentDTO))
			{
				cypher.append("SET " + CYPHER__RETURN_VALUE_TOKEN + ".name = ? ");
			}
			// If the expressions don't match build: SET x.expression = dto.getExpression
			if (FormulaDTO.expressionIsChanging(newDTO, currentDTO))
			{
				cypher.append("SET " + CYPHER__RETURN_VALUE_TOKEN + ".expression = ? ");
			}
			// If the periods don't match build: SET x.period = dto.getPeriod
			if (FormulaDTO.periodIsChanging(newDTO, currentDTO))
			{
				cypher.append("SET " + CYPHER__RETURN_VALUE_TOKEN + ".period = ? ");
			}
			
//			if (FormulaService.formulaDependenciesHaveBeenProvided(newDTO))
//			{
				// If the dependencyIDs don't match: find the diffs and add/delete relationships/edges as appropriate:
				if (FormulaService.formulaDependenciesProvidedAreDifferentFromCurrent(newDTO, currentDTO))
				{
					log.trace("formulaDependenciesProvidedAreDifferentFromCurrent i.e. there ARE differences between the 2 lists.");

					// ALL of the 'to-be-created' Dependency IDs
					Set<Integer> newDependencyIdSet = Arrays.stream(newDTO.getDependencyIds()).boxed().collect(Collectors.toSet());
					Set<Integer> oldDependencyIdSet = Arrays.stream(currentDTO.getDependencyIds()).boxed().collect(Collectors.toSet());
					newDependencyIdSet.removeAll(oldDependencyIdSet);
					for (int newDependencyIdToAdd: newDependencyIdSet)
					{
						log.trace("ADD: " + newDependencyIdToAdd);
						String vertexLabel = getVertexLabelForDTO(MetadataCache.getDTOById(newDependencyIdToAdd));
						String cypherToAddDependency = "MATCH (d:" + vertexLabel + " { Id: " + newDependencyIdToAdd + " }) MATCH (f:Formula { Id: " + id + " }) CREATE (d)<-[:Depends_On]-(f)";
						jdbcTemplate.execute(cypherToAddDependency);
						log.trace("ADD: " + cypherToAddDependency);
					}
	
					// ALL of the 'to-be-deleted' Dependency IDs
					Set<Integer> newDependencyIdSet2 = Arrays.stream(newDTO.getDependencyIds()).boxed().collect(Collectors.toSet());
					Set<Integer> oldDependencyIdSet2 = Arrays.stream(currentDTO.getDependencyIds()).boxed().collect(Collectors.toSet());
					oldDependencyIdSet2.removeAll(newDependencyIdSet2);
					for (int oldDependencyIdToDelete: oldDependencyIdSet2)
					{
						log.trace("DELETE: " + oldDependencyIdToDelete);
						final String vertexLabel = getVertexLabelForDTO(MetadataCache.getDTOById(oldDependencyIdToDelete));
						String cypherToDeleteDependency = "MATCH (d:" + vertexLabel + " { Id: " + oldDependencyIdToDelete + " })-[r:Depends_On]-(f:Formula { id: " + id + " } ) DELETE r";
						jdbcTemplate.execute(cypherToDeleteDependency);
						log.trace("DELETE: " + cypherToDeleteDependency);
					}
				}
//			}

			try {
				PreparedStatement preparedStatement = jdbcTemplate.getDataSource().getConnection().prepareStatement(cypher.toString());
	
				int parameterIndex = 1;

				// If the names don't match build: SET x.name = dto.getName()
				if (FormulaDTO.nameIsChanging(newDTO, currentDTO))
				{
					preparedStatement.setString(parameterIndex++, newDTO.getName());
				}
				// If the expressions don't match build: SET x.expression = dto.getExpression
				if (FormulaDTO.expressionIsChanging(newDTO, currentDTO))
				{
					preparedStatement.setString(parameterIndex++, newDTO.getExpression());
				}
				// If the periods don't match build: SET x.period = dto.getPeriod
				if (FormulaDTO.periodIsChanging(newDTO, currentDTO))
				{
					preparedStatement.setInt(parameterIndex++, newDTO.getPeriod());
				}
				
				return preparedStatement.executeUpdate();
				
			} catch (Throwable t) {
				log.error(t.getMessage());
				return 0;
			}
		}
		return 0;
	}

	public int deleteDTO(DataTransferObject dto) 
	{
		InstanceType type = InstanceType.fromInt(dto.getTypeId());
		switch (type) {
			case Formula:
				return deleteGraphEntityById(CYPHER__DELETE_FORMULA, dto.getId());
			case ManualDatapoint:
				return deleteGraphEntityById(CYPHER__DELETE_MANUAL_DATAPOINT, dto.getId());
			case Reading:
				return deleteGraphEntityById(CYPHER__DELETE_READING, dto.getId());
			default:
				break;
			
		}
		return 0;
	}

	/**
	 * TODO: Use: Name: ?, TypeId: ?, ImmutableName: ?, Id: ? 
	 * @param cypher
	 * @param name
	 * @param typeId
	 * @return
	 */
	private int insertGraphEntity(String cypher, DataTransferObject dto)
	{
		return jdbcTemplate.update(
			    new PreparedStatementCreator() {
			        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
			            PreparedStatement ps = connection.prepareStatement(cypher);
			            ps.setString(1, dto.getName());
			            ps.setInt(2, dto.getTypeId());
			            ps.setInt(3, dto.getId());
			            return ps;
			        }
			    }
			);
	}

	private int updateGraphEntityById(String cypher, int id, String newName)
	{
		return jdbcTemplate.update(
			    new PreparedStatementCreator() {
			        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
			            PreparedStatement ps = connection.prepareStatement(cypher);
			            ps.setInt(1, id);
			            ps.setString(2, newName);
			            return ps;
			        }
			    }
			);
	}
	
	private int deleteGraphEntityById(String cypher, int id)
	{
		return jdbcTemplate.update(
			    new PreparedStatementCreator() {
			        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
			            PreparedStatement ps = connection.prepareStatement(cypher);
			            ps.setInt(1, id);
			            return ps;
			        }
			    }
			);
	}

	public int getNextId() 
	{
		return jdbcTemplate.queryForObject(SQL__GET_NEXT_SEQUENCE_ID, Integer.class);
	}

	/**
	 * i.e.
	 *  
	 *  MATCH (pcpDisplacement:Manual_Datapoint { Id: 7 })
	 *  MATCH (totalNetLift:Formula { Id: 6 })
	 *  CREATE (theoreticalPCPLiftTorque:Formula {
	 *		Name: 'Theoretical PCP Lift Torque',
	 *		ImmutableName: 'Formula_8',
	 *		Id: 8,
	 *		unitTypeId: 4,
	 *		typeId: 20,
	 *		Expression: '($6$ * $7$) / 125'
	 *  })
	 *  CREATE (pcpDisplacement)<-[:Depends_On]-(theoreticalPCPLiftTorque)
	 *  CREATE (totalNetLift)<-[:Depends_On]-(theoreticalPCPLiftTorque);
	 *  
	 * @param dto
	 * @return
	 */
	private int insertFormula(FormulaDTO dto) 
	{
		if (null != dto && dto.isReadyForUse())
		{
			StringBuilder cypher = new StringBuilder();
	
			int j = 0;
			for (DataTransferObject dependencyDTO: dto.getDependencies())
			{
				cypher.append("MATCH (d_" + j + ":" + getVertexLabelForDTO(dependencyDTO) + " { Id: ? }) ");
				j++;
			}
			cypher.append("CREATE (f:Formula { ");
			cypher.append("Name: ?, ");
			cypher.append("Id: ?, ");
//			cypher.append("unitTypeId: 4, "); TODO
			cypher.append("typeId: ?, ");
			cypher.append("Expression: ? ");
			cypher.append("}) ");
			for (int i = 0; i < dto.getDependencies().size(); i++)
			{
				cypher.append("CREATE (d_" + i + ")<-[:Depends_On]-(f) ");
			}
			log.debug(cypher);
			
			try {
				PreparedStatement preparedStatement = jdbcTemplate.getDataSource().getConnection().prepareStatement(cypher.toString());
	
				int parameterIndex = 1;
				for(DataTransferObject dependencyDTO: dto.getDependencies())
				{
					preparedStatement.setInt(parameterIndex++, dependencyDTO.getId());
				}
				preparedStatement.setString(parameterIndex++, dto.getName());
				preparedStatement.setInt(parameterIndex++, dto.getId());
//				preparedStatement.setInt(parameterIndex++, dto.getUnitTypeId()); TODO
				preparedStatement.setInt(parameterIndex++, dto.getTypeId());
				preparedStatement.setString(parameterIndex++, dto.getExpression());
				
				return preparedStatement.executeUpdate();
				
			} catch (SQLException e) {
				return 0;
			} catch (Throwable t) {
				return 0;
			}
		}
		return 0;
	}
	
	private String getVertexLabelForDTO(DataTransferObject dto)
	{
		return ((InstanceType.ManualDatapoint.getValue() == dto.getTypeId()) ? "Manual_Datapoint" : ((InstanceType.Reading.getValue() == dto.getTypeId()) ? "Reading" : "Formula"));
	}	
}
