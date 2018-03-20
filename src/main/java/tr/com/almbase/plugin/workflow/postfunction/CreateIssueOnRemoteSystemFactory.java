package tr.com.almbase.plugin.workflow.postfunction;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com on 16/11/2017.
 */

public class CreateIssueOnRemoteSystemFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory
{
    public CreateIssueOnRemoteSystemFactory(WorkflowManager workflowManager) {
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

    public Map<String,?> getDescriptorParams(Map<String, Object> formParams) {
        Map params = new HashMap();
        return params;
    }

}