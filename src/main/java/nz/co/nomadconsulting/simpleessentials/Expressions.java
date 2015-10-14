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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELContextEvent;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.el.ValueReference;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.weld.el.WeldELContextListener;
import org.jboss.weld.el.WeldExpressionFactory;
import org.jboss.weld.util.reflection.Reflections;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleResolver;


/**
 * @see http://4thline.org/articles/Java%20EL%20in%20CDI%20without%20JSF.html
 *
 */
public class Expressions {

    public static final class Factory extends WeldExpressionFactory {
        public Factory() {
            super(new ExpressionFactoryImpl());
        }
    }

    public final static Pattern PATTERN = Pattern.compile("#\\{(.+?)\\}");

    public final static ExpressionFactory expressionFactory = ExpressionFactory.newInstance();

    final protected ELContext context;


    @Inject
    public Expressions(final BeanManager beanManager) {
        // Chain the resolvers, the Weld resolver first, then
        // read-only "simple" bean/map/list resolvers
        final CompositeELResolver compositeELResolver = new CompositeELResolver();
        compositeELResolver.add(beanManager.getELResolver());
        compositeELResolver.add(new SimpleResolver(true));
        context = new de.odysseus.el.util.SimpleContext(compositeELResolver);

        // Let Weld know about the context, so it can handle dependent
        // scope properly if beans are instantiated by EL expressions
        final ELContextEvent event = new ELContextEvent(context);
        new WeldELContextListener().contextCreated(event);
    }


    public ExpressionFactory getExpressionFactory() {
        return expressionFactory;
    }


    public ELContext getContext() {
        return context;
    }


    public <T> T evaluateValueExpression(final String expression, final Class<T> expectedType) {
        final Object result = getExpressionFactory().createValueExpression(context, expression, expectedType).getValue(context);
        return result != null ? expectedType.cast(result) : null;
    }
    
    
    public ValueReference getValueExpression(final ValueExpression valueExpression) {
        return valueExpression.getValueReference(context);
    }


    public <T> T evaluateValueExpression(final String expression) {
        final Object result = evaluateValueExpression(expression, Object.class);
        return result != null ? Reflections.<T> cast(result) : null;
    }


    public <T> T evaluateMethodExpression(final String expression, final Class<T> expectedReturnType, final Object[] params,
            final Class<?>[] expectedParamTypes) {
        final Object result = getExpressionFactory().createMethodExpression(context, expression, expectedReturnType, expectedParamTypes)
                .invoke(context, params);
        return result != null ? expectedReturnType.cast(result) : null;
    }


    public <T> T evaluateMethodExpression(final String expression, final Class<T> expectedReturnType) {
        return evaluateMethodExpression(expression, expectedReturnType, new Object[0], new Class[0]);
    }


    public <T> T evaluateMethodExpression(final String expression) {
        final Object result = evaluateMethodExpression(expression, Object.class);
        return result != null ? Reflections.<T> cast(result) : null;
    }


    public <T> T evaluateMethodExpression(final String expression, final Object... params) {
        final Object result = evaluateMethodExpression(expression, Object.class, params, new Class[params.length]);
        return result != null ? Reflections.<T> cast(result) : null;
    }


    public String toExpression(final String name) {
        return "#{" + name + "}";
    }


    public <T> void addVariableValue(final String name, final Class<T> type, final T value) {
        getContext().getVariableMapper().setVariable(name, getExpressionFactory().createValueExpression(value, type));
    }


    public String evaluateAllValueExpressions(final String s) {
        final StringBuffer sb = new StringBuffer();
        final Matcher matcher = PATTERN.matcher(s);
        while (matcher.find()) {
            final String expression = toExpression(matcher.group(1));
            final Object result = evaluateValueExpression(expression);
            matcher.appendReplacement(sb, result != null ? result.toString() : "");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
