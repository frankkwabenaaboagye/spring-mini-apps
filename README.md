# General Info

To import into your IDE, import the parent pom `lab/pom.xml` as Maven projects or `lab/build.gradle` as Gradle projects.


# Note
 Some Tips 
 - credit: spring docs, spring academy

## Configuration

TODO

## Component Scanning

TODO

## Spring Container

TODO

## AOP
- There are some generic functionalities that are needed in many places in any application code
- With this in mind, there is the need to avoid code tangling and eliminate code scattering
- what can we do? Modularize? Yes!
- AOP helps to do that
- AOP Technologies - AspectJ, Spring AOP
- Core concepts
    - `Join Point`: A point in the execution of a program - Method calls  or exception
    - `Pointcut` : Expression that selects one or more `join point`
    - `Advice` : Code to be executed at each selected `join point`
    - `Aspect` : Module that encapsulated `Pointcuts` &  `Advice`
    - `Weaving` : Combining `Aspects` with main code
- Defining a pointcut
    - 