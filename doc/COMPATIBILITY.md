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

## Interfaces

### Adding a public method
The new version
```java
public interface Animal {
    void putInABox();
}
```
is compatible with the previous version
```java
public interface Animal {
}
```

### Removing a public method
The new version
```java
public interface Animal {
}
```
is not compatible with the previous version
```java
public interface Animal {
    void putInABox();
}
```
and will lead to the error `new version is missing public abstract void Animal.putInABox()`.

### Adding a public type

The new version
```java
public interface Animal {
}
```
is compatible with the previous version (not having the public type)

### Removing a public type

The new version (not having the public type)

is not compatible with the previous version
```java
public interface Animal {
}
```
and will lead to the error `new version removes type Animal`.

## Classes

### Increasing method visibility
The new version
```java
public class Cat {
    public void receiveFood(String type) {
    }
}
```
is compatible with the previous version
```java
public class Cat {
    void receiveFood(String type) {
    }
}
```

### Reducing method visibility
The new version
```java
public class Cat {
    void receiveFood(String type) {
    }
}
```
is not compatible with the previous version
```java
public class Cat {
    public void receiveFood(String type) {
    }
}
```
and will lead to the error `new version is missing public void Cat.receiveFood(java.lang.String)`.

### Removing package protected class

The new version, missing the class

is compatible with the previous version
```java
class Lion {
    public void receiveFood(String type) {
    }
}
```


## Methods

### Less specific return type

The new version
```java
public class Some {
    public String doStuff() {
        return null;
    }
}
```
is compatible with the previous version
```java
public class Some {
    public CharSequence doStuff() {
        return null;
    }
}
```
