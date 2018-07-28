package cat.nyaa.nyaacore.database.relational;

import javax.persistence.Column;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A java field is converted to a database column in two steps:
 *   1. determine the access method
 *   2. determine the SQL type decl and convertion rule by the object type
 * There are two access methods:
 *   1. directly get/set to a class field, the type is the field's type
 *   2. get/set through a pair of getter/setter with matching return/parameter type, the type is the return/parameter type.
 * There are several accepted java types:
 *   {@see DataTypeMapping}
 */
@SuppressWarnings("rawtypes")
public class ColumnStructure {
    public enum AccessMethod {
        DIRECT_FIELD,  // directly get from field
        GETTER_SETTER  // use getter and setter
    }

    public final String name;
    public final TableStructure table;
    public final boolean nullable;
    public final boolean unique;

    public final AccessMethod accessMethod;
    public final Field field;   // used if access method is DIRECT_FIELD
    public final Method setter; // used if access method is GETTER_SETTER
    public final Method getter; // used if access method is GETTER_SETTER

    public final Class javaType;
    public final DataTypeMapping.Types sqlType;
    public final DataTypeMapping.IDataTypeConverter typeConverter;

    /**
     * Constructor for field based table columns
     */
    public ColumnStructure(TableStructure table, Field dataField, Column anno) {
        if (anno == null) throw new IllegalArgumentException();
        if (anno.name().isEmpty()) {
            name = dataField.getName();
        } else {
            name = anno.name();
        }
        this.nullable = anno.nullable();
        this.unique = anno.unique();
        this.table = table;
        accessMethod = AccessMethod.DIRECT_FIELD;
        field = dataField;
        setter = null;
        getter = null;

        javaType = field.getType();
        typeConverter = DataTypeMapping.getDataTypeConverter(javaType);
        sqlType = typeConverter.getSqlType();
    }

    /**
     * Constructor for method based table columns
     */
    public ColumnStructure(TableStructure table, Method dataMethod, Column anno) {
        if (anno == null) throw new IllegalArgumentException();
        this.table = table;
        this.nullable = anno.nullable();
        this.unique = anno.unique();

        String methodName = dataMethod.getName();
        if (!methodName.startsWith("get") && !methodName.startsWith("set"))
            throw new IllegalArgumentException("Method is neither a setter nor a getter: " + dataMethod.toString());
        String methodSuffix = methodName.substring(3);
        String name = ("".equals(anno.name())) ? methodSuffix : anno.name();
        Class methodType;
        if (methodName.startsWith("get")) {
            methodType = dataMethod.getReturnType();
        } else {
            methodType = dataMethod.getParameterCount() == 1 ? dataMethod.getParameterTypes()[0] : null;
        }
        if (methodType == null || methodType == Void.class || methodType == Void.TYPE)
            throw new IllegalArgumentException("Cannot determine getter/setter type: " + dataMethod.toString());
        //if (methodType != String.class && methodType != Long.class && methodType != Double.class)
        //   throw new IllegalArgumentException("Only three types are supported for getter/setter columns: String/Long/Double");

        Method getter, setter;
        try {
            getter = dataMethod.getDeclaringClass().getDeclaredMethod("get" + methodSuffix);
            setter = dataMethod.getDeclaringClass().getDeclaredMethod("set" + methodSuffix, methodType);
            if (getter.getParameterCount() != 0 || getter.getReturnType() != methodType || Modifier.isStatic(getter.getModifiers()))
                throw new RuntimeException("getter signature mismatch");
            if (setter.getParameterCount() != 1 || setter.getParameterTypes()[0] != methodType ||
                    (setter.getReturnType() != Void.class && setter.getReturnType() != Void.TYPE) ||
                    Modifier.isStatic(setter.getModifiers()))
                throw new RuntimeException("setter signature mismatch");
            Id primary = getter.getDeclaredAnnotation(Id.class);
            if(primary == null){
                primary = setter.getDeclaredAnnotation(Id.class);
            }
            getter.setAccessible(true);
            setter.setAccessible(true);
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

        this.name = name;

        this.accessMethod = AccessMethod.GETTER_SETTER;
        this.field = null;
        this.getter = getter;
        this.setter = setter;

        this.javaType = methodType;
        this.typeConverter = DataTypeMapping.getDataTypeConverter(this.javaType);
        this.sqlType = this.typeConverter.getSqlType();
    }

    public String getName() {
        return name;
    }

    public TableStructure getTable() {
        return table;
    }

    public String getTableCreationScheme() {
        String ret = name + " " + sqlType.name();
        if (!nullable) ret += " NOT NULL";
        if (unique) ret += "UNIQUE";
        return ret;
    }

    public Object getJavaObject(Object entityObj) {
        try {
            if (accessMethod == AccessMethod.DIRECT_FIELD) {
                return field.get(entityObj);
            } else {
                return getter.invoke(entityObj);
            }
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setJavaObject(Object entityObj, Object obj) {
        try {
            if (accessMethod == AccessMethod.DIRECT_FIELD) {
                field.set(entityObj, obj);
            } else {
                setter.invoke(entityObj, obj);
            }
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Object getSqlObject(Object entityObj) {
        Object javaObj = getJavaObject(entityObj);
        if (javaObj == null) {
            return null;
        } else {
            return typeConverter.toSqlType(javaObj);
        }
    }

    public void setSqlObject(Object entityObj, Object obj) {
        if (obj == null) {
            setJavaObject(entityObj, null);
        } else {
            setJavaObject(entityObj, typeConverter.toJavaType(obj));
        }
    }
}
