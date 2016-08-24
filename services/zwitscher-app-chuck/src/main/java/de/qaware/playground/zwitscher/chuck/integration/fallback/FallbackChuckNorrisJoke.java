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
package de.qaware.playground.zwitscher.chuck.integration.fallback;

import de.qaware.playground.zwitscher.chuck.integration.IChuckNorrisJokes;
import org.slf4j.Logger;
import rx.Observable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * The last Chuck Norris joke standing.
 *
 * (c) on the joke by ourselves
 */
@ApplicationScoped
@Named("chucknorrisjoke-fallback")
public class FallbackChuckNorrisJoke implements IChuckNorrisJokes {

    private static final String FALLBACK_JOKE = "Every Chuck Norris joke matches on every keyword.";

    @Inject
    private Logger logger;

    @Override
    public String getRandomJoke() {
        logger.error("Default Chuck Norris joke picked!");
        return FALLBACK_JOKE;
    }

    @Override
    public Observable<String> getRandomJokeObservable() {
        return Observable.just(getRandomJoke());
    }
}
