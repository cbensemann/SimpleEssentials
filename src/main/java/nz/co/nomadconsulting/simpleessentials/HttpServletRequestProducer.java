package nz.co.nomadconsulting.simpleessentials;

import javax.enterprise.context.RequestScoped;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;

@WebListener 
public class HttpServletRequestProducer implements ServletRequestListener {
    private final static ThreadLocal<HttpServletRequest> holder = new ThreadLocal<HttpServletRequest>();

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        holder.remove();
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        holder.set((HttpServletRequest)sre.getServletRequest());
    }

    @Produces @RequestScoped HttpServletRequest get() {
        return holder.get();
    }
}