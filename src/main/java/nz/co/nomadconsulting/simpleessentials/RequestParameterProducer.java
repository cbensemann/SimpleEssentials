package nz.co.nomadconsulting.simpleessentials;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;

public class RequestParameterProducer {
    
    @Inject
    private HttpServletRequest request;

    @Produces
    @RequestScoped
    @RequestParameter
    public String getRequestParameter(HttpServletRequest request, InjectionPoint ip) {
        String name = ip.getAnnotated().getAnnotation(RequestParameter.class).value();
        if (name.isEmpty()) {
            name = ip.getMember().getName();
        }
        return request.getParameter(name);
    }
    
    @Produces
    @RequestScoped
    @RequestParameter
    public String getRequestParameter(InjectionPoint ip) {
        String name = ip.getAnnotated().getAnnotation(RequestParameter.class).value();
        if (name.isEmpty()) {
            name = ip.getMember().getName();
        }
        return request.getParameter(name);
    }
}
