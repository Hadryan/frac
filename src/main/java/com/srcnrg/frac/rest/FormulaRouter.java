package com.srcnrg.frac.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class FormulaRouter {
	
    @Bean
    public RouterFunction<ServerResponse> routes(FormulaHandler handler) {

        return RouterFunctions
        		// Integration-like JUnits tests currently use this endpoint:
        		.route(GET("/all").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::findAllFormulasAndDependencies)
	            
//	            .andRoute(PUT("/event/formula/{id}/{value}").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::fireFormulaEvent)
	            .andRoute(PUT("/event/manualDatapoint/{id}/{value}").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::updateManualDatapointValue)
	            .andRoute(PUT("/event/reading/{id}/{value}").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::updateReadingValue)

	            .andRoute(GET("/formulas").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::findAllFormulas)
	            .andRoute(POST("/formula").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::insertFormula)
	            .andRoute(PUT("/formula/{id}").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::updateFormula)
	            .andRoute(DELETE("/formula/{id}").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::deleteFormula)
	            .andRoute(GET("/formula/{id}").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::findCachedFormulaById)
	            
	            .andRoute(GET("/manualDatapoints").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::findAllManualDatapoints)
	            .andRoute(POST("/manualDatapoint").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::insertManualDatapoint)
	            .andRoute(PUT("/manualDatapoint/{id}").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::updateManualDatapoint)
	            .andRoute(GET("/manualDatapoint/{id}").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::findCachedManualDatapointById)
	            .andRoute(DELETE("/manualDatapoint/{id}").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::deleteManualDatapoint)
	            
	            .andRoute(GET("/readings").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::findAllReadings)
	            .andRoute(PUT("/reading/{id}").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::updateReading)
	            .andRoute(GET("/reading/{id}").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::findCachedReadingById)
	            .andRoute(DELETE("/reading/{id}").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::deleteReading)
	            .andRoute(POST("/reading").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::insertReading)
	            // TODO: REVIEW: 
	            //	If we were to choose to return a 201 (created) to callers then a GET URI must be returned
	            //		which can throw a syntax Exception when constructed; this is how we catch it...
	            //
				//	            		request -> {
				//							try {
				//								return handler.insertReading(request);
				//							} catch (URISyntaxException e) {
				//								// TODO Auto-generated catch block
				//								e.printStackTrace();
				//							}
				//							return null;
				//						})
	            ;
    }
}
