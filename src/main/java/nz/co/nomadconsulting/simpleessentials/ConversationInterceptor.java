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
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.enterprise.context.Conversation;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;


@SuppressWarnings("serial")
@Interceptor
@ConversationalBinding
public class ConversationInterceptor implements Serializable {

    private static final Logger log = Logger.getLogger(ConversationInterceptor.class.getName());

    @Inject
    private Conversation conversation;


    @AroundInvoke
    public Object authorisationCheck(final InvocationContext ctx) throws Exception {
        final Method method = ctx.getMethod();
        if (method.isAnnotationPresent(Begin.class) && conversation.isTransient()) {
            conversation.begin();
            log.fine("Beginning long running conversation" + conversation.getId());
        }

        try {
            final Object objectToReturn = ctx.proceed();
            if (method.isAnnotationPresent(End.class) && isLongRunning()) {
                endConversation();
            }
            return objectToReturn;
        }
        catch (final Exception e) {
            if (isLongRunning() && isEndOnError(method)) {
                endConversation();
            }
            throw e;
        }
    }


    protected boolean isEndOnError(final Method method) {
        final End annotation = method.getAnnotation(End.class);
        return annotation != null && annotation.onError();
    }


    private boolean isLongRunning() {
        return !conversation.isTransient();
    }


    private void endConversation() {
        conversation.end();
        log.fine("Ending long running conversation" + conversation.getId());
    }
}
