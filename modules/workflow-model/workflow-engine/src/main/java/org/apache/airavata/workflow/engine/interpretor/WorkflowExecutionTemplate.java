/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.workflow.engine.interpretor;

import java.util.List;

import org.apache.airavata.client.api.ExperimentAdvanceOptions;
import org.apache.airavata.workflow.model.wf.WorkflowInput;

public class WorkflowExecutionTemplate {
	private String workflowTemplateName;
	private List<WorkflowInput> input;
	private ExperimentAdvanceOptions advanceOptions;
	public String getWorkflowTemplateName() {
		return workflowTemplateName;
	}
	public void setWorkflowTemplateName(String workflowTemplateName) {
		this.workflowTemplateName = workflowTemplateName;
	}
	public List<WorkflowInput> getInput() {
		return input;
	}
	public void setInput(List<WorkflowInput> input) {
		this.input = input;
	}
	public ExperimentAdvanceOptions getAdvanceOptions() {
		return advanceOptions;
	}
	public void setAdvanceOptions(ExperimentAdvanceOptions advanceOptions) {
		this.advanceOptions = advanceOptions;
	}
}
