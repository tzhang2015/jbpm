/*
 * Copyright 2013 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.executor.impl;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jbpm.executor.ExecutorNotStartedException;
import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.executor.RequeueAware;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ErrorInfo;
import org.kie.internal.executor.api.Executor;
import org.kie.internal.executor.api.ExecutorAdminService;
import org.kie.internal.executor.api.ExecutorQueryService;
import org.kie.internal.executor.api.ExecutorService;
import org.kie.internal.executor.api.RequestInfo;
import org.kie.internal.executor.api.STATUS;

/**
 * Entry point of the executor component. Application should always talk
 * via this service to ensure all internals are properly initialized
 *
 */
public class ExecutorServiceImpl implements ExecutorService, RequeueAware {
	
    private TimeUnit timeunit = TimeUnit.valueOf(System.getProperty("org.kie.executor.timeunit", "SECONDS"));
    private long maxRunningTime = Long.parseLong(System.getProperty("org.kie.executor.running.max", "600"));
    
    private Executor executor;
    private boolean executorStarted = false;
     
    private ExecutorQueryService queryService;
    
    private ExecutorAdminService adminService;
    
    public ExecutorServiceImpl(){
    	
    }

    public ExecutorServiceImpl(Executor executor) {
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public ExecutorQueryService getQueryService() {
        return queryService;
    }

    public void setQueryService(ExecutorQueryService queryService) {
        this.queryService = queryService;
    }

    public ExecutorAdminService getAdminService() {
        return adminService;
    }

    public void setAdminService(ExecutorAdminService adminService) {
        this.adminService = adminService;
    }
    
    public List<RequestInfo> getFutureQueuedRequests() {
        return queryService.getFutureQueuedRequests();
    }

    public List<RequestInfo> getQueuedRequests() {
        return queryService.getQueuedRequests();
    }

    public List<RequestInfo> getCompletedRequests() {
        return queryService.getCompletedRequests();
    }

    public List<RequestInfo> getInErrorRequests() {
        return queryService.getInErrorRequests();
    }

    public List<RequestInfo> getCancelledRequests() {
        return queryService.getCancelledRequests();
    }

    public List<ErrorInfo> getAllErrors() {
        return queryService.getAllErrors();
    }

    public List<RequestInfo> getAllRequests() {
        return queryService.getAllRequests();
    }

    public List<RequestInfo> getRequestsByStatus(List<STATUS> statuses) {
    	return queryService.getRequestsByStatus(statuses);
    }
    
    public int clearAllRequests() {
        return adminService.clearAllRequests();
    }

    public int clearAllErrors() {
        return adminService.clearAllErrors();
    }

    public Long scheduleRequest(String commandName, CommandContext ctx) {
        return executor.scheduleRequest(commandName, ctx);
    }

    public void cancelRequest(Long requestId) {
        executor.cancelRequest(requestId);
    }

    
    public void init() {
    	if (!executorStarted) {
    		if (maxRunningTime > -1) {
    			requeue(maxRunningTime);
    		}
    		try {
		        executor.init();
		        this.executorStarted = true;
    		} catch (ExecutorNotStartedException e) {
    			this.executorStarted = false;
    		}
    	}
    }
    
    
    public void destroy() {  
    	if (executorStarted) {
    		ExecutorServiceFactory.resetExecutorService(this);
	    	this.executorStarted = false;
	        executor.destroy();	        
    	}
    }
    
    public boolean isActive() {
    	return executorStarted;
    }

    public int getInterval() {
        return executor.getInterval();
    }

    public void setInterval(int waitTime) {
        executor.setInterval(waitTime);
    }

    public int getRetries() {
        return executor.getRetries();
    }

    public void setRetries(int defaultNroOfRetries) {
        executor.setRetries(defaultNroOfRetries);
    }

    public int getThreadPoolSize() {
        return executor.getThreadPoolSize();
    }

    public void setThreadPoolSize(int nroOfThreads) {
        executor.setThreadPoolSize(nroOfThreads);
    }
    
	public TimeUnit getTimeunit() {
		return executor.getTimeunit();
	}

	public void setTimeunit(TimeUnit timeunit) {
		executor.setTimeunit(timeunit);
	}

    public List<RequestInfo> getPendingRequests() {
        return queryService.getPendingRequests();
    }

    public List<RequestInfo> getPendingRequestById(Long id) {
        return queryService.getPendingRequestById(id);
    }

    public Long scheduleRequest(String commandId, Date date, CommandContext ctx) {
        return executor.scheduleRequest(commandId, date, ctx);
    }

    public List<RequestInfo> getRunningRequests() {
        return queryService.getRunningRequests();
    }

    public RequestInfo getRequestById(Long requestId) {
    	return queryService.getRequestById(requestId);
    }

    public List<ErrorInfo> getErrorsByRequestId(Long requestId) {
    	return queryService.getErrorsByRequestId(requestId);
    }

    @Override
    public List<RequestInfo> getRequestsByBusinessKey(String businessKey) {
        return queryService.getRequestByBusinessKey(businessKey);
    }

	@Override
	public void requeue(Long olderThan) {		
        if (adminService instanceof RequeueAware) {
        	if (olderThan == null) {
        		olderThan = maxRunningTime;
        	}
        	((RequeueAware) adminService).requeue(timeunit.convert(olderThan, TimeUnit.MILLISECONDS));
        }
	}

	@Override
	public void requeueById(Long requestId) {
		if (adminService instanceof RequeueAware) {
        	((RequeueAware) adminService).requeueById(requestId);
        }
	}

}
