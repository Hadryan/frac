package com.srcnrg.frac.rest;

import com.srcnrg.frac.domain.dto.DataTransferObject;
import com.srcnrg.frac.domain.dto.FormulaDTO;
import com.srcnrg.frac.domain.dto.ManualDatapointDTO;
import com.srcnrg.frac.domain.dto.ReadingDTO;
import com.srcnrg.frac.services.FormulaService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
@Log4j2
public class FormulaHandler 
{
	@Autowired
    private FormulaService formulaService;

	public Mono<ServerResponse> deleteFormula(ServerRequest serverRequest) 
	{
		final int id = Integer.valueOf(serverRequest.pathVariable("id"));
		final Mono<FormulaDTO> monoFormulaDTO = Mono.just(FormulaDTO.builder().id(id).build());

	    return monoFormulaDTO
	        .flatMap(md -> ok()
	        				.body(
	        					fromPublisher(
	        							monoFormulaDTO
    									// TODO: Emit error status code (4xx) if IllegalArgumentException caught e.g. 
    									// when trying to delete an in-use Formula
	    								.flatMap(formulaService::deleteFormula)
	    								.doOnError(e -> log.error("doOnError: " + e))
	    								// NOTE: Temporary hack , return negative version of ID for error indication
	    								.onErrorReturn(FormulaDTO.builder().id(-1*id).build()), 
//	    								.onErrorReturn(null), 
//	    								.onErrorReturn(Mono.empty()), 
	    								FormulaDTO.class
	        					)
	        				)
	        				.switchIfEmpty(notFound().build()));
	}

	public Mono<ServerResponse> deleteManualDatapoint(ServerRequest serverRequest) 
	{
		final int id = Integer.valueOf(serverRequest.pathVariable("id"));
		final Mono<ManualDatapointDTO> monoManualDatapointDTO = Mono.just(ManualDatapointDTO.builder().id(id).build());

	    return monoManualDatapointDTO
	        .flatMap(md -> ok()
	        				.body(
	        					fromPublisher(
	    							monoManualDatapointDTO
    									// TODO: Emit error status code (4xx) if IllegalArgumentException caught e.g. 
    									// when trying to delete an in-use ManualDatapoint
	    								.flatMap(formulaService::deleteManualDatapoint)
	    								.doOnError(e -> log.error("doOnError: " + e))
	    								// NOTE: Temporary hack , return negative version of ID for error indication
	    								.onErrorReturn(ManualDatapointDTO.builder().id(-1*id).build()), 
//	    								.onErrorReturn(null), 
//	    								.onErrorReturn(Mono.empty()), 
//	    								.onErrorReturn(Mono.justOrEmpty(null)), 
	    							ManualDatapointDTO.class
	        					)
	        				)
	        				.switchIfEmpty(notFound().build()));
	}

	public Mono<ServerResponse> deleteReading(ServerRequest serverRequest) 
	{
		final int id = Integer.valueOf(serverRequest.pathVariable("id"));
		final Mono<ReadingDTO> monoReadingDTO = Mono.just(ReadingDTO.builder().id(id).build());

	    return monoReadingDTO
	        .flatMap(md -> ok()
	        				.body(
	        					fromPublisher(
	        							monoReadingDTO
	    								// TODO: Emit error status code (4xx) if IllegalArgumentException caught e.g. 
	    								// when trying to delete an in-use Reading
	    								.flatMap(formulaService::deleteReading)
	    								.doOnError(e -> log.error("doOnError: " + e))
	    								// NOTE: Temporary hack , return negative version of ID for error indication
	    								.onErrorReturn(ReadingDTO.builder().id(-1*id).build()), 
//	    								.onErrorReturn(null), 
//	    								.onErrorReturn(Mono.empty()), 
	    								ReadingDTO.class
	        					)
	        				)
	        				.switchIfEmpty(notFound().build()));
	}

	public Mono<ServerResponse> findAllFormulas(ServerRequest serverRequest) 
	{
	    return ok().body(fromPublisher(formulaService.findAllCachedFormulaDTOs(), FormulaDTO.class));
	}

	Mono<ServerResponse> findAllFormulasAndDependencies(ServerRequest serverRequest)
	{
	    return ok().body(fromPublisher(formulaService.findAllDTOs(), DataTransferObject.class));
	}

	public Mono<ServerResponse> findAllManualDatapoints(ServerRequest serverRequest) 
	{
		return ok()
    		.body(
    			fromPublisher(
    					formulaService.findAllCachedManualDatapointDTOs(), ManualDatapointDTO.class)
    			);
	}

	public Mono<ServerResponse> findAllReadings(ServerRequest serverRequest) 
	{
		return ok()
    		.body(
    			fromPublisher(
    					formulaService.findAllCachedReadingDTOs(), ReadingDTO.class)
    			);
	}
	
