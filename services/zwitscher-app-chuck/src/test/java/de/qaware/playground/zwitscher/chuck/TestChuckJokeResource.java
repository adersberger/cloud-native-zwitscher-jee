package de.qaware.playground.zwitscher.chuck;

import de.qaware.playground.zwitscher.chuck.rest.ChuckJokeResource;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(CdiTestRunner.class)
public class TestChuckJokeResource {

    @Inject
    private ChuckJokeResource chuckJokeResource;

    @Test
    public void testChuckJokeResource() {
        System.setProperty("CONFIG_ENV", "zwitscher");
        Response response = chuckJokeResource.getJoke();
        assertThat(response.getStatusInfo().getStatusCode(), equalTo(200));
    }
}