package br.com.projectreactor;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class FluxTest {

	private final static Logger logger = LoggerFactory.getLogger(FluxTest.class);

	@Test
	public void fluxSubscriber() {
		Flux<String> fluxString = Flux.just("Bruno", "Carvalho", "DevDojo", "Academy").log();

		StepVerifier.create(fluxString).expectNext("Bruno", "Carvalho", "DevDojo", "Academy").verifyComplete();
	}

	@Test
	public void fluxSubscriberNumbers() {
		Flux<Integer> fluxNumbers = Flux.range(1, 5).log();

		fluxNumbers.subscribe(i -> logger.info("Number {}", i));

		StepVerifier.create(fluxNumbers).expectNext(1, 2, 3, 4, 5).verifyComplete();

	}

	@Test
	public void fluxSubscriberNumbersError() {
		Flux<Integer> flux = Flux.range(1, 5).log().map(i -> {
			if (i == 4) {
				throw new IndexOutOfBoundsException("index error");
			}
			return i;
		});

		flux.subscribe(i -> logger.info("Number {}", i), Throwable::printStackTrace, () -> logger.info("DONE!"));

		logger.info("-----------------------------------------------------------");

		StepVerifier.create(flux).expectNext(1, 2, 3).expectError(IndexOutOfBoundsException.class).verify();
	}

	@Test
	public void fluxSubscriberNumbersUglyBackpressure() {
		Flux<Integer> flux = Flux.range(1, 10).log();

		flux.subscribe(new BaseSubscriber<>() {
			private int count = 0;
			private final int requestCount = 2;

			@Override
			protected void hookOnSubscribe(Subscription subscription) {
				request(2);
			}

			@Override
			protected void hookOnNext(Integer value) {
				if (count >= requestCount) {
					count = 0;
					request(requestCount);
				}

			}

		});

		logger.info("-----------------------------------------------------------");

		StepVerifier.create(flux).expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).verifyComplete();
	}

	@Test
	public void fluxSubscriberNumberNotSoUglyBackpressure() {
	}

	@Test
	public void fluxSubscriberIntervalOne() throws Exception {
		Flux<Long> interval = Flux.interval(Duration.ofMillis(100)).take(10).log();

		interval.subscribe(i -> logger.info("Number{}", i));

		Thread.sleep(3000);
	}

	@Test
	public void fluxSubscriberIntervalTwo() throws Exception {
		StepVerifier.withVirtualTime(this::createInterval)
		        .expectSubscription()
		        .expectNoEvent(Duration.ofDays(1))
				.thenAwait(Duration.ofDays(1))
				.expectNext(0L)
				.thenAwait(Duration.ofDays(1))
				.expectNext(1L)
				.thenCancel()
				.verify();
	}
	
	@Test
	public void fluxSubscriberPrettyBackPressure() {
		Flux<Integer> flux = Flux.range(1, 10)
				.limitRate(3)
				.log();
		
		flux.subscribe(i->logger.info("Number {}",i));
		
		logger.info("-----------------------------------------------");
		StepVerifier.create(flux)
		   .expectNext(1,2,3,4,5,6,7,8,9,10)
		   .verifyComplete();
	}
	
	@Test
	public void connectableFlux()throws Exception {
		ConnectableFlux<Integer> connectableFlux = Flux.range(1, 10)
				.log()
			    .delayElements(Duration.ofMillis(100))
			    .publish();
		
		connectableFlux.connect();
		
		logger.info("Thread sleeping for 300ms");
		
		Thread.sleep(300);
		
		connectableFlux.subscribe(i-> logger.info("Sub1 number{}",i));
		
		logger.info("Thread sleeping for 200ms");
		
		connectableFlux.subscribe(i-> logger.info("Sub2 number{}",i));
		
		StepVerifier
		   .create(connectableFlux)
		   .then(connectableFlux::connect)
		   .expectNext(1,2,3,4,5,6,7,8,9,10)
		   .expectComplete();
	}
	
	@Test
	public void connectableFluxAutoConnect()throws Exception {
		Flux<Integer> connectableFlux = Flux.range(1, 10)
				.log()
				.delayElements(Duration.ofMillis(100))
				.publish()
				.autoConnect(2);
		
		StepVerifier
		   .create(connectableFlux)
		   .expectNext(1,2,3,4,5)
		   .expectComplete()
		   .verify();
		
	}

	private Flux<Long> createInterval() {
		return Flux.interval(Duration.ofDays(1)).log();
	}

}