	Mono<ServerResponse> findCachedFormulaById(ServerRequest serverRequest)
	{
		final int id = Integer.valueOf(serverRequest.pathVariable("id"));
		final Mono<FormulaDTO> f = formulaService.findCachedFormulaDTOById(id);
	    return f
	        .flatMap(p -> ok().body(fromPublisher(f, FormulaDTO.class)))
	        .switchIfEmpty(notFound().build());
	}
	
	
	Mono<ServerResponse> findCachedManualDatapointById(ServerRequest serverRequest)
	{
		final int id = Integer.valueOf(serverRequest.pathVariable("id"));
		final Mono<ManualDatapointDTO> md = formulaService.findCachedManualDatapointDTOById(id);
	    return md
	        .flatMap(p -> ok().body(fromPublisher(md, ManualDatapointDTO.class)))
	        .switchIfEmpty(notFound().build());
	}

	Mono<ServerResponse> findCachedReadingById(ServerRequest serverRequest)
	{
		final int id = Integer.valueOf(serverRequest.pathVariable("id"));
		final Mono<ReadingDTO> r = formulaService.findCachedReadingDTOById(id);
	    return r
	        .flatMap(p -> ok().body(fromPublisher(r, ReadingDTO.class)))
	        .switchIfEmpty(notFound().build());
	}

	public Mono<ServerResponse> insertFormula(ServerRequest serverRequest) 
	{
		final Mono<FormulaDTO> monoFormulaDTO = serverRequest.bodyToMono(FormulaDTO.class);
//			return created(/* TODO: Find a way to get the DB-generated ID and return a correct path here */ 
//					UriComponentsBuilder.fromPath("formulas").build().toUri())
			return ok()
					.body(
						fromPublisher(
								monoFormulaDTO.flatMap(formulaService::insertFormula), FormulaDTO.class));
	}

	public Mono<ServerResponse> insertManualDatapoint(ServerRequest serverRequest) 
	{
        return ServerResponse
                .ok()
                .body(serverRequest.bodyToMono(ReadingDTO.class)
                		.flatMap(r -> this.formulaService.insertManualDatapoint(r.getName())), ManualDatapointDTO.class);
    }
	
	

	public Mono<ServerResponse> insertReading(ServerRequest serverRequest) // TODO: Revisit introducing this specificity i.e. return 201, but may throw URISyntaxException 
	{
        return ServerResponse
        		.ok()
//                .created(new URI("foo")) // TODO: Return a REAL URI incorporating the created ID
                .body(serverRequest.bodyToMono(ReadingDTO.class)
                		.flatMap(r -> this.formulaService.insertReading(r.getName())), ReadingDTO.class);
    }
	
	public Mono<ServerResponse> updateFormula(ServerRequest serverRequest) 
	{
		final int id = Integer.valueOf(serverRequest.pathVariable("id"));
		if (null == formulaService.findCachedFormulaDTOById(id)) 
		{
			log.warn("PUT Formula request made for non-existent Formula, please use POST instead.");
			// TODO: Send back a message too
			return notFound().build();
		}
		final Mono<FormulaDTO> monoFormulaDTO = serverRequest.bodyToMono(FormulaDTO.class);
		return ok().body(fromPublisher(monoFormulaDTO.flatMap(f -> this.formulaService.updateFormula(id, f)), FormulaDTO.class));
	}

	public Mono<ServerResponse> updateManualDatapoint(ServerRequest serverRequest) 
	{
		final int id = Integer.valueOf(serverRequest.pathVariable("id"));
        return ServerResponse
                .ok()
                .body(serverRequest.bodyToMono(ManualDatapointDTO.class)
                		.flatMap(r -> this.formulaService.updateManualDatapoint(id, r.getName())), 
                		ManualDatapointDTO.class);
    }

	public Mono<ServerResponse> updateManualDatapointValue(ServerRequest serverRequest) 
	{
		final int id = Integer.valueOf(serverRequest.pathVariable("id"));
		if (null == formulaService.findCachedManualDatapointDTOById(id)) 
		{
			log.warn("PUT ManualDatapoint request made for non-existent ManualDatapoint, please use POST instead.");
			return notFound().build();
		}

		final int value = Integer.valueOf(serverRequest.pathVariable("value"));
		final Mono<ManualDatapointDTO> dtoMono = Mono.just(ManualDatapointDTO.builder().id(id).value(value).build());
	    
		return ok()
				.body(
					fromPublisher(
							dtoMono.flatMap(formulaService::updateManualDatapointValue), ManualDatapointDTO.class)
					)
				;
	}
	
	public Mono<ServerResponse> updateReading(ServerRequest serverRequest) 
	{
		final int id = Integer.valueOf(serverRequest.pathVariable("id"));
        return ServerResponse
                .ok()
                .body(serverRequest.bodyToMono(ReadingDTO.class)
                		.flatMap(r -> this.formulaService.updateReading(id, r.getName())), 
                		ReadingDTO.class);
    }
	
	public Mono<ServerResponse> updateReadingValue(ServerRequest serverRequest) 
	{
		final int id = Integer.valueOf(serverRequest.pathVariable("id"));
		if (null == formulaService.findCachedReadingDTOById(id)) 
		{
			log.warn("PUT Reading request made for non-existent Reading, please use POST instead.");
			return notFound().build();
		}

		final int value = Integer.valueOf(serverRequest.pathVariable("value"));
		final Mono<ReadingDTO> readingMono = Mono.just(ReadingDTO.builder().id(id).value(value).build());
	    
		return ok()
				.body(
					fromPublisher(
							readingMono.flatMap(formulaService::updateReadingValue), ReadingDTO.class)
					)
				;
	}
}
