package tr.com.almbase.plugin.workflow.postfunction;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class AssignToGroupUserFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory
{
    public AssignToGroupUserFactory() {
    }

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams) {
    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {

        getVelocityParamsForInput(velocityParams);
        getVelocityParamsForView(velocityParams, descriptor);
    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        if (!(descriptor instanceof FunctionDescriptor)) {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        }
    }

    public Map<String, ?> getDescriptorParams(Map<String, Object> formParams) {
        return formParams;
    }
}