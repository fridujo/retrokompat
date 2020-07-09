# RetroKompat
Backward compatibility for Java binaries.

## Motivation
When publishing a library, a sdk or any binary that is used externally we often want to check for backward compatibility.

That is, if some code uses a library, it will not break when upgrading it to a newer MINOR version (see [semantic versionning](https://semver.org/)).

This tool will help achieving that verification in the form of a Maven plugin.

