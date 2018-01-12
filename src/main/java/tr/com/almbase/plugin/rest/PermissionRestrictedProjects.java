package tr.com.almbase.plugin.rest;

import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.RestrictedProject;
import tr.com.almbase.plugin.activeobject.RestrictedProjectController;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kivanc.ahat@almbase.com on 10/01/2018.
 */

@Path("/getRestrictedProjects")
public class PermissionRestrictedProjects {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PermissionRestrictedProjects.class);

    private RestrictedProjectController restrictedProjectController;

    public PermissionRestrictedProjects (RestrictedProjectController restrictedProjectController) {
        this.restrictedProjectController = restrictedProjectController;
    }

    @GET
    @Consumes("application/json")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getMessage() {
        List<RestrictedProjectModule> projects = new ArrayList<>();
        try {
            RestrictedProject[] restrictedProjects = restrictedProjectController.getAllEntriesFromAOTable();

            for (RestrictedProject restrictedProject : restrictedProjects) {
                projects.add(new RestrictedProjectModule(restrictedProject.getProjectKey()));
            }
        }
        catch (Exception e) {
            log.error("Json projects error");
            return Response.serverError().entity("Json projects error").build();
        }

        return Response.ok(projects, MediaType.APPLICATION_JSON).build();
    }
}
