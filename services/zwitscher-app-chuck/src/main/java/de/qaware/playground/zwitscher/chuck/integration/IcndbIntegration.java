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
package de.qaware.playground.zwitscher.chuck.integration;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import rx.Observable;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Obtains a random Chuck Norris joke from THE INTERNET CHUCK NORRIS DATABASE.
 *
 * JSON response from ICNDB (http://api.icndb.com):<br />
 * <code>
 * { "type": "success",
 *   "value": {
 *      "id": 419,
 *      "joke": "JOKE",
 *      "categories": []
 *  }}
 *  </code>
 */
@RequestScoped
public class IcndbIntegration {

    private static final String FALLBACK_JOKE = "The original title for Alien vs. Predator was Alien and Predator vs " +
            "Chuck Norris. The film was cancelled shortly after going into preproduction. No one would pay nine dollars " +
            "to see a movie fourteen seconds long.";

    public String getRandomJoke(){
        IcndbIntegrationCommand cmd = new IcndbIntegrationCommand();
        Response response = null;
        try {
            response = cmd.observe().toBlocking().toFuture().get();
            Map<String, Map<String, String>> json = response.readEntity(Map.class);
            return json.get("value").get("joke");
        } catch (InterruptedException | ExecutionException e) {
            return FALLBACK_JOKE;
        }

    }

    private static class IcndbIntegrationCommand extends HystrixObservableCommand<Response> {

        public IcndbIntegrationCommand() {
            super(HystrixCommandGroupKey.Factory.asKey("zwitscher"));
        }

        @Override
        protected Observable<Response> construct() {
            return RxObservable.newClient()
                    .target("http://api.icndb.com")
                    .path("jokes/random")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .rx()
                    .get();
        }
    }

}
