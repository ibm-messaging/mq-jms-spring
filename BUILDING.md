# Building the library from source

The library can be built directly using Gradle from the build.gradle control
file. The output can be copied to a local directory or pushed to a Maven repository.

Make sure the VERSION is correctly set for the output jar file, and that versions
of the dependencies in the build.gradle file are also those desired.

Then run gradlew to compile:

```
  export PushToMaven=true # if we want to push to a Maven repository
  ./gradlew -i --rerun-tasks uploadArchives
```

## Building for a Maven repository

The VERSION file in this directory contains the version number associated with the build.
For example, "0.1.2-SNAPSHOT".

Output from the build can be uploaded to a Maven repository.

The uploadArchives task controls publishing of the output. It uses the VERSION number to
determine what to do, along with an environment variable.
This means that we can build a non-SNAPSHOT version while still not pushing it out and
the github version of the file can match exactly what was built.

-   If the version contains 'SNAPSHOT' that we will use that temporary repo in the Central Repository.
    else we push to the RELEASE repository
-   If the version contains 'LOCAL'  or the environment variable "PushToMaven" is not set
    ** then the output will be copied to a local Maven repository
    under the user's home directory (~/.m2/repository).
    ** otherwise we attempt to push the jar files to the Nexus Central Repository.


## Releasing from Nexus
If pushing to the Nexus Release area, then once the build has been successfully transferred
you must log into Nexus to do the final promotion (CLOSE/RELEASE) of the artifact. Although it is
possible to automate that process, I am not doing it in this build file so we do a manual check
that the build has been successful and to check validity before freezing a version number.

Using Nexus Central Repository requires authentication and authorisation. The userid and password
associated with the account are held in a local file (gradle.properties) that is not part
of this public repository. That properties file also holds information about the signing key that Nexus
requires.

    ---- Example gradle.properties file --------
    # These access the GPG key and certificate
    signing.keyId=AAA111BB
    signing.password=MyPassw0rd
    signing.secretKeyRingFile=/home/user/.gnupg/secring.gpg
    # This is the authentication to Nexus
    ossrhUsername=myNexusId
    ossrhPassword=MyOtherPassw0rd
    --------------------------------------------
