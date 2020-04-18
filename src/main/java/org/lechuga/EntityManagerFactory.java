package org.lechuga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lechuga.anno.Column;
import org.lechuga.anno.EntityListeners;
import org.lechuga.anno.Enumerated;
import org.lechuga.anno.Generated;
import org.lechuga.anno.Handler;
import org.lechuga.anno.Id;
import org.lechuga.anno.Table;
import org.lechuga.anno.Transient;
import org.lechuga.autogen.Generator;
import org.lechuga.handler.ColumnHandler;
import org.lechuga.handler.EnumColumnHandler;
import org.lechuga.handler.Handlers;
import org.lechuga.reflect.PropertyPathAccessor;
import org.lechuga.reflect.PropertyPathAccessorScanner;
import org.lechuga.reflect.ReflectUtils;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityManagerFactory {

	protected static final Logger LOG = LoggerFactory.getLogger(JdbcDataAccesFacade.class);
	protected static final PropertyPathAccessorScanner SCANNER = new PropertyPathAccessorScanner();

	public EntityManager buildEntityManager(DataAccesFacade facade, Class<?>... entityClasses) {
		return buildEntityManager(facade, Arrays.asList(entityClasses));
	}

	public EntityManager buildEntityManager(DataAccesFacade facade, List<Class<?>> entityClasses) {
		LOG.info("building " + getClass().getSimpleName() + "...");
		Map<Class<?>, EntityConfig<?>> entityConfigs = new LinkedHashMap<>();
		for (Class<?> entityClass : entityClasses) {
			EntityConfig<?> ec = buildConfig(entityClass);
			LOG.info(generateTypeSafeInterface(ec));
			entityConfigs.put(entityClass, ec);
		}
		return new EntityManager(entityConfigs, facade);
	}

	@SuppressWarnings("unchecked")
	public <E> EntityConfig<E> buildConfig(Class<E> entityClass) {
		Map<String, PropertyPathAccessor> ps = SCANNER.scan(entityClass);

		final String tableName;
		if (entityClass.isAnnotationPresent(Table.class)) {
			Table annoTable = entityClass.getAnnotation(Table.class);
			tableName = annoTable.value();
		} else {
			tableName = Conventions.tableNameOf(entityClass);
		}

		List<EntityListener<E>> entityListeners = new ArrayList<>();
		if (entityClass.isAnnotationPresent(EntityListeners.class)) {
			EntityListeners annoEntListeners = entityClass.getAnnotation(EntityListeners.class);
			for (Class<? extends EntityListener<?>> entityListener : annoEntListeners.value()) {
				EntityListener<E> er = (EntityListener<E>) ReflectUtils.newInstance(entityListener);
				entityListeners.add(er);
			}
		} else {
			entityListeners = Collections.emptyList();
		}

		final Map<String, PropertyConfig> allPropsMap = new LinkedHashMap<>();
		final List<PropertyConfig> idProps = new ArrayList<>();
		final List<PropertyConfig> regularProps = new ArrayList<>();
		final List<PropertyConfig> autogenProps = new ArrayList<>();

		for (Entry<String, PropertyPathAccessor> e : ps.entrySet()) {

			try {

				PropertyPathAccessor propertyChain = e.getValue();

				if (propertyChain.getAnnotations().containsKey(Transient.class)) {
					continue;
				}

				final boolean isPk;
				final String columnName;
				final ColumnHandler columnHandler;
				final Generator generator;
				{
					isPk = propertyChain.getAnnotations().containsKey(Id.class);

					if (propertyChain.getAnnotations().containsKey(Column.class)) {
						Column column = (Column) propertyChain.getAnnotations().get(Column.class);
						columnName = column.value();
					} else {
						columnName = Conventions.columnNameOf(propertyChain.getLastPartPropertyName());
					}

					if (propertyChain.getAnnotations().containsKey(Enumerated.class)) {
						columnHandler = new EnumColumnHandler(
								(Class<? extends Enum<?>>) propertyChain.getPropertyType());
					} else if (propertyChain.getAnnotations().containsKey(Handler.class)) {
						Handler annoHandler = (Handler) propertyChain.getAnnotations().get(Handler.class);
						columnHandler = ReflectUtils.newInstance(annoHandler.value(), annoHandler.args());
					} else {
						columnHandler = Handlers.getHandlerFor(propertyChain.getPropertyType());
					}

					if (propertyChain.getAnnotations().containsKey(Generated.class)) {
						Generated annoGene = (Generated) propertyChain.getAnnotations().get(Generated.class);
						generator = ReflectUtils.newInstance(annoGene.value(), annoGene.args());
					} else {
						generator = null;
					}
				}

				PropertyConfig p = new PropertyConfig(isPk, columnName, columnHandler, generator, propertyChain);

				allPropsMap.put(e.getKey(), p);
				if (isPk) {
					idProps.add(p);
				} else {
					regularProps.add(p);
				}
				if (generator != null) {
					autogenProps.add(p);
				}

			} catch (Exception e2) {
				throw new RuntimeException(
						"configuring property '" + entityClass + "#" + e.getKey() + " => " + e.getValue(), e2);
			}

		}

		return new EntityConfig<E>(entityClass, tableName, allPropsMap, idProps, regularProps, autogenProps,
				entityListeners);
	}

	protected <E> String generateTypeSafeInterface(EntityConfig<E> ec) {

		StringBuilder s = new StringBuilder();
		s.append("\npublic interface ");
		s.append(ec.getEntityClass().getSimpleName());
		s.append("_ {\n");

		for (PropertyConfig p : ec.getAllPropsMap().values()) {
			s.append("\tpublic static final FieldDef<");
			s.append(p.getPropertyType().getSimpleName());
			s.append(", ");
			s.append(ec.getEntityClass().getSimpleName());
			s.append("> ");

			s.append(p.getPropertyLastName());
			s.append(" = new FieldDef<>(");
			s.append(ec.getEntityClass().getSimpleName());
			s.append(".class, \"");
			s.append(p.getPropertyName());
			s.append("\");\n");
		}
		s.append("}\n");
		return s.toString();
	}
}
