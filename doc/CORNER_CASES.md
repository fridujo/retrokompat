# Conner cases

## Similar signatures disambiguity

The new version
```java
public class Some {
    public void doStuff(String s) {
    }

    public void doStuff(CharSequence s) {
    }
}
```
is compatible with the previous version
```java
public class Some {
    public void doStuff(String s) {
    }

    public void doStuff(CharSequence s) {
    }
}
```
