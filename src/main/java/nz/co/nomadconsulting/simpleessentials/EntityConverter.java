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
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;


@SuppressWarnings("serial")
@ConversationScoped
@Named
public class EntityConverter implements Converter, Serializable {
    
    @Inject
    private EntityManager em;
    
    private final List<Identifier> identifiers = new LinkedList<EntityConverter.Identifier>();
    

    @Override
    public Object getAsObject(final FacesContext context, final UIComponent uIcomponent, final String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        final Identifier identifier = identifiers.get(new Integer(value));
        return em.find(identifier.clazz, identifier.id);
    }


    @Override
    public String getAsString(final FacesContext context, final UIComponent uIcomponent, final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        final Identifier identifier = new Identifier(value.getClass(), Entities.getIdentifier(value, em));
        identifiers.add(identifier);
        
        return String.valueOf(identifiers.indexOf(identifier));
    }
    
    
    static class Identifier implements Serializable {
        private Class<?> clazz;

        private Object id;


        public Identifier(Class<?> clazz, Object id) {
            if (clazz == null || id == null)
            {
                throw new IllegalArgumentException("Id and clazz must not be null");
            }
            this.clazz = clazz;
            this.id = id;
        }


        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }


        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Identifier other = (Identifier) obj;
            if (clazz == null) {
                if (other.clazz != null)
                    return false;
            }
            else if (!clazz.equals(other.clazz))
                return false;
            if (id == null) {
                if (other.id != null)
                    return false;
            }
            else if (!id.equals(other.id))
                return false;
            return true;
        }
    }
}
