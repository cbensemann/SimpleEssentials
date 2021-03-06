package nz.co.nomadconsulting.simpleessentials.jsf;

import nz.co.nomadconsulting.simpleessentials.Expressions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.el.ValueExpression;
import javax.el.ValueReference;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.FacesComponent;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIMessage;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.context.FacesContext;
import javax.faces.validator.BeanValidator;
import javax.inject.Inject;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;


@FacesComponent(Decorator.COMPONENT_TYPE)
public class Decorator extends UIComponentBase implements NamingContainer {

    @Inject
    private transient Expressions expressions;
    
    /**
     * The standard component type for this component.
     */
    public static final String COMPONENT_TYPE = "nz.co.nomadconsulting.simpleessentials.jsf.Decorator";

    protected static final String HTML_ID_ATTR_NAME = "id";

    protected static final String HTML_CLASS_ATTR_NAME = "class";

    protected static final String HTML_STYLE_ATTR_NAME = "style";

    private boolean beanValidationPresent = false;


    public Decorator() {
        beanValidationPresent = isClassPresent("javax.validation.Validator");
    }


    @Override
    public String getFamily() {
        return UINamingContainer.COMPONENT_FAMILY;
    }


    /**
     * The name of the auto-generated composite component attribute that holds a boolean indicating whether the the
     * template contains an invalid input.
     */
    public String getInvalidAttributeName() {
        return "invalid";
    }


    /**
     * The name of the auto-generated composite component attribute that holds a boolean indicating whether the template
     * contains a required input.
     */
    public String getRequiredAttributeName() {
        return "required";
    }


    /**
     * The name of the composite component attribute that holds the string label for this set of inputs. If the label
     * attribute is not provided, one will be generated from the id of the composite component or, if the id is
     * defaulted, the name of the property bound to the first input.
     */
    public String getLabelAttributeName() {
        return "label";
    }


    /**
     * The name of the auto-generated composite component attribute that holds the elements in this input container. The
     * elements include the label, a list of inputs and a cooresponding list of messages.
     */
    public String getElementsAttributeName() {
        return "elements";
    }


    /**
     * The name of the composite component attribute that holds a boolean indicating whether the component template
     * should be enclosed in an HTML element, so that it be referenced from JavaScript.
     */
    public String getEncloseAttributeName() {
        return "enclose";
    }


    public String getContainerElementName() {
        return "div";
    }


    public String getDefaultLabelId() {
        return "label";
    }


    public String getDefaultInputId() {
        return "input";
    }


    public String getDefaultMessageId() {
        return "message";
    }


    @Override
    public void encodeBegin(final FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }

        super.encodeBegin(context);

        final InputContainerElements elements = scan(getFacet(UIComponent.COMPOSITE_FACET_NAME), null, context);
        // assignIds(elements, context);
        wire(elements, context);

        getAttributes().put(getElementsAttributeName(), elements);

        if (elements.hasValidationError()) {
            getAttributes().put(getInvalidAttributeName(), true);
        }

        // set the required attribute, but only if the user didn't already assign it
        if (!getAttributes().containsKey(getRequiredAttributeName()) && elements.hasRequiredInput()) {
            getAttributes().put(getRequiredAttributeName(), true);
        }

        if (!getAttributes().containsKey(getLabelAttributeName())) {
            getAttributes().put(getLabelAttributeName(), generateLabel(elements, context));
        }

