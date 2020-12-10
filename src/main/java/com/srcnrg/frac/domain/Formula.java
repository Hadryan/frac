package com.srcnrg.frac.domain;

import com.srcnrg.frac.domain.cache.FormulaCache;
import com.srcnrg.frac.domain.cache.MetadataCache;
import com.srcnrg.frac.domain.dto.DataTransferObject;
import com.srcnrg.frac.domain.dto.FormulaDTO;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.PrimitiveElement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

/*
 * (Code-generated) Formulas' UPDATEs using: CompilerUtils.CACHED_COMPILER.loadFromJava(canonicalClassName, javaCode.replaceAll(REGEX_EMPTY_LINE_DETECTOR, ""))...
 * do not cause a runtime failure but NEITHER do they update the in-memory Class (so e.g. the old Expression or dependencies may still be in-play) used
 * (by CachedCompiler) in creating any new instances of a FormulaListener i.e. emitting dependency Events causes the old/original Expression to be used.
 * 
 * New versions of Java Classes are primarily introduced to the JVM by additional non-'System' ClassLoaders, a Class definition CANNOT be simply replaced 
 * without some byte-code manipulation or other runtime approach e.g. jdb
 * 
 * The CachedCompiler by-passes attempts to re-compile an existing Class (this COULD be worked around if we forked the OpenHFT project)
 * but a new ClassLoader is required anyway to introduce the new Class definition to the JVM; UNFORTUNATELY, this new ClassLoader would also HAVE TO
 * be used to recompile/instantiate/load ALL the downstream AND upstream dependencies for the current Event-ing system to work.
 * 
 * 	- Using an always-new ClassLoader instance fixes (failed) updates to Formulas (e.g. Expressions) BUT fails Event handling
 * 		(i.e. they are never responded to, due to the use of different ClassLoaders for Formulas and their dependencies)
 * 	- Re-using a particular ClassLoader instance fixes (failed) Event handling but fails to update Formulas' Expressions
 * 
 * Possible workarounds (that support the current design i.e. code generation & Event dispatching):
 * 
 * 	- fork the OpenHFT project to allow class re-compilation 
 * 		and, possibly, disk usage (for use by some of the following tools) 
 * 		PLUS (for class reloading, one of):
 * 
 * 		- JRebel ($$$)
 * 		- RelProxy (free)
 * 		- DCE VM (free, VM patch, uses JDWP)
 * 		- OSGi (re-arch part of app)
 * 		- GroovyLoader (Groovy stacktraces are not fun)
 * 		- jdb (CLI, can use JDWP)
 * 		- JDK's Instrumentation API (free)
 * 		- SpringLoaded (free, works with disk files)
 * 
 * DCE VM is promising and has JDK v11 binaries available at: https://github.com/TravaOpenJDK/trava-jdk-11-dcevm
 * but has a smaller feature set than JRebel (https://www.reddit.com/r/java/comments/47f131/anyone_using_dcevm_instead_of_jrebel/)
 * See: https://www.researchgate.net/profile/Thomas_Wuerthinger/publication/221303100_Dynamic_code_evolution_for_Java/links/54d369d80cf25017918214d1/Dynamic-code-evolution-for-Java.pdf
 * 
 * SpringLoaded is used by the Grails project and is here: https://github.com/spring-projects/spring-loaded
 * Also, from that page: 
 * 	'if you have existing caches in your system that stash reflective information (AnnotationEventManager?) 
 * 		assuming it never changes, those will need to be cleared after a reload'
 * 
 * The aforementioned choices seem a little heavy-handed and dangerous to implement/support in PROD environments.
 * 
 * You can explore the application's heap at runtime with the 'Java VisualVM' tool [supplied by the JDK(?)] by checking out from git
 * the point-in-time-branch of this project: 'addRESTcrudWithCodeGeneration'
 * 
 * 
 * An ALTERNATIVE design may be a safer/better choice; PROPOSED/IMPLEMENTED/TESTED/EXTENDED (to encompass Rules too):
 * 
 * 	- Keep the DTOs as they are now; they are convenient wrt/(un)marshalling (to/from HTTP/AgensGraph) and contain all you need to know about Formulas.
 * 		- Possibly, introduce an FormulaUpdateDTO that carries before/after structural changes to a Formula e.g. Expression and/or dependencies
 * 
 * 	- The current Event management essentially uses reflection on generated code to build-up data structures that can be used to delegate changes
 * 		to Formula and/or dependency values to the appropriate parties/listeners. Replace this mechanism.
 * 
 * 	- The current design uses code generation; this is no longer necessary - discard this feature.
 * 
 * 	- Implement (in FormulaService?) mechanisms that leverage (new) data structures that map between dependencies and Formulas e.g.
 * 
 * 		- Scenario: Dependency value change:
 * 			- Reading_1 changes it's value to 10
 * 			- Lookup Formula(s) that carry Reading_1 as a dependency and evaluate/calculate their Expression(s) w/new dependency value
 * 				- For each Formula (e.g. Formula_6) that is affected, lookup Formula(s) that carry Formula_6 as a dependency and evaluate/calculate their Expression(s) w/new dependency value
 * 					- ad infinitum
 * 
 * 		- Scenario: Formula Expression change:
 * 			- Formula_6 changes it's Expression String WITHOUT adding/deleting dependencies
 * 			- Recreate Formula_6's Expression(s) w/new String & existing dependencies
 * 
 * 		- Scenario: Formula Expression change:
 * 			- Formula_6 changes it's Expression String, ADDing dependencies
 * 			- Change Formula_6's list of dependencies
 * 			- Recreate Formula_6's Expression(s) w/new String & new dependencies
 * 
 * 		- Scenario: Formula Expression change:
 * 			- Formula_6 changes it's Expression String, DELETing dependencies
 * 			- Change Formula_6's list of dependencies
 * 			- Recreate Formula_6's Expression(s) w/new String & new dependencies
 * 
 * 		- Scenario: Formula Expression change:
 * 			- Formula_6 changes it's Expression String, BOTH adding & deleting dependencies
 * 			- Change Formula_6's list of dependencies
 * 			- Recreate Formula_6's Expression(s) w/new String & new dependencies
 * 
 * 		mxParser implementation example:
 * 
 * 			List args = new ArrayList();
 * 			args.add(new Argument("r_1", 10));
 * 			args.add(new Argument("md_2", 100));
 * 			Expression expr = new Expression("5 * r_1 + md_2", (PrimitiveElement[]) args.toArray(new Argument[args.size()]));
 * 			expr.checkSyntax();
 * 			log.info(expr.calculate());
 * 
 * The above proposal is somewhat naive? e.g. rather than implementing the cascading changes procedurally it would be preferable to do it more declaratively i.e. re-introduce Event Listeners
 * Of course, discarding the aforementioned elements from the current design robs us of the experience of more fully exploring the possibilities & hazards of Java code generation & lifecycle management 8^)
 * 
 */
