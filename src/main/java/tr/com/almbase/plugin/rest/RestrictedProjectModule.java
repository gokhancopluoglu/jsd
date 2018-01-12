package tr.com.almbase.plugin.rest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by kivanc.ahat@almbase.com on 12/01/2018.
 */


@XmlRootElement
public class RestrictedProjectModule {
    @XmlElement
    private String projectKey;

    private RestrictedProjectModule() { }

    public RestrictedProjectModule(String projectKey)
    {
        this.projectKey = projectKey;
    }
}

