# RetroKompat
[![Build Status](https://travis-ci.com/fridujo/retrokompat.svg?branch=master)](https://travis-ci.com/fridujo/retrokompat)
[![Coverage Status](https://codecov.io/gh/fridujo/retrokompat/branch/master/graph/badge.svg)](https://codecov.io/gh/fridujo/retrokompat/)
[![License](https://img.shields.io/github/license/fridujo/retrokompat.svg)](https://opensource.org/licenses/Apache-2.0)

Backward compatibility for Java binaries.

## Motivation
When publishing a library, a sdk or any binary that is used externally we often want to check for backward compatibility.

That is, if some code uses a library, it will not break when upgrading it to a newer MINOR version (see [semantic versionning](https://semver.org/)).

This tool will help achieving that verification in the form of a Maven plugin.

