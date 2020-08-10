package br.com.projectreactor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

public class OperatorsTest {
	
	
	private final static Logger logger = LoggerFactory.getLogger(OperatorsTest.class);
	
	@Test
	public void subscribeOnSimple() {
		
		Flux<Integer>flux=Flux.range(1, 4)
		.map(i->{
			logger.info("Map 1 - Number {} or Thread {}",i, Thread.currentThread().getName());
			return i;
		})
		.subscribeOn(Schedulers.single())
		.map(i->{
			logger.info("Map 2 - Number {} or Thread {}",i, Thread.currentThread().getName());
			return i;
		});
		
		StepVerifier.create(flux)
		 .expectSubscription()
		 .expectNext(1,2,3,4)
		 .verifyComplete();
		
	}
	
	
	@Test
	public void publishOnSimple() {
		
		Flux<Integer>flux=Flux.range(1, 4)
		.map(i->{
			logger.info("Map 1 - Number {} or Thread {}",i, Thread.currentThread().getName());
			return i;
		})
		.subscribeOn(Schedulers.boundedElastic())
		.map(i->{
			logger.info("Map 2 - Number {} or Thread {}",i, Thread.currentThread().getName());
			return i;
		});
		
		StepVerifier.create(flux)
		 .expectSubscription()
		 .expectNext(1,2,3,4)
		 .verifyComplete();
		
	}
	
	@Test
	public void multiplePublishOnSimple() {
		
		Flux<Integer> flux = Flux.range(1, 4)
				.publishOn(Schedulers.single())
				.map(i->{
					logger.info("Map 1 - Number {} on Thread {}",i, Thread.currentThread().getName());
					return i;
				})
				.publishOn(Schedulers.boundedElastic())
				.map(i->{
					logger.info("Map 1 - Number {} on Thread {}",i,Thread.currentThread().getName());
					return i;
				});
		
		StepVerifier.create(flux)
		    .expectSubscription()
		    .expectNext(1,2,3,4)
		    .verifyComplete();
	}
	
	@Test
	public void subscribeOnIO() {
		Mono<List<String>> lista = Mono.fromCallable(()-> Files.readAllLines(Path.of("text-file")))
				.log()
				.subscribeOn(Schedulers.boundedElastic());
				
		StepVerifier.create(lista)
		.expectSubscription()
		.thenConsumeWhile(l->{
			Assertions.assertFalse(l.isEmpty());
			logger.info("Size {}",l.size());
			return true;
		})
		.verifyComplete();
	}
	
	@Test
	public void switchIfEmptyOperator() {
		Flux<Object> flux = emptyFlux()
				.switchIfEmpty(Flux.just("not empty anymore"))
				.log();
		
		StepVerifier.create(flux)
		        .expectSubscription()
		        .expectNext("not empty anymore")
		        .expectComplete()
		        .verify();
	}
	
	@Test
	public void deferOperator() throws Exception {
		Mono<Long> just = Mono.just(System.currentTimeMillis());
		Mono<Long> defer = Mono.defer(()->Mono.just(System.currentTimeMillis()));
		
		just.subscribe(l->logger.info("time {}",l));
		Thread.sleep(100);
		just.subscribe(l->logger.info("time {}",l));
		Thread.sleep(100);
		just.subscribe(l->logger.info("time {}",l));
		Thread.sleep(100);
		
		just.subscribe(l->logger.info("time {}",l));
		
		AtomicLong atomicLong = new AtomicLong();
		defer.subscribe(atomicLong::set);
		Assertions.assertTrue(atomicLong.get() > 0);
		
	}


	private Flux<Object> emptyFlux() {
		
		return Flux.empty();
	}
	
	

}
