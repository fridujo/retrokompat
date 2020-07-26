# Compatibility

## Enums

### Adding an enum value
The new version
```java
public enum MyEnum {
    VAL_1,
    VAL_2;
}
```
is compatible with the previous version
```java
public enum MyEnum {
    VAL_1;
}
```

### Removing an enum value
The new version
```java
public enum MyEnum {
    VAL_1;
}
```
is not compatible with the previous version
```java
public enum MyEnum {
    VAL_1,
    VAL_2;
}
```
and will lead to the error `new version removes values from MyEnum : [VAL_2]`.
