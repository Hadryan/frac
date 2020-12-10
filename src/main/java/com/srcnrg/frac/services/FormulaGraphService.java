package com.srcnrg.frac.services;

import com.srcnrg.frac.domain.InstanceType;
import com.srcnrg.frac.domain.cache.FormulaCache;
import com.srcnrg.frac.domain.cache.MetadataCache;
import com.srcnrg.frac.domain.dto.DataTransferObject;
import com.srcnrg.frac.domain.dto.FormulaDTO;
import com.srcnrg.frac.domain.dto.ManualDatapointDTO;
import com.srcnrg.frac.domain.dto.ReadingDTO;
import com.srcnrg.frac.repo.AgensDAO;
import lombok.extern.log4j.Log4j2;
import net.bitnine.agensgraph.graph.Path;
import net.bitnine.agensgraph.graph.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.srcnrg.frac.domain.InstanceType.TYPE_ID_PROPERTY_NAME;

/**
 * Translate between AgensGraph database objects & JVM-resident Formulas and their dependencies.
 */
@Service
@Log4j2
public class FormulaGraphService 
{
	private static final String EMPTY = "";
	private static final String EXPRESSION = "expression";
	private static final String ID = "id";
	private static final String IMMUTABLE_NAME = "immutablename";
	private static final String NAME = "name";
	private static final String PERIOD = "period";
	private static final String REGEX_FOR_WHITESPACE = "\\s+";

	@Autowired
	FormulaCache formulaCache;
	
    @Autowired
    private AgensDAO dao;
    
    Collection<Path> pathsBetweenFormulasAndTheirDependencies;
    private Collection<Vertex> formulaVertices;
	
	/**
	 * (Graph) Paths go from a Formula to a dependency i.e. a ManualDatapoint, a Reading or another Formula.
	 * 
	 * @param formulaId
	 * @param eventType
	 * @return an array of names of Formula dependencies
	 */
	public String[] getImmutableNamesOfEvents(final String formulaId, InstanceType eventType)
	{
		return pathsBetweenFormulasAndTheirDependencies
		        .stream()
		        // Each Path will have only a single Edge; it's 'start' (GraphId) MUST == formulaId for it to be a candidate for use here.
		        .filter((p)-> p.edges().iterator().next().getStartVertexId().toString().equals(formulaId))
		        // Each Path will have only 2 Vertices (i.e. a Formula and a dependency [one of Reading, ManualDatapoint or another Formula)
		        	//the first one with the 'gid' (i.e. GraphId) MUST != formulaId for it to be a candidate for use here.
		        .filter((p)-> !p.vertices().iterator().next().getVertexId().toString().equals(formulaId))
		        // The first Vertex MUST have the TypeId property equal to the the specified DependencyEventType
		        .filter((p)-> p.vertices().iterator().next().getInt(TYPE_ID_PROPERTY_NAME) == eventType.getValue())
		        .peek(log::trace)
		        .map((p)-> p.vertices().iterator().next().getProperties().getString(IMMUTABLE_NAME).replaceAll(REGEX_FOR_WHITESPACE,EMPTY))
		        .collect(Collectors.toList())
		        .toArray(new String[0]);
	}
	
	private List<Vertex> getFormulas(Collection<Path> pathsToFormulas) 
	{
		List<Vertex> returnValue = new ArrayList<>();
		for (Path pathToFormula: pathsToFormulas) 
		{
			Vertex lastVertex = FormulaGraphService.getLastVertexInPath(pathToFormula);
			if (!returnValue.contains(lastVertex)) 
			{
				returnValue.add(lastVertex);
			}
		}
		returnValue.sort(Comparator.comparingInt((Vertex v) -> v.getProperties().getInt("id")));
		return returnValue;
	}
	
	private String getFormulaVertexId(Vertex formula) {
		return formula.getVertexId().toString();
	}

	private void loadFormulasFromStorage() 
	{
		pathsBetweenFormulasAndTheirDependencies = dao.getPathsToFormulaDependencies();
        formulaVertices = getFormulas(pathsBetweenFormulasAndTheirDependencies);
	}

	public void cacheMetadataForAllFormulas() 
	{
		loadFormulasFromStorage();
		
		for (Vertex graphFormula: formulaVertices) 
		{
			final String formulaVertexId = getFormulaVertexId(graphFormula);
			Map<Integer, DataTransferObject> dependenciesById = new HashMap<>();
			
			// Add Reading dependencies, if any:
			for (Integer readingId: getIdsFromVertexByType(formulaVertexId, InstanceType.Reading))
			{
				dependenciesById.put(readingId, MetadataCache.getDTOById(readingId));
			}

			// Add ManualDatapoint dependencies, if any:
			for (Integer manualDatapointId: getIdsFromVertexByType(formulaVertexId, InstanceType.ManualDatapoint))
			{
				dependenciesById.put(manualDatapointId, MetadataCache.getDTOById(manualDatapointId));
			}

			// Add Formula dependencies, if any:
			for (Integer id: getIdsFromVertexByType(formulaVertexId, InstanceType.Formula))
			{
				dependenciesById.put(id, MetadataCache.getDTOById(id));
			}
			
			MetadataCache.addDTO(
									FormulaDTO
										.builder()
											.name(graphFormula.getProperties().getString(NAME))
											.id(graphFormula.getProperties().getInt(ID))
											.typeId(graphFormula.getProperties().getInt(DataTransferObject.TYPE_ID_PROPERTY_NAME))
											.expression(graphFormula.getProperties().getString(EXPRESSION))
											.period((graphFormula.getProperties().containsKey(PERIOD)) ? graphFormula.getProperties().getInt(PERIOD) : 0)
											.dependencyDTOs(dependenciesById)
											.value(Double.NaN)
										.build());
		}
	}
	
