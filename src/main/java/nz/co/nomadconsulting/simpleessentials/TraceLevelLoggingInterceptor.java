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

import java.util.logging.Logger;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;


public class TraceLevelLoggingInterceptor {

    @AroundInvoke
    public Object authorisationCheck(final InvocationContext ctx) throws Exception {
        final Logger logger = Logger.getLogger(ctx.getTarget().getClass().getName());

        logger.finest(">>> " + ctx.getMethod().getName());
        try {
            return ctx.proceed();
        }
        finally {
            logger.finest("<<< " + ctx.getMethod().getName());
        }
    }
}
