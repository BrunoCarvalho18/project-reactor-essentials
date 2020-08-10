package br.com.projectreactor;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class MonoTest {
	
	/*
	 * 
	 * Reactive Streams
	 * 1. Asyncronous
	 * 2. Non-blocking
	 * 3. Backpressure
	 * Publisher <- (subscribe) Subscriber
	 * Subscription is created
	 * Publisher (onSuscribe with subscription) -> Subscriber
	 * Publisher -> (onNext) Subscriber
	 * until:
	 * 1. Publisher sends all the objects requested
	 * 2. Publisher sends all the objects it has. (onComplete) subscriber and subscription will be canceled
	 * 3. There is an error. (onError) -> subscriber and subscription will be canceled
	 */
	
	private final static Logger logger = LoggerFactory.getLogger(MonoTest.class);
	
	@Test
	public void MonoSubscriber() {
	 String name = "Bruno Carvalho";
	 Mono<String> mono = Mono.just(name)
			 .log();
	 
	 mono.subscribe();
	 logger.info("----------------------------------");
	 StepVerifier.create(mono)
	    .expectNext(name)
	    .verifyComplete();
	    
	 
	 logger.info("Mono {}",mono);
	 logger.info("Everything working as intended");
	}
	
	@Test
	public void MonoSubscriberConsumer() {
	 String name = "Bruno Carvalho";
	 Mono<String> mono = Mono.just(name)
			.log();
	
	 mono.subscribe(s->logger.info("Value {}",s));
	 logger.info("----------------------------------");
	 
	 StepVerifier.create(mono)
	    .expectNext(name)
	    .verifyComplete();
	}
	
	@Test
	public void monoSubscriberConsumerError() {
		String name= "Bruno  Carvalho";
		Mono<String> mono = Mono.just(name)
			.map(s-> {throw new RuntimeException("Testing mono with error");});
		
		mono.subscribe(s->logger.info("Name{}",s),s->logger.error("Something had happened"));
		mono.subscribe(s->logger.info("Name{}",s),Throwable::printStackTrace);
		
		logger.info("----------------------------------------------------------------------");
		
		StepVerifier.create(mono)
		   .expectError(RuntimeException.class)
		   .verify();
	}
	
	@Test
	public void monoSubscriberConsumerComplete() {
		String name= "Bruno Carvalho";
		Mono<String> mono = Mono.just(name)
				.log()
				.map(String::toUpperCase);
		
		mono.subscribe(s->logger.info("Value {}",s),
				Throwable::printStackTrace,
				()-> logger.info("FINISHED!"));
		
		logger.info("----------------------------------------------------------------------");
		
		
		StepVerifier.create(mono)
		  .expectNext(name.toUpperCase())
		  .verifyComplete();
	}
	
	@Test
	public void monoSubscriberConsumerSubscription() {
		String name= "Bruno Carvalho";
		Mono<String> mono = Mono.just(name)
				.log()
				.map(String::toUpperCase);
		
		mono.subscribe(s->logger.info("Value {}",s),
				Throwable::printStackTrace,
				()-> logger.info("FINISHED!"),
				Subscription::cancel);
		
		logger.info("----------------------------------------------------------------------");
		
		
		StepVerifier.create(mono)
		  .expectNext(name.toUpperCase())
		  .verifyComplete();
	}
	
	@Test
	public void monoDoOnMethods() {
		String name= "Bruno Carvalho";
		Mono<Object> mono = Mono.just(name)
				.log()
				.map(String::toUpperCase)
				.doOnSubscribe(subscription->logger.info("Subscribed"))
				.doOnRequest(longNumber-> logger.info("Request Received, starting doing something..."))
				.doOnNext(s->logger.info("Value is here. Executing doOnNext {}",s))
				.flatMap(s->Mono.empty())
				.doOnNext(s->logger.info("Value is here. Executing doOnNext {}",s))
				.doOnSuccess(s->logger.info("doOnSuccess executed"));
		
		mono.subscribe(s-> logger.info("Value {}",s),
				Throwable::printStackTrace,
				()-> logger.info("FINISHED!"));
		
		logger.info("------------------------------------------------------------------");
		
		mono.subscribe(s->logger.info("Value {}",s),
				Throwable::printStackTrace,
				()-> logger.info("FINISHED!"));
	}
	
	@Test
	public void monoOnErrorReturn() {
		String name = "Bruno Carvalho";
		Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal Argument exception"))
				.onErrorReturn("EMPTY")
				.onErrorResume(s->{
					logger.info("Inside on Error Resume");
					return Mono.just(name);
				})
				
				.doOnError(e-> MonoTest.logger.error("Error message:{}",e.getMessage()))
				.log();
		
		StepVerifier.create(error)
		 .expectNext("EMPTY")
		 .verifyComplete();
	}
	
	
	
}
