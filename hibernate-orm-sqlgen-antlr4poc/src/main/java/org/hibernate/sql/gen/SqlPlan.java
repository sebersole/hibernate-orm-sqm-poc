package org.hibernate.sql.gen;

import java.util.List;
import java.util.Set;

/**
 * Created by johara on 27/08/15.
 */
public interface SqlPlan {

	List<String> getSqlStatements();

	Set<ParameterBinding> getParameterBindings();

}
