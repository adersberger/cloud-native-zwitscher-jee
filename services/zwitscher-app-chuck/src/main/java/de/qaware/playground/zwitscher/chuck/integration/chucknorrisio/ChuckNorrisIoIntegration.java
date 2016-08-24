/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Josef Adersberger
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.qaware.playground.zwitscher.chuck.integration.chucknorrisio;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import de.qaware.playground.zwitscher.chuck.integration.IChuckNorrisJokes;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.slf4j.Logger;
import rx.Observable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Obtains a random Chuck Norris Joke from https://api.chucknorris.io/jokes/random
 *
 * Example response:
 *
 * <pre><code>
 * {
 * "icon_url" : "https://assets.chucknorris.host/img/avatar/chuck-norris.png",
 * "id" : "-avQRsn9Sa6ugzSDWw4H6A",
 * "url" : "http://api.chucknorris.io/jokes/-avQRsn9Sa6ugzSDWw4H6A"
 * "value" : "I don't know why we haven't been able to explore Black Holes yet....just send Chuck Norris"
 * }
 * </code></pre>
 */
@ApplicationScoped
@Named("chucknorrisjoke-chucknorrisio")
public class ChuckNorrisIoIntegration implements IChuckNorrisJokes {

    private static final int TIMEOUT = 3000;

    @Inject
    @Named("chucknorrisjoke-fallback")
    private IChuckNorrisJokes fallback;

    @Inject
    private Logger logger;

    @Override
    public String getRandomJoke() {
        ChuckNorrisIoIntegrationCommand cmd = new ChuckNorrisIoIntegrationCommand();
        try {
            return cmd.observe().toBlocking().toFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("chucknorris.io not available!", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Observable<String> getRandomJokeObservable() {
        return new ChuckNorrisIoIntegrationCommand().observe();
    }

    public class ChuckNorrisIoIntegrationCommand extends HystrixObservableCommand<String> {

        ChuckNorrisIoIntegrationCommand() {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("zwitscher"))
                    .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionTimeoutInMilliseconds(TIMEOUT)));
        }

        @Override
        protected Observable<String> construct() {
            logger.info("Requesting chucknorris.io async");
            return RxObservable.newClient()
                    .target("https://api.chucknorris.io")
                    .path("jokes/random")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .rx()
                    .get()
                    .map(response -> {
                        Map<String, String> json = response.readEntity(Map.class);
                        return json.get("value");
                    });
        }

        @Override
        protected Observable<String> resumeWithFallback() {
            logger.error("Requesting chucknorris.io failed. Performing fallback.");
            return fallback.getRandomJokeObservable();
        }

    }

}