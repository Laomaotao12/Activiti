/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class Process extends BaseElement implements FlowElementsContainer, HasExecutionListeners, AcceptUpdates {

  protected String name;
  protected boolean executable = true;
  protected String documentation;
  protected IOSpecification ioSpecification;
  protected List<ActivitiListener> executionListeners = new ArrayList<ActivitiListener>();
  protected List<Lane> lanes = new ArrayList<Lane>();
  protected List<FlowElement> flowElementList = new ArrayList<FlowElement>();
  protected List<ValuedDataObject> dataObjects = new ArrayList<ValuedDataObject>();
  protected List<Artifact> artifactList = new ArrayList<Artifact>();
  protected List<String> candidateStarterUsers = new ArrayList<String>();
  protected List<String> candidateStarterGroups = new ArrayList<String>();
  protected List<EventListener> eventListeners = new ArrayList<EventListener>();
  protected Map<String, FlowElement> flowElementMap = new LinkedHashMap<String, FlowElement>();

  // Added during process definition parsing
  protected FlowElement initialFlowElement;

  public Process() {

  }

  public String getDocumentation() {
    return documentation;
  }

  public void setDocumentation(String documentation) {
    this.documentation = documentation;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isExecutable() {
    return executable;
  }

  public void setExecutable(boolean executable) {
    this.executable = executable;
  }

  public IOSpecification getIoSpecification() {
    return ioSpecification;
  }

  public void setIoSpecification(IOSpecification ioSpecification) {
    this.ioSpecification = ioSpecification;
  }

  public List<ActivitiListener> getExecutionListeners() {
    return executionListeners;
  }

  public void setExecutionListeners(List<ActivitiListener> executionListeners) {
    this.executionListeners = executionListeners;
  }

  public List<Lane> getLanes() {
    return lanes;
  }

  public void setLanes(List<Lane> lanes) {
    this.lanes = lanes;
  }

  public Map<String, FlowElement> getFlowElementMap() {
    return flowElementMap;
  }

  public void setFlowElementMap(Map<String, FlowElement> flowElementMap) {
    this.flowElementMap = flowElementMap;
  }

  public boolean containsFlowElementId(String id) {
    return flowElementMap.containsKey(id);
  }

  public FlowElement getFlowElement(String flowElementId) {
    return getFlowElement(flowElementId, false);
  }

  /**
   * @param searchRecurive: searches the whole process, including subprocesses
   */
  public FlowElement getFlowElement(String flowElementId, boolean searchRecurive) {
    if (searchRecurive) {
      return flowElementMap.get(flowElementId);
    } else {
      return findFlowElementInList(flowElementId);
    }
  }

  public List<Association> findAssociationsWithSourceRefRecursive(String sourceRef) {
    return findAssociationsWithSourceRefRecursive(this, sourceRef);
  }

  protected List<Association> findAssociationsWithSourceRefRecursive(FlowElementsContainer flowElementsContainer, String sourceRef) {
    List<Association> associations = new ArrayList<Association>();
    for (Artifact artifact : flowElementsContainer.getArtifacts()) {
      if (artifact instanceof Association) {
        Association association = (Association) artifact;
        if (association.getSourceRef() != null && association.getTargetRef() != null && association.getSourceRef().equals(sourceRef)) {
          associations.add(association);
        }
      }
    }

    for (FlowElement flowElement : flowElementsContainer.getFlowElements()) {
      if (flowElement instanceof FlowElementsContainer) {
        associations.addAll(findAssociationsWithSourceRefRecursive((FlowElementsContainer) flowElement, sourceRef));
      }
    }
    return associations;
  }

  public List<Association> findAssociationsWithTargetRefRecursive(String targetRef) {
    return findAssociationsWithTargetRefRecursive(this, targetRef);
  }

  protected List<Association> findAssociationsWithTargetRefRecursive(FlowElementsContainer flowElementsContainer, String targetRef) {
    List<Association> associations = new ArrayList<Association>();
    for (Artifact artifact : flowElementsContainer.getArtifacts()) {
      if (artifact instanceof Association) {
        Association association = (Association) artifact;
        if (association.getTargetRef() != null && association.getTargetRef().equals(targetRef)) {
          associations.add(association);
        }
      }
    }

    for (FlowElement flowElement : flowElementsContainer.getFlowElements()) {
      if (flowElement instanceof FlowElementsContainer) {
        associations.addAll(findAssociationsWithTargetRefRecursive((FlowElementsContainer) flowElement, targetRef));
      }
    }
    return associations;
  }

  /**
   * Searches the whole process, including subprocesses
   */
  public FlowElementsContainer getFlowElementsContainer(String flowElementId) {
    return getFlowElementsContainer(this, flowElementId);
  }

  protected FlowElementsContainer getFlowElementsContainer(FlowElementsContainer flowElementsContainer, String flowElementId) {
    for (FlowElement flowElement : flowElementsContainer.getFlowElements()) {
      if (flowElement.getId() != null && flowElement.getId().equals(flowElementId)) {
        return flowElementsContainer;
      } else if (flowElement instanceof FlowElementsContainer) {
        FlowElementsContainer result = getFlowElementsContainer((FlowElementsContainer) flowElement, flowElementId);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  protected FlowElement findFlowElementInList(String flowElementId) {
    for (FlowElement f : flowElementList) {
      if (f.getId() != null && f.getId().equals(flowElementId)) {
        return f;
      }
    }
    return null;
  }

  public Collection<FlowElement> getFlowElements() {
    return flowElementList;
  }

  public void addFlowElement(FlowElement element) {
    flowElementList.add(element);
    element.setParentContainer(this);
    if (StringUtils.isNotEmpty(element.getId())) {
      flowElementMap.put(element.getId(), element);
    }
    if(element instanceof FlowElementsContainer){
      flowElementMap.putAll(((FlowElementsContainer) element).getFlowElementMap());
    }
  }

  public void addFlowElementToMap(FlowElement element) {
    if (element != null && StringUtils.isNotEmpty(element.getId())) {
      flowElementMap.put(element.getId(), element);
    }
  }

  public void removeFlowElement(String elementId) {
    FlowElement element = flowElementMap.get(elementId);
    if (element != null) {
      flowElementList.remove(element);
      flowElementMap.remove(element.getId());
    }
  }

  public void removeFlowElementFromMap(String elementId) {
    if (StringUtils.isNotEmpty(elementId)) {
      flowElementMap.remove(elementId);
    }
  }

  public Artifact getArtifact(String id) {
    Artifact foundArtifact = null;
    for (Artifact artifact : artifactList) {
      if (id.equals(artifact.getId())) {
        foundArtifact = artifact;
        break;
      }
    }
    return foundArtifact;
  }

  public Collection<Artifact> getArtifacts() {
    return artifactList;
  }

  public void addArtifact(Artifact artifact) {
    artifactList.add(artifact);
  }

  public void removeArtifact(String artifactId) {
    Artifact artifact = getArtifact(artifactId);
    if (artifact != null) {
      artifactList.remove(artifact);
    }
  }

  public List<String> getCandidateStarterUsers() {
    return candidateStarterUsers;
  }

  public void setCandidateStarterUsers(List<String> candidateStarterUsers) {
    this.candidateStarterUsers = candidateStarterUsers;
  }

  public List<String> getCandidateStarterGroups() {
    return candidateStarterGroups;
  }

  public void setCandidateStarterGroups(List<String> candidateStarterGroups) {
    this.candidateStarterGroups = candidateStarterGroups;
  }

  public List<EventListener> getEventListeners() {
    return eventListeners;
  }

  public void setEventListeners(List<EventListener> eventListeners) {
    this.eventListeners = eventListeners;
  }

  public <FlowElementType extends FlowElement> List<FlowElementType> findFlowElementsOfType(Class<FlowElementType> type) {
    return findFlowElementsOfType(type, true);
  }

  @SuppressWarnings("unchecked")
  public <FlowElementType extends FlowElement> List<FlowElementType> findFlowElementsOfType(Class<FlowElementType> type, boolean goIntoSubprocesses) {
    List<FlowElementType> foundFlowElements = new ArrayList<FlowElementType>();
    for (FlowElement flowElement : this.getFlowElements()) {
      if (type.isInstance(flowElement)) {
        foundFlowElements.add((FlowElementType) flowElement);
      }
      if (flowElement instanceof SubProcess) {
        if (goIntoSubprocesses) {
          foundFlowElements.addAll(findFlowElementsInSubProcessOfType((SubProcess) flowElement, type));
        }
      }
    }
    return foundFlowElements;
  }

  public <FlowElementType extends FlowElement> List<FlowElementType> findFlowElementsInSubProcessOfType(SubProcess subProcess, Class<FlowElementType> type) {
    return findFlowElementsInSubProcessOfType(subProcess, type, true);
  }

  @SuppressWarnings("unchecked")
  public <FlowElementType extends FlowElement> List<FlowElementType> findFlowElementsInSubProcessOfType(SubProcess subProcess, Class<FlowElementType> type, boolean goIntoSubprocesses) {

    List<FlowElementType> foundFlowElements = new ArrayList<FlowElementType>();
    for (FlowElement flowElement : subProcess.getFlowElements()) {
      if (type.isInstance(flowElement)) {
        foundFlowElements.add((FlowElementType) flowElement);
      }
      if (flowElement instanceof SubProcess) {
        if (goIntoSubprocesses) {
          foundFlowElements.addAll(findFlowElementsInSubProcessOfType((SubProcess) flowElement, type));
        }
      }
    }
    return foundFlowElements;
  }

  public FlowElementsContainer findParent(FlowElement childElement) {
    return findParent(childElement, this);
  }

  public FlowElementsContainer findParent(FlowElement childElement, FlowElementsContainer flowElementsContainer) {
    for (FlowElement flowElement : flowElementsContainer.getFlowElements()) {
      if (childElement.getId() != null && childElement.getId().equals(flowElement.getId())) {
        return flowElementsContainer;
      }
      if (flowElement instanceof FlowElementsContainer) {
        FlowElementsContainer result = findParent(childElement, (FlowElementsContainer) flowElement);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  public Process clone() {
    Process clone = new Process();
    clone.setValues(this);
    return clone;
  }

  public void setValues(Process otherElement) {
    super.setValues(otherElement);

//    setBpmnModel(bpmnModel);
    setName(otherElement.getName());
    setExecutable(otherElement.isExecutable());
    setDocumentation(otherElement.getDocumentation());
    if (otherElement.getIoSpecification() != null) {
      setIoSpecification(otherElement.getIoSpecification().clone());
    }

    executionListeners = new ArrayList<ActivitiListener>();
    if (otherElement.getExecutionListeners() != null && !otherElement.getExecutionListeners().isEmpty()) {
      for (ActivitiListener listener : otherElement.getExecutionListeners()) {
        executionListeners.add(listener.clone());
      }
    }

    candidateStarterUsers = new ArrayList<String>();
    if (otherElement.getCandidateStarterUsers() != null && !otherElement.getCandidateStarterUsers().isEmpty()) {
      candidateStarterUsers.addAll(otherElement.getCandidateStarterUsers());
    }

    candidateStarterGroups = new ArrayList<String>();
    if (otherElement.getCandidateStarterGroups() != null && !otherElement.getCandidateStarterGroups().isEmpty()) {
      candidateStarterGroups.addAll(otherElement.getCandidateStarterGroups());
    }

    eventListeners = new ArrayList<EventListener>();
    if (otherElement.getEventListeners() != null && !otherElement.getEventListeners().isEmpty()) {
      for (EventListener listener : otherElement.getEventListeners()) {
        eventListeners.add(listener.clone());
      }
    }

    /*
     * This is required because data objects in Designer have no DI info and are added as properties, not flow elements
     *
     * Determine the differences between the 2 elements' data object
     */
    for (ValuedDataObject thisObject : getDataObjects()) {
      boolean exists = false;
      for (ValuedDataObject otherObject : otherElement.getDataObjects()) {
        if (thisObject.getId().equals(otherObject.getId())) {
          exists = true;
        }
      }
      if (!exists) {
        // missing object
        removeFlowElement(thisObject.getId());
      }
    }

    dataObjects = new ArrayList<ValuedDataObject>();
    if (otherElement.getDataObjects() != null && !otherElement.getDataObjects().isEmpty()) {
      for (ValuedDataObject dataObject : otherElement.getDataObjects()) {
        ValuedDataObject clone = dataObject.clone();
        dataObjects.add(clone);
        // add it to the list of FlowElements
        // if it is already there, remove it first so order is same as
        // data object list
        removeFlowElement(clone.getId());
        addFlowElement(clone);
      }
    }
  }

  public List<ValuedDataObject> getDataObjects() {
    return dataObjects;
  }

  public void setDataObjects(List<ValuedDataObject> dataObjects) {
    this.dataObjects = dataObjects;
  }

  public FlowElement getInitialFlowElement() {
    return initialFlowElement;
  }

  public void setInitialFlowElement(FlowElement initialFlowElement) {
    this.initialFlowElement = initialFlowElement;
  }

  @Override
  public void accept(ReferenceOverrider referenceOverrider) {
    getFlowElements().forEach(flowElement -> flowElement.accept(referenceOverrider));
  }

}
