package cat.nyaa.nyaacore.database;

public enum ColumnAccessMethod {
    FIELD,        // go through normal type conversion routing, and Enum/ItemStacks etc. are handled automatically.
    FIELD_PARSE, // if the field type has static parse()/fromString() method
    METHOD;      // access via a pair of getter/setter
}
