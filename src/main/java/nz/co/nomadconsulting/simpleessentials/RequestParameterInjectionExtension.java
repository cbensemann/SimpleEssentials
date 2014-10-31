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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;


public class RequestParameterInjectionExtension implements Extension {

    public <T> void processAnnotatedType(@Observes @WithAnnotations(RequestParameter.class) final ProcessAnnotatedType<T> pat) {
        final AnnotatedType<T> type = pat.getAnnotatedType();

        AnnotatedType<T> wrapped = new AnnotatedType<T>() {

            @SuppressWarnings("serial")
            class InjectLiteral extends AnnotationLiteral<Inject> implements Inject {
            }

            private final InjectLiteral injectLiteral = new InjectLiteral();


            @SuppressWarnings("unchecked")
            @Override
            public <X extends Annotation> X getAnnotation(final Class<X> annType) {
                return (X) (annType.equals(Inject.class) ? injectLiteral : type.getAnnotation(annType));
            }


            @Override
            public Set<Annotation> getAnnotations() {
                Set<Annotation> annotations = new HashSet<Annotation>(type.getAnnotations());
                annotations.add(injectLiteral);

                return annotations;
            }


            @Override
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                return annotationType.equals(Inject.class) ?
                        true : type.isAnnotationPresent(annotationType);
            }


            @Override
            public Type getBaseType() {
                return type.getBaseType();
            }


            @Override
            public Set<Type> getTypeClosure() {
                return type.getTypeClosure();
            }


            @Override
            public Class<T> getJavaClass() {
                return type.getJavaClass();
            }


            @Override
            public Set<AnnotatedConstructor<T>> getConstructors() {
                return type.getConstructors();
            }


            @Override
            public Set<AnnotatedMethod<? super T>> getMethods() {
                return type.getMethods();
            }


            @Override
            public Set<AnnotatedField<? super T>> getFields() {
                return type.getFields();
            }
        };

        pat.setAnnotatedType(wrapped);
    }
}
