package com.cloudbees.jenkins.plugins.sshagent;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHAuthenticator;
import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.Stapler;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class SSHAgentStep extends AbstractStepImpl implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The {@link com.cloudbees.plugins.credentials.common.StandardUsernameCredentials#getId()}s of the credentials
     * to use.
     */
    private final List<String> credentials;

    /**
     * If a credentials is missed, the SSH Agent is launched anyway.
     * By the fault is false. Initialized in the constructor.
     */
    private boolean ignoreMissing;
    
    /**
     * Path to use for SSH_AUTH_SOCK. If null or blank, ssh-agent default will be used.
     */
    private String socketPath;

    /**
     * Default parameterized constructor.
     *
     * @param credentials
     */
    @DataBoundConstructor
    public SSHAgentStep(final List<String> credentials) {
        this.credentials = credentials;
        this.ignoreMissing = false;
        this.socketPath = null;
    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(SSHAgentStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "sshagent";
        }

        @Override
        public String getDisplayName() {
            return Messages.SSHAgentBuildWrapper_DisplayName();
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }

        /**
         * Populate the list of credentials available to the job.
         *
         * @return the list box model.
         */
        @SuppressWarnings("unused") // used by stapler
        public ListBoxModel doFillCredentialsItems() {
            Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
            return new StandardUsernameListBoxModel()
                    .includeMatchingAs(
                            item instanceof Queue.Task ? Tasks.getAuthenticationOf((Queue.Task)item) : ACL.SYSTEM,
                            item,
                            SSHUserPrivateKey.class,
                            Collections.<DomainRequirement>emptyList(),
                            SSHAuthenticator.matcher()
                    );
        }

    }

    @DataBoundSetter
    public void setIgnoreMissing(final boolean ignoreMissing) {
        this.ignoreMissing = ignoreMissing;
    }

    public boolean isIgnoreMissing() {
      return ignoreMissing;
    }

    @DataBoundSetter
    public void setSocketPath(final String socketPath) {
        this.socketPath = socketPath;
    }
    
    public String getSocketPath() {
        return socketPath;
    }

    public List<String> getCredentials() {
        return credentials;
    }

}
