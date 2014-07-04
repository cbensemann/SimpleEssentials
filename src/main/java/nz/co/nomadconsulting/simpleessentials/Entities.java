package nz.co.nomadconsulting.simpleessentials;

import java.lang.reflect.Field;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

public class Entities {

    public static Object getIdentifier(final Object bean, final EntityManager em) {
        final String idProperty = getIdProperty(bean.getClass(), em);
        try {
            final Field field = bean.getClass().getDeclaredField(idProperty);
            field.setAccessible(true);
            return field.get(bean);
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("TODO i should be a proper exception");
        }
    }
    
    private static String getIdProperty(final Class<?> entityClass, final EntityManager em) {
        String idProperty = null;
        final Metamodel metamodel = em.getMetamodel();
        final EntityType entity = metamodel.entity(entityClass);
        final Set<SingularAttribute> singularAttributes = entity.getSingularAttributes();
        for (final SingularAttribute singularAttribute : singularAttributes) {
            if (singularAttribute.isId()) {
                idProperty = singularAttribute.getName();
                break;
            }
        }

        return idProperty;
    }
}
