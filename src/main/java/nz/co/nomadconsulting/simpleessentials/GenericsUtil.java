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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.persistence.Entity;


public class GenericsUtil {

    public static <E> Class<E> extract(final Class<?> theClass) {
        for (final Type inf : theClass.getGenericInterfaces()) {
            final Class<E> result = extractFrom(inf);
            if (result != null) {
                return result;
            }
        }
        final Class<E> result = extractFrom(theClass.getGenericSuperclass());
        if (result != null) {
            return result;
        }

        if (theClass.getSuperclass() != null) {
            return extract(theClass.getSuperclass());
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    private static <E> Class<E> extractFrom(final Type type) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        final ParameterizedType parametrizedType = (ParameterizedType) type;
        final Type[] genericTypes = parametrizedType.getActualTypeArguments();
        Class<E> result = null;
        for (final Type genericType : genericTypes) {
            if (genericType instanceof Class && ((Class<?>) genericType).isAnnotationPresent(Entity.class)) {
                result = (Class<E>) genericType;
                break;
            }
        }
        return result;
    }
}
