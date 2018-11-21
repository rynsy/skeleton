package io.cresco.skeleton.jaxrs;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

@Component(
        //scope= ServiceScope.PROTOTYPE,
        property = {
                JaxrsWhiteboardConstants.JAX_RS_APPLICATION_SELECT + "=(osgi.jaxrs.name=.default)",
                JaxrsWhiteboardConstants.JAX_RS_EXTENSION + "=true",
        }
)

public class ExampleFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext)
            throws IOException {

        System.out.println("FILTERED YO! TS:" + System.currentTimeMillis());
    }

}

