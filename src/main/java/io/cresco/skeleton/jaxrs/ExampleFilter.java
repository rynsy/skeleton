package io.cresco.skeleton.jaxrs;

import io.cresco.library.plugin.PluginService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;


@Component(

        scope= ServiceScope.PROTOTYPE,
        property = {
                JaxrsWhiteboardConstants.JAX_RS_APPLICATION_SELECT + "=(osgi.jaxrs.name=.default)",
                JaxrsWhiteboardConstants.JAX_RS_EXTENSION + "=true",
        },
        servicefactory = true,
        reference = @Reference(
                name="io.cresco.library.plugin.PluginService",
                service= PluginService.class,
                target="(dashboard=core)"
        )
)

public class ExampleFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext)
            throws IOException {

        System.out.println("FILTERED WEB TS:" + System.currentTimeMillis());
    }

}

