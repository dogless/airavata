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

package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.registry.api.exception.worker.ExperimentLazyLoadedException;
import org.apache.airavata.registry.api.impl.ExperimentDataImpl;
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExperimentDataRetriever {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentDataRetriever.class);

    public ExperimentData getExperiment(String experimentId){
        String connectionURL =  Utils.getJDBCURL();
        Connection connection = null;
        ResultSet rs = null;
        Statement statement;
        List<WorkflowInstance> experimentWorkflowInstances = new ArrayList<WorkflowInstance>();
        ExperimentData experimentData = null;
        try {
            Class.forName(Utils.getJDBCDriver()).newInstance();
            connection = DriverManager.getConnection(connectionURL, Utils.getJDBCUser(), Utils.getJDBCPassword());
            statement = connection.createStatement();
            String queryString = "SELECT ed.experiment_ID, ed.name, ed.username, em.metadata, " +
                    "wd.workflow_instanceID, wd.template_name, wd.status, wd.start_time," +
                    "wd.last_update_time, nd.node_id, nd.inputs, nd.outputs, " +
                    "e.project_name, e.submitted_date, nd.node_type, nd.status," +
                    "nd.start_time, nd.last_update_time " +
                    "FROM Experiment e " +
                    "LEFT JOIN Experiment_Data ed " +
                    "ON e.experiment_ID = ed.experiment_ID " +
                    "LEFT JOIN Experiment_Metadata em " +
                    "ON ed.experiment_ID = em.experiment_ID  " +
                    "LEFT JOIN Workflow_Data wd " +
                    "ON e.experiment_ID = wd.experiment_ID " +
                    "LEFT JOIN Node_Data nd " +
                    "ON wd.workflow_instanceID = nd.workflow_instanceID " +
                    "WHERE e.experiment_ID ='" + experimentId + "'";

            rs = statement.executeQuery(queryString);
            if (rs != null){
                while (rs.next()) {
                    experimentData = new ExperimentDataImpl();
                    experimentData.setExperimentId(rs.getString(1));
                    experimentData.setExperimentName(rs.getString(2));
                    experimentData.setUser(rs.getString(3));
                    experimentData.setMetadata(rs.getString(4));
                    experimentData.setTopic(rs.getString(1));

                    WorkflowInstance workflowInstance = new WorkflowInstance(experimentId, rs.getString(5));
                    workflowInstance.setTemplateName(rs.getString(6));
                    workflowInstance.setExperimentId(rs.getString(1));
                    workflowInstance.setWorkflowInstanceId(rs.getString(5));
                    experimentWorkflowInstances.add(workflowInstance);

                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                    Date lastUpdateDate = dateFormat.parse(rs.getString(9));
                    WorkflowInstanceData workflowInstanceData = new WorkflowInstanceData(null,
                            workflowInstance, new WorkflowInstanceStatus(workflowInstance,
                            rs.getString(7)==null? null:ExecutionStatus.valueOf(rs.getString(7)),lastUpdateDate), null);
                    workflowInstanceData.setExperimentData(experimentData);

                    WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(workflowInstance, rs.getString(10));

                    WorkflowInstanceNodeData workflowInstanceNodeData = new WorkflowInstanceNodeData(workflowInstanceNode);
                    workflowInstanceNodeData.setInput(rs.getString(11));
                    workflowInstanceNodeData.setOutput(rs.getString(12));

                    workflowInstanceData.getNodeDataList().add(workflowInstanceNodeData);
                    try {
						experimentData.getWorkflowInstanceData().add(workflowInstanceData);
					} catch (ExperimentLazyLoadedException e) {
						e.printStackTrace();
					}
                }
            }
            if(rs != null){
                rs.close();
            }
            statement.close();
            connection.close();
        } catch (InstantiationException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } catch (SQLException e){
            e.printStackTrace();
        }catch (ParseException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return experimentData;
    }

    public List<String> getExperimentIdByUser(String user){
        List<String> result=new ArrayList<String>();
        String connectionURL =  Utils.getJDBCURL();
        Connection connection = null;
        ResultSet rs = null;
        Statement statement = null;
        try {
            String jdbcDriver =  Utils.getJDBCDriver();
            Class.forName(jdbcDriver).newInstance();
            connection = DriverManager.getConnection(connectionURL, Utils.getJDBCUser(), Utils.getJDBCPassword());
            statement = connection.createStatement();

//            String queryString = "SELECT experiment_ID FROM Experiment WHERE user_name ='" +  user + "'";
            // FIXME : pass user ID as a regular expression
            String queryString = "SELECT ed.experiment_ID FROM Experiment_Data ed " +
                    "LEFT JOIN Experiment e " +
                    "ON ed.experiment_ID = e.experiment_ID " +
                    "WHERE ed.username ='" + user + "'";
            rs = statement.executeQuery(queryString);
            if(rs != null){
                while (rs.next()) {
                    result.add(rs.getString(1));
                }
            }
            if(rs != null){
                rs.close();
            }
            statement.close();
            connection.close();
        } catch (InstantiationException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } catch (SQLException e){
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }

        return result;

    }

    public String getExperimentName(String experimentId){
        String connectionURL =  Utils.getJDBCURL();
        Connection connection;
        Statement statement;
        ResultSet rs;
        try {
            Class.forName(Utils.getJDBCDriver()).newInstance();
            connection = DriverManager.getConnection(connectionURL, Utils.getJDBCUser(), Utils.getJDBCPassword());
            statement =  connection.createStatement();
            String queryString = "SELECT ed.name FROM Experiment e " +
                    "LEFT JOIN Experiment_Data ed " +
                    "ON e.experiment_ID = ed.experiment_ID " +
                    "WHERE e.experiment_ID='" + experimentId + "'";
            rs = statement.executeQuery(queryString);
            if(rs != null){
                while (rs.next()) {
                    return rs.getString(1);
                }
            }
            if(rs != null){
                rs.close();
            }

            statement.close();
            connection.close();
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (SQLException e){
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public List<ExperimentData> getExperiments(){
        List<ExperimentData> experimentDataList = new ArrayList<ExperimentData>();
        String connectionURL =  Utils.getJDBCURL();
        Connection connection = null;
        ResultSet rs = null;
        Statement statement;
        List<WorkflowInstance> experimentWorkflowInstances = new ArrayList<WorkflowInstance>();
        ExperimentData experimentData;
        try {
            Class.forName(Utils.getJDBCDriver()).newInstance();
            connection = DriverManager.getConnection(connectionURL, Utils.getJDBCUser(),
                    Utils.getJDBCPassword());
            statement = connection.createStatement();
            String queryString = "SELECT e.experiment_ID, ed.name, ed.username, em.metadata, " +
                    "wd.workflow_instanceID, wd.template_name, wd.status, wd.start_time," +
                    "wd.last_update_time, nd.node_id, nd.inputs, nd.outputs, " +
                    "e.project_name, e.submitted_date, nd.node_type, nd.status," +
                    "nd.start_time, nd.last_update_time" +
                    " FROM Experiment e INNER JOIN Experiment_Data ed " +
                    "ON e.experiment_ID = ed.experiment_ID " +
                    "LEFT JOIN Experiment_Metadata em " +
                    "ON ed.experiment_ID = em.experiment_ID  " +
                    "LEFT JOIN Workflow_Data wd " +
                    "ON e.experiment_ID = wd.experiment_ID " +
                    "LEFT JOIN Node_Data nd " +
                    "ON wd.workflow_instanceID = nd.workflow_instanceID'";

            rs = statement.executeQuery(queryString);
            while (rs.next()) {
                experimentData = new ExperimentDataImpl();
                experimentData.setExperimentId(rs.getString(1));
                experimentData.setExperimentName(rs.getString(2));
                experimentData.setUser(rs.getString(3));
                experimentData.setMetadata(rs.getString(4));
                experimentData.setTopic(rs.getString(1));

                WorkflowInstance workflowInstance = new WorkflowInstance(rs.getString(1), rs.getString(5));
                workflowInstance.setTemplateName(rs.getString(6));
                workflowInstance.setExperimentId(rs.getString(1));
                workflowInstance.setWorkflowInstanceId(rs.getString(5));
                experimentWorkflowInstances.add(workflowInstance);

                Date lastUpdateDate = new Date(rs.getLong(9));
                WorkflowInstanceData workflowInstanceData = new WorkflowInstanceData(null,
                        workflowInstance, new WorkflowInstanceStatus(workflowInstance,
                        rs.getString(7)==null? null: WorkflowInstanceStatus.ExecutionStatus.valueOf(rs.getString(7)),lastUpdateDate), null);
                workflowInstanceData.setExperimentData(experimentData);

                WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(workflowInstance, rs.getString(10));

                WorkflowInstanceNodeData workflowInstanceNodeData = new WorkflowInstanceNodeData(workflowInstanceNode);
                workflowInstanceNodeData.setInput(rs.getString(11));
                workflowInstanceNodeData.setOutput(rs.getString(12));

                workflowInstanceData.getNodeDataList().add(workflowInstanceNodeData);
                try {
					experimentData.getWorkflowInstanceData().add(workflowInstanceData);
				} catch (ExperimentLazyLoadedException e) {
					e.printStackTrace();
				}
                experimentDataList.add(experimentData);
            }
            rs.close();
            statement.close();
            connection.close();
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (SQLException e){
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return experimentDataList;
    }

    public ExperimentData getExperimentMetaInformation(String experimentId){
        String connectionURL =  Utils.getJDBCURL();
        Connection connection = null;
        ResultSet rs = null;
        Statement statement;
        List<WorkflowInstance> experimentWorkflowInstances = new ArrayList<WorkflowInstance>();
        ExperimentData experimentData = null;
        try {
            Class.forName(Utils.getJDBCDriver()).newInstance();
            connection = DriverManager.getConnection(connectionURL, Utils.getJDBCUser(), Utils.getJDBCPassword());
            statement = connection.createStatement();
            String queryString = "SELECT e.experiment_ID, ed.name, ed.username, em.metadata, " +
                    "e.project_name, e.submitted_date " +
                    "FROM Experiment e " +
                    "LEFT JOIN Experiment_Data ed " +
                    "ON e.experiment_ID = ed.experiment_ID " +
                    "LEFT JOIN Experiment_Metadata em " +
                    "ON ed.experiment_ID = em.experiment_ID  " +
                    "WHERE e.experiment_ID ='" + experimentId + "'";

            rs = statement.executeQuery(queryString);
            if (rs != null){
                while (rs.next()) {
                    experimentData = new ExperimentDataImpl(true);
                    experimentData.setExperimentId(rs.getString(1));
                    experimentData.setExperimentName(rs.getString(2));
                    experimentData.setUser(rs.getString(3));
                    experimentData.setMetadata(rs.getString(4));
                    experimentData.setTopic(rs.getString(1));

                    WorkflowInstance workflowInstance = new WorkflowInstance(experimentId, rs.getString(5));
                    workflowInstance.setTemplateName(rs.getString(6));
                    workflowInstance.setExperimentId(rs.getString(1));
                    workflowInstance.setWorkflowInstanceId(rs.getString(5));
                    experimentWorkflowInstances.add(workflowInstance);
                }
            }
            if(rs != null){
                rs.close();
            }
            statement.close();
            connection.close();
        } catch (InstantiationException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } catch (SQLException e){
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return experimentData;
    }

    public List<ExperimentData> getAllExperimentMetaInformation(String user){
        String connectionURL =  Utils.getJDBCURL();
        Connection connection = null;
        ResultSet rs = null;
        Statement statement;
        List<ExperimentData> experimentDataList = new ArrayList<ExperimentData>();
        List<WorkflowInstance> experimentWorkflowInstances = new ArrayList<WorkflowInstance>();
        ExperimentData experimentData = null;
        try {
            Class.forName(Utils.getJDBCDriver()).newInstance();
            connection = DriverManager.getConnection(connectionURL, Utils.getJDBCUser(), Utils.getJDBCPassword());
            statement = connection.createStatement();
            //FIXME : pass user ID as a regular expression
            String queryString = "SELECT e.experiment_ID, ed.name, ed.username, em.metadata, " +
                    "e.project_name, e.submitted_date " +
                    "FROM Experiment e " +
                    "LEFT JOIN Experiment_Data ed " +
                    "ON e.experiment_ID = ed.experiment_ID " +
                    "LEFT JOIN Experiment_Metadata em " +
                    "ON ed.experiment_ID = em.experiment_ID  " +
                    "WHERE ed.username ='" + user + "'";

            rs = statement.executeQuery(queryString);
            if (rs != null){
                while (rs.next()) {
                    experimentData = new ExperimentDataImpl(true);
                    experimentData.setExperimentId(rs.getString(1));
                    experimentData.setExperimentName(rs.getString(2));
                    experimentData.setUser(rs.getString(3));
                    experimentData.setMetadata(rs.getString(4));
                    experimentData.setTopic(rs.getString(1));

                    WorkflowInstance workflowInstance = new WorkflowInstance(rs.getString(1), rs.getString(5));
                    workflowInstance.setTemplateName(rs.getString(6));
                    workflowInstance.setExperimentId(rs.getString(1));
                    workflowInstance.setWorkflowInstanceId(rs.getString(5));
                    experimentWorkflowInstances.add(workflowInstance);
                    experimentDataList.add(experimentData);
                }
            }
            if(rs != null){
                rs.close();
            }
            statement.close();
            connection.close();
        } catch (InstantiationException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } catch (SQLException e){
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return experimentDataList;
    }
}