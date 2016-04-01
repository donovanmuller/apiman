/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.manager.api.beans.apis;

import io.apiman.manager.api.beans.orgs.OrganizationBasedCompositeId;
import io.apiman.manager.api.beans.orgs.OrganizationBean;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Models an API.
 *
 * @author eric.wittmann@redhat.com
 */
@Entity
@Table(name = "apis")
@IdClass(OrganizationBasedCompositeId.class)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ApiBean implements Serializable {

    private static final long serialVersionUID = 1526742536153467539L;

    @Id
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="organization_id", referencedColumnName="id")
    })
    private OrganizationBean organization;
    @Id
    @Column(nullable=false)
    private String id;
    @Column(nullable=false)
    private String name;
    @Column(updatable=true, nullable=true, length=512)
    private String description;
    @Column(name = "created_by", updatable=false, nullable=false)
    private String createdBy;
    @Column(name = "created_on", updatable=false, nullable=false)
    private Date createdOn;
    @Column(name = "num_published", updatable=true, nullable=true)
    private Integer numPublished;

    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval=true, fetch = FetchType.EAGER, mappedBy="api")
    @JsonManagedReference
    Set<ApiVersionBean> apiVersionBeans = new LinkedHashSet<>();

    /**
     * Constructor.
     */
    public ApiBean() {
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the createdOn
     */
    public Date getCreatedOn() {
        return createdOn;
    }

    /**
     * @param createdOn the createdOn to set
     */
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the organization
     */
    public OrganizationBean getOrganization() {
        return organization;
    }

    /**
     * @param organization the organization to set
     */
    public void setOrganization(OrganizationBean organization) {
        this.organization = organization;
    }

    /**
     * @return the numPublished
     */
    public Integer getNumPublished() {
        return numPublished;
    }

    /**
     * @param numPublished the numPublished to set
     */
    public void setNumPublished(Integer numPublished) {
        this.numPublished = numPublished;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "APIBean [organization=" + organization + ", id=" + id + ", name=" + name
                + ", description=" + description + ", createdBy=" + createdBy + ", createdOn=" + createdOn
                + "]";
    }

//    @PreRemove
//    public void preRemove() {
//        organization = null;
//    }

//    public Set<ApiVersionBean> getApiVersionBeans() {
//        return apiVersionBeans;
//    }
//
//    public void setApiVersionBeans(Set<ApiVersionBean> apiVersionBeans) {
//        this.apiVersionBeans = apiVersionBeans;
//    }
}
