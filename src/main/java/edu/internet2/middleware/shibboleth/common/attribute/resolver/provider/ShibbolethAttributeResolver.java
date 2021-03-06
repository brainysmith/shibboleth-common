/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.shibboleth.common.attribute.resolver.provider;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.opensaml.common.SAMLObject;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml2.core.NameID;
import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolver;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition.AttributeDefinition;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition.ContextualAttributeDefinition;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.ContextualDataConnector;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.DataConnector;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.principalConnector.ContextualPrincipalConnector;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.principalConnector.PrincipalConnector;
import edu.internet2.middleware.shibboleth.common.config.BaseReloadableService;
import edu.internet2.middleware.shibboleth.common.profile.provider.SAMLProfileRequestContext;
import edu.internet2.middleware.shibboleth.common.service.ServiceException;

/**
 * Primary implementation of {@link AttributeResolver}.
 * 
 * "Raw" attributes are gathered by the registered {@link DataConnector}s while the {@link AttributeDefinition}s refine
 * the raw attributes or create attributes of their own. Connectors and definitions may depend on each other so
 * implementations must use a directed dependency graph when performing the resolution.
 */
public class ShibbolethAttributeResolver extends BaseReloadableService implements
        AttributeResolver<SAMLProfileRequestContext> {

    /** Resolution plug-in types. */
    public static final Collection<Class> PLUGIN_TYPES = Arrays.asList(new Class[] {DataConnector.class,
            AttributeDefinition.class, PrincipalConnector.class,});

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ShibbolethAttributeResolver.class.getName());

    /** Data connectors defined for this resolver. */
    private Map<String, DataConnector> dataConnectors;

    /** Attribute definitions defined for this resolver. */
    private Map<String, AttributeDefinition> definitions;

    /** Principal connectors defined for this resolver. */
    private Map<String, PrincipalConnector> principalConnectors;

    /** Constructor. */
    public ShibbolethAttributeResolver() {
        super();
        dataConnectors = new HashMap<String, DataConnector>();
        definitions = new HashMap<String, AttributeDefinition>();
        principalConnectors = new HashMap<String, PrincipalConnector>();
    }

    /**
     * Gets the attribute definitions registered with this resolver.
     * 
     * @return attribute definitions registered with this resolver
     */
    public Map<String, AttributeDefinition> getAttributeDefinitions() {
        return definitions;
    }

    /**
     * Gets the data connectors registered with this provider.
     * 
     * @return data connectors registered with this provider
     */
    public Map<String, DataConnector> getDataConnectors() {
        return dataConnectors;
    }

    /**
     * Gets the principal connectors registered with this resolver.
     * 
     * @return principal connectors registered with this resolver
     */
    public Map<String, PrincipalConnector> getPrincipalConnectors() {
        return principalConnectors;
    }

    /** {@inheritDoc} */
    public Map<String, BaseAttribute> resolveAttributes(SAMLProfileRequestContext attributeRequestContext)
            throws AttributeResolutionException {
        ShibbolethResolutionContext resolutionContext = new ShibbolethResolutionContext(attributeRequestContext);

        log.debug("{} resolving attributes for principal {}", getId(), attributeRequestContext.getPrincipalName());

        if (getAttributeDefinitions().size() == 0) {
            log.debug("No attribute definitions loaded in {} so no attributes can be resolved for principal {}",
                    getId(), attributeRequestContext.getPrincipalName());
            return new HashMap<String, BaseAttribute>();
        }

        Lock readLock = getReadWriteLock().readLock();
        readLock.lock();
        Map<String, BaseAttribute> resolvedAttributes = null;
        try {
            resolvedAttributes = resolveAttributes(resolutionContext);
            cleanResolvedAttributes(resolvedAttributes, resolutionContext);
        } finally {
            readLock.unlock();
        }

        log.debug(getId() + " resolved, for principal {}, the attributes: {}",
                attributeRequestContext.getPrincipalName(), resolvedAttributes.keySet());
        return resolvedAttributes;
    }

    /** {@inheritDoc} */
    public void validate() throws AttributeResolutionException {
        for (DataConnector plugin : dataConnectors.values()) {
            if (plugin != null) {
                validateDataConnector(plugin);
            }
        }

        for (AttributeDefinition plugin : definitions.values()) {
            if (plugin != null) {
                plugin.validate();
            }
        }

        for (PrincipalConnector plugin : principalConnectors.values()) {
            if (plugin != null) {
                plugin.validate();
            }
        }
    }

    /**
     * Validates that a data connector is valid, per {@link ResolutionPlugIn#validate()} and, if invalid, fails over to
     * a connector's failover connector, if present.
     * 
     * @param connector connector to validate
     * 
     * @throws AttributeResolutionException thrown if the connector is invalid and does not define a failover connector
     *             or, if a failover connector is defined, if that connector is invalid
     */
    protected void validateDataConnector(DataConnector connector) throws AttributeResolutionException {
        try {
            connector.validate();
        } catch (AttributeResolutionException e) {
            if (connector.getFailoverDependencyId() != null) {
                DataConnector failoverConnector = dataConnectors.get(connector.getFailoverDependencyId());
                if (failoverConnector != null) {
                    validateDataConnector(failoverConnector);
                    return;
                }
            }

            throw e;
        }
    }

    /**
     * Resolves the principal name for the subject of the request.
     * 
     * @param requestContext current request context
     * 
     * @return principal name for the subject of the request
     * 
     * @throws AttributeResolutionException thrown if the subject identifier information can not be resolved into a
     *             principal name
     */
    public String resolvePrincipalName(SAMLProfileRequestContext requestContext) throws AttributeResolutionException {
        String nameIdFormat = getNameIdentifierFormat(requestContext.getSubjectNameIdentifier());

        log.debug("Resolving principal name from name identifier of format: {}", nameIdFormat);

        PrincipalConnector effectiveConnector = null;
        for (PrincipalConnector connector : principalConnectors.values()) {
            if (connector.getFormat().equals(nameIdFormat)) {
                if (connector.getRelyingParties().contains(requestContext.getInboundMessageIssuer())) {
                    effectiveConnector = connector;
                    break;
                }

                if (connector.getRelyingParties().isEmpty()) {
                    effectiveConnector = connector;
                }
            }
        }

        if (effectiveConnector == null) {
            throw new AttributeResolutionException(
                    "No principal connector available to resolve a subject name with format " + nameIdFormat
                            + " for relying party " + requestContext.getInboundMessageIssuer());
        }
        log.debug("Using principal connector {} to resolve principal name.", effectiveConnector.getId());
        effectiveConnector = new ContextualPrincipalConnector(effectiveConnector);

        ShibbolethResolutionContext resolutionContext = new ShibbolethResolutionContext(requestContext);

        // resolve all the connectors dependencies
        resolveDependencies(effectiveConnector, resolutionContext);

        return effectiveConnector.resolve(resolutionContext);
    }

    /**
     * Gets the format of the name identifier used to identify the subject.
     * 
     * @param nameIdentifier name identifier used to identify the subject
     * 
     * @return format of the name identifier used to identify the subject
     */
    protected String getNameIdentifierFormat(SAMLObject nameIdentifier) {
        String subjectNameFormat = null;

        if (nameIdentifier instanceof NameIdentifier) {
            NameIdentifier identifier = (NameIdentifier) nameIdentifier;
            subjectNameFormat = identifier.getFormat();
        } else if (nameIdentifier instanceof NameID) {
            NameID identifier = (NameID) nameIdentifier;
            subjectNameFormat = identifier.getFormat();
        }

        if (DatatypeHelper.isEmpty(subjectNameFormat)) {
            subjectNameFormat = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
        }

        return subjectNameFormat;
    }

    /**
     * Resolves the attributes requested in the resolution context or all attributes if no specific attributes were
     * requested. This method does not remove dependency only attributes or attributes that do not contain values.
     * 
     * @param resolutionContext current resolution context
     * 
     * @return resolved attributes
     * 
     * @throws AttributeResolutionException thrown if the attributes could not be resolved
     */
    protected Map<String, BaseAttribute> resolveAttributes(ShibbolethResolutionContext resolutionContext)
            throws AttributeResolutionException {
        Collection<String> attributeIDs = resolutionContext.getAttributeRequestContext().getRequestedAttributesIds();
        Map<String, BaseAttribute> resolvedAttributes = new HashMap<String, BaseAttribute>();

        // if no attributes requested, then resolve everything
        if (attributeIDs == null || attributeIDs.isEmpty()) {
            log.debug("Specific attributes for principal {} were not requested, resolving all attributes.",
                    resolutionContext.getAttributeRequestContext().getPrincipalName());
            attributeIDs = getAttributeDefinitions().keySet();
        }

        Lock readLock = getReadWriteLock().readLock();
        readLock.lock();
        try {
            for (String attributeID : attributeIDs) {
                BaseAttribute resolvedAttribute = resolveAttribute(attributeID, resolutionContext);
                if (resolvedAttribute != null) {
                    resolvedAttributes.put(resolvedAttribute.getId(), resolvedAttribute);
                }
            }
        } finally {
            readLock.unlock();
        }

        return resolvedAttributes;
    }

    /**
     * Resolve the {@link AttributeDefinition} which has the specified ID. The definition is then added to the
     * {@link ShibbolethResolutionContext} for use by other {@link ResolutionPlugIn}s and the resolution of the
     * specified definition is added to <code>resolvedAttributes</code> to be returned by the resolver.
     * 
     * @param attributeID id of the attribute definition to resolve
     * @param resolutionContext resolution context that we are working in
     * 
     * @return resolution of the specified attribute definition
     * 
     * @throws AttributeResolutionException if unable to resolve the requested attribute definition
     */
    protected BaseAttribute resolveAttribute(String attributeID, ShibbolethResolutionContext resolutionContext)
            throws AttributeResolutionException {

        AttributeDefinition definition = resolutionContext.getResolvedAttributeDefinitions().get(attributeID);

        if (definition == null) {
            log.debug("Resolving attribute {} for principal {}", attributeID, resolutionContext
                    .getAttributeRequestContext().getPrincipalName());

            definition = getAttributeDefinitions().get(attributeID);
            if (definition == null) {
                log.warn("{} requested attribute {} but no attribute definition exists for that attribute",
                        resolutionContext.getAttributeRequestContext().getInboundMessageIssuer(), attributeID);
                return null;
            } else {
                // wrap attribute definition for use within the given resolution context
                definition = new ContextualAttributeDefinition(definition);

                // register definition as resolved for this resolution context
                resolutionContext.getResolvedPlugins().put(attributeID, definition);
            }
        }

        // resolve all the definitions dependencies
        resolveDependencies(definition, resolutionContext);

        // return the actual resolution of the definition
        BaseAttribute attribute = definition.resolve(resolutionContext);
        log.debug("Resolved attribute {} containing {} values", attributeID, attribute.getValues().size());
        return attribute;
    }

    /**
     * Resolve the {@link DataConnector} which has the specified ID and add it to the resolution context.
     * 
     * @param connectorID id of the data connector to resolve
     * @param resolutionContext resolution context that we are working in
     * 
     * @throws AttributeResolutionException if unable to resolve the requested connector
     */
    protected void resolveDataConnector(String connectorID, ShibbolethResolutionContext resolutionContext)
            throws AttributeResolutionException {

        DataConnector dataConnector = resolutionContext.getResolvedDataConnectors().get(connectorID);

        if (dataConnector == null) {
            log.debug("Resolving data connector {} for principal {}", connectorID, resolutionContext
                    .getAttributeRequestContext().getPrincipalName());

            dataConnector = getDataConnectors().get(connectorID);
            if (dataConnector == null) {
                log.warn("{} requested to resolve data connector {} but does not have such a data connector", getId(),
                        connectorID);
            } else {
                // wrap connector for use within the given resolution context
                dataConnector = new ContextualDataConnector(dataConnector);

                // register connector as resolved for this resolution context
                resolutionContext.getResolvedPlugins().put(connectorID, dataConnector);
            }
        }

        // resolve all the connectors dependencies
        resolveDependencies(dataConnector, resolutionContext);

        try {
            dataConnector.resolve(resolutionContext);
        } catch (AttributeResolutionException e) {
            String failoverDataConnectorId = dataConnector.getFailoverDependencyId();

            if (DatatypeHelper.isEmpty(failoverDataConnectorId)) {
                log.error("Received the following error from data connector " + dataConnector.getId()
                        + ", no failover data connector available", e);
                throw e;
            }

            log.warn("Received the following error from data connector " + dataConnector.getId()
                    + ", trying its failover connector " + failoverDataConnectorId, e.getMessage());
            log.debug("Error recieved from data connector " + dataConnector.getId(), e);
            resolveDataConnector(failoverDataConnectorId, resolutionContext);

            DataConnector failoverConnector =
                    resolutionContext.getResolvedDataConnectors().get(failoverDataConnectorId);
            log.debug("Using failover connector {} in place of {} for the remainder of this resolution",
                    failoverConnector.getId(), connectorID);
            resolutionContext.getResolvedPlugins().put(connectorID, failoverConnector);
        }
    }

    /**
     * Resolves all the dependencies for a given plugin.
     * 
     * @param plugin plugin whose dependencies should be resolved
     * @param resolutionContext current resolution context
     * 
     * @throws AttributeResolutionException thrown if there is a problem resolving a dependency
     */
    protected void resolveDependencies(ResolutionPlugIn<?> plugin, ShibbolethResolutionContext resolutionContext)
            throws AttributeResolutionException {

        for (String dependency : plugin.getDependencyIds()) {
            if (dataConnectors.containsKey(dependency)) {
                resolveDataConnector(dependency, resolutionContext);
            } else if (definitions.containsKey(dependency)) {
                resolveAttribute(dependency, resolutionContext);
            }
        }
    }

    /**
     * Removes attributes that contain no values or those which are dependency only.
     * 
     * @param resolvedAttributes attribute set to clean up
     * @param resolutionContext current resolution context
     */
    protected void cleanResolvedAttributes(Map<String, BaseAttribute> resolvedAttributes,
            ShibbolethResolutionContext resolutionContext) {
        AttributeDefinition attributeDefinition;

        Iterator<Entry<String, BaseAttribute>> attributeItr = resolvedAttributes.entrySet().iterator();
        BaseAttribute<?> resolvedAttribute;
        Set<Object> values;
        while (attributeItr.hasNext()) {
            resolvedAttribute = attributeItr.next().getValue();

            // remove nulls
            if (resolvedAttribute == null) {
                attributeItr.remove();
                continue;
            }

            // remove dependency-only attributes
            attributeDefinition = getAttributeDefinitions().get(resolvedAttribute.getId());
            if (attributeDefinition.isDependencyOnly()) {
                log.debug("Removing dependency-only attribute {} from resolution result for principal {}.",
                        resolvedAttribute.getId(), resolutionContext.getAttributeRequestContext().getPrincipalName());
                attributeItr.remove();
                continue;
            }

            // remove value-less attributes
            if (resolvedAttribute.getValues().size() == 0) {
                log.debug("Removing attribute {} from resolution result for principal {}.  It contains no values.",
                        resolvedAttribute.getId(), resolutionContext.getAttributeRequestContext().getPrincipalName());
                attributeItr.remove();
                continue;
            }

            // remove duplicate attribute values
            Iterator<?> valueItr = resolvedAttribute.getValues().iterator();
            values = new HashSet<Object>();
            while (valueItr.hasNext()) {
                Object value = valueItr.next();
                if (!values.add(value)) {
                    log.debug("Removing duplicate value {} of attribute {} from resolution result", value,
                            resolvedAttribute.getId());
                    valueItr.remove();
                }
            }

            log.debug("Attribute {} has {} values after post-processing", resolvedAttribute.getId(), resolvedAttribute
                    .getValues().size());
        }
    }

    /**
     * Add a resolution plug-in and dependencies to a directed graph.
     * 
     * @param graph directed graph
     * @param plugin plug-in to add
     */
    protected void addVertex(DirectedGraph<ResolutionPlugIn, DefaultEdge> graph, ResolutionPlugIn<?> plugin) {
        graph.addVertex(plugin);
        ResolutionPlugIn<?> dependency = null;

        // add edges for dependencies
        for (String id : plugin.getDependencyIds()) {
            if (dataConnectors.containsKey(id)) {
                dependency = dataConnectors.get(id);
            } else if (definitions.containsKey(id)) {
                dependency = definitions.get(id);
            }

            if (dependency != null) {
                graph.addVertex(dependency);
                graph.addEdge(plugin, dependency);
            }
        }
    }

    /** {@inheritDoc} */
    protected void onNewContextCreated(ApplicationContext newServiceContext) throws ServiceException {
        String[] beanNames;

        Map<String, DataConnector> oldDataConnectors = dataConnectors;
        Map<String, DataConnector> newDataConnectors = new HashMap<String, DataConnector>();
        DataConnector dConnector;
        beanNames = newServiceContext.getBeanNamesForType(DataConnector.class);
        log.debug("Loading {} data connectors", beanNames.length);
        for (String beanName : beanNames) {
            dConnector = (DataConnector) newServiceContext.getBean(beanName);
            newDataConnectors.put(dConnector.getId(), dConnector);
        }

        Map<String, AttributeDefinition> oldAttributeDefinitions = definitions;
        Map<String, AttributeDefinition> newAttributeDefinitions = new HashMap<String, AttributeDefinition>();
        AttributeDefinition aDefinition;
        beanNames = newServiceContext.getBeanNamesForType(AttributeDefinition.class);
        log.debug("Loading {} attribute definitions", beanNames.length);
        for (String beanName : beanNames) {
            aDefinition = (AttributeDefinition) newServiceContext.getBean(beanName);
            newAttributeDefinitions.put(aDefinition.getId(), aDefinition);
        }

        Map<String, PrincipalConnector> oldPrincipalConnectors = principalConnectors;
        Map<String, PrincipalConnector> newPrincipalConnectors = new HashMap<String, PrincipalConnector>();
        PrincipalConnector pConnector;
        beanNames = newServiceContext.getBeanNamesForType(PrincipalConnector.class);
        log.debug("Loading {} principal connectors", beanNames.length);
        for (String beanName : beanNames) {
            pConnector = (PrincipalConnector) newServiceContext.getBean(beanName);
            newPrincipalConnectors.put(pConnector.getId(), pConnector);
        }

        try {
            dataConnectors = newDataConnectors;
            definitions = newAttributeDefinitions;
            principalConnectors = newPrincipalConnectors;
            validate();
        } catch (AttributeResolutionException e) {
            dataConnectors = oldDataConnectors;
            definitions = oldAttributeDefinitions;
            principalConnectors = oldPrincipalConnectors;
            throw new ServiceException(getId() + " configuration is not valid, retaining old configuration", e);
        }
    }
}