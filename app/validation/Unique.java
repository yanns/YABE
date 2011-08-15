package validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.persistence.Query;
import org.apache.commons.lang.StringUtils;
import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.configuration.annotation.Constraint;
import net.sf.oval.context.FieldContext;
import net.sf.oval.context.OValContext;
import play.db.jpa.JPA;
import play.db.jpa.Model;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Constraint(checkWith = Unique.UniqueCheck.class)
public @interface Unique {
    String message() default UniqueCheck.mes;

    String additionalHQL() default "";

    String[] additionalObjects() default {};

    public static class UniqueCheck extends AbstractAnnotationCheck<Unique> {
        public static final String mes = "validation.unique";
        private String additionalHQL;
        private String[] additionalObjects;

        @Override
        public void configure(Unique unique) {
            setMessage(unique.message());
            additionalHQL = unique.additionalHQL();
            additionalObjects = unique.additionalObjects();
        }

        public boolean isSatisfied(Object validatedObject, Object value,
                OValContext context, Validator validator) {
            if (value == null || value.toString().length() == 0) {
                return false;
            }
            Model validatedModel = (Model) validatedObject;
            String field = ((FieldContext) context).getField().getName();
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT COUNT(*) FROM ");
            hql.append(validatedObject.getClass().getSimpleName());
            hql.append(" AS o WHERE o.");
            hql.append(field);
            hql.append("=?");
            if (validatedModel.id != null)
                hql.append(" AND id IS NOT ?");
            if (additionalHQL.length() > 0) {
                hql.append(" AND ");
                hql.append(additionalHQL);
            }
            Query query = JPA.em().createQuery(hql.toString());
            int index = 1;
            query.setParameter(index++, value);
            if (validatedModel.id != null)
                query.setParameter(index++, validatedModel.id);
            for (String additionalObject : additionalObjects) {
                String methodName = "get"
                        + StringUtils.capitalize(additionalObject);
                try {
                    Object val = validatedObject.getClass()
                            .getMethod(methodName).invoke(validatedObject);
                    query.setParameter(index++, val);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            Long count = (Long) query.getSingleResult();
            return count.equals(0l);
        }
    }
}