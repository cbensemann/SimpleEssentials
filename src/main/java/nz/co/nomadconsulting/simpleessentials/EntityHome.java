/*
 * Copyright 2014 Nomad Consulting Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.nomadconsulting.simpleessentials;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;


@SuppressWarnings("serial")
public abstract class EntityHome<E, PK extends Serializable> implements Serializable {

    private PK id;

    private E entity;

    private Class<E> entityClass;

    private List<E> entities;


    @SuppressWarnings("unchecked")
    @PostConstruct
    protected void postConstruct() {
        try {
            entityClass = GenericsUtil.extract((Class<E>) getClass());
        }
        catch (final Exception e) {
            throw new RuntimeException("Failed initializing EntityHome", e);
        }
    }


    public E create() {
        if (entity == null) {
            try {
                entity = entityClass.newInstance();
            }
            catch (InstantiationException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return entity;
    }


    public E getEntity() {
        if (entity == null) {
            if (id == null) {
                return create();
            }
            else {
                return find();
            }
        }
        else {
            return entity;
        }
    }


    public E find() {
        if (entity == null) {
            entity = getEntityManager().find(entityClass, id);
        }

        return entity;
    }


    public boolean isManaged() {
        return id != null;
    }


    public void update() {
        // its a hack. I know it and you know it
        // https://struberg.wordpress.com/2012/04/25/is-there-a-way-to-fix-the-jpa-entitymanager/
        entity = getEntityManager().merge(entity);
        save();
    }


    public void save() {
        getEntityManager().persist(entity);
    }


    public void delete() {
        // its a hack. I know it and you know it
        // https://struberg.wordpress.com/2012/04/25/is-there-a-way-to-fix-the-jpa-entitymanager/
        entity = getEntityManager().merge(entity);
        getEntityManager().remove(entity);
    }


    public List<E> list() {
        if (entities == null) {
            entities = getEntityManager().createQuery(listQuery(), entityClass).getResultList();
        }
        return entities;
    }


    protected void clearEntities() {
        entities = null;
    }


    protected String listQuery() {
        return "FROM " + entityClass.getName();
    }


    public PK getId() {
        return id;
    }


    public void setId(final PK id) {
        this.id = id;
    }


    public abstract EntityManager getEntityManager();
}
