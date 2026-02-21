package org.dddjava.jig.infrastructure.springdatajdbc;

import org.dddjava.jig.domain.model.data.rdbaccess.SqlStatement;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlStatementId;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlStatements;
import org.dddjava.jig.domain.model.data.rdbaccess.Query;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlType;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class SpringDataJdbcStatementsReader {
    private static final Logger logger = LoggerFactory.getLogger(SpringDataJdbcStatementsReader.class);

    private static final String REPOSITORY_ANNOTATION = "org.springframework.stereotype.Repository";
    private static final String SPRING_DATA_REPOSITORY = "org.springframework.data.repository.Repository";
    private static final String SPRING_DATA_QUERY = "org.springframework.data.jdbc.repository.query.Query";
    private static final String SPRING_DATA_TABLE = "org.springframework.data.relational.core.mapping.Table";

    public SqlStatements readFrom(Collection<JigTypeHeader> jigTypeHeaders, List<Path> classPaths) {
        Collection<String> classNames = jigTypeHeaders.stream()
                .filter(jigTypeHeader -> jigTypeHeader.jigTypeAttributes()
                        .declaredAnnotation(TypeId.valueOf(REPOSITORY_ANNOTATION)))
                .map(JigTypeHeader::fqn)
                .toList();

        if (classNames.isEmpty()) return SqlStatements.empty();

        URL[] classLocationUrls = classPaths.stream()
                .flatMap(path -> {
                    try {
                        return Stream.of(path.toUri().toURL());
                    } catch (MalformedURLException e) {
                        logger.warn("classPath({})は読み飛ばします", path, e);
                        return Stream.empty();
                    }
                })
                .toArray(URL[]::new);

        try (URLClassLoader classLoader = new URLClassLoader(classLocationUrls, ClassLoader.getSystemClassLoader())) {
            Class<?> repositoryInterface = loadClassIfPresent(classLoader, SPRING_DATA_REPOSITORY).orElse(null);
            if (repositoryInterface == null) return SqlStatements.empty();

            Map<SqlStatementId, SqlStatement> statements = new LinkedHashMap<>();
            for (String className : classNames) {
                Class<?> repositoryType = Class.forName(className, false, classLoader);
                if (!repositoryType.isInterface()) continue;
                if (!repositoryInterface.isAssignableFrom(repositoryType)) continue;

                Optional<String> tableName = resolveTableName(repositoryType);
                extractStatements(repositoryType, tableName).forEach(statement ->
                        statements.put(statement.sqlStatementId(), statement));
            }
            return new SqlStatements(List.copyOf(statements.values()));
        } catch (Exception e) {
            logger.warn("Spring Data JDBC の読み取りに失敗しました。", e);
            return SqlStatements.empty();
        }
    }

    private Stream<SqlStatement> extractStatements(Class<?> repositoryType, Optional<String> tableName) {
        return Arrays.stream(repositoryType.getMethods())
                .filter(method -> method.getDeclaringClass() != Object.class)
                .flatMap(method -> createStatement(repositoryType, method, tableName).stream());
    }

    private Optional<SqlStatement> createStatement(Class<?> repositoryType, Method method, Optional<String> tableName) {
        SqlType sqlType = inferSqlType(method).orElse(null);
        if (sqlType == null) return Optional.empty();

        Query query = readQuery(method).orElseGet(() ->
                tableName.map(name -> Query.from(defaultQuery(sqlType, name))).orElse(Query.unsupported()));

        SqlStatementId statementId = SqlStatementId.from(repositoryType.getCanonicalName() + "." + method.getName());
        return Optional.of(new SqlStatement(statementId, query, sqlType));
    }

    private Optional<SqlType> inferSqlType(Method method) {
        String methodName = method.getName().toLowerCase(Locale.ROOT);

        if (methodName.startsWith("find")
                || methodName.startsWith("read")
                || methodName.startsWith("get")
                || methodName.startsWith("query")
                || methodName.startsWith("count")
                || methodName.startsWith("exists")) {
            return Optional.of(SqlType.SELECT);
        }
        if (methodName.startsWith("save")
                || methodName.startsWith("insert")
                || methodName.startsWith("create")
                || methodName.startsWith("add")) {
            return Optional.of(SqlType.INSERT);
        }
        if (methodName.startsWith("update")
                || methodName.startsWith("set")) {
            return Optional.of(SqlType.UPDATE);
        }
        if (methodName.startsWith("delete")
                || methodName.startsWith("remove")) {
            return Optional.of(SqlType.DELETE);
        }
        return Optional.empty();
    }

    private Optional<Query> readQuery(Method method) {
        return Arrays.stream(method.getAnnotations())
                .filter(annotation -> annotation.annotationType().getName().equals(SPRING_DATA_QUERY))
                .findFirst()
                .flatMap(annotation -> {
                    try {
                        Method valueMethod = annotation.annotationType().getMethod("value");
                        Object value = valueMethod.invoke(annotation);
                        if (!(value instanceof String query)) return Optional.empty();
                        if (query.isBlank()) return Optional.empty();
                        return Optional.of(Query.from(query));
                    } catch (ReflectiveOperationException e) {
                        logger.debug("Queryアノテーションの読み取りに失敗しました。method={}", method, e);
                        return Optional.empty();
                    }
                });
    }

    private Optional<String> resolveTableName(Class<?> repositoryType) {
        Optional<Class<?>> entityType = resolveEntityType(repositoryType, new HashSet<>());
        if (entityType.isEmpty()) return Optional.empty();

        Annotation[] annotations = entityType.orElseThrow().getAnnotations();
        for (Annotation annotation : annotations) {
            if (!annotation.annotationType().getName().equals(SPRING_DATA_TABLE)) continue;
            try {
                Method valueMethod = annotation.annotationType().getMethod("value");
                Object value = valueMethod.invoke(annotation);
                if (value instanceof String tableName && !tableName.isBlank()) {
                    return Optional.of(tableName);
                }
            } catch (ReflectiveOperationException e) {
                logger.debug("Tableアノテーションの読み取りに失敗しました。entity={}", entityType.orElseThrow(), e);
            }
        }
        return Optional.of(toSnakeCase(entityType.orElseThrow().getSimpleName()));
    }

    private Optional<Class<?>> resolveEntityType(Class<?> repositoryType, Set<Class<?>> visited) {
        if (!visited.add(repositoryType)) return Optional.empty();

        for (Type genericInterface : repositoryType.getGenericInterfaces()) {
            Optional<Class<?>> entityType = resolveEntityType(genericInterface, visited);
            if (entityType.isPresent()) return entityType;
        }
        return Optional.empty();
    }

    private Optional<Class<?>> resolveEntityType(Type type, Set<Class<?>> visited) {
        if (type instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> rawClass
                    && rawClass.getName().startsWith("org.springframework.data.repository.")) {
                Type[] arguments = parameterizedType.getActualTypeArguments();
                if (arguments.length > 0 && arguments[0] instanceof Class<?> entityType) return Optional.of(entityType);
            }
            if (rawType instanceof Class<?> rawClass) {
                Optional<Class<?>> entityType = resolveEntityType(rawClass, visited);
                if (entityType.isPresent()) return entityType;
            }
            return Optional.empty();
        }
        if (type instanceof Class<?> interfaceType) {
            return resolveEntityType(interfaceType, visited);
        }
        return Optional.empty();
    }

    private static Optional<Class<?>> loadClassIfPresent(ClassLoader classLoader, String className) {
        try {
            return Optional.of(Class.forName(className, false, classLoader));
        } catch (ClassNotFoundException ignored) {
            return Optional.empty();
        }
    }

    private String defaultQuery(SqlType sqlType, String tableName) {
        return switch (sqlType) {
            case INSERT -> "insert into " + tableName + " values (?)";
            case SELECT -> "select * from " + tableName;
            case UPDATE -> "update " + tableName + " set id = id";
            case DELETE -> "delete from " + tableName;
        };
    }

    private String toSnakeCase(String text) {
        StringBuilder builder = new StringBuilder(text.length() + 4);
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (Character.isUpperCase(current) && i > 0) builder.append('_');
            builder.append(Character.toLowerCase(current));
        }
        return builder.toString();
    }
}
