package org.opendaylight.alto.manager;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Matchers.any;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpDelete;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class AltoDeleteTest {
    @Mock(name = "httpClient")
    private HttpClient httpClient;

    @InjectMocks
    @Spy
    private AltoDelete altoDelete = new AltoDelete();

    private final String DEFAULT_NETWORK_MAP = "{\"default-alto-network-map\":{\"resource-id\":\"default-network-map\"}}";

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDoExecute() throws Exception {
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = EntityBuilder.create().setText(DEFAULT_NETWORK_MAP).build();
        StatusLine statusLine = mock(StatusLine.class);
        doReturn(response).when(httpClient).execute(any(HttpDelete.class));
        doReturn(statusLine).when(response).getStatusLine();
        doReturn(200).when(statusLine).getStatusCode();
        doReturn(response).when(altoDelete).httpGet(AltoManagerConstants.IRD_DEFAULT_NETWORK_MAP_URL);
        doReturn(entity).when(response).getEntity();

        altoDelete.resourceType = "network-map";
        altoDelete.resourceId = "my-default-network-map";
        altoDelete.doExecute();
        verify(httpClient, atLeastOnce()).execute(any(HttpDelete.class));
        altoDelete.resourceType = "cost-map";
        altoDelete.resourceId = "my-default-network-map-routingcost-numerical";
        altoDelete.doExecute();
        verify(httpClient, atLeastOnce()).execute(any(HttpDelete.class));
        altoDelete.resourceType = "endpoint-property-map";
        altoDelete.resourceId = null;
        altoDelete.doExecute();
        verify(httpClient, atLeastOnce()).execute(any(HttpDelete.class));
        altoDelete.resourceType = "otherwise";
        altoDelete.resourceId = "";
        try {
            altoDelete.doExecute();
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), UnsupportedOperationException.class);
        }
    }
}