        if (Boolean.TRUE.equals(getAttributes().get(getEncloseAttributeName()))) {
            startContainerElement(context);
        }
    }


    @Override
    public void encodeEnd(final FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }

        super.encodeEnd(context);

        if (Boolean.TRUE.equals(getAttributes().get(getEncloseAttributeName()))) {
            endContainerElement(context);
        }
    }


    protected void startContainerElement(final FacesContext context) throws IOException {
        context.getResponseWriter().startElement(getContainerElementName(), this);
        final String style = getAttributes().get("style") != null ? getAttributes().get("style").toString().trim() : null;
        if (style.length() > 0) {
            context.getResponseWriter().writeAttribute(HTML_STYLE_ATTR_NAME, style, HTML_STYLE_ATTR_NAME);
        }
        final String styleClass = getAttributes().get("styleClass") != null ? getAttributes().get("styleClass").toString().trim() : null;
        if (styleClass.length() > 0) {
            context.getResponseWriter().writeAttribute(HTML_CLASS_ATTR_NAME, styleClass, HTML_CLASS_ATTR_NAME);
        }
        context.getResponseWriter().writeAttribute(HTML_ID_ATTR_NAME, getClientId(context), HTML_ID_ATTR_NAME);
    }


    protected void endContainerElement(final FacesContext context) throws IOException {
        context.getResponseWriter().endElement(getContainerElementName());
    }


    protected String generateLabel(final InputContainerElements elements, final FacesContext context) {
        final String name = getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX) ? elements.getPropertyName(context) : getId();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }


    /**
     * Walk the component tree branch built by the composite component and locate the input container elements.
     *
     * @return a composite object of the input container elements
     */
    protected InputContainerElements scan(final UIComponent component, InputContainerElements elements, final FacesContext context) {
        if (elements == null) {
            elements = new InputContainerElements(expressions);
        }

        // NOTE we need to walk the tree ignoring rendered attribute because it's condition
        // could be based on what we discover
        if (elements.getLabel() == null && component instanceof HtmlOutputLabel) {
            elements.setLabel((HtmlOutputLabel) component);
        }
        else if (component instanceof EditableValueHolder) {
            elements.registerInput((EditableValueHolder) component, getDefaultValidator(context), context);
        }
        else if (component instanceof UIMessage) {
            elements.registerMessage((UIMessage) component);
        }
        // may need to walk smarter to ensure "element of least suprise"
        for (final UIComponent child : component.getChildren()) {
            scan(child, elements, context);
        }

        return elements;
    }


    // assigning ids seems to break form submissions, but I don't know why
    public void assignIds(final InputContainerElements elements, final FacesContext context) {
        boolean refreshIds = false;
        if (getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
            setId(elements.getPropertyName(context));
            refreshIds = true;
        }
        final UIComponent label = elements.getLabel();
        if (label != null) {
            if (label.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
                label.setId(getDefaultLabelId());
            }
            else if (refreshIds) {
                label.setId(label.getId());
            }
        }
        for (int i = 0, len = elements.getInputs().size(); i < len; i++) {
            final UIComponent input = (UIComponent) elements.getInputs().get(i);
            if (input.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
                input.setId(getDefaultInputId() + (i == 0 ? "" : i + 1));
            }
            else if (refreshIds) {
                input.setId(input.getId());
            }
        }
        for (int i = 0, len = elements.getMessages().size(); i < len; i++) {
            final UIComponent msg = elements.getMessages().get(i);
            if (msg.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
                msg.setId(getDefaultMessageId() + (i == 0 ? "" : i + 1));
            }
            else if (refreshIds) {
                msg.setId(msg.getId());
            }
        }
    }


    /**
     * Wire the label and messages to the input(s)
     */
    protected void wire(final InputContainerElements elements, final FacesContext context) {
        elements.wire(context);
    }


    /**
     * Get the default Bean Validation Validator to read the contraints for a property.
     */
    private Validator getDefaultValidator(final FacesContext context) throws FacesException {
        if (!beanValidationPresent) {
            return null;
        }

        ValidatorFactory validatorFactory;
        final Object cachedObject = context.getExternalContext().getApplicationMap().get(BeanValidator.VALIDATOR_FACTORY_KEY);
        if (cachedObject instanceof ValidatorFactory) {
            validatorFactory = (ValidatorFactory) cachedObject;
        }
        else {
            try {
                validatorFactory = Validation.buildDefaultValidatorFactory();
            }
            catch (final ValidationException e) {
                throw new FacesException("Could not build a default Bean Validator factory", e);
            }
            context.getExternalContext().getApplicationMap().put(BeanValidator.VALIDATOR_FACTORY_KEY, validatorFactory);
        }
        return validatorFactory.getValidator();
    }


    private boolean isClassPresent(final String fqcn) {
        try {
            if (Thread.currentThread().getContextClassLoader() != null) {
                return Thread.currentThread().getContextClassLoader().loadClass(fqcn) != null;
            }
            else {
                return Class.forName(fqcn) != null;
            }
        }
        catch (final ClassNotFoundException e) {
            return false;
        }
        catch (final NoClassDefFoundError e) {
            return false;
        }
    }

    public static class InputContainerElements {
        private String propertyName;

        private HtmlOutputLabel label;

        private final List<EditableValueHolder> inputs = new ArrayList<EditableValueHolder>();

        private final List<UIMessage> messages = new ArrayList<UIMessage>();

        private boolean validationError = false;

        private boolean requiredInput = false;

        private Expressions expressions;


        public InputContainerElements(Expressions expressions) {
            this.expressions = expressions;
            // TODO Auto-generated constructor stub
        }


        public HtmlOutputLabel getLabel() {
            return label;
        }


        public void setLabel(final HtmlOutputLabel label) {
            this.label = label;
        }


        public List<EditableValueHolder> getInputs() {
            return inputs;
        }


        public void registerInput(final EditableValueHolder input, final Validator validator, final FacesContext context) {
            inputs.add(input);
            if (input.isRequired() || isRequiredByConstraint(input, validator, context)) {
                requiredInput = true;
            }
            if (!input.isValid()) {
                validationError = true;
            }
            // optimization to avoid loop if already flagged
            else if (!validationError) {
                final Iterator<FacesMessage> it = context.getMessages(((UIComponent) input).getClientId(context));
                while (it.hasNext()) {
                    if (it.next().getSeverity().compareTo(FacesMessage.SEVERITY_WARN) >= 0) {
                        validationError = true;
                        break;
                    }
                }
            }
        }


        public List<UIMessage> getMessages() {
            return messages;
        }


        public void registerMessage(final UIMessage message) {
            messages.add(message);
        }


        public boolean hasValidationError() {
            return validationError;
        }


        public boolean hasRequiredInput() {
            return requiredInput;
        }


        private boolean isRequiredByConstraint(final EditableValueHolder input, final Validator validator, final FacesContext context) {
            if (validator == null) {
                return false;
            }

            // NOTE believe it or not, getValueReference on ValueExpression is broken, so we have to do it ourselves
            final ValueExpression valueExpression = ((UIComponent) input).getValueExpression("value");
            if (valueExpression != null) {
//                ValueReference vref = valueExpression.getValueReference(context.getELContext());
                final ValueReference vref = expressions.getValueExpression(valueExpression);
                BeanDescriptor constraintsForClass = validator.getConstraintsForClass(vref.getBase().getClass());
                PropertyDescriptor d = constraintsForClass.getConstraintsForProperty((String) vref.getProperty());
                return (d != null) && d.hasConstraints();
            }
//            ValueExpression valueExpression = ((UIComponent) input).getValueExpression("value");
//            if (valueExpression != null)
//            {
//               valueExpressionAnalyzer valueExpressionAnalyzer = new ValueExpressionAnalyzer(valueExpression);
//               ValueReference vref = valueExpressionAnalyzer.getValueReference(context.getELContext());
//               BeanDescriptor constraintsForClass = validator.getConstraintsForClass(vref.getBase().getClass());
//               PropertyDescriptor d = constraintsForClass.getConstraintsForProperty((String) vref.getProperty());
//               return (d != null) && d.hasConstraints();
//            }
            return false;
        }


        public String getPropertyName(final FacesContext context) {
            if (propertyName != null) {
                return propertyName;
            }

            if (inputs.size() == 0) {
                return null;
            }
            propertyName = (String) ((UIComponent) inputs.get(0)).getValueExpression("value").getValueReference(context.getELContext()).getProperty();
            return propertyName;
        }


        public void wire(final FacesContext context) {
            final int numInputs = inputs.size();
            if (numInputs > 0) {
                if (label != null) {
                    label.setFor(((UIComponent) inputs.get(0)).getClientId(context));
                }
                for (int i = 0, len = messages.size(); i < len; i++) {
                    if (i < numInputs) {
                        messages.get(i).setFor(((UIComponent) inputs.get(i)).getClientId(context));
                    }
                }
            }
        }
    }
}