@Data
@Log4j2
public class Formula implements Comparable<Formula>
{
	/**
	 * Based upon the type of a DTO derive a name, that is used in the mxParser Expression of a Formula.
	 * 
	 * @param dto a DTO to derive a name for
	 * @return Argument name that is comprised of a type identifier [ f_ | md_ | r_ ] and an (Integer) ID.
	 */
	public static String getArgumentName(DataTransferObject dto)
	{
		return ((InstanceType.ManualDatapoint.getValue() == dto.getTypeId()) ? "md_" : ((InstanceType.Reading.getValue() == dto.getTypeId()) ? "r_" : "f_")) + dto.getId();
	}
	
	/**
	 * Utility method for logging.
	 * 
	 * @param f the Formula to use in deriving a (trace) log prefix
	 * @return a (trace) log prefix
	 */
	private static String getPrefixForTraceLogging(Formula f) 
	{
		return log.isTraceEnabled() ? (f.getClass().getCanonicalName() + " :: " + f.id + " :: ") : "";
	}
	
	/**
	 * Utility method for logging.
	 * 
	 * @param f the Formula to use in (trace) logging mxParser Expression Argument values
	 * @param arg the Formula's mxParser Expression Argument to log
	 * @param value the new value of the Argument, to be logged
	 */
	private static void traceArgumentUpdate(Formula f, Argument arg, double value) 
	{
		if (log.isTraceEnabled()) 
		{
			final String logPrefix = getPrefixForTraceLogging(f) ; 
			log.trace(logPrefix + " Argument :: " + arg.getArgumentName() + " was updated to the value " + arg.getArgumentValue());
			int argCount = f.expression.getArgumentsNumber();
			log.trace(logPrefix + " Expression: {}.", f.expression.getExpressionString());
			log.trace(logPrefix + " Expression arg count: {}.", argCount);
			log.trace(logPrefix + " Expression Argument(s):");
			for (int i = 0; i < argCount; i++) 
			{
				log.trace(logPrefix +  "\t\t [{}] :: name: {} :: value: {}", i, f.expression.getArgument(i).getArgumentName(), f.expression.getArgument(i).getArgumentValue());
			}
			
			// This one-liner:
			//	- exposes the runtime state of Reading, ManualDatapoint and Formula Expression arguments for inspection via a REST interface
			// 	- is a feature no one asked for
			//	- is a handy diagnostic
			MetadataCache.updateDTOValue(arg, value);
		}
	}
	
