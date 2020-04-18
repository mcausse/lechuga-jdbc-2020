package org.lechuga;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.lechuga.reflect.ReflectUtils;
import org.lenteja.jdbc.Mapable;

public class EntityConfig<E> implements Mapable<E> {

	private final Class<E> entityClass;
	private final String tableName;

	private final Map<String, PropertyConfig> allPropsMap;
	private final List<PropertyConfig> idProps;
	private final List<PropertyConfig> regularProps;
	private final List<PropertyConfig> autogenProps;

	private final List<EntityListener<E>> entityListeners;

	private final EntityManagerOperations entityManagerOperations;

	public EntityConfig(Class<E> entityClass, String tableName, Map<String, PropertyConfig> allPropsMap,
			List<PropertyConfig> idProps, List<PropertyConfig> regularProps, List<PropertyConfig> autogenProps,
			List<EntityListener<E>> entityListeners) {
		super();
		this.entityClass = entityClass;
		this.tableName = tableName;
		this.allPropsMap = allPropsMap;
		this.idProps = idProps;
		this.regularProps = regularProps;
		this.autogenProps = autogenProps;
		this.entityListeners = entityListeners;
		this.entityManagerOperations = new EntityManagerOperations(this);
	}

	@Override
	public E map(ResultSet rs) throws SQLException {
		E r = ReflectUtils.newInstance(entityClass);
		for (PropertyConfig p : allPropsMap.values()) {
			p.readValue(r, rs);
		}
		return r;
	}

	public EntityManagerOperations getEntityManagerOperations() {
		return entityManagerOperations;
	}

	public Class<E> getEntityClass() {
		return entityClass;
	}

	public String getTableName() {
		return tableName;
	}

	public Map<String, PropertyConfig> getAllPropsMap() {
		return allPropsMap;
	}

	public List<PropertyConfig> getIdProps() {
		return idProps;
	}

	public List<PropertyConfig> getRegularProps() {
		return regularProps;
	}

	public List<PropertyConfig> getAutogenProps() {
		return autogenProps;
	}

	public List<EntityListener<E>> getListeners() {
		return entityListeners;
	}

	@Override
	public String toString() {
		return "EntityManagerConfig [entityClass=" + entityClass.getName() + ", tableName=" + tableName + ", props="
				+ allPropsMap.values() + "]";
	}

}