# GenericDao
GenericDao is a java library, writen in Java 8 and under APACHE licence.

# Introduction
Its main goal is to provide automatic DAO abilities over POJO entities by simply extending 
the GenericDao superclass. 
The library uses Annotations and Reflections and so do not recommend for Android developer since these 2 patterns works 
really slow on Android.

Some of the out of the box method you'll get are:
    select(Long id);
    selectForIndex(Long id, String indexName, Object indexValue);
    insert(BaseEntity entity);
    
    // TO BE CONTINUE..

# Usage example

# Still to come 
