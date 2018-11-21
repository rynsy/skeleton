package io.cresco.skeleton.jaxrs;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Component(
        property = {
                JaxrsWhiteboardConstants.JAX_RS_APPLICATION_SELECT + "=(osgi.jaxrs.name=.default)",
                JaxrsWhiteboardConstants.JAX_RS_RESOURCE + "=true"
        },
        service = ExampleEndpoint.class
)

public class ExampleEndpoint {

    @GET
    @Path("/dashboard/skeleton/{name}")
    @Produces(MediaType.TEXT_HTML)
    public Response sayHello(@PathParam("name") String name) {

        return Response.ok("Your Name is: " + name).build();
    }


}