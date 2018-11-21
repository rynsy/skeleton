package io.cresco.skeleton.jaxrs;

import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.plugin.PluginService;
import io.cresco.library.utilities.CLogger;
import io.cresco.skeleton.Plugin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Component(
        scope = ServiceScope.PROTOTYPE,
        property = {
                JaxrsWhiteboardConstants.JAX_RS_APPLICATION_SELECT + "=(osgi.jaxrs.name=.default)",
                JaxrsWhiteboardConstants.JAX_RS_RESOURCE + "=true"
        },
        service = ExampleEndpoint.class,
        servicefactory = true,
        reference = @Reference(
                name="io.cresco.library.plugin.PluginService",
                service= PluginService.class,
                target="(dashboard=core)"
        )
)

public class ExampleEndpoint {

    private PluginBuilder plugin;
    private CLogger logger;

    public ExampleEndpoint() {
        if(plugin == null) {
            if(Plugin.pluginBuilder != null) {
                plugin = Plugin.pluginBuilder;
                logger = plugin.getLogger(ExampleEndpoint.class.getName(), CLogger.Level.Info);
            }
        }
    }

    @GET
    @Path("/dashboard/skeleton/status")
    @Produces(MediaType.TEXT_HTML)
    public Response sayStatus() {

        return Response.ok(plugin.getAgent()).build();
    }

    @GET
    @Path("/dashboard/skeleton/{name}")
    @Produces(MediaType.TEXT_HTML)
    public Response sayHello(@PathParam("name") String name) {

        return Response.ok("Your Name is: " + name).build();
    }


}