	private Collection<Integer> getIdsFromVertexByType(String formulaVertexId, InstanceType eventType) 
	{
		
		return pathsBetweenFormulasAndTheirDependencies
		        .stream()
		        // Each Path will have only a single Edge; it's 'start' (GraphId) MUST == formulaVertexId
		        .filter((p)-> p.edges().iterator().next().getStartVertexId().toString().equals(formulaVertexId))
		        // Each Path will have only 2 Vertices (i.e. a Formula and a dependency [one of: Reading, ManualDatapoint or another Formula])
	        	// The first Vertex's ID MUST != formulaVertexId
		        .filter((p)-> !p.vertices().iterator().next().getVertexId().toString().equals(formulaVertexId))
		        // The first Vertex MUST have the TypeId property equal to the the specified InstanceType's value
		        .filter((p)-> p.vertices().iterator().next().getInt(TYPE_ID_PROPERTY_NAME) == eventType.getValue())
		        .map((p)-> p.vertices().iterator().next().getProperties().getInt(ID))
		        .collect(Collectors.toList());
	}

	public Collection<? extends ReadingDTO> findAllReadings() 
	{
		List<ReadingDTO> dtos = new ArrayList<>();
		Collection<Vertex> graphReadings = dao.getAllReadings();
		
		for (Vertex graphReading: graphReadings) 
		{
			dtos.add(ReadingDTO
						.builder()
							.name(graphReading.getProperties().getString(NAME))
							.id(graphReading.getProperties().getInt(ID))
							.typeId(graphReading.getProperties().getInt(DataTransferObject.TYPE_ID_PROPERTY_NAME))
							.value(Double.NaN)
						.build()
			);
		}
		
		return dtos;
	}

	public Collection<? extends ManualDatapointDTO> findAllManualDatapoints() 
	{
		List<ManualDatapointDTO> dtos = new ArrayList<>();
		Collection<Vertex> graphManualDatapoints = dao.getAllManualDatapoints();
		
		for (Vertex graphManualDatapoint: graphManualDatapoints) 
		{
			dtos.add(ManualDatapointDTO
						.builder()
							.name(graphManualDatapoint.getProperties().getString(NAME))
							.id(graphManualDatapoint.getProperties().getInt(ID))
							.typeId(graphManualDatapoint.getProperties().getInt(DataTransferObject.TYPE_ID_PROPERTY_NAME))
							.value(Double.NaN)
						.build()
			);
		}
		
		return dtos;
	}

	public String testMultipleCypherStatementsWithRespectToSequenceUse() 
	{
		return dao.testMultipleCypherStatementsWithRespectToSequenceUse();
	}

	/**
	 * Gather GraphEntity (i.e. Readings, ManualDatapoints, Formulas)
	 * metadata from the Graph datastore and cache it in a Collection of DTOs
	 * for use in subsequent Formula instantiation and manipulation via REST.
	 */
	public void cacheMetadataForAllReadingsManualDatapointsAndFormulas()
	{
		cacheMetadataForAllReadings();
		cacheMetadataForAllManualDatapoints();
		cacheMetadataForAllFormulas();
	}

	private void cacheMetadataForAllManualDatapoints() 
	{
		Collection<? extends ManualDatapointDTO> metadataForAllManualDatapoints = findAllManualDatapoints();
		for (ManualDatapointDTO dto: metadataForAllManualDatapoints)
		{
			MetadataCache.addDTO(dto);
		}
	}

	private void cacheMetadataForAllReadings() 
	{
		Collection<? extends ReadingDTO> metadataForAllReadings = findAllReadings();
		for (ReadingDTO dto: metadataForAllReadings)
		{
			MetadataCache.addDTO(dto);
		}
	}

	@SuppressWarnings("unchecked")
	public void buildAndCacheFormulaInstances() 
	{
		cacheMetadataForAllReadingsManualDatapointsAndFormulas();
		Collection<? extends DataTransferObject> metadataForAllFormulas = MetadataCache.getDTOsByType(InstanceType.Formula);
        formulaCache.cacheNewInstances((List<FormulaDTO>) metadataForAllFormulas);
	}

	public int deleteDTO(DataTransferObject dto) 
	{
		return dao.deleteDTO(dto);		
	}

	public int updateGraphEntityFromDTO(DataTransferObject dto) 
	{
		return dao.updateDTO(dto);	
	}

	public int insertGraphEntityFromDTO(DataTransferObject dto) 
	{
		return dao.insertDTO(dto);
	}

	/**
	 * Get an unique (shard-specific?) ID for use when creating a new Reading, ManualDatapoint, Formula or Rule.
	 * 
	 * @return the next value from the ID generator housed in the DB.
	 */
	public int getNextId() 
	{
		return dao.getNextId();
	}

	/**
	 * TODO: Migrate this method to a more central object - it is called from both Formula AND Rule GraphService classes.
	 * 
	 * @param path
	 * @return
	 */
	public static Vertex getLastVertexInPath(Path path) 
	{
		Vertex lastVertexInPath = null;
		for (Vertex v : path.vertices()) 
		{
			lastVertexInPath = v;
		}
		log.trace(lastVertexInPath.getLabel());
		return lastVertexInPath;
	}
}
