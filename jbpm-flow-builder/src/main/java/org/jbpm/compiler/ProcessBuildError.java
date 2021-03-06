package org.jbpm.compiler;

import org.drools.compiler.compiler.DescrBuildError;
import org.kie.api.definition.process.Process;
import org.drools.compiler.lang.descr.BaseDescr;

public class ProcessBuildError extends DescrBuildError {
    private final Process process;
    public ProcessBuildError(final Process process,
                           final BaseDescr descr,
                           final Object object,
                           final String message) {
              super(descr, descr, object, message);
              this.process = process;
          }
    
    public Process getProcess() {
        return this.process;
    }
}