	/**
	 * Utility method for logging.
	 * 
	 * @param f the Formula to use in (trace) logging the new calculated value
	 * @param value the new calculated value of the Formula, to be logged
	 */
	private static void traceFormulaCalculationResult(Formula f, double value)
	{
		if (log.isTraceEnabled()) 
		{
			log.trace(getPrefixForTraceLogging(f) + " Calculated  :: " + value);
			// This one liner:
			//	- exposes the runtime state of Formula calculation results for inspection via a REST interface
			// 	- is a feature no one asked for
			//	- is a handy diagnostic
			MetadataCache.updateFormulaDTOValue(f, value);
		}
	}
	
	private Integer id;
	private Expression expression;
	private List<Argument> expressionArguments = new ArrayList<>();
	private final ConcurrentSkipListMap<Integer, Argument> expressionArgumentsById = new ConcurrentSkipListMap<>();
	private String name;
	private Integer period = 0;

    public Formula(FormulaDTO dto)
	{
		if (null == dto.getExpression() || null == dto.getDependencies() || dto.getDependencies().isEmpty())
		{
			throw new IllegalArgumentException("A Formula REQUIRES an expression (specifying dependency IDs) AND a list of dependency IDs.");
		}
		
		this.name = dto.getName();
		this.id = dto.getId();
		this.period = dto.getPeriod();
		
		for (DataTransferObject dependency: dto.getDependencies())
		{
			Argument arg = new Argument(Formula.getArgumentName(dependency));
			// Overload the Argument 'description' field to hold the ID of the dependency (used by FormulaCache):
			arg.setDescription("" + dependency.getId());
			expressionArguments.add(arg);
			expressionArgumentsById.put(dependency.getId(), arg);
		}
		expression = new Expression(dto.getCompilableExpression(), (PrimitiveElement[]) expressionArguments.toArray(new Argument[expressionArguments.size()]));

		if (!expression.checkSyntax()) 
		{
			log.fatal(this.name + " has invalid Expression syntax :: " + this.expression.getExpressionString()); 
			throw new IllegalArgumentException(this.expression.getErrorMessage());
		}
	}
    
    /**
     * Called from Runnable (when this is an instance of ScheduledFormula) or updateExpressionArgumentValue()
     */
    public void calculate()
    {
		double calculatedValue = expression.calculate();
		FormulaCache.broadcastCalculatedValue(getId(), calculatedValue);
		traceFormulaCalculationResult(this, calculatedValue);
    }

    /**
     * Supports inclusion of Formulas in Collections
     */
    @Override
    public int compareTo(Formula f) 
    {
    	return Integer.compare(this.id, f.id);
    }

	/**
	 * Update the specified Argument value iff the new value is different from the old.
	 * 
	 * @param id
	 * @param newValue
	 */
	public void updateExpressionArgumentValue(int id, double newValue)
    {
    	if (expressionArgumentsById.containsKey(id))
    	{
    		Argument arg = expressionArgumentsById.get(id);
    		double oldValue = arg.getArgumentValue();
    		
    		if (oldValue != newValue)
    		{
        		if (!Double.isNaN(newValue))
        		{
		    		arg.setArgumentValue(newValue);
		    		traceArgumentUpdate(this, arg, newValue);
		
		        	if (this.period <= 0)
		        	{
		        		this.calculate();
		        	}
        		}
    		}
    	}
    }
}
