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
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.inject.Inject;


/**
 * Utility class used to create {@link FacesMessage}s 
 */
@SuppressWarnings("serial")
public class FacesMessages implements Serializable {

    @Inject
    private transient FacesContext facesContext;


    public void postFieldMessage(final String subjectMessageKey, final Object subjectParams[], final Severity severity, final String fieldId) {
        this.postFieldMessage(subjectMessageKey, subjectParams, null, null, severity, fieldId);
    }


    public void postFieldMessage(final String subjectMessageKey, final Object subjectParams[], final String detailMessageKey,
            final Object detailParams[], final Severity severity, final String fieldId) {
        final UIViewRoot viewRoot = facesContext.getViewRoot();
        final UIInput uiField = (UIInput) viewRoot.findComponent(fieldId);
        final Locale myLocale = facesContext.getExternalContext().getRequestLocale();
        final ResourceBundle myResources = ResourceBundle.getBundle("messages", myLocale);

        String subjectMessageFromResource = null;
        if (subjectMessageKey != null) {
            subjectMessageFromResource = myResources.getString(subjectMessageKey);
            if (subjectParams != null) {
                final MessageFormat mf = new MessageFormat(subjectMessageFromResource, myLocale);
                subjectMessageFromResource = mf.format(subjectParams, new StringBuffer(), null).toString();
            }
        }

        String detailMessageFromResource = null;
        if (detailMessageKey != null) {
            detailMessageFromResource = myResources.getString(detailMessageKey);
            if (detailParams != null) {
                final MessageFormat mf = new MessageFormat(detailMessageFromResource, myLocale);
                detailMessageFromResource = mf.format(detailParams, new StringBuffer(), null).toString();
            }
        }

        FacesMessage fieldMessage = null;
        if (subjectMessageKey != null && detailMessageKey != null) {
            fieldMessage = new FacesMessage(subjectMessageFromResource, detailMessageFromResource);
        }
        else if (subjectMessageKey != null && detailMessageKey == null) {
            fieldMessage = new FacesMessage(subjectMessageFromResource);
        }
        else if (subjectMessageKey == null && detailMessageKey != null) {
            fieldMessage = new FacesMessage(null, detailMessageFromResource);
        }
        else if (subjectMessageKey == null && detailMessageKey == null) {
            throw new RuntimeException("Attempt to post filed message with no subject or detail");
        }
        fieldMessage.setSeverity(severity);
        facesContext.addMessage(uiField.getClientId(), fieldMessage);
    }


    public void postFieldMessage(final String subjectMessageKey, final Severity severity, final String fieldId) {
        this.postFieldMessage(subjectMessageKey, null, null, null, severity, fieldId);
    }


    // Thanks to hack in allTheJSFFixes (MultiPageMessageSupport) this can now survive a redirect.

    public void postGlobalMessage(final String subjectMessageKey, final Severity severity, final Object... subjectParams) {
        this.postGlobalMessage(subjectMessageKey, subjectParams, null, null, severity);
    }


    public void postGlobalMessage(final String subjectMessageKey, final Object subjectParams[], final String detailMessageKey,
            final Object detailParams[], final Severity severity) {
        final Locale myLocale = facesContext.getExternalContext().getRequestLocale();
        final ResourceBundle myResources = ResourceBundle.getBundle("messages", myLocale);

        String subjectMessageFromResource = null;
        if (subjectMessageKey != null) {
            subjectMessageFromResource = myResources.getString(subjectMessageKey);
            if (subjectParams != null) {
                final MessageFormat mf = new MessageFormat(subjectMessageFromResource, myLocale);
                subjectMessageFromResource = mf.format(subjectParams, new StringBuffer(), null).toString();
            }
        }

        String detailMessageFromResource = null;
        if (detailMessageKey != null) {
            detailMessageFromResource = myResources.getString(detailMessageKey);
            if (detailParams != null) {
                final MessageFormat mf = new MessageFormat(detailMessageFromResource, myLocale);
                detailMessageFromResource = mf.format(detailParams, new StringBuffer(), null).toString();
            }
        }

        FacesMessage globalMessage = null;
        if (subjectMessageKey != null && detailMessageKey != null) {
            globalMessage = new FacesMessage(subjectMessageFromResource, detailMessageFromResource);
        }
        else if (subjectMessageKey != null && detailMessageKey == null) {
            globalMessage = new FacesMessage(subjectMessageFromResource);
        }
        else if (subjectMessageKey == null && detailMessageKey != null) {
            globalMessage = new FacesMessage(null, detailMessageFromResource);
        }
        else if (subjectMessageKey == null && detailMessageKey == null) {
            throw new RuntimeException("Attempt to post global message with no subject or detail");
        }

        globalMessage.setSeverity(severity);
        facesContext.addMessage(null, globalMessage);
    }


    public void postGlobalMessage(final String subjectMessageKey, final Severity severity) {
        this.postGlobalMessage(subjectMessageKey, null, null, null, severity);
    }
}
